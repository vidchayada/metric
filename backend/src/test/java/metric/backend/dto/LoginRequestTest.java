package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void shouldSupportBuilderAndAccessors() {
    var request = LoginRequest.builder()
        .username("alice")
        .password("secret")
        .build();

    assertEquals("alice", request.getUsername());
    assertEquals("secret", request.getPassword());

    request.setUsername("bob");
    request.setPassword("updated");

    assertEquals("bob", request.getUsername());
    assertEquals("updated", request.getPassword());
  }

  @Test
  void shouldSupportConstructors() {
    assertNull(new LoginRequest().getUsername());

    var request = new LoginRequest("alice", "secret");
    assertEquals("alice", request.getUsername());
    assertEquals("secret", request.getPassword());
  }

  @Test
  void shouldRejectNullAndBlankCredentials() {
    var request = new LoginRequest(" ", null);

    assertEquals(2, validator.validate(request).size());
  }

  @Test
  void shouldAcceptValidCredentials() {
    assertTrue(validator.validate(
        new LoginRequest("alice", "secret")).isEmpty());
  }
}
