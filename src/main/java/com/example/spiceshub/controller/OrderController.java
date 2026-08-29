package com.example.spiceshub.controller;

import com.example.spiceshub.model.Order;
import com.example.spiceshub.service.FirebaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final FirebaseService firebaseService;

    public OrderController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = firebaseService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve order collection: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{uid}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable String uid) {
        try {
            List<Order> allOrders = firebaseService.getAllOrders();
            
            List<Map<String, Object>> responseList = allOrders.stream()
                    .filter(order -> order != null && uid.equals(order.getUserId()))
                    .map(order -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("orderId", order.getOrderId());
                        map.put("status", order.getStatus());
                        map.put("amount", order.getTotalAmount());
                        map.put("items", order.getItems());
                        map.put("shippingAddress", order.getShippingAddress());
                        map.put("paymentMethod", order.getPaymentMethod());
                        
                        // Robust Timestamp handling
                        if (order.getTimestamp() != null) {
                            Object ts = order.getTimestamp();
                            if (ts instanceof com.google.cloud.Timestamp) {
                                map.put("date", ( ts));
                            } else {
                                map.put("date", ts.toString());
                            }
                        } else {
                            map.put("date", "N/A");
                        }
                        
                        return map; // 💡 This return is required for the stream
                    })
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Data transformation failed: " + e.getMessage()));
        }
    }

    @PatchMapping("/{orderId}/status")
public ResponseEntity<?> updateOrderStatus(
        @PathVariable String orderId,
        @RequestParam String status) {

    try {

        firebaseService.updateOrderStatus(orderId, status);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Order status updated successfully",
                        "orderId", orderId,
                        "status", status
                )
        );

    } catch (IllegalArgumentException e) {

        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));

    } catch (Exception e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));

    }
}

    @PostMapping("/checkout/{uid}")
    public ResponseEntity<?> placeOrder(@PathVariable String uid, @RequestBody Map<String, String> payload) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing checkout request payload."));
            }

            String address = payload.getOrDefault("shippingAddress", "Store Pickup");
            String paymentMethod = payload.get("paymentMethod");
            
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment method selection is required."));
            }

            Map<String, Object> completedOrder = firebaseService.checkoutAndPlaceOrder(uid, address, paymentMethod);
            return ResponseEntity.ok(completedOrder);
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Transaction settlement failed: " + e.getMessage()));
        }
    }
}