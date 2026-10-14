package metric.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import metric.backend.entity.Category;
import metric.backend.repository.CategoryRepository;
import metric.backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASH = Pattern.compile("(?:^-|-$)");

    private static String toSlug(String name) {
        String dashed = NON_ALNUM.matcher(name.toLowerCase(Locale.ROOT)).replaceAll("-");
        return EDGE_DASH.matcher(dashed).replaceAll("");
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCategories(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search) {

        List<Map<String, Object>> response = categoryRepository.findAll().stream()
                .filter(c -> {
                    if (filter != null && !filter.trim().isEmpty()) {
                        if (!filter.equalsIgnoreCase(c.getVisibility())) {
                            return false;
                        }
                    }

                    if (search != null && !search.trim().isEmpty()) {
                        String keyword = search.trim().toLowerCase();
                        boolean matchName = c.getCategoryName() != null && c.getCategoryName().toLowerCase().contains(keyword);
                        boolean matchSlug = c.getSlug() != null && c.getSlug().toLowerCase().contains(keyword);
                        return matchName || matchSlug;
                    }
                    return true;
                })
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    Long catId = c.getCategoryId();
                    map.put("id", catId);
                    map.put("categoryId", catId);
                    map.put("name", c.getCategoryName());
                    map.put("categoryName", c.getCategoryName());
                    map.put("slug", c.getSlug());
                    map.put("visibility", c.getVisibility());

                    long count = productRepository.findAll().stream()
                            .filter(p -> p.getCategory() != null && catId.equals(p.getCategory().getCategoryId()))
                            .count();
                    map.put("productCount", count);
                    return map;
                }).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, String> body) {
        String name = body.get("name") != null ? body.get("name") : body.get("categoryName");
        String visibility = body.getOrDefault("visibility", "show");
        String slug = toSlug(name);
        Category category = Category.builder()
                .categoryName(name)
                .slug(slug)
                .visibility(visibility)
                .build();

        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        if (body.containsKey("name")) {
            String name = body.get("name");
            category.setCategoryName(name);
            category.setSlug(toSlug(name));
        }
        if (body.containsKey("visibility")) {
            category.setVisibility(body.get("visibility"));
        }

        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Category> setVisibilityPatch(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        if (body.containsKey("visibility")) {
            category.setVisibility(body.get("visibility"));
        }
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<Category> setVisibilityPut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return setVisibilityPatch(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}