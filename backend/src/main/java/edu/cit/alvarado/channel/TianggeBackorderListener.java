package edu.cit.alvarado.channel;

import edu.cit.alvarado.shop.OrderResponse;
import edu.cit.alvarado.shop.OrderService;
import edu.cit.alvarado.supplier.SupplierOrderDeliveredEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
final class TianggeBackorderListener {
    private final TianggeOrderLinkRepository links;
    private final OrderService orderService;
    private final MarketplaceGateway marketplace;
    private final TianggeStockSync stockSync;

    TianggeBackorderListener(TianggeOrderLinkRepository links, OrderService orderService, MarketplaceGateway marketplace, TianggeStockSync stockSync) {
        this.links = links;
        this.orderService = orderService;
        this.marketplace = marketplace;
        this.stockSync = stockSync;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(10)
    void deliveryArrived(SupplierOrderDeliveredEvent event) {
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
            } catch (RuntimeException ignored) {
            } finally {
                TianggeOrderContext.end();
            }
        }
    }
}
