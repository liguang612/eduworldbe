package com.example.eduworldbe.config;

// import org.springframework.beans.factory.annotation.Value; // Không cần nữa
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // Không cần nữa
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import org.springframework.web.servlet.config.annotation.CorsRegistry; // Có thể không cần nữa
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  // private String uploadDir; // Không cần nữa

  // @Override // Không cần nữa
  // public void addResourceHandlers(ResourceHandlerRegistry registry) {
  // registry.addResourceHandler("/uploads/**")
  // .addResourceLocations("file:" + uploadDir + "/");
  // }

  // @Override // Xem xét xóa nếu SecurityConfig đã đủ
  // public void addCorsMappings(CorsRegistry registry) {
  // registry.addMapping("/**")
  // .allowedOrigins("*")
  // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
  // .allowedHeaders("*");
  // }

  @Bean
  public StandardServletMultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }
}