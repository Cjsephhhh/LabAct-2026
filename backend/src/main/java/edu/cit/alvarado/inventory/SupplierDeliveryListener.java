package edu.cit.alvarado.inventory;

import edu.cit.alvarado.supplier.SupplierOrderDeliveredEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SupplierDeliveryListener {
    private final InventoryService inventoryService;

    public SupplierDeliveryListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Order(0)
    public void handleSupplierDelivery(SupplierOrderDeliveredEvent event) {
        inventoryService.restock(event.productId(), event.units());
    }
}
