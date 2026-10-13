package metric.backend.dto;

import java.math.BigDecimal;
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
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Long attributeId;
    private String size;
    private String color;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public Long getId() {
        return cartItemId;
    }

    public String getName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return unitPrice;
    }

    public String getImage() {
        return imageUrl;
    }
}