package org.ever._4ever_be_scm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("4Ever SCM/PP API")
                        .description("SCM 및 PP 도메인 API 문서")
                        .version("v1"));
    }
}
