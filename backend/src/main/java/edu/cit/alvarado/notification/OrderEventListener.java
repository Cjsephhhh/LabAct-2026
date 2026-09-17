package edu.cit.alvarado.notification;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final NotificationRepository repository;

    public OrderEventListener(NotificationRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent e) {
        repository.save(
            new Notification(
                "Order " + e.orderId()
                    + " confirmed (" + e.itemCount() + " item line(s))."
            )
        );
    }

    @EventListener
    public void onOrderRejected(OrderRejectedEvent e) {
        repository.save(
            new Notification(
                "Order " + e.orderId()
                    + " rejected: " + e.reason()
            )
        );
    }

    @EventListener
    public void onLowStock(LowStockEvent e) {
        repository.save(
            new Notification(
                "Order needed: " + e.productName()
                    + " (" + e.productId() + ") stock is " + e.stock()
                    + ", below threshold " + e.threshold() + "."
            )
        );
    }
}