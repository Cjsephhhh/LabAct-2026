package edu.cit.alvarado.inventory;
import jakarta.persistence.*;
@Entity @Table(name="inventory")
public class Inventory {
 @Id @Column(name="product_id",length=20) private String productId;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false) private int stock;
 public Inventory(){}
 public Inventory(String productId,String name,int stock){this.productId=productId;this.name=name;this.stock=stock;}
 public String getProductId(){return productId;} public String getName(){return name;} public int getStock(){return stock;} public void setStock(int stock){this.stock=stock;}
}
