package com.freshtrack.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FreshtrackApiApplication {

	public static void main(String[] args) {
        System.out.println(System.getenv("SPRING_DATASOURCE_URL"));
		SpringApplication.run(FreshtrackApiApplication.class, args);
	}
}
