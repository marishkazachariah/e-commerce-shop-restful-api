package com.startstepszalando.ecommerceshop;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "E-Commerce Shop APIs", version = "1.0", description = "API documentation for final project"))
public class ECommerceShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceShopApplication.class, args);
	}

}
