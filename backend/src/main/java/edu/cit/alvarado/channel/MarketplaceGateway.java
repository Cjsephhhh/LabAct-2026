package edu.cit.alvarado.channel;

import java.util.List;

public interface MarketplaceGateway {
    void heartbeat(long uptimeSeconds);
    void publishListings(List<TianggeListing> listings);
    void publishStock(List<TianggeStock> stock);
    void decide(String orderId, TianggeDecision decision);
    void resolve(String orderId, String status);
    void confirmCancellation(String orderId, boolean restocked);
}
