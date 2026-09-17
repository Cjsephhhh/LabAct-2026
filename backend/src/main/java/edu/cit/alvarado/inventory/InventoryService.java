package edu.cit.alvarado.inventory;
public interface InventoryService { Inventory get(String productId); void validateAvailability(String productId,int quantity); Inventory reserve(String productId,int quantity); Inventory restock(String productId,int quantity); }
