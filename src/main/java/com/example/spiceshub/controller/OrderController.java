package com.example.spiceshub.controller;

import com.example.spiceshub.model.Order;
import com.example.spiceshub.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Allows Flutter/Angular to talk to it
public class OrderController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/place")
    public String placeOrder(@RequestBody Order order) throws ExecutionException, InterruptedException {
        return firebaseService.placeOrder(order);
    }

    @GetMapping("/all")
    public List<Order> getOrders() throws ExecutionException, InterruptedException {
        return firebaseService.getAllOrders();
    }

    // Update an existing order
    @PutMapping("/update/{id}")
    public String updateOrder(@PathVariable String id, @RequestBody Order order)
            throws ExecutionException, InterruptedException {
        return firebaseService.updateOrder(id, order);
    }

    // Delete an order
    @DeleteMapping("/delete/{id}")
    public String deleteOrder(@PathVariable String id) throws ExecutionException, InterruptedException {
        return firebaseService.deleteOrder(id);
    }
}
