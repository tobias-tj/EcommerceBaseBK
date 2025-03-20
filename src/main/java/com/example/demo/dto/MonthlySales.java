package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class MonthlySales {
    private String monthName;
    private BigInteger quantitySales;
    private Integer quantityObjective;
}
