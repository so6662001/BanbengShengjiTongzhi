package com.company.notify.client.config;

import com.company.notify.client.security.ClientAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 注册客户端鉴权/限流拦截器，放行文档。 */
@Configuration
@RequiredArgsConstructor
public class ClientWebConfig implements WebMvcConfigurer {

    private final ClientAuthInterceptor clientAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientAuthInterceptor)
                .addPathPatterns("/client/**");
    }
}
