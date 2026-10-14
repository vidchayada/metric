package metric.backend.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import metric.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

class SecurityConfigTest {

  @Test
  void passwordEncoderShouldBeBCrypt() {
    SecurityConfig config =
        new SecurityConfig(mock(JwtAuthenticationFilter.class));

    PasswordEncoder encoder = config.passwordEncoder();

    assertNotNull(encoder);
    assertInstanceOf(BCryptPasswordEncoder.class, encoder);

    String encoded = encoder.encode("test-password");

    assertNotEquals("test-password", encoded);
    assertTrue(encoder.matches("test-password", encoded));
    assertFalse(encoder.matches("wrong-password", encoded));
  }

  @Test
  void securityFilterChainShouldBuildSuccessfully() throws Exception {
    JwtAuthenticationFilter jwtFilter =
        mock(JwtAuthenticationFilter.class);

    SecurityConfig config = new SecurityConfig(jwtFilter);

    HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);

    SecurityFilterChain chain = mock(SecurityFilterChain.class);
    doReturn(chain).when(http).build();

    SecurityFilterChain result = config.securityFilterChain(http);

    assertNotNull(result);
    assertSame(chain, result);
    verify(http).build();
  }
}
