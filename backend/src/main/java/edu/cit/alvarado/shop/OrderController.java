package edu.cit.alvarado.shop;
import jakarta.validation.Valid; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/orders") @CrossOrigin(origins="http://localhost:5173")
public class OrderController {private final OrderService orderService; public OrderController(OrderService s){orderService=s;}
 @PostMapping public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request){return ResponseEntity.ok(orderService.placeOrder(request));}
 @GetMapping public ResponseEntity<List<OrderResponse>> getOrders(){return ResponseEntity.ok(orderService.getOrders());}
 @PostMapping("/{orderId}/cancel") public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId){return ResponseEntity.ok(orderService.cancelOrder(orderId));}
}
