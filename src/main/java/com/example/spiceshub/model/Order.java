package com.example.spiceshub.model;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private long totalAmount;
    private String status;         // PLACED, DELIVERED, etc.
    private String paymentMethod;  // "COD" or "CREDIT_LIMIT"
    private String timestamp;      // Holds the stringified date format
    private String shippingAddress;

    // Default Constructor (Required by Jackson/Firestore for data mapping)
    public Order() {
    }

    // Parameterized Constructor
    public Order(String orderId, String userId, List<CartItem> items, long totalAmount, 
                 String status, String paymentMethod, String timestamp, String shippingAddress) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
        this.shippingAddress = shippingAddress;
    }

    // ==========================================
    // Getters and Setters
    // ==========================================

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}