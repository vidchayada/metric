package metric.backend.service;

import metric.backend.dto.AuthResponse;
import metric.backend.dto.LoginRequest;
import metric.backend.entity.Role;
import metric.backend.entity.User;
import metric.backend.repository.UserRepository;
import metric.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtTokenProvider jwtTokenProvider;

  @InjectMocks AuthService service;

  private User user(String password) {
    return User.builder()
        .userId(1L)
        .username("alice")
        .email("alice@example.com")
        .passwordHash(password)
        .role(Role.user)
        .build();
  }

  @Test
  void login_successWithEncodedPassword() {
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(user("encoded")));
    when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
    when(jwtTokenProvider.createToken("alice", "user", 1L))
        .thenReturn("jwt");

    AuthResponse response = service.login(
        new LoginRequest("alice", "secret"));

    assertEquals("jwt", response.getToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals("alice", response.getUsername());
    assertEquals("alice@example.com", response.getEmail());
    assertEquals("user", response.getRole());
  }

  @Test
  void login_successWithPlainPasswordFallback() {
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(user("secret")));
    when(passwordEncoder.matches("secret", "secret")).thenReturn(false);
    when(jwtTokenProvider.createToken("alice", "user", 1L))
        .thenReturn("jwt");

    assertEquals("jwt",
        service.login(new LoginRequest("alice", "secret")).getToken());
  }

  @Test
  void login_userNotFound() {
    when(userRepository.findByUsername("missing"))
        .thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
        () -> service.login(new LoginRequest("missing", "secret")));
  }

  @Test
  void login_wrongPassword() {
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(user("encoded")));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThrows(IllegalArgumentException.class,
        () -> service.login(new LoginRequest("alice", "wrong")));
  }

  @Test
  void register_success() {
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.empty());
    when(passwordEncoder.encode("secret")).thenReturn("encoded");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User saved = invocation.getArgument(0);
      saved.setUserId(1L);
      return saved;
    });
    when(jwtTokenProvider.createToken("alice", "user", 1L))
        .thenReturn("jwt");

    AuthResponse response = service.register(
        "alice", "alice@example.com", "secret");

    assertEquals("jwt", response.getToken());
    assertEquals(1L, response.getUserId());
    assertEquals("alice", response.getUsername());
    assertEquals("alice@example.com", response.getEmail());
    verify(passwordEncoder).encode("secret");
  }

  @Test
  void register_duplicateUsername() {
    when(userRepository.findByUsername("alice"))
        .thenReturn(Optional.of(user("encoded")));

    assertThrows(IllegalArgumentException.class,
        () -> service.register("alice", "a@example.com", "secret"));

    verify(userRepository, never()).save(any(User.class));
  }
}
