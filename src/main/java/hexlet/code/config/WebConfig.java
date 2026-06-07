package hexlet.code.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] DEFAULT_ALLOWED_ORIGINS = {
        "http://localhost:5173",
        "http://localhost:3000",
        "http://localhost:8080"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(DEFAULT_ALLOWED_ORIGINS)
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders(TotalCountResponseAdvice.TOTAL_COUNT_HEADER);
    }
}
