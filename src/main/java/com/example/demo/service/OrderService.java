package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourcesNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.model.*;
import com.example.demo.repositories.OrderRepositoy;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepositoy orderRepositoy;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;

    @Transactional
    public OrderDTO createOrder(Long userId, String address, String phoneNumber){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
        if(!user.isEmailConfirmation()){
            throw new IllegalStateException("Email not confirmed. Please confirm your email");
        }
        CartDTO cartDTO = cartService.getCart(userId);
        Cart cart = cartMapper.toEntity(cartDTO);

//        for (CartItem cartItem : cart.getCartItems()) {
//            Product product = productRepository.findById(cartItem.getId()).orElseThrow(() -> new ResourcesNotFoundException("Product not found with id: " + cartItem.getId()));
//            cartItem.setProduct(product);
//        }

        if (cart.getCartItems().isEmpty()) {
            throw new ResourcesNotFoundException("Cannot create an order with an empty cart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPhoneNumber(phoneNumber);
        order.setStatus(Order.OrderStatus.PREPARING);
        order.setCreateAt(LocalDateTime.now());

        List<OrderItem> orderItems = createOrder(cart, order);
        order.setItems(orderItems);

        Order saverdOrder = orderRepositoy.save(order);
        cartService.clearCart(userId);

        try{
            emailService.sendOrderConfirmation(saverdOrder);
        }catch (MailException e){
            logger.error("Failed to send order confirmation email for order ID " + saverdOrder.getId(), e);
        }
        return orderMapper.toDto(saverdOrder);
    }


    private List<OrderItem> createOrder(Cart cart, Order order){
        return cart.getCartItems().stream().map(cartItem -> {
            Product product = productRepository.findById(cartItem.getProduct().getId()).orElseThrow(() -> new EntityNotFoundException("Product not found with id: "+cartItem.getProduct().getId()));

            if(product.getQuantity() == null){
                throw new IllegalStateException("Product quantity is not set for product "+product.getName());
            }

            if(product.getQuantity() < cartItem.getQuantity()){
                throw new InsufficientStockException("Not enough stock for product "+product.getName());
            }

            product.setQuantity(product.getQuantity()-cartItem.getQuantity());
            productRepository.save(product);

            return new OrderItem(null, order, product, cartItem.getQuantity(), product.getPrice());
        }).collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders(){
        return orderMapper.toDto(orderRepositoy.findAll());
    }

    public List<OrderDTO> getUserOrders(Long userId){
        return orderMapper.toDto(orderRepositoy.findByUserId(userId));
    }

    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status){
        Order order = orderRepositoy.findById(orderId).orElseThrow(() -> new ResourcesNotFoundException("Order not found with id: "+orderId));
        order.setStatus(status);
        Order updatedOrder = orderRepositoy.save(order);
        return orderMapper.toDto(updatedOrder);
    }
}
