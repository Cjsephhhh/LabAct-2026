package edu.cit.alvarado.inventory;
import java.util.Optional; import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface InventoryRepository extends JpaRepository<Inventory,String>{
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select i from Inventory i where i.productId = :productId")
 Optional<Inventory> findByProductIdForUpdate(String productId);
}
