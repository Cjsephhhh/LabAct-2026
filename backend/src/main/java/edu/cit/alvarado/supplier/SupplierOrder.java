package edu.cit.alvarado.supplier;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "supplier_orders")
public class SupplierOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, length = 20)
    private String productId;

    @Column(name = "buyer_ref", nullable = false, unique = true, length = 100)
    private String buyerRef;

    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @Column(name = "po_number", length = 50)
    private String poNumber;

    @Column(name = "cases", nullable = false)
    private int cases;

    @Column(name = "units", nullable = false)
    private int units;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SupplierOrderStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SupplierOrder() {
    }

    public SupplierOrder(
            String productId,
            String buyerRef,
            String requestId,
            int cases,
            int units,
            SupplierOrderStatus status
    ) {
        this.productId = productId;
        this.buyerRef = buyerRef;
        this.requestId = requestId;
        this.cases = cases;
        this.units = units;
        this.status = status;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getBuyerRef() {
        return buyerRef;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public int getCases() {
        return cases;
    }

    public int getUnits() {
        return units;
    }

    public SupplierOrderStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
        touch();
    }

    public void setCases(int cases) {
        this.cases = cases;
        touch();
    }

    public void setUnits(int units) {
        this.units = units;
        touch();
    }

    public void setStatus(SupplierOrderStatus status) {
        this.status = status;
        touch();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}