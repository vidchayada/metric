package metric.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.CreateProductRequest;
import metric.backend.dto.ProductResponse;
import metric.backend.service.AdminProductService;
import metric.backend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getProducts(category, search));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("data") CreateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminProductService.createProduct(request, images));
    }

    @PutMapping(value = "/{productId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestPart("data") CreateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(adminProductService.updateProduct(productId, request, images));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}