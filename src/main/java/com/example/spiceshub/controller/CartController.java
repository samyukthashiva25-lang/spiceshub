package com.example.spiceshub.controller;

import com.example.spiceshub.model.Cart;
import com.example.spiceshub.model.CartItem;
import com.example.spiceshub.service.FirebaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final FirebaseService firebaseService;

    public CartController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    /**
     * GET /api/cart/{uid}
     */
    @GetMapping("/{uid}")
    public ResponseEntity<?> getCart(@PathVariable String uid) {
        try {
            Cart cart = firebaseService.getOrCreateCart(uid);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve vendor cart instance: " + e.getMessage()));
        }
    }

    /**
     * POST /api/cart/{uid}/add
     */
    @PostMapping("/{uid}/add")
    public ResponseEntity<?> addItem(@PathVariable String uid, @RequestBody Map<String, Object> payload) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Empty request payload."));
            }

            // Case-Insensitive Extraction Strategy
            String productId = payload.containsKey("productId") ? (String) payload.get("productId") : (String) payload.get("productid");
            String productName = payload.containsKey("productName") ? (String) payload.get("productName") : (String) payload.get("productname");
            String selectedWeight = payload.containsKey("selectedWeight") ? (String) payload.get("selectedWeight") : (String) payload.get("selectedweight");
            String imagePath = payload.containsKey("imagePath") ? (String) payload.get("imagePath") : (String) payload.get("imagepath");
            
            Object rawPrice = payload.containsKey("pricePerUnit") ? payload.get("pricePerUnit") : payload.get("priceperunit");
            // ✅ FIX: Extract as a Number and convert to longValue() to match your model structure
            long pricePerUnit = (rawPrice instanceof Number) ? ((Number) rawPrice).longValue() : 0L;

            Object rawQty = payload.get("quantity");
            long quantity = (rawQty instanceof Number) ? ((Number) rawQty).longValue() : 1L;

            if (productId == null || productId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Malformed payload: Missing valid product identifier."));
            }

            // Initialize and assemble CartItem cleanly
            CartItem item = new CartItem();
            item.setProductid(productId); 
            item.setProductname(productName);
            item.setSelectedweight(selectedWeight);
            item.setImagepath(imagePath);
            item.setPriceperunit(pricePerUnit); // ✅ Now this matches perfect! (long to long)
            item.setQuantity((int) quantity);

            Cart updatedCart = firebaseService.addItemToCart(uid, item);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to append item to vendor cart: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/cart/{uid}/update
     */
    @PutMapping("/{uid}/update")
    public ResponseEntity<?> updateQuantity(
            @PathVariable String uid,
            @RequestBody Map<String, Object> payload) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required payload update fields."));
            }

            // Case-Insensitive Extraction Strategy
            String productId = payload.containsKey("productId") ? (String) payload.get("productId") : (String) payload.get("productid");
            String selectedWeight = payload.containsKey("selectedWeight") ? (String) payload.get("selectedWeight") : (String) payload.get("selectedweight");
            
            Object rawQuantity = payload.get("quantity");
            if (productId == null || rawQuantity == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters: productid or quantity."));
            }
            
            long quantity = (rawQuantity instanceof Number) ? ((Number) rawQuantity).longValue() : Long.parseLong(rawQuantity.toString());

            Cart updatedCart = firebaseService.updateCartItemQuantity(uid, productId, selectedWeight, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to alter target item properties count: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/cart/{uid}/clear
     */
    @DeleteMapping("/{uid}/clear")
    public ResponseEntity<?> clearCart(@PathVariable String uid) {
        try {
            Cart emptyCart = firebaseService.clearCart(uid);
            return ResponseEntity.ok(emptyCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to purge tracking list elements layout: " + e.getMessage()));
        }
    }
}