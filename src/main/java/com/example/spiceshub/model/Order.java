package com.example.spiceshub.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @DocumentId
    private String orderId;
    private String customerEmail;
    private List<String> spiceIds; // List of Spices purchased
    private Double totalAmount;
    private String status;         // e.g., "PENDING", "SHIPPED", "DELIVERED"
    private Date orderDate;
}