package com.javacore.spring_api_luvine;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRabbit
@SpringBootApplication
public class SpringApiLuvineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringApiLuvineApplication.class, args);
	}

}