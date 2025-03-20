package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SaleProducts {
    private BigDecimal totalIncoming;
    private Integer saleTotal;
    private Long countProducts;
    private Long countClients;
    private List<MonthlySales> saleList;
    private List<BrandSales> brandList;
    private List<TopProducts> topProductsList;
}