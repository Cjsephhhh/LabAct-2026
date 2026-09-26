package edu.cit.alvarado.supplier;

public record SupplierOrderResult(
        String buyerRef,
        String poNumber,
        String productId,
        int cases,
        int units,
        SupplierOrderStatus status
) {
}