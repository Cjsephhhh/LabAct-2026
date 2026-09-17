package edu.cit.alvarado.shop; import java.util.List;
public interface OrderService {OrderResponse placeOrder(OrderRequest request); OrderResponse cancelOrder(Long orderId); List<OrderResponse> getOrders();}
