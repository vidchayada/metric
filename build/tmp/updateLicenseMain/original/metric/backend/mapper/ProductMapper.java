package metric.backend.mapper;

import java.util.Collections;
import java.util.List;
import metric.backend.dto.CategoryResponse;
import metric.backend.dto.ProductAttributeResponse;
import metric.backend.dto.ProductResponse;
import metric.backend.entity.Category;
import metric.backend.entity.Product;
import metric.backend.entity.ProductAttribute;
import metric.backend.entity.ProductImage;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .slug(category.getSlug())
                .visibility(category.getVisibility())
                .build();
    }

    public ProductAttributeResponse toAttributeResponse(ProductAttribute attribute) {
        if (attribute == null) {
            return null;
        }
        return ProductAttributeResponse.builder()
                .attributeId(attribute.getAttributeId())
                .size(attribute.getSize())
                .color(attribute.getColor())
                .stockQty(attribute.getStockQty())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream().map(ProductImage::getImageUrl).toList()
                : Collections.emptyList();

        List<ProductAttributeResponse> attributes = product.getAttributes() != null
                ? product.getAttributes().stream().map(this::toAttributeResponse).toList()
                : Collections.emptyList();

        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .imageUrls(imageUrls)
                .attributes(attributes)
                .createdAt(product.getCreatedAt())
                .build();
    }
}