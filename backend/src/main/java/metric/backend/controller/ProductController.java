/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.CategoryResponse;
import metric.backend.dto.ProductResponse;
import metric.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(productService.getProductsByCategorySlug(category));
        }
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(search));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/home")
    public ResponseEntity<java.util.Map<String, Object>> getHomeData() {
        var allProducts = productService.getAllProducts();
        var allCategories = productService.getAllCategories();
        var bestSellers = (allProducts != null)
                ? allProducts.stream().limit(4).toList()
                : java.util.Collections.<ProductResponse>emptyList();

        String newestImage = null;
        if (allProducts != null) {
            for (ProductResponse p : allProducts) {
                if (p.getImageUrls() != null && !p.getImageUrls().isEmpty() && p.getImageUrls().get(0) != null) {
                    newestImage = p.getImageUrls().get(0);
                    break;
                }
            }
        }

        java.util.List<java.util.Map<String, Object>> formattedCategories = new java.util.ArrayList<>();
        if (allCategories != null) {
            for (CategoryResponse cat : allCategories) {
                String catImage = null;
                if (allProducts != null) {
                    for (ProductResponse prod : allProducts) {
                        if (prod.getCategoryId() != null && prod.getCategoryId().equals(cat.getCategoryId())) {
                            if (prod.getImageUrls() != null && !prod.getImageUrls().isEmpty()) {
                                catImage = prod.getImageUrls().get(0);
                                break;
                            }
                        }
                    }
                }

                java.util.Map<String, Object> cMap = new java.util.HashMap<>();
                cMap.put("id", cat.getCategoryId());
                cMap.put("name", cat.getCategoryName());
                cMap.put("slug", cat.getSlug());
                cMap.put("image", catImage != null ? catImage : "");
                formattedCategories.add(cMap);
            }
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("bestSellers", bestSellers);
        response.put("categories", formattedCategories);
        response.put("newestImage", newestImage != null ? newestImage : "");

        return ResponseEntity.ok(response);
    }
}