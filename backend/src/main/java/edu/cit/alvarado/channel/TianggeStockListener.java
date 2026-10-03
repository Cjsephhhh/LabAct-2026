package edu.cit.alvarado.channel;

import edu.cit.alvarado.notification.StockChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
final class TianggeStockListener {
    private final TianggeStockSync stockSync;

    TianggeStockListener(TianggeStockSync stockSync) {
        this.stockSync = stockSync;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void stockChanged(StockChangedEvent event) {
        stockSync.changed(event);
    }
}
