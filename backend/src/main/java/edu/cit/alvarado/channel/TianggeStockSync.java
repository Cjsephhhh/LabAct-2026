package edu.cit.alvarado.channel;

import edu.cit.alvarado.notification.StockChangedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
final class TianggeStockSync {
    private final TianggePendingStockRepository repository;
    private final MarketplaceGateway marketplace;

    TianggeStockSync(TianggePendingStockRepository repository, MarketplaceGateway marketplace) {
        this.repository = repository;
        this.marketplace = marketplace;
    }

    void changed(StockChangedEvent event) {
        TianggePendingStock pending = repository.findById(event.productId())
                .orElseGet(() -> new TianggePendingStock(event.productId(), event.stock()));
        pending.setAvailable(event.stock());
        repository.save(pending);

        if (!TianggeOrderContext.active()) flush();
    }

    void flush() {
        List<TianggePendingStock> pending = repository.findAll();
        if (pending.isEmpty()) return;
        send(pending.stream().map(p -> new TianggeStock(p.getSellerSku(), p.getAvailable())).toList());
        repository.deleteAll(pending);
    }

    void clear() {
        repository.deleteAll();
    }

    private void send(List<TianggeStock> stock) {
        marketplace.publishStock(stock);
    }
}
