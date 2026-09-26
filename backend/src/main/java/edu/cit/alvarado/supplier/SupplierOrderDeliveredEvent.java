package edu.cit.alvarado.supplier;

public record SupplierOrderDeliveredEvent(
        String productId,
        int units,
        String buyerRef,
        String poNumber
) {
}