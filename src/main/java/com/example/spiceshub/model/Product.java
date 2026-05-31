package com.example.spiceshub.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                // Generates getters, setters, toString, etc.
@NoArgsConstructor   // Required for JSON parsing
@AllArgsConstructor  // Useful for creating objects quickly
public class Product {
    @DocumentId
    private String id;          // Firestore document reference ID
    
    private String productid;    // String identification key
    private String productname;  
    private String category;
    private String description;
    private String images;      // Matches your singular 'images' string property
    private boolean ispublished; 
    private String status;
    private String tags;
    private String variant;
}