package edu.cit.alvarado.shop;
import edu.cit.alvarado.inventory.*; import edu.cit.alvarado.notification.*;
import org.springframework.context.ApplicationEventPublisher; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class OrderServiceImpl implements OrderService {
 private final OrderRepository orderRepository; private final InventoryService inventoryService; private final ApplicationEventPublisher publisher;
 public OrderServiceImpl(OrderRepository r,InventoryService i,ApplicationEventPublisher p){orderRepository=r;inventoryService=i;publisher=p;}
 @Override @Transactional public OrderResponse placeOrder(OrderRequest request){
  try { for(var item:request.items()) inventoryService.validateAvailability(item.productId(),item.quantity()); }
  catch(InsufficientStockException|IllegalArgumentException ex){
   Order rejected=new Order("REJECTED",ex.getMessage()); for(var item:request.items()) rejected.addItem(new OrderItem(item.productId(),item.quantity()));
   Order saved=orderRepository.save(rejected); publisher.publishEvent(new OrderRejectedEvent(saved.getOrderId(),ex.getMessage())); return toResponse(saved);
  }
  Order order=new Order("CONFIRMED","Order placed successfully."); List<Inventory> inventory=new ArrayList<>();
  for(var item:request.items()){inventory.add(inventoryService.reserve(item.productId(),item.quantity()));order.addItem(new OrderItem(item.productId(),item.quantity()));}
  Order saved=orderRepository.save(order); publisher.publishEvent(new OrderPlacedEvent(saved.getOrderId(),saved.getItems().size())); return toResponse(saved,inventory);
 }
 @Override @Transactional public OrderResponse cancelOrder(Long orderId){
  Order order=orderRepository.findById(orderId).orElseThrow(()->new OrderNotFoundException("Order not found: "+orderId));
  if("CANCELLED".equals(order.getStatus())) throw new OrderAlreadyCancelledException("Order "+orderId+" is already CANCELLED.");
  if(!"CONFIRMED".equals(order.getStatus())) throw new IllegalStateException("Only CONFIRMED orders can be cancelled.");
  for(OrderItem item:order.getItems()) inventoryService.restock(item.getProductId(),item.getQuantity());
  order.setStatus("CANCELLED"); order.setReason("Order cancelled and inventory restocked."); return toResponse(orderRepository.save(order));
 }
 @Override @Transactional(readOnly=true) public List<OrderResponse> getOrders(){return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();}
 private OrderResponse toResponse(Order o){return toResponse(o,List.of());}
 private OrderResponse toResponse(Order o,List<Inventory> inv){
  var items=o.getItems().stream().map(i->new OrderResponse.ItemResult(i.getProductId(),i.getQuantity(),o.getStatus())).toList();
  var views=inv.stream().map(i->new OrderResponse.InventoryView(i.getProductId(),i.getName(),i.getStock())).toList();
  return new OrderResponse(o.getOrderId(),o.getStatus(),o.getReason(),items,views,o.getCreatedAt());
 }
 public static class OrderNotFoundException extends RuntimeException{public OrderNotFoundException(String m){super(m);}}
 public static class OrderAlreadyCancelledException extends RuntimeException{public OrderAlreadyCancelledException(String m){super(m);}}
}
