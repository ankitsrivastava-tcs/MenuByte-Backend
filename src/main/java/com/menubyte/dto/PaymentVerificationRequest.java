package com.menubyte.dto;

import java.util.List;
import java.util.Map;

public class PaymentVerificationRequest {
    private String razorpay_payment_id;
    private String razorpay_order_id;
    private String razorpay_signature;
    private PaymentDetails paymentDetails;
    private List<Map<String, Object>> orderItems;
    private Long businessId;
    private Long userId;

    // Getters and Setters for all fields
    public String getRazorpay_payment_id() {
        return razorpay_payment_id;
    }

    public void setRazorpay_payment_id(String razorpay_payment_id) {
        this.razorpay_payment_id = razorpay_payment_id;
    }

    public String getRazorpay_order_id() {
        return razorpay_order_id;
    }

    public void setRazorpay_order_id(String razorpay_order_id) {
        this.razorpay_order_id = razorpay_order_id;
    }

    public String getRazorpay_signature() {
        return razorpay_signature;
    }

    public void setRazorpay_signature(String razorpay_signature) {
        this.razorpay_signature = razorpay_signature;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public List<Map<String, Object>> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<Map<String, Object>> orderItems) {
        this.orderItems = orderItems;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
