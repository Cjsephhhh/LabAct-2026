package edu.cit.alvarado.shop;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.alvarado.inventory.InsufficientStockException;
import edu.cit.alvarado.inventory.Inventory;
import edu.cit.alvarado.inventory.InventoryService;

@Service
public class OrderServiceImpl implements OrderService {

    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            InventoryService inventoryService,
            OrderRepository orderRepository) {
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        Inventory current = inventoryService.getItem(request.productId());

        try {
            Inventory updated = inventoryService.reserve(
                    request.productId(),
                    request.quantity());

            saveOrder(request, "CONFIRMED", "Order placed successfully.");

            return new OrderResponse(
                    "CONFIRMED",
                    "Order placed successfully.",
                    updated);

        } catch (InsufficientStockException ex) {
            saveOrder(request, "REJECTED", ex.getMessage());

            return new OrderResponse(
                    "REJECTED",
                    ex.getMessage(),
                    current);
        }
    }

    private void saveOrder(
            OrderRequest request,
            String status,
            String reason) {

        Order order = new Order();
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setStatus(status);
        order.setReason(reason);
        orderRepository.save(order);
    }
}
