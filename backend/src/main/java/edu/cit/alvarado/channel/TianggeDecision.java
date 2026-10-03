package edu.cit.alvarado.channel;

public record TianggeDecision(String decision, String shopOrderId, String reason) {
    public static TianggeDecision accepted(String shopOrderId) {
        return new TianggeDecision("ACCEPTED", shopOrderId, null);
    }

    public static TianggeDecision rejected(String shopOrderId, String reason) {
        return new TianggeDecision("REJECTED", shopOrderId, reason);
    }

    public static TianggeDecision backordered(String shopOrderId, String reason) {
        return new TianggeDecision("BACKORDERED", shopOrderId, reason);
    }
}
