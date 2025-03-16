package com.example.demo.dto;

import com.example.demo.model.Product;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    @Positive
    private Integer quantity;

    public void setProduct( Product product ) {

    }
}
