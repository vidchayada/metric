package metric.backend.config;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

class WebMvcConfigTest {

  @Test
  void addResourceHandlersShouldRegisterAllImageLocations() {
    WebMvcConfig config = new WebMvcConfig();

    ResourceHandlerRegistry registry =
        mock(ResourceHandlerRegistry.class);
    ResourceHandlerRegistration registration =
        mock(ResourceHandlerRegistration.class);

    when(registry.addResourceHandler("/image/**"))
        .thenReturn(registration);
    when(registration.addResourceLocations(
        any(String[].class)
    )).thenReturn(registration);

    config.addResourceHandlers(registry);

    verify(registry).addResourceHandler("/image/**");
    verify(registration).addResourceLocations(
        "classpath:/static/image/",
        "classpath:/static/",
        "file:image/",
        "file:./image/",
        "file:backend/image/",
        "file:src/main/resources/static/image/"
    );
  }
}
