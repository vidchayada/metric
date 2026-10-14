package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CategoryResponseTest {

  @Test
  void shouldSupportBuilderAndAccessors() {
    CategoryResponse response = CategoryResponse.builder()
        .categoryId(1L)
        .categoryName("Shoes")
        .slug("shoes")
        .visibility("show")
        .build();

    assertEquals(1L, response.getCategoryId());
    assertEquals("Shoes", response.getCategoryName());
    assertEquals("shoes", response.getSlug());
    assertEquals("show", response.getVisibility());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    CategoryResponse empty = new CategoryResponse();
    assertNull(empty.getCategoryId());

    CategoryResponse response =
        new CategoryResponse(1L, "Shoes", "shoes", "show");

    response.setCategoryId(2L);
    response.setCategoryName("Bags");
    response.setSlug("bags");
    response.setVisibility("hide");

    assertEquals(2L, response.getCategoryId());
    assertEquals("Bags", response.getCategoryName());
    assertEquals("bags", response.getSlug());
    assertEquals("hide", response.getVisibility());
  }
}
