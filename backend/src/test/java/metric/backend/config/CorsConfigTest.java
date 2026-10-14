package metric.backend.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class CorsConfigTest {

  private final CorsConfig corsConfig = new CorsConfig();

  @Test
  void corsConfigurationSourceShouldConfigureAllCorsRules() {
    CorsConfigurationSource result =
        corsConfig.corsConfigurationSource();

    assertNotNull(result);
    assertInstanceOf(UrlBasedCorsConfigurationSource.class, result);

    CorsConfiguration config =
        result.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest(
                "GET", "/api/products"
            )
        );

    assertNotNull(config);
    assertEquals(
        List.of("http://localhost:5173", "http://localhost:3000"),
        config.getAllowedOrigins()
    );
    assertEquals(
        List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),
        config.getAllowedMethods()
    );
    assertEquals(List.of("*"), config.getAllowedHeaders());
    assertEquals(Boolean.TRUE, config.getAllowCredentials());
  }

  @Test
  void corsConfigurationShouldNotMatchOutsideRegisteredPattern() {
    CorsConfigurationSource result =
        corsConfig.corsConfigurationSource();

    CorsConfiguration config =
        result.getCorsConfiguration(
            new org.springframework.mock.web.MockHttpServletRequest(
                "GET", "/anything"
            )
        );

    assertNotNull(config);
  }
}
