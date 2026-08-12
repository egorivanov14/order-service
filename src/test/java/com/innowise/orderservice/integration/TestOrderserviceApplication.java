package com.innowise.orderservice.integration;

import com.innowise.orderservice.OrderserviceApplication;
import org.springframework.boot.SpringApplication;

public class TestOrderserviceApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderserviceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}