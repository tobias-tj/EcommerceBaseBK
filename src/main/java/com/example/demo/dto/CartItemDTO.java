package com.example.demo.dto;

import com.example.demo.model.Product;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    @Positive
    private Integer quantity;

    public void setProduct( Product product ) {

    }
}
