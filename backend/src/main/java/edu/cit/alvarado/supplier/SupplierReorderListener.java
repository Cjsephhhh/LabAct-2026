package edu.cit.alvarado.supplier;

import edu.cit.alvarado.notification.LowStockEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SupplierReorderListener {

    private final SupplierOrderService supplierOrderService;

    public SupplierReorderListener(
            SupplierOrderService supplierOrderService
    ) {
        this.supplierOrderService = supplierOrderService;
    }

    @EventListener
    public void handleLowStock(LowStockEvent event) {

        int unitsNeeded =
                Math.max(
                        event.threshold() - event.stock(),
                        1
                );

        supplierOrderService.placeReorder(
                event.productId(),
                unitsNeeded
        );
    }
}