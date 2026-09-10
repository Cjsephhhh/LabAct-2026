package edu.cit.alvarado.inventory;

public interface InventoryService {

    Inventory getItem(String productId);

    Inventory reserve(String productId, int quantity);
}
