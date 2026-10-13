package metric.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.CreateProductAttributeRequest;
import metric.backend.dto.CreateProductRequest;
import metric.backend.dto.ProductResponse;
import metric.backend.entity.Category;
import metric.backend.entity.Product;
import metric.backend.entity.ProductAttribute;
import metric.backend.entity.ProductImage;
import metric.backend.repository.CategoryRepository;
import metric.backend.repository.ProductAttributeRepository;
import metric.backend.repository.ProductImageRepository;
import metric.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductService productService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                images.add(ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .build());
            }
            productImageRepository.saveAll(images);
        }

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            List<ProductAttribute> attributes = new ArrayList<>();
            for (CreateProductAttributeRequest attrReq : request.getAttributes()) {
                attributes.add(ProductAttribute.builder()
                        .product(savedProduct)
                        .size(attrReq.getSize())
                        .color(attrReq.getColor())
                        .stockQty(attrReq.getStockQty())
                        .build());
            }
            productAttributeRepository.saveAll(attributes);
        }

        entityManager.flush();
        entityManager.clear();

        return productService.getProductById(savedProduct.getProductId());
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, CreateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setCategory(category);

        productRepository.save(product);

        if (request.getImageUrls() != null) {
            productImageRepository.deleteByProduct_ProductId(productId);
            List<ProductImage> newImages = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                newImages.add(ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .build());
            }
            productImageRepository.saveAll(newImages);
        }

        if (request.getAttributes() != null) {
            productAttributeRepository.deleteByProduct_ProductId(productId);
            List<ProductAttribute> newAttributes = new ArrayList<>();
            for (CreateProductAttributeRequest attrReq : request.getAttributes()) {
                newAttributes.add(ProductAttribute.builder()
                        .product(product)
                        .size(attrReq.getSize())
                        .color(attrReq.getColor())
                        .stockQty(attrReq.getStockQty())
                        .build());
            }
            productAttributeRepository.saveAll(newAttributes);
        }

        entityManager.flush();
        entityManager.clear();

        return productService.getProductById(productId);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }
        productAttributeRepository.deleteByProduct_ProductId(productId);
        productImageRepository.deleteByProduct_ProductId(productId);
        productRepository.deleteById(productId);
    }
}