package metric.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**")
                .addResourceLocations(
                        "classpath:/static/image/",
                        "classpath:/static/",
                        "file:image/",
                        "file:./image/",
                        "file:backend/image/",
                        "file:src/main/resources/static/image/"
                );
    }
}