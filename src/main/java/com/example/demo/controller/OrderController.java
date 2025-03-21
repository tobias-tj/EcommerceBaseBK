package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OrderDTO;
import com.example.demo.model.Order;
import com.example.demo.model.User;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> confirmOrder( @AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestParam String address, @RequestParam String phoneNumber, @RequestParam String paymentToken){
        if (!(userDetails instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Long userId = ((User) userDetails).getId();
        OrderDTO orderDTO = orderService.confirmOrder(userId, address, phoneNumber, paymentToken);
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders(){
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) Order.OrderStatus status,
                                                        @RequestParam(defaultValue = "false") Boolean isReview) {
        Long userId = ((User) userDetails).getId();
        List<OrderDTO> orders;
        if (status != null) {
            orders = orderService.getUserOrdersByStatus(userId, status, isReview);
        } else {
            orders = orderService.getUserOrders(userId);
        }
        return ResponseEntity.ok(orders);
    }

    @PutMapping("{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus( @PathVariable Long orderId,
                                                             @RequestParam Order.OrderStatus orderStatus ){
        OrderDTO updateOrder = orderService.updateOrderStatus(orderId, orderStatus);
        return ResponseEntity.ok(updateOrder);
    }

    @PostMapping("/preparing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> prepareOrder( @AuthenticationPrincipal UserDetails userDetails){
        Long userId = ((User) userDetails).getId();
        orderService.createPreparingOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Orden Creada con Exito!"));
    }

    @DeleteMapping("/clean")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cleanOrder( @AuthenticationPrincipal UserDetails userDetails){
        if (!(userDetails instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Long userId = ((User) userDetails).getId();
        orderService.clearOrders(userId);
        return ResponseEntity.noContent().build();
    }

}
