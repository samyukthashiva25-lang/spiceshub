package com.example.spiceshub.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {

    @JsonProperty("productid")
    private String productid;

    @JsonProperty("productname")
    private String productname;

    @JsonProperty("selectedweight")
    private String selectedweight;

    @JsonProperty("priceperunit")
    private long priceperunit;

    @JsonProperty("quantity")
    private long quantity;
    
    @JsonProperty("imagepath")
    private String imagepath;

    @JsonProperty("subtotal")
    private long subtotal;
}