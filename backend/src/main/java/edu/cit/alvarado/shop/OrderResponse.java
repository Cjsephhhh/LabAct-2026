package edu.cit.alvarado.shop;

import edu.cit.alvarado.inventory.Inventory;

public record OrderResponse(
        String status,
        String reason,
        Inventory inventory
) {
}
