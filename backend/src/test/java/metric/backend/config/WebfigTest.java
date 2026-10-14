package metric.backend.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

class WebfigTest {

  @Test
  void addResourceHandlersShouldRegisterProductImageLocations() {
    Webfig config = new Webfig();
    ResourceHandlerRegistry registry =
        mock(ResourceHandlerRegistry.class);
    ResourceHandlerRegistration registration =
        mock(ResourceHandlerRegistration.class);

    when(registry.addResourceHandler("/image/products/**"))
        .thenReturn(registration);
    when(registration.addResourceLocations(
        any(String[].class)
    )).thenReturn(registration);

    config.addResourceHandlers(registry);

    verify(registry).addResourceHandler("/image/products/**");

    String expectedUploadPath =
        Paths.get("uploads/products")
            .toFile()
            .getAbsolutePath();

    verify(registration).addResourceLocations(
        "file:" + expectedUploadPath + "/",
        "classpath:/static/image/products/"
    );
  }
}
