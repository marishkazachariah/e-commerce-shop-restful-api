package com.startstepszalando.ecommerceshop.config;

import com.startstepszalando.ecommerceshop.product.controller.ProductController;
import com.startstepszalando.ecommerceshop.user.controller.UserController;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
@OpenAPIDefinition
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI(){
        Server devServer = new Server();
        String devUrl = "http://localhost:8080/";
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL for DEV - API E-Commerce shop");

        Contact contact = new Contact();
        contact.setEmail("marishka.zachariah@gmail.com");
        contact.setName("Marishka Zachariah");

        Info info = new Info()
                .title("E-Commerce API")
                .version("1.0")
                .contact(contact)
                .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                .description("This API exposes endpoints to an e-commerce functionality.");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearer-key", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .info(info)
                .servers(List.of(devServer));
    }

    static {
        SpringDocUtils.getConfig().addRestControllers(
                UserController.class, ProductController.class);
    }
}
