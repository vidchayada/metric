package metric.backend.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        var allCategories = productService.getAllCategories();

        List<Map<String, Object>> response = new ArrayList<>();
        if (allCategories != null) {
            for (var cat : allCategories) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cat.getCategoryId());
                map.put("name", cat.getCategoryName());
                map.put("slug", cat.getSlug());
                map.put("image", null);
                response.add(map);
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {

        String searchTerm = resolveSearchTerm(q, search, keyword);
        List<ProductResponse> products = findProducts(category, searchTerm);
        if (products == null) {
            products = Collections.emptyList();
        }

        return ResponseEntity.ok(buildPagedResponse(products, page, size));
    }

    private String resolveSearchTerm(String q, String search, String keyword) {
        if (q != null && !q.isBlank()) {
            return q.trim();
        }
        if (search != null && !search.isBlank()) {
            return search.trim();
        }
        if (keyword != null && !keyword.isBlank()) {
            return keyword.trim();
        }
        return null;
    }

    private List<ProductResponse> findProducts(String category, String searchTerm) {
        if (category != null && !category.isBlank()) {
            return productService.getProductsByCategorySlug(category.trim());
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            return productService.getProducts(null, searchTerm);
        }
        return productService.getAllProducts();
    }

    private Map<String, Object> buildPagedResponse(List<ProductResponse> products, int page, int size) {
        int totalItems = products.size();
        int safeSize = size > 0 ? size : 8;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / safeSize));
        int fromIndex = Math.clamp((long) (page - 1) * safeSize, 0, totalItems); // fix for S6885
        int toIndex = Math.min(fromIndex + safeSize, totalItems);
        List<ProductResponse> pagedProducts = products.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("items", pagedProducts);
        response.put("products", pagedProducts);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        response.put("page", page);
        response.put("size", safeSize);
        return response;
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
    public ResponseEntity<Map<String, Object>> getHomeData() {
        List<ProductResponse> allProducts = productService.getAllProducts();
        List<CategoryResponse> allCategories = productService.getAllCategories();

        List<ProductResponse> bestSellers = (allProducts != null)
                ? allProducts.stream().limit(4).toList()
                : Collections.emptyList();

        String newestImage = findFirstProductImage(allProducts);
        List<Map<String, Object>> formattedCategories = buildFormattedCategories(allCategories, allProducts);

        Map<String, Object> response = new HashMap<>();
        response.put("bestSellers", bestSellers);
        response.put("categories", formattedCategories);
        response.put("newestImage", newestImage != null ? newestImage : "");

        return ResponseEntity.ok(response);
    }

    private String findFirstProductImage(List<ProductResponse> allProducts) {
        if (allProducts == null) {
            return null;
        }
        for (ProductResponse p : allProducts) {
            if (p.getImageUrls() != null && !p.getImageUrls().isEmpty() && p.getImageUrls().get(0) != null) {
                return p.getImageUrls().get(0);
            }
        }
        return null;
    }

    private List<Map<String, Object>> buildFormattedCategories(List<CategoryResponse> allCategories, List<ProductResponse> allProducts) {
        List<Map<String, Object>> formattedCategories = new ArrayList<>();
        if (allCategories == null) {
            return formattedCategories;
        }
        for (CategoryResponse cat : allCategories) {
            String catImage = findCategoryImage(cat, allProducts);

            Map<String, Object> cMap = new HashMap<>();
            cMap.put("id", cat.getCategoryId());
            cMap.put("name", cat.getCategoryName());
            cMap.put("slug", cat.getSlug());
            cMap.put("image", catImage != null ? catImage : "");
            formattedCategories.add(cMap);
        }
        return formattedCategories;
    }

    private String findCategoryImage(CategoryResponse cat, List<ProductResponse> allProducts) {
        if (allProducts == null) {
            return null;
        }
        for (ProductResponse prod : allProducts) {
            // fix for S1066: merged nested if into single && condition
            if (prod.getCategoryId() != null && prod.getCategoryId().equals(cat.getCategoryId())
                    && prod.getImageUrls() != null && !prod.getImageUrls().isEmpty()) {
                return prod.getImageUrls().get(0);
            }
        }
        return null;
    }
}