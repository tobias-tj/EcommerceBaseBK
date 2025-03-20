package com.example.demo.mapper;

import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.model.Comment;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "image", source = "image")
    ProductDTO toDto(Product product);

    default ProductDTO toDtoWithUserMail(Product product, UserService userService) {
        ProductDTO productDTO = toDto(product);
        if (product.getComments() != null) {
            List<CommentDTO> commentDTOs = product.getComments().stream()
                    .map(comment -> {
                        CommentDTO commentDTO = new CommentDTO();
                        commentDTO.setId(comment.getId());
                        commentDTO.setContent(comment.getContent());
                        commentDTO.setScore(comment.getScore());
                        commentDTO.setUserId(comment.getUser().getId());
                        // Obtener el correo electrónico del usuario
                        User user = userService.getUserById(comment.getUser().getId());
                        commentDTO.setEmail(user.getEmail());
                        return commentDTO;
                    })
                    .collect(Collectors.toList());
            productDTO.setComments(commentDTOs);
        }
        return productDTO;
    }

    @Mapping(target = "image", source = "image")
    Product toEntity(ProductDTO productDTO);

    @Mapping(target = "userId", source = "user.id")
    CommentDTO toDto(Comment comment);
    @Mapping(target="user.id", source = "userId")
    @Mapping(target = "product", ignore = true)
    Comment toEntity(CommentDTO commentDTO);
}
