/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.CategoryResponse;
import metric.backend.dto.ProductResponse;
import metric.backend.mapper.ProductMapper;
import metric.backend.repository.CategoryRepository;
import metric.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByVisibility("show").stream()
                .map(productMapper::toCategoryResponse)
                .toList();
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toProductResponse)
                .orElse(null);
    }

    public List<ProductResponse> getProductsByCategorySlug(String slug) {
        return productRepository.findByCategory_Slug(slug).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public List<ProductResponse> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(keyword.trim()).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public List<ProductResponse> getProducts(Long categoryId, String search) {
        return productRepository.findAll().stream()
                .filter(p -> {
                    if (categoryId != null && p.getCategory() != null) {
                        if (!categoryId.equals(p.getCategory().getCategoryId())) {
                            return false;
                        }
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String keyword = search.trim().toLowerCase();
                        boolean matchName = p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword);
                        boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword);
                        return matchName || matchDesc;
                    }
                    return true;
                })
                .map(productMapper::toProductResponse)
                .toList();
    }
}