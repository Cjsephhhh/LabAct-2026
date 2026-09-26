package edu.cit.alvarado.supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierOrderRepository
        extends JpaRepository<SupplierOrder, Long> {

    Optional<SupplierOrder> findByBuyerRef(String buyerRef);

    Optional<SupplierOrder> findByRequestId(String requestId);

    Optional<SupplierOrder> findByPoNumber(String poNumber);

    List<SupplierOrder> findByStatusIn(
            List<SupplierOrderStatus> statuses
    );

    Optional<SupplierOrder>
    findFirstByProductIdAndStatusInOrderByCreatedAtDesc(
            String productId,
            List<SupplierOrderStatus> statuses
    );
}