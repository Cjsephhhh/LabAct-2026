package edu.cit.alvarado.supplier;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SupplierOrderScheduler {

    private final SupplierOrderRepository repository;
    private final SupplierGateway supplierGateway;
    private final ApplicationEventPublisher publisher;

    public SupplierOrderScheduler(
            SupplierOrderRepository repository,
            SupplierGateway supplierGateway,
            ApplicationEventPublisher publisher
    ) {
        this.repository = repository;
        this.supplierGateway = supplierGateway;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void retryPendingOrders() {

        List<SupplierOrder> pendingOrders =
                repository.findByStatusIn(
                        List.of(SupplierOrderStatus.PENDING)
                );

        for (SupplierOrder order : pendingOrders) {

            try {
                SupplierOrderResult result =
                        supplierGateway.placeOrder(
                                order.getProductId(),
                                order.getUnits(),
                                order.getBuyerRef(),
                                order.getRequestId()
                        );

                order.setPoNumber(result.poNumber());
                order.setCases(result.cases());
                order.setUnits(result.units());
                order.setStatus(result.status());

                repository.save(order);

            } catch (RuntimeException exception) {

                order.setStatus(SupplierOrderStatus.PENDING);
                repository.save(order);
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void pollOpenOrders() {

        List<SupplierOrder> openOrders =
                repository.findByStatusIn(
                        List.of(
                                SupplierOrderStatus.ACCEPTED,
                                SupplierOrderStatus.PICKING,
                                SupplierOrderStatus.SHIPPED
                        )
                );

        for (SupplierOrder order : openOrders) {

            try {
                SupplierOrderStatus previousStatus =
                        order.getStatus();

                SupplierOrderResult result =
                        supplierGateway.getOrderStatus(
                                order.getBuyerRef()
                        );

                order.setStatus(result.status());

                if (result.poNumber() != null
                        && !result.poNumber().isBlank()) {

                    order.setPoNumber(result.poNumber());
                }

                if (result.cases() > 0) {
                    order.setCases(result.cases());
                }

                if (result.units() > 0) {
                    order.setUnits(result.units());
                }

                repository.save(order);

                if (previousStatus != SupplierOrderStatus.DELIVERED
                        && result.status()
                        == SupplierOrderStatus.DELIVERED) {

                    publisher.publishEvent(
                            new SupplierOrderDeliveredEvent(
                                    order.getProductId(),
                                    result.units(),
                                    order.getBuyerRef(),
                                    result.poNumber()
                            )
                    );
                }

            } catch (RuntimeException exception) {
                // Keep the current status.
                // The next scheduled poll will try again.
            }
        }
    }
}