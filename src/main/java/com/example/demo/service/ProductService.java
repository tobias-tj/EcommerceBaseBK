package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductListDTO;
import com.example.demo.exception.ResourcesNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserService userService;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile productImage) throws IOException {
        Product product = productMapper.toEntity(productDTO);
        if(productImage != null && !productImage.isEmpty()){
            String fileName = saveImage(productImage);
            product.setImage("/images/"+fileName);
        }
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile productImage) throws IOException {
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new ResourcesNotFoundException("Product Not Found"));

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        if(productImage != null && !productImage.isEmpty()){
            String fileName = saveImage(productImage);
            existingProduct.setImage("/images/"+fileName);
        }
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id){
        if(!productRepository.existsById(id)){
            throw new ResourcesNotFoundException("Product Not Found");
        }

        productRepository.deleteById(id);
    }


    public ProductDTO getProduct(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourcesNotFoundException("Product Not Found"));
        return productMapper.toDtoWithUserMail(product, userService);
    }

    public List<ProductListDTO> getAllProducts(){
        return productRepository.findAllWithoutComments();
    }

    public List<ProductListDTO> searchProducts(  String searchTerm,
                                                 Double minPrice,
                                                 Double maxPrice,
                                                 Boolean inStock,
                                                 String brand) {
        return productRepository.findByFilters(searchTerm, minPrice, maxPrice, inStock, brand);
    }


    private String saveImage(MultipartFile productImage) throws IOException {
        String fileName = UUID.randomUUID().toString()+"."+productImage.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, productImage.getBytes());
        return fileName;
    }

}
