package web.car_system.Car_Service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales_orders", indexes = {
        @Index(name = "idx_order_number", columnList = "order_number", unique = true),
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_payment_status", columnList = "payment_status"),
        @Index(name = "idx_customer_phone", columnList = "customer_phone"),
        @Index(name = "idx_sales_staff", columnList = "sales_staff_id"),
        @Index(name = "idx_order_date", columnList = "order_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE sales_orders SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Filter(name = "tenantFilter", condition = "showroom_id = :tenantId")
public class SalesOrder extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false, length = 20)
    private String orderNumber;
    
    // === Customer Information ===
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    
    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;
    
    @Column(name = "customer_email", length = 100)
    private String customerEmail;
    
    @Lob
    @Column(name = "customer_address")
    private String customerAddress;
    
    @Column(name = "customer_id_number", length = 20)
    private String customerIdNumber; // CCCD/CMND
    
    // === Relationships ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_car_id", nullable = false)
    private InventoryCar inventoryCar;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_staff_id")
    private User salesStaff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead; // Source lead if converted
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showroom_id")
    private Showroom showroom;
    
    // === Pricing ===
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "additional_fees", precision = 15, scale = 2)
    private BigDecimal additionalFees; // Phí đăng ký, biển số, bảo hiểm
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount;
    
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "remaining_amount", precision = 15, scale = 2)
    private BigDecimal remainingAmount;
    
    // === Status ===
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;
    
    // === Dates ===
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;
    
    @Column(name = "completed_date")
    private LocalDate completedDate;
    
    @Column(name = "cancelled_date")
    private LocalDate cancelledDate;
    
    // === Payment Deadlines ===
    @Column(name = "deposit_payment_deadline")
    private java.time.LocalDateTime depositPaymentDeadline;
    
    @Column(name = "final_payment_deadline")
    private java.time.LocalDateTime finalPaymentDeadline;
    
    // === Notes ===
    @Lob
    private String notes; // Customer-visible notes
    
    @Lob
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Lob
    @Column(name = "internal_notes")
    private String internalNotes; // Staff-only notes
    
    /**
     * Auto-generate order number and set defaults before persisting
     */
    @PrePersist
    public void generateOrderNumber() {
        if (this.orderNumber == null) {
            // Format: ORD-2024-00001
            this.orderNumber = "ORD-" + LocalDate.now().getYear() + "-" + 
                String.format("%05d", System.currentTimeMillis() % 100000);
        }
        if (this.orderDate == null) {
            this.orderDate = LocalDate.now();
        }
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.DRAFT;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        if (this.additionalFees == null) {
            this.additionalFees = BigDecimal.ZERO;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate total price from components
     */
    public void calculateTotalPrice() {
        this.totalPrice = this.basePrice
            .add(this.additionalFees != null ? this.additionalFees : BigDecimal.ZERO)
            .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }
    
    /**
     * Calculate remaining amount
     */
    public void calculateRemainingAmount() {
        this.remainingAmount = this.totalPrice
            .subtract(this.paidAmount != null ? this.paidAmount : BigDecimal.ZERO);
    }
}
