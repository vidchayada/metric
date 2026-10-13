/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

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
public class OrderItemResponse {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Long attributeId;
    private String size;
    private String color;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}