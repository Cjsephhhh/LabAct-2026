package edu.cit.alvarado.shop;
import jakarta.persistence.*;
@Entity @Table(name="order_items") public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="order_item_id") private Long orderItemId;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false) private Order order;
 @Column(name="product_id",nullable=false,length=20) private String productId; @Column(nullable=false) private int quantity;
 public OrderItem(){} public OrderItem(String productId,int quantity){this.productId=productId;this.quantity=quantity;}
 public Long getOrderItemId(){return orderItemId;} public Order getOrder(){return order;} public void setOrder(Order o){order=o;} public String getProductId(){return productId;} public int getQuantity(){return quantity;}
}
