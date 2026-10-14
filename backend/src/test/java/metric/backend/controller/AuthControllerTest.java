package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.Optional;
import metric.backend.dto.AuthResponse;
import metric.backend.dto.LoginRequest;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;
import metric.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock AuthService authService;
  @Mock UserRepository userRepository;

  @InjectMocks AuthController controller;

  @Test
  void loginShouldReturnAuthResponse() {
    LoginRequest request = mock(LoginRequest.class);
    AuthResponse expected = mock(AuthResponse.class);
    when(authService.login(request)).thenReturn(expected);

    var response = controller.login(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertSame(expected, response.getBody());
  }

  @Test
  void registerShouldRejectMissingFields() {
    var response = controller.register(java.util.Map.of(
        "username", " ",
        "email", "user@example.com",
        "password", "secret"
    ));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("All fields are required.",
        response.getBody().get("message"));
    verifyNoInteractions(authService);
  }

  @Test
  void registerShouldRejectMissingUsername() {
    var response = controller.register(java.util.Map.of(
        "email", "user@example.com",
        "password", "secret"
    ));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void registerShouldReturnTokenOnSuccess() {
    AuthResponse auth = mock(AuthResponse.class);
    when(authService.register("alice", "alice@example.com", "secret"))
        .thenReturn(auth);
    when(auth.getToken()).thenReturn("jwt-token");
    when(auth.getUsername()).thenReturn("alice");

    var response = controller.register(java.util.Map.of(
        "username", " alice ",
        "email", " alice@example.com ",
        "password", "secret"
    ));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("jwt-token", response.getBody().get("token"));
    assertEquals("alice", response.getBody().get("username"));
    verify(authService).register(
        "alice", "alice@example.com", "secret");
  }

  @Test
  void registerShouldReturnBadRequestWhenServiceThrows() {
    when(authService.register("alice", "a@example.com", "secret"))
        .thenThrow(new IllegalArgumentException("Email already exists"));

    var response = controller.register(java.util.Map.of(
        "username", "alice",
        "email", "a@example.com",
        "password", "secret"
    ));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Email already exists", response.getBody().get("message"));
  }

  @Test
  void getCurrentUserShouldReturnUnauthorizedWithoutUsername() {
    var response = controller.getCurrentUser(null, null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Unauthorized", response.getBody().get("error"));
  }

  @Test
  void getCurrentUserShouldUseUserPrincipal() {
    User principal = mock(User.class);
    when(principal.getUsername()).thenReturn("alice");

    User found = mock(User.class);
    when(found.getUserId()).thenReturn(1L);
    when(found.getUsername()).thenReturn("alice");
    when(found.getEmail()).thenReturn("alice@example.com");
    when(found.getRole()).thenReturn("USER");
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(found));

    var response = controller.getCurrentUser(principal, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1L, response.getBody().get("id"));
    assertEquals(1L, response.getBody().get("userId"));
    assertEquals("alice", response.getBody().get("username"));
    assertEquals("alice@example.com", response.getBody().get("email"));
    assertEquals("USER", response.getBody().get("role"));
  }

  @Test
  void getCurrentUserShouldUsePrincipalWhenAuthenticationPrincipalIsNotUser() {
    Principal principal = () -> "bob";
    User found = mock(User.class);
    when(found.getUserId()).thenReturn(2L);
    when(found.getUsername()).thenReturn("bob");
    when(found.getEmail()).thenReturn("bob@example.com");
    when(found.getRole()).thenReturn("USER");
    when(userRepository.findByUsername("bob"))
        .thenReturn(Optional.of(found));

    var response = controller.getCurrentUser("bob", principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("bob", response.getBody().get("username"));
  }

  @Test
  void getCurrentUserShouldReturnUnauthorizedWhenUserNotFound() {
    Principal principal = () -> "unknown";
    when(userRepository.findByUsername("unknown"))
        .thenReturn(Optional.empty());

    var response = controller.getCurrentUser(null, principal);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("User not found", response.getBody().get("error"));
  }
}
