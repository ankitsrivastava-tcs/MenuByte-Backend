package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.menubyte.enums.OrderStatus;
import com.menubyte.enums.PaymentMode;
import com.menubyte.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razorpay_order_id", unique = true, nullable = true)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true, nullable = true)
    private String razorpayPaymentId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // --- Enum Status Mapping ---
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> orderItems;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false)
    private PaymentMode paymentMode = PaymentMode.ONLINE; // Default Online
    @Column(name = "table_number", nullable = true) // Set nullable=true as not all orders need a table
    private String tableNumber;

    @Column(name = "order_note", length = 500)
    private String orderNote;
}
