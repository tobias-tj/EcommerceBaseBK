package com.example.demo.mapper;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "cartItems")
    CartDTO toDto(Cart cart);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "cartItems", source = "items")
    Cart toEntity(CartDTO cartDTO);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(source = "product.name", target = "productName")
    CartItemDTO toDto(CartItem cartItem);

    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "product.name", source = "productName")
    CartItem toEntity(CartItemDTO cartItemDTO);
}
