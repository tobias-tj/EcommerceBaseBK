package com.example.demo.mapper;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "orderItems", source = "items")
    OrderDTO toDto(Order order);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "items", source = "orderItems")
    Order toEntity(OrderDTO orderDTO);

    List<OrderDTO> toDto(List<Order> orders);
    List<Order> toEntities(List<OrderDTO> ordersDTOS);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImage", source = "product.image")
    @Mapping(target = "productBrand", source = "product.brand")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);

    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "product.name", source = "productName")
    @Mapping(target = "product.image", source = "productImage")
    @Mapping(target = "product.brand", source = "productBrand")
    OrderItem toOrderItemEntity(OrderItemDTO orderItem);

    List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> orderItem);
    List<OrderItem> toOrderItemEntities(List<OrderItemDTO> orderItemDTOS);


}
