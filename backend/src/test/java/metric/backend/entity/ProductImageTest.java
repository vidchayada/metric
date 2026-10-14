package metric.backend.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductImageTest {

  @Test
  void shouldInitializeBuilderDefaults() {
    ProductImage image = ProductImage.builder().build();

    assertEquals(Boolean.FALSE, image.getIsPrimary());
    assertEquals(0, image.getSortOrder());
    assertNull(image.getImageId());
    assertNull(image.getProduct());
    assertNull(image.getImageUrl());
  }

  @Test
  void shouldSupportAllFieldsAndSetters() {
    Product product = Product.builder().productId(1L).build();

    ProductImage image = ProductImage.builder()
        .imageId(2L)
        .product(product)
        .imageUrl("/images/shoes.png")
        .isPrimary(true)
        .sortOrder(3)
        .build();

    assertEquals(2L, image.getImageId());
    assertSame(product, image.getProduct());
    assertEquals("/images/shoes.png", image.getImageUrl());
    assertTrue(image.getIsPrimary());
    assertEquals(3, image.getSortOrder());

    image.setImageId(4L);
    image.setProduct(null);
    image.setImageUrl("/images/bag.png");
    image.setIsPrimary(false);
    image.setSortOrder(0);

    assertEquals(4L, image.getImageId());
    assertNull(image.getProduct());
    assertEquals("/images/bag.png", image.getImageUrl());
    assertFalse(image.getIsPrimary());
    assertEquals(0, image.getSortOrder());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new ProductImage().getImageId());

    ProductImage image =
        new ProductImage(1L, null, "/image.png", true, 2);

    assertEquals(1L, image.getImageId());
    assertNull(image.getProduct());
    assertEquals("/image.png", image.getImageUrl());
    assertTrue(image.getIsPrimary());
    assertEquals(2, image.getSortOrder());
  }
}
