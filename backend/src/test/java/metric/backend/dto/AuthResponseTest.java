package metric.backend.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthResponseTest {

  @Test
  void shouldSupportBuilderAndAccessors() {
    AuthResponse response = AuthResponse.builder()
        .token("jwt-token")
        .tokenType("Bearer")
        .userId(1L)
        .username("alice")
        .email("alice@example.com")
        .role("USER")
        .build();

    assertEquals("jwt-token", response.getToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(1L, response.getUserId());
    assertEquals("alice", response.getUsername());
    assertEquals("alice@example.com", response.getEmail());
    assertEquals("USER", response.getRole());
  }

  @Test
  void shouldSupportNoArgsAndAllArgsConstructors() {
    AuthResponse empty = new AuthResponse();
    assertNull(empty.getToken());

    AuthResponse response = new AuthResponse(
        "token", "Bearer", 2L, "bob", "bob@example.com", "ADMIN");

    assertEquals(2L, response.getUserId());
    assertEquals("ADMIN", response.getRole());
  }

  @Test
  void shouldSupportSetters() {
    AuthResponse response = new AuthResponse();

    response.setToken("t");
    response.setTokenType("Bearer");
    response.setUserId(3L);
    response.setUsername("user");
    response.setEmail("user@example.com");
    response.setRole("USER");

    assertEquals("t", response.getToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(3L, response.getUserId());
    assertEquals("user", response.getUsername());
    assertEquals("user@example.com", response.getEmail());
    assertEquals("USER", response.getRole());
  }
}
