package edu.bupt.robot.config;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//增加一个配置类，实现WebMvcConfigurer,复写addCorsMappings方法，允许跨域！

//注意！当allowCredentials设置为True时，allowedOrigins不能设置成*，
// 前端还是会说：Access-Control-Allow-Origin没有设置。
//将.allowedOrigins替换成.allowedOriginPatterns即可。
@Configuration
public class CrossConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080")
                //.allowedOriginPatterns("*")
                .allowedMethods("GET","HEAD","POST","PUT","DELETE","OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}