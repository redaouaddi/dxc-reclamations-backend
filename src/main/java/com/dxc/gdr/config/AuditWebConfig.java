package com.dxc.gdr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuditWebConfig implements WebMvcConfigurer {

    private final ApiActionAuditInterceptor apiActionAuditInterceptor;

    public AuditWebConfig(ApiActionAuditInterceptor apiActionAuditInterceptor) {
        this.apiActionAuditInterceptor = apiActionAuditInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiActionAuditInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/admin/audit-logs",
                        "/api/admin/audit-logs/**"
                );
    }
}
