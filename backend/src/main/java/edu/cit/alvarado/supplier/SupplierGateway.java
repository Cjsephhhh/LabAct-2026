package edu.cit.alvarado.supplier;

public interface SupplierGateway {

    SupplierOrderResult placeOrder(
            String productId,
            int unitsNeeded,
            String buyerRef,
            String requestId
    );

    SupplierOrderResult getOrderStatus(
            String buyerRef
    );
}