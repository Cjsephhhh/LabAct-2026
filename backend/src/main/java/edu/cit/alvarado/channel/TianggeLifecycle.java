package edu.cit.alvarado.channel;

import edu.cit.alvarado.inventory.Inventory;
import edu.cit.alvarado.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
final class TianggeLifecycle {
    private static final Logger log = LoggerFactory.getLogger(TianggeLifecycle.class);
    private final MarketplaceGateway marketplace;
    private final InventoryService inventoryService;
    private final ClientInstance instance;
    private final TianggeStockSync stockSync;
    private volatile boolean synced;

    TianggeLifecycle(MarketplaceGateway marketplace, InventoryService inventoryService, ClientInstance instance, TianggeStockSync stockSync) {
        this.marketplace = marketplace;
        this.inventoryService = inventoryService;
        this.instance = instance;
        this.stockSync = stockSync;
    }

    @EventListener(ApplicationReadyEvent.class)
    void start() {
        try {
            marketplace.heartbeat(0);
            syncShop();
            log.info("Tiangge instance started: {}", instance.id());
        } catch (RuntimeException ex) {
            log.error("Tiangge startup sync failed", ex);
        }
    }

    @Scheduled(fixedRate = 30_000)
    void heartbeat() {
        long uptime = java.time.Duration.between(instance.startedAt(), java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)).getSeconds();
        try {
            marketplace.heartbeat(uptime);
            if (!synced) syncShop();
        } catch (RuntimeException ex) {
            log.warn("Tiangge heartbeat failed: {}", ex.getMessage());
        }
    }

    private void syncShop() {
        publishListings();
        publishStock();
        stockSync.clear();
        synced = true;
    }

    void publishStock() {
        marketplace.publishStock(List.of(stock("P100"), stock("P200"), stock("P300")));
    }

    private TianggeStock stock(String productId) {
        Inventory inventory = inventoryService.get(productId);
        return new TianggeStock(inventory.getProductId(), inventory.getStock());
    }

    private void publishListings() {
        marketplace.publishListings(List.of(
                new TianggeListing("P100", "Wireless Mouse", "YQP-1135"),
                new TianggeListing("P200", "Mechanical Keyboard", "YQP-4388"),
                new TianggeListing("P300", "USB-C Hub", "YQP-6798")
        ));
    }
}
