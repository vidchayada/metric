package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import metric.backend.entity.OrderStatus;
import org.junit.jupiter.api.Test;

class UpdateOrderStatusRequestTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldSupportBuilderAndAccessors() {
    OrderStatus status = OrderStatus.values()[0];

    var request = UpdateOrderStatusRequest.builder()
        .status(status)
        .build();

    assertEquals(status, request.getStatus());

    OrderStatus anotherStatus = OrderStatus.values()[
        OrderStatus.values().length - 1];

    request.setStatus(anotherStatus);
    assertEquals(anotherStatus, request.getStatus());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new UpdateOrderStatusRequest().getStatus());

    OrderStatus status = OrderStatus.values()[0];
    assertEquals(status, new UpdateOrderStatusRequest(status).getStatus());
  }

  @Test
  void shouldRejectNullStatus() {
    var request = new UpdateOrderStatusRequest(null);

    assertTrue(validator.validate(request).stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("status")));
  }

  @Test
  void shouldAcceptNonNullStatus() {
    var request = new UpdateOrderStatusRequest(OrderStatus.values()[0]);

    assertTrue(validator.validate(request).isEmpty());
  }
}