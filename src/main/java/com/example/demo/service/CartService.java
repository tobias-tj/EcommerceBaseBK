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

    public void addToCart( Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourcesNotFoundException("Product not found"));

        if (quantity > 0 && product.getQuantity() < quantity) {
            throw new InsufficientStockException("Not enough available");
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(new Cart(null, user, new ArrayList<>()));
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                    .filter(cartItem -> cartItem.getProduct().getId().equals(product.getId())).findFirst();

        if(existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            // Si la nueva cantidad es <= 0, elimina el ítem del carrito
            if (newQuantity <= 0) {
                cart.getCartItems().remove(cartItem);
            } else {
                cartItem.setQuantity(newQuantity);
            }
        }else{
            // Si el ítem no existe y la cantidad es positiva, agrégalo al carrito
            if (quantity > 0) {
                CartItem cartItem = new CartItem(null, cart, product, quantity);
                cart.getCartItems().add(cartItem);
            } else {
                throw new IllegalArgumentException("No se puede agregar una cantidad negativa de un producto nuevo");
            }
        }

        Cart savedCart = cartRepository.save(cart);
        cartMapper.toDto(savedCart);
    }


    public CartDTO getCart(Long userId){
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        Cart cart = cartOptional.orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new ResourcesNotFoundException("User not found")));
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        return cartMapper.toDto(cart);
    }

    public void clearCart(Long userId){
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new ResourcesNotFoundException("User not found"));
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }


}
