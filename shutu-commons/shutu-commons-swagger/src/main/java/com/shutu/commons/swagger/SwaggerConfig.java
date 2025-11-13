package com.shutu.commons.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Swagger配置
 *
 * @author jiujingz@126.com
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"com.shutu"};
        return GroupedOpenApi.builder().group("default")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("人人开源");

        return new OpenAPI().info(new Info()
                .title("人人开源")
                .description("接口文档")
                .contact(contact)
                .version("4.0")
                .termsOfService("https://www.renren.io/community"));
    }
}
