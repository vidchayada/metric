package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductResponseTest {

  @Test
  void shouldSupportBuilderAndAllFields() {
    var attribute = ProductAttributeResponse.builder()
        .attributeId(1L)
        .size("M")
        .color("Black")
        .stockQty(5)
        .build();

    var createdAt = OffsetDateTime.parse("2026-03-01T10:00:00Z");

    var response = ProductResponse.builder()
        .productId(1L)
        .productName("Shoes")
        .description("Running shoes")
        .basePrice(new BigDecimal("99.99"))
        .categoryId(2L)
        .categoryName("Footwear")
        .categorySlug("footwear")
        .imageUrls(List.of("/shoes.png"))
        .attributes(List.of(attribute))
        .createdAt(createdAt)
        .build();

    assertEquals(1L, response.getProductId());
    assertEquals("Shoes", response.getProductName());
    assertEquals("Running shoes", response.getDescription());
    assertEquals(new BigDecimal("99.99"), response.getBasePrice());
    assertEquals(2L, response.getCategoryId());
    assertEquals("Footwear", response.getCategoryName());
    assertEquals("footwear", response.getCategorySlug());
    assertEquals(List.of("/shoes.png"), response.getImageUrls());
    assertEquals(List.of(attribute), response.getAttributes());
    assertEquals(createdAt, response.getCreatedAt());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new ProductResponse().getProductId());

    var response = new ProductResponse(
        1L, "Shoes", "Description", BigDecimal.TEN,
        2L, "Footwear", "footwear",
        List.of("/a.png"), List.of(), null);

    response.setProductId(3L);
    response.setProductName("Bag");
    response.setDescription("Leather");
    response.setBasePrice(BigDecimal.ONE);
    response.setCategoryId(4L);
    response.setCategoryName("Bags");
    response.setCategorySlug("bags");
    response.setImageUrls(null);
    response.setAttributes(null);
    response.setCreatedAt(null);

    assertEquals(3L, response.getProductId());
    assertEquals("Bag", response.getProductName());
    assertEquals("Leather", response.getDescription());
    assertEquals(BigDecimal.ONE, response.getBasePrice());
    assertEquals(4L, response.getCategoryId());
    assertEquals("Bags", response.getCategoryName());
    assertEquals("bags", response.getCategorySlug());
    assertNull(response.getImageUrls());
    assertNull(response.getAttributes());
    assertNull(response.getCreatedAt());
  }
}

