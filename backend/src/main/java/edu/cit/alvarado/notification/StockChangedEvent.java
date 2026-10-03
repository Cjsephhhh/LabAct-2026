package edu.cit.alvarado.notification;

public record StockChangedEvent(String productId, String name, int stock) {
}
