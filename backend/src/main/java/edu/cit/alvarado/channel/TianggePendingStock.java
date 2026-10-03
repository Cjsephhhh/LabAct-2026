package edu.cit.alvarado.channel;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tiangge_pending_stock")
public class TianggePendingStock {
    @Id
    private String sellerSku;
    private int available;

    protected TianggePendingStock() {}
    TianggePendingStock(String sellerSku, int available) {
        this.sellerSku = sellerSku;
        this.available = available;
    }
    public String getSellerSku() { return sellerSku; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
}
