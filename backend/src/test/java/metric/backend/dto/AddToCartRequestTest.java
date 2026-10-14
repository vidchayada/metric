package metric.backend.dto;


import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AddToCartRequestTest {

  static Validator validator;

  @BeforeAll
  static void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @AfterAll
  static void tearDown() {
    validator.getClass(); // Validator ปิดผ่าน factory ได้ในโปรเจกต์จริง
  }

  @Test
  void shouldSupportBuilderAndAccessors() {
    AddToCartRequest request = AddToCartRequest.builder()
        .productId(1L)
        .attributeId(2L)
        .quantity(3)
        .build();

    assertEquals(1L, request.getProductId());
    assertEquals(2L, request.getAttributeId());
    assertEquals(3, request.getQuantity());

    request.setProductId(10L);
    request.setAttributeId(20L);
    request.setQuantity(5);

    assertEquals(10L, request.getProductId());
    assertEquals(20L, request.getAttributeId());
    assertEquals(5, request.getQuantity());
  }

  @Test
  void shouldSupportConstructors() {
    AddToCartRequest empty = new AddToCartRequest();
    assertNull(empty.getProductId());

    AddToCartRequest request = new AddToCartRequest(1L, 2L, 3);
    assertEquals(1L, request.getProductId());
    assertEquals(2L, request.getAttributeId());
    assertEquals(3, request.getQuantity());
  }

  @Test
  void shouldRejectMissingRequiredFields() {
    AddToCartRequest request = new AddToCartRequest(null, null, null);

    Set<String> fields = validator.validate(request).stream()
        .map(v -> v.getPropertyPath().toString())
        .collect(Collectors.toSet());

    assertTrue(fields.contains("productId"));
    assertTrue(fields.contains("attributeId"));
    assertTrue(fields.contains("quantity"));
  }

  @Test
  void shouldRejectQuantityBelowOne() {
    AddToCartRequest request = new AddToCartRequest(1L, 2L, 0);

    assertTrue(validator.validate(request).stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
  }

  @Test
  void shouldAcceptValidRequest() {
    AddToCartRequest request = new AddToCartRequest(1L, 2L, 1);

    assertTrue(validator.validate(request).isEmpty());
  }
}
