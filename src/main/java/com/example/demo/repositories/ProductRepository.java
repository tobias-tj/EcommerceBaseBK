package com.example.demo.repositories;

import com.example.demo.dto.ProductListDTO;
import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT new com.example.demo.dto.ProductListDTO(p.id, p.name, p.description, p.price, p.quantity, p.image, p.rating, p.brand) FROM Product p")
    List<ProductListDTO> findAllWithoutComments();
    @Query("SELECT new com.example.demo.dto.ProductListDTO(p.id, p.name, p.description, p.price, p.quantity, p.image, p.rating, p.brand) " +
            "FROM Product p " +
            "WHERE (:searchTerm IS NULL OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:inStock IS NULL OR (:inStock = true AND p.quantity > 0) OR (:inStock = false AND p.quantity = 0)) " +
            "AND (:brand IS NULL OR p.brand = :brand)")
    List<ProductListDTO> findByFilters(
            @Param("searchTerm") String searchTerm,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock,
            @Param("brand") String brand
    );
}
