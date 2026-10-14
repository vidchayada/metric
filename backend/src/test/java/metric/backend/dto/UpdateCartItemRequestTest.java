package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class UpdateCartItemRequestTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldSupportConstructorsAndSetters() {
    assertNull(new UpdateCartItemRequest().getQuantity());

    var request = new UpdateCartItemRequest(2);
    assertEquals(2, request.getQuantity());

    request.setQuantity(5);
    assertEquals(5, request.getQuantity());
  }

  @Test
  void shouldRejectNullQuantity() {
    var request = new UpdateCartItemRequest(null);

    assertTrue(validator.validate(request).stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
  }

  @Test
  void shouldRejectZeroAndNegativeQuantity() {
    assertFalse(validator.validate(
        new UpdateCartItemRequest(0)).isEmpty());

    assertFalse(validator.validate(
        new UpdateCartItemRequest(-1)).isEmpty());
  }

  @Test
  void shouldAcceptPositiveQuantity() {
    assertTrue(validator.validate(
        new UpdateCartItemRequest(1)).isEmpty());
  }
}