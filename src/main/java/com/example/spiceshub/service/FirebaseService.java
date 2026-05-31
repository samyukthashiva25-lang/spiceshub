package com.example.spiceshub.service;

import com.example.spiceshub.model.Product;
import com.example.spiceshub.model.Order;
import com.example.spiceshub.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public String placeOrder(Order order) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        order.setOrderDate(new Date());
        order.setStatus("PENDING");

        ApiFuture<DocumentReference> docRef = db.collection("orders").add(order);
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
    // User-Related Methods (Fixed & Protected)
    // ==========================================

    public String registerUser(User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Check if the UID is null, empty string "", or just spaces
        if (user.getUid() == null || user.getUid().trim().isEmpty()) {
            // Automatically generate a random, unique Firestore document ID
            String generatedUid = db.collection("users").document().getId();
            user.setUid(generatedUid);
        }

        // Set your backend business defaults
        user.setRole("VENDOR");
        user.setStatus("PENDING");

        // Type-safe check for your long creditlimit property
        if (user.getCreditlimit() <= 0) {
            user.setCreditlimit(0);
        }

        // Now user.getUid() is guaranteed to be a valid, non-empty path string
        db.collection("users").document(user.getUid()).set(user);
        return "Registration successful with ID: " + user.getUid();
    }

    public String approveUser(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = db.collection("users").document(uid).update("status", "APPROVED");
        return "User approved at " + writeResult.get().getUpdateTime();
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
        Firestore db = FirestoreClient.getFirestore();

        // 1. Target query precisely matches the "phonenumber" field key stored in your document models
        CollectionReference usersCollection = db.collection("users");
        Query mobileQuery = usersCollection.whereEqualTo("phonenumber", phonenumber != null ? phonenumber.trim() : "");
        
        List<QueryDocumentSnapshot> documents = mobileQuery.get().get().getDocuments();

        // 2. Safeguard: Throw unified response message if profile cannot be located
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("Invalid phone number or matching password profile configuration.");
        }

        // 3. Serialize document data straight back down into a Java User profile object
        User user = documents.get(0).toObject(User.class);

        // 4. Verify password string equality constraints
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid phone number or matching password profile configuration.");
        }

        // 5. Enforce Admin verification gates
        if ("PENDING".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Your vendor account registration is currently pending administrator approval.");
        } else if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Your account access has been rejected by management.");
        }

        // Clear sensitive password string parameters out before network transit safely
        user.setPassword(null);
        return user;
    }
}