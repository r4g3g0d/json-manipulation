package com.json.data.jsonproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.json.data.jsonproducer"})
public class JsonProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JsonProducerApplication.class, args);
	}
}
