package com.example.spiceshub.controller;

import com.example.spiceshub.model.User;
import com.example.spiceshub.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private FirebaseService firebaseService;

    // POST: Register a new Shop Owner -> returns {"message": "..."}
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) throws ExecutionException, InterruptedException {
        String result = firebaseService.registerUser(user);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // GET: Fetch shop details by UID -> returns User JSON object
    @GetMapping("/profile/{uid}")
    public ResponseEntity<?> getProfile(@PathVariable String uid) throws ExecutionException, InterruptedException {
        User user = firebaseService.getUserProfile(uid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User profile not found"));
        }
        return ResponseEntity.ok(user);
    }

    // GET: Fetch all users -> returns JSON Array of Users
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() throws ExecutionException, InterruptedException {
        List<User> users = firebaseService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // PUT: Update user details -> returns {"message": "..."}
    @PutMapping("/update/{uid}")
    public ResponseEntity<?> update(@PathVariable String uid, @RequestBody User user)
            throws ExecutionException, InterruptedException {
        String result = firebaseService.updateUserDetails(uid, user);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // DELETE: Delete a user profile -> returns {"message": "..."}
    @DeleteMapping("/delete/{uid}")
    public ResponseEntity<?> delete(@PathVariable String uid) throws ExecutionException, InterruptedException {
        String result = firebaseService.deleteUser(uid);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // GET: Gatekeeper status check using lowercase "status" variable -> returns structured JSON error/success objects
    @GetMapping("/check-status/{uid}")
    public ResponseEntity<?> checkApprovalStatus(@PathVariable String uid)
            throws ExecutionException, InterruptedException {
        User user = firebaseService.getUserProfile(uid);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        // Checks the exact "status" string matching your model variable
        if (user.getStatus() == null || !user.getStatus().equalsIgnoreCase("APPROVED")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", user.getStatus() != null ? user.getStatus() : "UNKNOWN",
                "error", "Account is pending admin confirmation status"
            ));
        }

        return ResponseEntity.ok(user);
    }
}