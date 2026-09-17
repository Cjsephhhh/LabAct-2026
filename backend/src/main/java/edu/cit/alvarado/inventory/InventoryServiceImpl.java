package edu.cit.alvarado.inventory;
import edu.cit.alvarado.notification.LowStockEvent;
import org.springframework.context.ApplicationEventPublisher; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service class InventoryServiceImpl implements InventoryService {
 private static final int LOW_STOCK_THRESHOLD=5; private final InventoryRepository repository; private final ApplicationEventPublisher publisher;
 InventoryServiceImpl(InventoryRepository repository,ApplicationEventPublisher publisher){this.repository=repository;this.publisher=publisher;}
 @Override @Transactional(readOnly=true) public Inventory get(String productId){return repository.findById(productId).orElseThrow(()->new IllegalArgumentException("Product not found: "+productId));}
 @Override @Transactional public void validateAvailability(String productId,int quantity){
  Inventory i=repository.findByProductIdForUpdate(productId).orElseThrow(()->new IllegalArgumentException("Product not found: "+productId));
  if(i.getStock()<quantity) throw new InsufficientStockException("Requested quantity ("+quantity+") exceeds available stock ("+i.getStock()+") for "+productId+".");
 }
 @Override @Transactional public Inventory reserve(String productId,int quantity){
  Inventory i=repository.findByProductIdForUpdate(productId).orElseThrow(()->new IllegalArgumentException("Product not found: "+productId));
  if(i.getStock()<quantity) throw new InsufficientStockException("Requested quantity ("+quantity+") exceeds available stock ("+i.getStock()+") for "+productId+".");
  i.setStock(i.getStock()-quantity); Inventory saved=repository.save(i);
  if(saved.getStock()<LOW_STOCK_THRESHOLD) publisher.publishEvent(new LowStockEvent(saved.getProductId(),saved.getName(),saved.getStock(),LOW_STOCK_THRESHOLD));
  return saved;
 }
 @Override @Transactional public Inventory restock(String productId,int quantity){
  Inventory i=repository.findByProductIdForUpdate(productId).orElseThrow(()->new IllegalArgumentException("Product not found: "+productId));
  i.setStock(i.getStock()+quantity); return repository.save(i);
 }
}
