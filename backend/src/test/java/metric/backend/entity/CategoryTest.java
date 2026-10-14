package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class CategoryTest {

  @Test
  void shouldInitializeBuilderDefaults() {
    Category category = Category.builder().build();

    assertEquals("show", category.getVisibility());
    assertNotNull(category.getProducts());
    assertTrue(category.getProducts().isEmpty());
    assertNull(category.getCategoryId());
    assertNull(category.getCategoryName());
    assertNull(category.getSlug());
    assertNull(category.getCreatedAt());
  }

  @Test
  void shouldSupportAllFieldsAndSetters() {
    Product product = Product.builder().productId(2L).build();
    OffsetDateTime now = OffsetDateTime.now();

    Category category = Category.builder()
        .categoryId(1L)
        .categoryName("Shoes")
        .slug("shoes")
        .visibility("hide")
        .createdAt(now)
        .products(new ArrayList<>(java.util.List.of(product)))
        .build();

    assertEquals(1L, category.getCategoryId());
    assertEquals("Shoes", category.getCategoryName());
    assertEquals("shoes", category.getSlug());
    assertEquals("hide", category.getVisibility());
    assertEquals(now, category.getCreatedAt());
    assertEquals(1, category.getProducts().size());

    category.setCategoryId(3L);
    category.setCategoryName("Bags");
    category.setSlug("bags");
    category.setVisibility("show");
    category.setCreatedAt(null);
    category.setProducts(new ArrayList<>());

    assertEquals(3L, category.getCategoryId());
    assertEquals("Bags", category.getCategoryName());
    assertEquals("bags", category.getSlug());
    assertEquals("show", category.getVisibility());
    assertNull(category.getCreatedAt());
    assertTrue(category.getProducts().isEmpty());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new Category().getCategoryId());

    Category category = new Category(
        1L, "Shoes", "shoes", "show", null,
        new ArrayList<>());

    assertEquals(1L, category.getCategoryId());
    assertEquals("Shoes", category.getCategoryName());
    assertEquals("shoes", category.getSlug());
    assertEquals("show", category.getVisibility());
    assertNotNull(category.getProducts());
  }
}
