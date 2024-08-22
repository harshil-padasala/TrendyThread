package com.trendythread.app;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
		info = @Info(
				title = "Blog microservice REST API Documentation",
				description = "TrendyThread Blog  microservice REST API Documentation",
				version = "v1",
				contact = @Contact(
						name = "Harshil Padasala",
						email = "harshilpadsala@gmail.com",
						url = "https://github.com/harshil-padasala"
				),
				license = @License(
						name = "Apache2.0",
						url = "https://github.com/harshil-padasala"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "TrendyThread Blog  microservice REST API Documentation",
				url = "https://github.com/harshil-padasala"
		)
)
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class BlogAppApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogAppApiApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}

