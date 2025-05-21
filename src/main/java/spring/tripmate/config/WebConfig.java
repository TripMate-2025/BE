package spring.tripmate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // 상대 경로
      
        registry.addResourceHandler("/images/profile/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/profile-images/");
    }
}
