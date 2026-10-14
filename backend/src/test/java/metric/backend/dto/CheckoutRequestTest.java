package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CheckoutRequestTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldBuildAndAccessShippingAddress() {
    CheckoutRequest request = CheckoutRequest.builder()
        .shippingAddress("Chiang Mai, Thailand")
        .build();

    assertEquals("Chiang Mai, Thailand", request.getShippingAddress());

    request.setShippingAddress("Bangkok");
    assertEquals("Bangkok", request.getShippingAddress());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new CheckoutRequest().getShippingAddress());
    assertEquals("Address",
        new CheckoutRequest("Address").getShippingAddress());
  }

  @Test
  void shouldRejectNullAndBlankShippingAddress() {
    assertFalse(validator.validate(new CheckoutRequest(null)).isEmpty());
    assertFalse(validator.validate(new CheckoutRequest(" ")).isEmpty());
  }

  @Test
  void shouldAcceptValidShippingAddress() {
    assertTrue(validator.validate(
        new CheckoutRequest("123 Main Street")).isEmpty());
  }
}