package metric.backend.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import metric.backend.dto.CategoryResponse;
import metric.backend.dto.ProductAttributeResponse;
import metric.backend.dto.ProductResponse;
import metric.backend.entity.Category;
import metric.backend.entity.Product;
import metric.backend.entity.ProductAttribute;
import metric.backend.entity.ProductImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductMapperTest {

  private ProductMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ProductMapper();
  }

  @Test
  void toCategoryResponseShouldReturnNullWhenCategoryIsNull() {
    assertNull(mapper.toCategoryResponse(null));
  }

  @Test
  void toCategoryResponseShouldMapAllFields() {
    Category category = Category.builder()
        .categoryId(1L)
        .categoryName("Shoes")
        .slug("shoes")
        .visibility("show")
        .build();

    CategoryResponse response = mapper.toCategoryResponse(category);

    assertNotNull(response);
    assertEquals(1L, response.getCategoryId());
    assertEquals("Shoes", response.getCategoryName());
    assertEquals("shoes", response.getSlug());
    assertEquals("show", response.getVisibility());
  }

  @Test
  void toCategoryResponseShouldPreserveNullFields() {
    Category category = Category.builder().build();

    CategoryResponse response = mapper.toCategoryResponse(category);

    assertNotNull(response);
    assertNull(response.getCategoryId());
    assertNull(response.getCategoryName());
    assertNull(response.getSlug());
    assertNull(response.getVisibility());
  }

  @Test
  void toAttributeResponseShouldReturnNullWhenAttributeIsNull() {
    assertNull(mapper.toAttributeResponse(null));
  }

  @Test
  void toAttributeResponseShouldMapAllFields() {
    ProductAttribute attribute = ProductAttribute.builder()
        .attributeId(10L)
        .size("L")
        .color("Black")
        .stockQty(25)
        .build();

    ProductAttributeResponse response =
        mapper.toAttributeResponse(attribute);

    assertNotNull(response);
    assertEquals(10L, response.getAttributeId());
    assertEquals("L", response.getSize());
    assertEquals("Black", response.getColor());
    assertEquals(25, response.getStockQty());
  }

  @Test
  void toAttributeResponseShouldPreserveNullFields() {
    ProductAttribute attribute = ProductAttribute.builder()
        .stockQty(null)
        .build();

    ProductAttributeResponse response =
        mapper.toAttributeResponse(attribute);

    assertNotNull(response);
    assertNull(response.getAttributeId());
    assertNull(response.getSize());
    assertNull(response.getColor());
    assertNull(response.getStockQty());
  }

  @Test
  void toProductResponseShouldReturnNullWhenProductIsNull() {
    assertNull(mapper.toProductResponse(null));
  }

  @Test
  void toProductResponseShouldMapAllFieldsAndRelationships() {
    OffsetDateTime createdAt =
        OffsetDateTime.parse("2026-01-10T10:15:30+07:00");

    Category category = Category.builder()
        .categoryId(1L)
        .categoryName("Shoes")
        .slug("shoes")
        .visibility("show")
        .build();

    ProductImage image1 = ProductImage.builder()
        .imageId(1L)
        .imageUrl("/images/shoes-1.png")
        .build();

    ProductImage image2 = ProductImage.builder()
        .imageId(2L)
        .imageUrl("/images/shoes-2.png")
        .build();

    ProductAttribute attribute1 = ProductAttribute.builder()
        .attributeId(11L)
        .size("M")
        .color("Black")
        .stockQty(10)
        .build();

    ProductAttribute attribute2 = ProductAttribute.builder()
        .attributeId(12L)
        .size("L")
        .color("White")
        .stockQty(5)
        .build();

    Product product = Product.builder()
        .productId(100L)
        .productName("Running Shoes")
        .description("Lightweight running shoes")
        .basePrice(new BigDecimal("1299.50"))
        .category(category)
        .images(List.of(image1, image2))
        .attributes(List.of(attribute1, attribute2))
        .createdAt(createdAt)
        .build();

    ProductResponse response = mapper.toProductResponse(product);

    assertNotNull(response);
    assertEquals(100L, response.getProductId());
    assertEquals("Running Shoes", response.getProductName());
    assertEquals("Lightweight running shoes", response.getDescription());
    assertEquals(new BigDecimal("1299.50"), response.getBasePrice());
    assertEquals(1L, response.getCategoryId());
    assertEquals("Shoes", response.getCategoryName());
    assertEquals("shoes", response.getCategorySlug());
    assertEquals(createdAt, response.getCreatedAt());

    assertEquals(
        List.of("/images/shoes-1.png", "/images/shoes-2.png"),
        response.getImageUrls());

    assertEquals(2, response.getAttributes().size());

    ProductAttributeResponse first = response.getAttributes().get(0);
    assertEquals(11L, first.getAttributeId());
    assertEquals("M", first.getSize());
    assertEquals("Black", first.getColor());
    assertEquals(10, first.getStockQty());

    ProductAttributeResponse second = response.getAttributes().get(1);
    assertEquals(12L, second.getAttributeId());
    assertEquals("L", second.getSize());
    assertEquals("White", second.getColor());
    assertEquals(5, second.getStockQty());
  }

  @Test
  void toProductResponseShouldUseEmptyListsWhenImagesAndAttributesAreNull() {
    Product product = Product.builder()
        .productId(1L)
        .productName("Product")
        .images(null)
        .attributes(null)
        .build();

    ProductResponse response = mapper.toProductResponse(product);

    assertNotNull(response);
    assertNotNull(response.getImageUrls());
    assertTrue(response.getImageUrls().isEmpty());
    assertNotNull(response.getAttributes());
    assertTrue(response.getAttributes().isEmpty());
  }

  @Test
  void toProductResponseShouldUseEmptyListsWhenImagesAndAttributesAreEmpty() {
    Product product = Product.builder()
        .productId(1L)
        .images(new ArrayList<>())
        .attributes(new ArrayList<>())
        .build();

    ProductResponse response = mapper.toProductResponse(product);

    assertNotNull(response);
    assertTrue(response.getImageUrls().isEmpty());
    assertTrue(response.getAttributes().isEmpty());
  }

  @Test
  void toProductResponseShouldHandleNullCategory() {
    Product product = Product.builder()
        .productId(2L)
        .productName("Uncategorized Product")
        .category(null)
        .build();

    ProductResponse response = mapper.toProductResponse(product);

    assertNotNull(response);
    assertNull(response.getCategoryId());
    assertNull(response.getCategoryName());
    assertNull(response.getCategorySlug());
  }

  @Test
  void toProductResponseShouldHandleNullOptionalFields() {
    Product product = Product.builder()
        .productId(3L)
        .productName("Minimal Product")
        .description(null)
        .basePrice(null)
        .category(null)
        .createdAt(null)
        .build();

    ProductResponse response = mapper.toProductResponse(product);

    assertNotNull(response);
    assertEquals(3L, response.getProductId());
    assertEquals("Minimal Product", response.getProductName());
    assertNull(response.getDescription());
    assertNull(response.getBasePrice());
    assertNull(response.getCategoryId());
    assertNull(response.getCategoryName());
    assertNull(response.getCategorySlug());
    assertNull(response.getCreatedAt());
    assertTrue(response.getImageUrls().isEmpty());
    assertTrue(response.getAttributes().isEmpty());
  }

  @Test
  void toProductResponseShouldPreserveNullImageUrls() {
    ProductImage image = ProductImage.builder()
        .imageId(1L)
        .imageUrl(null)
        .build();

    Product product = Product.builder()
        .images(List.of(image))
        .build();

    ProductResponse response = mapper.toProductResponse(product);

    assertEquals(1, response.getImageUrls().size());
    assertNull(response.getImageUrls().get(0));
  }

  @Test
  void toProductResponseShouldMapNullAttributeEntry() {
    Product product = Product.builder()
        .attributes(java.util.Arrays.asList(
            ProductAttribute.builder()
                .attributeId(1L)
                .size("M")
                .build(),
            null))
        .build();

    assertThrows(
        NullPointerException.class,
        () -> mapper.toProductResponse(product));
  }
}
