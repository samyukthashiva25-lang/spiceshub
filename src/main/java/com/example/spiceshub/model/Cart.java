package com.example.spiceshub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart {

    @DocumentId
    private String uid; // Document ID maps perfectly to the corresponding User's Auth UID
    private List<CartItem> items = new ArrayList<>();
    private long totalprice;
    private long updatedat;
}