package metric.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal basePrice;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private List<String> imageUrls;
    private List<ProductAttributeResponse> attributes;
    private OffsetDateTime createdAt;
}