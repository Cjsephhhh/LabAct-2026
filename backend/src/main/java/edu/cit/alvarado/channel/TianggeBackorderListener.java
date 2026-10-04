package edu.cit.alvarado.channel;

import edu.cit.alvarado.inventory.InventoryService;
import edu.cit.alvarado.shop.OrderResponse;
import edu.cit.alvarado.shop.OrderService;
import edu.cit.alvarado.supplier.SupplierOrderDeliveredEvent;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
final class TianggeBackorderListener {
    private final TianggeOrderLinkRepository links;
    private final OrderService orderService;
    private final MarketplaceGateway marketplace;
    private final TianggeStockSync stockSync;
    private final InventoryService inventoryService;

    TianggeBackorderListener(TianggeOrderLinkRepository links, OrderService orderService, MarketplaceGateway marketplace, TianggeStockSync stockSync, InventoryService inventoryService) {
        this.links = links;
        this.orderService = orderService;
        this.marketplace = marketplace;
        this.stockSync = stockSync;
        this.inventoryService = inventoryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(10)
    void deliveryArrived(SupplierOrderDeliveredEvent event) {
        inventoryService.restock(event.productId(), event.units());
        resolveOpenBackorders();
    }

    @Scheduled(fixedDelay = 10000)
    void retryOpenBackorders() {
        resolveOpenBackorders();
    }

    private void resolveOpenBackorders() {
        for (TianggeOrderLink link : links.findByDecision("BACKORDERED")) {
            TianggeOrderContext.begin();
            try {
                OrderResponse response = orderService.acceptOrder(link.getShopOrderId());
                if ("CONFIRMED".equals(response.status())) {
                    link.setDecision("ACCEPTED");
                    links.save(link);
                    marketplace.resolve(link.getTianggeOrderId(), "ACCEPTED");
                    stockSync.flush();
                }
            } catch (RuntimeException exception) {
                System.out.println("Tiangge backorder retry failed for "
                        + link.getTianggeOrderId() + ": " + exception.getMessage());
            } finally {
                TianggeOrderContext.end();
            }
        }
    }
}
