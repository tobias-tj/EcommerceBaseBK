package com.example.demo.repositories;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepositoy extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
