// src/main/java/com/darina/PalaTOS/service/ProductService.java
package com.danven.web_library.service;

import com.danven.web_library.domain.product.Product;
import com.danven.web_library.dto.ProductDto;
import com.danven.web_library.dto.ProductEditDto;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.List;

public interface ProductService {
    List<ProductDto> getAllProducts();
    List<ProductDto> getProductsByCategory(Long categoryId);
    List<ProductDto> getProductsByCategoryName(String categoryName);
    ProductDto getProductById(Long productId);

    ProductDto addProduct(ProductEditDto dto) throws ValidationException, IOException;

    void editProduct(Long productId, ProductEditDto dto) throws IOException;
    void removeProduct(Long productId);
    Product getProductEntityById(Long productId);

    void deleteImage(Long prodId, Long imgId);
}

