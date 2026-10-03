package edu.cit.alvarado.channel;

import edu.cit.alvarado.shop.OrderResponse;
import edu.cit.alvarado.shop.OrderService;
import edu.cit.alvarado.supplier.SupplierOrderDeliveredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
final class TianggeBackorderListener {
    private final TianggeOrderLinkRepository links;
    private final OrderService orderService;
    private final MarketplaceGateway marketplace;

    TianggeBackorderListener(TianggeOrderLinkRepository links, OrderService orderService, MarketplaceGateway marketplace) {
        this.links = links;
        this.orderService = orderService;
        this.marketplace = marketplace;
    }

    @EventListener
    void deliveryArrived(SupplierOrderDeliveredEvent event) {
        for (TianggeOrderLink link : links.findByDecision("BACKORDERED")) {
            try {
                OrderResponse response = orderService.acceptOrder(link.getShopOrderId());
                if ("CONFIRMED".equals(response.status())) {
                    link.setDecision("ACCEPTED");
                    links.save(link);
                    marketplace.resolve(link.getTianggeOrderId(), "ACCEPTED");
                }
            } catch (RuntimeException ignored) {
            }
        }
    }
}
