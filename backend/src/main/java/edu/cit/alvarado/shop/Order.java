package edu.cit.alvarado.shop;
import java.time.OffsetDateTime; import java.util.*; import jakarta.persistence.*;
@Entity @Table(name="orders") public class Order {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="order_id") private Long orderId;
 @Column(nullable=false,length=20) private String status; @Column(length=255) private String reason;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) private List<OrderItem> items=new ArrayList<>();
 public Order(){createdAt=OffsetDateTime.now();} public Order(String status,String reason){this();this.status=status;this.reason=reason;}
 public void addItem(OrderItem item){items.add(item);item.setOrder(this);} public Long getOrderId(){return orderId;} public String getStatus(){return status;} public void setStatus(String s){status=s;} public String getReason(){return reason;} public OffsetDateTime getCreatedAt(){return createdAt;} public List<OrderItem> getItems(){return items;}
 public void setReason(String reason) {
    this.reason = reason;
}
}

