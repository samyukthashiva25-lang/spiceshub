package com.example.spiceshub.service;

import com.example.spiceshub.model.Product;
import com.example.spiceshub.model.Order;
import com.example.spiceshub.model.User;
import com.example.spiceshub.model.Cart;
import com.example.spiceshub.model.CartItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.Date;

@Service
public class FirebaseService {

    // ==========================================
    // Product-Related Methods
    // ==========================================

    public String saveSpice(Product product) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<DocumentReference> docRef = db.collection("spices").add(product);
        return docRef.get().getId();
    }

    public List<Product> getAllSpices() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<Product> productList = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection("spices").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            Product product = document.toObject(Product.class);
            product.setId(document.getId());
            productList.add(product);
        }
        return productList;
    }

    public String updateSpice(String id, Product product) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("spices").document(id).set(product);
        return writeResult.get().getUpdateTime().toString();
    }

    public String deleteSpice(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("spices").document(id).delete();
        return "Product with ID " + id + " has been deleted";
    }

    // ==========================================
    // Order-Related Methods
    // ==========================================

    /**
     * Dedicated Order Placement & Account Settlement Processing Workflow
     */
   /**
     * Dedicated Order Placement & Account Settlement Processing Workflow
     */
    public Map<String, Object> checkoutAndPlaceOrder(String uid, String shippingAddress, String paymentMethod) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        
        // 1. Fetch current cart details
        Cart cart = getOrCreateCart(uid);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }
        
        long totalAmount = cart.getTotalprice();
        DocumentReference userRef = db.collection("users").document(uid);
        
        // 2. Handle Credit Limit Logic Validation and Deduction if chosen
        if ("CREDIT_LIMIT".equalsIgnoreCase(paymentMethod)) {
            DocumentSnapshot userSnap = userRef.get().get();
            if (!userSnap.exists()) {
                throw new IllegalArgumentException("User profile not found.");
            }
            
            long currentLimit = userSnap.contains("creditlimit") ? userSnap.getLong("creditlimit") : 0L;
            
            if (currentLimit < totalAmount) {
                throw new IllegalStateException("Insufficient credit limit. Available: ₹" + currentLimit + ", Required: ₹" + totalAmount);
            }
            
            // Deduct the order total directly from the user's available credit balance
            long updatedLimit = currentLimit - totalAmount;
            userRef.update("creditlimit", updatedLimit).get();
        }
        
        // 3. Assemble order properties dynamically as a Map to avoid Order.java setter compilation issues
        String uniqueOrderId = "ORD_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, Object> orderDocument = new HashMap<>();
        
        orderDocument.put("orderId", uniqueOrderId);
        orderDocument.put("userId", uid);
        orderDocument.put("items", new ArrayList<>(cart.getItems()));
        orderDocument.put("totalAmount", totalAmount);
        orderDocument.put("status", "PLACED");
        orderDocument.put("paymentMethod", paymentMethod.toUpperCase());
        orderDocument.put("timestamp", new Date().toString());
        orderDocument.put("shippingAddress", shippingAddress);
        
        // 4. Save the compiled Map document directly to Firestore
        db.collection("orders").document(uniqueOrderId).set(orderDocument).get();
        
        // 5. Purge/Clear current cart tracking list elements layout safely
        clearCart(uid);
        
        return orderDocument;
    }

    /**
     * Legacy Order Placement 
     */
    public String placeOrder(Map<String, Object> orderPayload) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        orderPayload.put("timestamp", new Date().toString());
        orderPayload.put("status", "PENDING");

        ApiFuture<DocumentReference> docRef = db.collection("orders").add(orderPayload);
        return docRef.get().getId();
    }

    

    public List<Order> getAllOrders() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection("orders").get().get().toObjects(Order.class);
    }

    public String updateOrder(String id, Order order) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("orders").document(id).set(order);
        return "Order " + id + " updated at " + writeResult.get().getUpdateTime();
    }

    public String deleteOrder(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("orders").document(id).delete();
        return "Order with ID " + id + " has been successfully deleted";
    }

    // ==========================================
    // Cart-Related Methods
    // ==========================================

    /**
     * Fetches a vendor's cart document by their UID. Creates a new initialized empty cart 
     * if the document doesn't exist yet.
     */
    public Cart getOrCreateCart(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("carts").document(uid);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            return document.toObject(Cart.class);
        } else {
            Cart newCart = new Cart(uid, new ArrayList<>(), 0L, new Date().getTime());
            docRef.set(newCart).get();
            return newCart;
        }
    }


    public void updateOrderStatus(String orderId, String status) throws Exception {

    Firestore db = FirestoreClient.getFirestore();

    ApiFuture<QuerySnapshot> future =
            db.collection("orders")
              .whereEqualTo("orderId", orderId)
              .get();

    List<QueryDocumentSnapshot> documents = future.get().getDocuments();

    if (documents.isEmpty()) {
        throw new IllegalArgumentException("Order not found");
    }

    DocumentReference docRef = documents.get(0).getReference();

    docRef.update("status", status).get();
}

    /**
     * Adds an item to a user's cart. Merges quantities and recalculates subtotals if 
     * matching variant attributes exist.
     */
    public Cart addItemToCart(String uid, CartItem newItem) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Cart cart = getOrCreateCart(uid);

        boolean itemExists = false;
        for (CartItem item : cart.getItems()) {
            if (item.getProductid().equals(newItem.getProductid()) && 
                item.getSelectedweight().equalsIgnoreCase(newItem.getSelectedweight())) {
                
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                item.setSubtotal(item.getQuantity() * item.getPriceperunit());
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            newItem.setSubtotal(newItem.getQuantity() * newItem.getPriceperunit());
            cart.getItems().add(newItem);
        }

        recalculateCartTotals(cart);
        db.collection("carts").document(uid).set(cart).get();
        return cart;
    }

    /**
     * Updates an explicit variant item count line. Removes the array item entirely 
     * if the quantity is adjusted below or equal to zero.
     */
    public Cart updateCartItemQuantity(String uid, String productId, String weight, long quantity) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Cart cart = getOrCreateCart(uid);

        if (cart.getUid() == null || cart.getUid().trim().isEmpty()) {
            cart.setUid(uid);
        }

        if (quantity <= 0) {
            cart.getItems().removeIf(item -> item.getProductid().equals(productId) && 
                                             item.getSelectedweight().equalsIgnoreCase(weight));
        } else {
            for (CartItem item : cart.getItems()) {
                if (item.getProductid().equals(productId) && item.getSelectedweight().equalsIgnoreCase(weight)) {
                    item.setQuantity(quantity);
                    item.setSubtotal(quantity * item.getPriceperunit());
                    break;
                }
            }
        }

        recalculateCartTotals(cart);
        db.collection("carts").document(uid).set(cart).get();
        return cart;
    }

    /**
     * Purges all elements out of an active tracking document card and resets the global value parameters.
     */
    public Cart clearCart(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Cart cart = getOrCreateCart(uid);
        
        cart.getItems().clear();
        cart.setTotalprice(0L);
        cart.setUpdatedat(new Date().getTime());
        
        db.collection("carts").document(uid).set(cart).get();
        return cart;
    }

    private void recalculateCartTotals(Cart cart) {
        long grandTotal = 0;
        for (CartItem item : cart.getItems()) {
            grandTotal += item.getSubtotal();
        }
        cart.setTotalprice(grandTotal);
        cart.setUpdatedat(new Date().getTime());
    }

    // ==========================================
    // User-Related Methods
    // ==========================================

    public String registerUser(User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        if (user.getUid() == null || user.getUid().trim().isEmpty()) {
            String generatedUid = db.collection("users").document().getId();
            user.setUid(generatedUid);
        }

        user.setRole("VENDOR");
        user.setStatus("PENDING");

        if (user.getCreditlimit() <= 0) {
            user.setCreditlimit(0);
        }

        db.collection("users").document(user.getUid()).set(user);
        return "Registration successful with ID: " + user.getUid();
    }

    public String approveUser(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("users").document(uid).update("status", "APPROVED");
        return "User approved at " + writeResult.get().getUpdateTime();
    }

    public String approveUserWithLimit(String uid, long customCreditLimit) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference userDocRef = db.collection("users").document(uid);

        DocumentSnapshot snapshot = userDocRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("User with UID " + uid + " does not exist.");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "APPROVED");
        updates.put("creditlimit", customCreditLimit);

        ApiFuture<WriteResult> writeResult = userDocRef.update(updates);
        return "User " + uid + " successfully approved with a credit limit of " + customCreditLimit + " at " + writeResult.get().getUpdateTime();
    }

    public String toggleUserStatus(String uid, String currentStatus) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference userDocRef = db.collection("users").document(uid);

        String newStatus = "INACTIVE".equalsIgnoreCase(currentStatus) ? "ACTIVE" : "INACTIVE";

        ApiFuture<WriteResult> writeResult = userDocRef.update("status", newStatus);
        return "User " + uid + " status toggled to " + newStatus + " at " + writeResult.get().getUpdateTime();
    }

    public User getUserProfile(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("users").document(uid);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            User user = document.toObject(User.class);
            if (user != null && (user.getUid() == null || user.getUid().trim().isEmpty())) {
                user.setUid(document.getId());
            }
            return user;
        }
        return null;
    }

    public String updateUserDetails(String uid, User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        if (user.getUid() == null || user.getUid().trim().isEmpty()) {
            user.setUid(uid);
        }

        ApiFuture<WriteResult> writeResult = db.collection("users").document(uid).set(user);
        return "User " + (user.getOwnername() != null ? user.getOwnername() : "") + " updated at "
                + writeResult.get().getUpdateTime();
    }

    public String deleteUser(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("users").document(uid).delete();
        return "User with UID " + uid + " has been deleted.";
    }

    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<User> userList = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection("users").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            User user = document.toObject(User.class);
            if (user != null) {
                if (user.getUid() == null || user.getUid().trim().isEmpty()) {
                    user.setUid(document.getId());
                }
                userList.add(user);
            }
        }
        return userList;
    }

    public User loginWithPhoneAndPassword(String phonenumber, String password) throws ExecutionException, InterruptedException {
        List<User> allUsers = getAllUsers();

        if (allUsers == null || allUsers.isEmpty()) {
            throw new IllegalArgumentException("Invalid phone number or matching password profile configuration.");
        }

        final String searchPhone = phonenumber != null ? phonenumber.trim() : "";
        User matchedUser = allUsers.stream()
                .filter(u -> u.getPhonenumber() != null && u.getPhonenumber().trim().equals(searchPhone))
                .findFirst()
                .orElse(null);

        if (matchedUser == null) {
            throw new IllegalArgumentException("Invalid phone number or matching password profile configuration.");
        }

        if (matchedUser.getPassword() == null || !matchedUser.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid phone number or matching password profile configuration.");
        }

        String currentStatus = matchedUser.getStatus() != null ? matchedUser.getStatus() : "PENDING";
        if ("PENDING".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException("Your vendor account registration is currently pending administrator approval.");
        } else if ("REJECTED".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException("Your account access has been rejected by management.");
        } else if ("INACTIVE".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException("Your account access has been marked as inactive.");
        }

        matchedUser.setPassword(null);
        return matchedUser;
    }
}