package edu.cit.alvarado.supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierOrderService {

    private final SupplierOrderRepository repository;
    private final SupplierGateway supplierGateway;

    public SupplierOrderService(
            SupplierOrderRepository repository,
            SupplierGateway supplierGateway
    ) {
        this.repository = repository;
        this.supplierGateway = supplierGateway;
    }

    @Transactional
    public SupplierOrder placeReorder(
            String productId,
            int unitsNeeded
    ) {
        if (unitsNeeded <= 0) {
            throw new IllegalArgumentException(
                    "Units needed must be greater than zero"
            );
        }

        List<SupplierOrderStatus> activeStatuses =
                List.of(
                        SupplierOrderStatus.PENDING,
                        SupplierOrderStatus.ACCEPTED,
                        SupplierOrderStatus.PICKING,
                        SupplierOrderStatus.SHIPPED
                );

        SupplierOrder existing =
                repository
                        .findFirstByProductIdAndStatusInOrderByCreatedAtDesc(
                                productId,
                                activeStatuses
                        )
                        .orElse(null);

        if (existing != null) {
            return existing;
        }

        String buyerRef = generateBuyerRef();

        String requestId = "REQ-" + UUID.randomUUID();

        SupplierOrder pending = new SupplierOrder(
                productId,
                buyerRef,
                requestId,
                0,
                unitsNeeded,
                SupplierOrderStatus.PENDING
        );

        repository.save(pending);

        try {
            SupplierOrderResult result =
                    supplierGateway.placeOrder(
                            productId,
                            unitsNeeded,
                            buyerRef,
                            requestId
                    );

            pending.setPoNumber(result.poNumber());
            pending.setCases(result.cases());
            pending.setUnits(result.units());
            pending.setStatus(result.status());

            return repository.save(pending);

        } catch (RuntimeException exception) {

            pending.setStatus(
                    SupplierOrderStatus.PENDING
            );

            return repository.save(pending);
        }
    }

    private String generateBuyerRef() {
        return "RO-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}