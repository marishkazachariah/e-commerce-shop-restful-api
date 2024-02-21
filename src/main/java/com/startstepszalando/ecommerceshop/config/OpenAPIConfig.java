package com.startstepszalando.ecommerceshop.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Principal;
import java.util.List;

@Configuration
@OpenAPIDefinition
public class OpenAPIConfig {
    @Value("${dev.url}")
    private String devUrl;

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL for DEV - API E-Commerce shop");

        Contact contact = new Contact()
                .email("marishka.zachariah@gmail.com")
                .name("Marishka Zachariah");

        Info info = new Info()
                .title("Startsteps x Zalando E-Commerce API")
                .version("1.0.0")
                .contact(contact)
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                .description("""
                        Startsteps x Zalando E-Commerce is a RESTful API designed to simulate basic endpoints of an online shopping experience, from product listings to order fulfillment. Product and User endpoints tested through unit tests using J-Unit.
                                                     
                        This application caters to two user roles: Admins ("ADMINS") and Customers ("CUSTOMERS"), each with tailored permissions and functionalities. Endpoints are secured with JWT authentication and user role privileges.
                                                                             """);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearer-key", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .info(info)
                .servers(List.of(devServer));
    }

    static {
        SpringDocUtils.getConfig()
                .removeRequestWrapperToIgnore(Principal.class, HttpServletRequest.class);
    }
}
