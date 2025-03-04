package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourcesNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartDTO addToCart(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourcesNotFoundException("Product not found"));

        if(product.getQuantity() < quantity) {
            throw new InsufficientStockException("Not enough available");
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(new Cart(null, user, new ArrayList<>()));
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                    .filter(cartItem -> cartItem.getProduct().getId().equals(product.getId())).findFirst();

        if(existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }else{
            CartItem cartItem = new CartItem(null, cart, product, quantity);
            cart.getCartItems().add(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }


    public CartDTO getCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourcesNotFoundException("User not found"));

        return cartMapper.toDto(cart);
    }

    public void clearCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }


}
