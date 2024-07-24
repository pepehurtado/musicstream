package com.musicapp.musicstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@SpringBootApplication(exclude= {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class MusicstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusicstreamApplication.class, args);
	}

	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                .title("Music App API")
                .version("1.0")
                .description("API documentation for Music App"));
    }

}
