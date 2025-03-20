package com.example.demo.repositories;

import com.example.demo.dto.BrandSales;
import com.example.demo.dto.MonthlySales;
import com.example.demo.dto.TopProducts;
import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;


public interface OrderRepositoy extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);

    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.order.status = 'CONFIRM'")
    BigDecimal calculateTotalIncoming();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CONFIRM'")
    Integer countCompletedOrders();

    // 5. Ventas mensuales
    @Query(value = "SELECT " +
            "to_char(o.create_at, 'Month') AS month_name," +
            "COUNT(o.id) AS quantity_sales," +
            "0 AS quantity_objective " +
            "FROM orders o " +
            "WHERE o.status = 'CONFIRM' " +
            "GROUP BY to_char(o.create_at, 'Month'), EXTRACT(MONTH FROM o.create_at)" +
            "ORDER BY EXTRACT(MONTH FROM o.create_at)",
            nativeQuery = true)
    List<Object[]> findMonthlySalesRaw();

    // 6. Ventas por marca
    @Query("SELECT new com.example.demo.dto.BrandSales(" +
            "p.brand, " +
            "SUM(oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE oi.order.status = 'CONFIRM' " +
            "GROUP BY p.brand")
    List<BrandSales> findSalesByBrand();


    // 7. Productos más vendidos
    @Query("SELECT new com.example.demo.dto.TopProducts(" +
            "p.name, " +
            "SUM(oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "WHERE oi.order.status = 'CONFIRM' " +
            "GROUP BY p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopProducts> findTopProducts();
}
