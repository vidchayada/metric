package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CreateProductAttributeRequestTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldSupportBuilderAndAccessors() {
    CreateProductAttributeRequest request =
        CreateProductAttributeRequest.builder()
            .size("M")
            .color("Black")
            .stockQty(10)
            .build();

    assertEquals("M", request.getSize());
    assertEquals("Black", request.getColor());
    assertEquals(10, request.getStockQty());

    request.setSize("L");
    request.setColor("Red");
    request.setStockQty(0);

    assertEquals("L", request.getSize());
    assertEquals("Red", request.getColor());
    assertEquals(0, request.getStockQty());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new CreateProductAttributeRequest().getSize());

    var request = new CreateProductAttributeRequest("S", "Blue", 5);
    assertEquals("S", request.getSize());
    assertEquals("Blue", request.getColor());
    assertEquals(5, request.getStockQty());
  }

  @Test
  void shouldRejectMissingFields() {
    var request = new CreateProductAttributeRequest(null, " ", null);

    assertEquals(3, validator.validate(request).size());
  }

  @Test
  void shouldRejectNegativeStock() {
    var request = new CreateProductAttributeRequest("M", "Black", -1);

    assertTrue(validator.validate(request).stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("stockQty")));
  }

  @Test
  void shouldAcceptZeroStock() {
    var request = new CreateProductAttributeRequest("M", "Black", 0);

    assertTrue(validator.validate(request).isEmpty());
  }
}
