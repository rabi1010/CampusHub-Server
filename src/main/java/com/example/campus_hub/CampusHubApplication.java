package com.example.campus_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CampusHubApplication {
	public static void main(String[] args) {
		SpringApplication.run(CampusHubApplication.class, args);
	}
}