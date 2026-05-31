package com.example.spiceshub.controller;

import com.example.spiceshub.model.Product;
import com.example.spiceshub.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // Allows Angular dashboard and Flutter mobile app to communicate smoothly
public class ProductController {

    @Autowired
    private FirebaseService firebaseService;

    // POST: Add a new product -> returns {"id": "..."}
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Product product) throws ExecutionException, InterruptedException {
        String productId = firebaseService.saveSpice(product);
        return ResponseEntity.ok(Map.of("id", productId));
    }

    // GET: Fetch all products -> returns JSON Array of Products
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() throws ExecutionException, InterruptedException {
        List<Product> products = firebaseService.getAllSpices();
        return ResponseEntity.ok(products);
    }

    // PUT: Update an existing product by document ID -> returns {"message": "...", "updatedTime": "..."}
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product product) throws ExecutionException, InterruptedException {
        String updatedTime = firebaseService.updateSpice(id, product);
        return ResponseEntity.ok(Map.of(
            "message", "Product updated successfully",
            "updatedTime", updatedTime
        ));
    }

    // DELETE: Permanently remove a product -> returns {"message": "..."}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) throws ExecutionException, InterruptedException {
        String result = firebaseService.deleteSpice(id);
        return ResponseEntity.ok(Map.of("message", result));
    }
}