package edu.cit.alvarado.channel;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tiangge_order_links")
public class TianggeOrderLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tianggeOrderId;
    private Long shopOrderId;
    private String decision;

    protected TianggeOrderLink() {}
    public TianggeOrderLink(String tianggeOrderId, Long shopOrderId, String decision) {
        this.tianggeOrderId = tianggeOrderId;
        this.shopOrderId = shopOrderId;
        this.decision = decision;
    }
    public Long getId() { return id; }
    public String getTianggeOrderId() { return tianggeOrderId; }
    public Long getShopOrderId() { return shopOrderId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}
