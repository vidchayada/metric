/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

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
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private String status;
    private OffsetDateTime orderDate;
    private List<OrderItemResponse> items;
}