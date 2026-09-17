package edu.cit.alvarado.shop;
import java.time.OffsetDateTime; import java.util.List;
public record OrderResponse(Long orderId,String status,String reason,List<ItemResult> items,List<InventoryView> inventory,OffsetDateTime createdAt){
 public record ItemResult(String productId,int quantity,String outcome){} public record InventoryView(String productId,String name,int stock){}
}
