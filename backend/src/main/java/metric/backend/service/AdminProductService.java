package metric.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import org.springframework.web.multipart.MultipartFile;

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

    private static final String UPLOAD_DIR = "uploads/products/";

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, List<MultipartFile> files) {
        Category category = getCategoryById(request.getCategoryId());

        Product product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        List<ProductImage> images = processImages(savedProduct, request.getImageUrls(), files);
        if (!images.isEmpty()) {
            productImageRepository.saveAll(images);
        }

        List<ProductAttribute> attributes = processAttributes(savedProduct, request.getAttributes());
        if (!attributes.isEmpty()) {
            productAttributeRepository.saveAll(attributes);
        }

        entityManager.flush();
        entityManager.clear();

        return productService.getProductById(savedProduct.getProductId());
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, CreateProductRequest request, List<MultipartFile> files) {
        Product product = getProductById(productId);
        Category category = getCategoryById(request.getCategoryId());

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setCategory(category);

        productRepository.save(product);

        boolean hasImageUrls = request.getImageUrls() != null && !request.getImageUrls().isEmpty();
        boolean hasFiles = files != null && !files.isEmpty();

        if (hasImageUrls || hasFiles) {
            productImageRepository.deleteByProduct_ProductId(productId);
            List<ProductImage> newImages = processImages(product, request.getImageUrls(), files);
            if (!newImages.isEmpty()) {
                productImageRepository.saveAll(newImages);
            }
        }

        if (request.getAttributes() != null) {
            productAttributeRepository.deleteByProduct_ProductId(productId);
            List<ProductAttribute> newAttributes = processAttributes(product, request.getAttributes());
            if (!newAttributes.isEmpty()) {
                productAttributeRepository.saveAll(newAttributes);
            }
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

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
    }

    private List<ProductImage> processImages(Product product, List<String> imageUrls, List<MultipartFile> files) {
        List<ProductImage> images = new ArrayList<>();

        if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                images.add(ProductImage.builder()
                        .product(product)
                        .imageUrl(imageUrls.get(i))
                        .isPrimary(images.isEmpty())
                        .build());
            }
        }

        if (files != null && !files.isEmpty()) {
            images.addAll(handleFileUploads(product, files, images.isEmpty()));
        }

        return images;
    }

    private List<ProductImage> handleFileUploads(Product product, List<MultipartFile> files, boolean isPrimaryEmpty) {
        List<ProductImage> uploadedImages = new ArrayList<>();
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        boolean currentPrimaryState = isPrimaryEmpty;

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String savedName = UUID.randomUUID().toString() + ext;
            Path targetLocation = Paths.get(UPLOAD_DIR).resolve(savedName);

            try {
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                uploadedImages.add(ProductImage.builder()
                        .product(product)
                        .imageUrl("/image/products/" + savedName)
                        .isPrimary(currentPrimaryState)
                        .build());
                currentPrimaryState = false;
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + originalName, e);
            }
        }
        return uploadedImages;
    }

    private List<ProductAttribute> processAttributes(Product product, List<CreateProductAttributeRequest> attributeRequests) {
        List<ProductAttribute> attributes = new ArrayList<>();
        if (attributeRequests == null || attributeRequests.isEmpty()) {
            return attributes;
        }

        for (CreateProductAttributeRequest attrReq : attributeRequests) {
            attributes.add(ProductAttribute.builder()
                    .product(product)
                    .size(attrReq.getSize())
                    .color(attrReq.getColor())
                    .stockQty(attrReq.getStockQty())
                    .build());
        }
        return attributes;
    }
}