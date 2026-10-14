package metric.backend.dto;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateProductRequestTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldSupportBuilderAndAllFields() {
    var attribute = CreateProductAttributeRequest.builder()
        .size("M").color("Black").stockQty(5).build();

    var request = CreateProductRequest.builder()
        .productName("Shoes")
        .description("Running shoes")
        .basePrice(new BigDecimal("99.99"))
        .categoryId(1L)
        .imageUrls(List.of("/shoes.png"))
        .attributes(List.of(attribute))
        .build();

    assertEquals("Shoes", request.getProductName());
    assertEquals("Running shoes", request.getDescription());
    assertEquals(new BigDecimal("99.99"), request.getBasePrice());
    assertEquals(1L, request.getCategoryId());
    assertEquals(List.of("/shoes.png"), request.getImageUrls());
    assertEquals(List.of(attribute), request.getAttributes());
  }

  @Test
  void shouldSupportConstructorsAndSetters() {
    var empty = new CreateProductRequest();
    assertNull(empty.getProductName());

    var request = new CreateProductRequest(
        "Shoes", "Description", BigDecimal.TEN, 1L,
        List.of("/a.png"), List.of());

    request.setProductName("Bag");
    request.setDescription("Leather bag");
    request.setBasePrice(BigDecimal.ONE);
    request.setCategoryId(2L);
    request.setImageUrls(List.of());
    request.setAttributes(List.of());

    assertEquals("Bag", request.getProductName());
    assertEquals("Leather bag", request.getDescription());
    assertEquals(BigDecimal.ONE, request.getBasePrice());
    assertEquals(2L, request.getCategoryId());
    assertTrue(request.getImageUrls().isEmpty());
    assertTrue(request.getAttributes().isEmpty());
  }

  @Test
  void shouldRejectInvalidRequiredFields() {
    var request = CreateProductRequest.builder()
        .productName(" ")
        .basePrice(BigDecimal.ZERO)
        .categoryId(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.stream().anyMatch(
        v -> v.getPropertyPath().toString().equals("productName")));
    assertTrue(violations.stream().anyMatch(
        v -> v.getPropertyPath().toString().equals("basePrice")));
    assertTrue(violations.stream().anyMatch(
        v -> v.getPropertyPath().toString().equals("categoryId")));
  }

  @Test
  void shouldRejectNullBasePrice() {
    var request = CreateProductRequest.builder()
        .productName("Shoes")
        .categoryId(1L)
        .basePrice(null)
        .build();

    assertTrue(validator.validate(request).stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("basePrice")));
  }

  @Test
  void shouldCascadeValidationToAttributes() {
    var invalidAttribute =
        new CreateProductAttributeRequest("", "", -1);

    var request = CreateProductRequest.builder()
        .productName("Shoes")
        .basePrice(BigDecimal.TEN)
        .categoryId(1L)
        .attributes(List.of(invalidAttribute))
        .build();

    assertTrue(validator.validate(request).stream()
        .anyMatch(v -> v.getPropertyPath().toString()
            .startsWith("attributes[")));
  }

  @Test
  void shouldAcceptValidRequest() {
    var request = CreateProductRequest.builder()
        .productName("Shoes")
        .basePrice(BigDecimal.ONE)
        .categoryId(1L)
        .attributes(List.of(
            new CreateProductAttributeRequest("M", "Black", 0)))
        .build();

    assertTrue(validator.validate(request).isEmpty());
  }
}
