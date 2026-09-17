package edu.cit.alvarado.inventory;
import org.springframework.web.bind.annotation.*; import org.springframework.http.ResponseEntity; import java.util.List;
@RestController @RequestMapping("/api/inventory") @CrossOrigin(origins="http://localhost:5173")
public class InventoryController {
 private final InventoryRepository repository; public InventoryController(InventoryRepository repository){this.repository=repository;}
 @GetMapping public ResponseEntity<List<InventoryView>> getInventory(){return ResponseEntity.ok(repository.findAll().stream().map(i->new InventoryView(i.getProductId(),i.getName(),i.getStock())).toList());}
 public record InventoryView(String productId,String name,int stock){}
}
