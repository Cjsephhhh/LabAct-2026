package edu.cit.alvarado.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory getItem(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found: " + productId));
    }

    @Override
    @Transactional
    public Inventory reserve(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        Inventory item = inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found: " + productId));

        if (quantity > item.getStock()) {
            throw new InsufficientStockException(
                    "Requested quantity (" + quantity +
                    ") exceeds available stock (" + item.getStock() + ").");
        }

        item.setStock(item.getStock() - quantity);
        return inventoryRepository.save(item);
    }
}
