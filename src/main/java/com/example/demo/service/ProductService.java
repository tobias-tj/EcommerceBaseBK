package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.ResourcesNotFoundException;
import com.example.demo.mapper.MonthlySalesMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repositories.OrderRepositoy;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserService userService;
    private final OrderRepositoy orderRepositoy;
    private final UserRepository userRepository;
    private final MonthlySalesMapper monthlySalesMapper;



    private static final String UPLOAD_DIR = "src/main/resources/static/images/";

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile productImage) throws IOException {
        Product product = productMapper.toEntity(productDTO);
        if(productImage != null && !productImage.isEmpty()){
            String fileName = saveImage(productImage);
            product.setImage("/images/"+fileName);
        }
        product.setBrand(productDTO.getBrand());
        product.setRating(BigDecimal.valueOf(3.5));
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

    public SaleProducts getSaleProducts(){
        // 1. Total de ingresos
        BigDecimal totalIncoming = orderRepositoy.calculateTotalIncoming();

        // 2. Total de ventas
        Integer saleTotal = orderRepositoy.countCompletedOrders();

        // 3. Cantidad de productos
        Long countProducts = productRepository.count();

        // 4. Cantidad de clientes
        Long countClients = userRepository.count();

        // 5. Ventas mensuales
        List<MonthlySales> saleList = getMonthlySales();

        // 6. Ventas por marca
        List<BrandSales> brandList = orderRepositoy.findSalesByBrand();

        // 7. Productos más vendidos
        List<TopProducts> topProductsList = orderRepositoy.findTopProducts();

        return new SaleProducts(
                totalIncoming,
                saleTotal,
                countProducts,
                countClients,
                saleList,
                brandList,
                topProductsList
        );
    }


    private String saveImage(MultipartFile productImage) throws IOException {
        String fileName = UUID.randomUUID().toString()+"."+productImage.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, productImage.getBytes());
        return fileName;
    }

    private List<MonthlySales> getMonthlySales() {
        List<Object[]> results = orderRepositoy.findMonthlySalesRaw();
        List<MonthlySales> mothSalesList = results.stream()
                .map(result -> monthlySalesMapper.toMonthlySales(
                        (String) result[0], // month_name
                        ((Number) result[1]).longValue(), // quantity_sales
                        (Integer) result[2] // quantity_objective
                ))
                .collect(Collectors.toList());
        System.out.println(mothSalesList);
        return mothSalesList;
    }

}
