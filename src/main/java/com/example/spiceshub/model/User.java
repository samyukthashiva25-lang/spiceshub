package com.example.spiceshub.model;

import com.google.cloud.firestore.annotation.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @DocumentId
    private String uid;          // Firebase Auth UID 
    private String shopname;     
    private String ownername;    
    private String phonenumber;  
    private String emailid;      
    private String password;     
    private String gstnumber;    
    private String image;        
    private long creditlimit;    // Maps perfectly to int64
    private String status;       // "PENDING", "APPROVED", etc.
    private String role;         // "VENDOR" or "ADMIN"
}

