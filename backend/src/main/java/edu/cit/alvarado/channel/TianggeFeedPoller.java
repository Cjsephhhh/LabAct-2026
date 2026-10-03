package edu.cit.alvarado.channel;

import edu.cit.alvarado.shop.OrderRequest;
import edu.cit.alvarado.shop.OrderResponse;
import edu.cit.alvarado.shop.OrderService;
import edu.cit.alvarado.supplier.SupplierOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
final class TianggeFeedPoller {
    private static final Logger log = LoggerFactory.getLogger(TianggeFeedPoller.class);
    private final TianggeClient client;
    private final TianggeStateRepository stateRepository;
    private final TianggeProcessedEventRepository processedRepository;
    private final TianggeOrderLinkRepository linkRepository;
    private final OrderService orderService;
    private final SupplierOrderService supplierOrderService;
    private final edu.cit.alvarado.inventory.InventoryService inventoryService;
    private final MarketplaceGateway marketplace;

    TianggeFeedPoller(TianggeClient client, TianggeStateRepository stateRepository,
                      TianggeProcessedEventRepository processedRepository,
                      TianggeOrderLinkRepository linkRepository, OrderService orderService,
                      SupplierOrderService supplierOrderService,
                      edu.cit.alvarado.inventory.InventoryService inventoryService,
                      MarketplaceGateway marketplace) {
        this.client = client;
        this.stateRepository = stateRepository;
        this.processedRepository = processedRepository;
        this.linkRepository = linkRepository;
        this.orderService = orderService;
        this.supplierOrderService = supplierOrderService;
        this.inventoryService = inventoryService;
        this.marketplace = marketplace;
    }

    @Scheduled(fixedDelay = 3000)
    void poll() {
        try {
            TianggeState state = stateRepository.findById(1L).orElseGet(() -> stateRepository.save(new TianggeState(1L, 0L)));
            long cursor = state.getCursor();
            TianggeClient.TianggeFeed feed = client.feed(cursor);
            for (TianggeClient.TianggeEvent event : feed.events) {
                processEvent(event);
                saveProgress(event);
            }
            if (feed.events.isEmpty() && feed.nextCursor > cursor) {
                state.setCursor(feed.nextCursor);
                stateRepository.save(state);
            }
        } catch (RuntimeException ex) {
            log.warn("Tiangge feed poll failed: {}", ex.getMessage());
        }
    }

    void saveProgress(TianggeClient.TianggeEvent event) {
        if (!processedRepository.existsById(event.eventId)) {
            processedRepository.save(new TianggeProcessedEvent(event.eventId, event.seq));
        }
        TianggeState state = stateRepository.findById(1L).orElseGet(() -> new TianggeState(1L, 0L));
        state.setCursor(event.seq);
        stateRepository.save(state);
    }

    private void processEvent(TianggeClient.TianggeEvent event) {
        if (processedRepository.existsById(event.eventId)) return;
        if ("ORDER_PLACED".equals(event.type)) processOrder(event);
        else if ("ORDER_CANCELLED".equals(event.type)) processCancellation(event);
        else throw new IllegalStateException("Unknown Tiangge event type: " + event.type);
    }

    private void processOrder(TianggeClient.TianggeEvent event) {
        var existing = linkRepository.findByTianggeOrderId(event.orderId);
        if (existing.isPresent()) {
            if ("ACCEPTED".equals(existing.get().getDecision())) marketplace.decide(event.orderId, TianggeDecision.accepted(String.valueOf(existing.get().getShopOrderId())));
            else if ("REJECTED".equals(existing.get().getDecision())) marketplace.decide(event.orderId, TianggeDecision.rejected(String.valueOf(existing.get().getShopOrderId()), "Order already processed."));
            else if ("BACKORDERED".equals(existing.get().getDecision())) marketplace.decide(event.orderId, TianggeDecision.backordered(String.valueOf(existing.get().getShopOrderId()), "Waiting for supplier stock."));
            return;
        }

        List<OrderRequest.Item> items = new ArrayList<>();
        List<OrderRequest.Item> missing = new ArrayList<>();

        for (TianggeClient.TianggeLine line : event.lines) {
            OrderRequest.Item item = new OrderRequest.Item(line.sellerSku, line.qty);
            items.add(item);
            try {
                if (inventoryService.get(line.sellerSku).getStock() < line.qty) missing.add(item);
            } catch (IllegalArgumentException ex) {
                decideRejected(event.orderId, items, ex.getMessage());
                return;
            }
        }

        OrderRequest request = new OrderRequest(items);
        if (missing.isEmpty()) {
            OrderResponse response = orderService.placeOrder(request);
            String decision = "CONFIRMED".equals(response.status()) ? "ACCEPTED" : "REJECTED";
            linkRepository.save(new TianggeOrderLink(event.orderId, response.orderId(), decision));
            if ("ACCEPTED".equals(decision)) {
                marketplace.decide(event.orderId, TianggeDecision.accepted(String.valueOf(response.orderId())));
            } else {
                marketplace.decide(event.orderId, TianggeDecision.rejected(String.valueOf(response.orderId()), response.reason()));
            }
            return;
        }

        for (OrderRequest.Item item : missing) supplierOrderService.placeReorder(item.productId(), item.quantity());

        OrderResponse response = orderService.createBackorder(request);
        linkRepository.save(new TianggeOrderLink(event.orderId, response.orderId(), "BACKORDERED"));
        marketplace.decide(event.orderId, TianggeDecision.backordered(String.valueOf(response.orderId()), "Waiting for supplier stock."));
    }

    private void decideRejected(String orderId, List<OrderRequest.Item> items, String reason) {
        OrderResponse response = orderService.placeOrder(new OrderRequest(items));
        linkRepository.save(new TianggeOrderLink(orderId, response.orderId(), "REJECTED"));
        marketplace.decide(orderId, TianggeDecision.rejected(String.valueOf(response.orderId()), reason));
    }

    private void processCancellation(TianggeClient.TianggeEvent event) {
        TianggeOrderLink link = linkRepository.findByTianggeOrderId(event.orderId)
                .orElseThrow(() -> new IllegalStateException("No local order for " + event.orderId));
        boolean restocked = "ACCEPTED".equals(link.getDecision());
        orderService.cancelOrder(link.getShopOrderId());
        link.setDecision("CANCELLED");
        linkRepository.save(link);
        marketplace.confirmCancellation(event.orderId, restocked);
    }
}
