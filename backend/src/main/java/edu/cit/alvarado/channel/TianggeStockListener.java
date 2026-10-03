package edu.cit.alvarado.channel;

import edu.cit.alvarado.notification.StockChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
final class TianggeStockListener {
    private final MarketplaceGateway marketplace;

    TianggeStockListener(MarketplaceGateway marketplace) {
        this.marketplace = marketplace;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void stockChanged(StockChangedEvent event) {
        try {
            marketplace.publishStock(java.util.List.of(new TianggeStock(event.productId(), event.stock())));
        } catch (RuntimeException ignored) {
        }
    }
}
