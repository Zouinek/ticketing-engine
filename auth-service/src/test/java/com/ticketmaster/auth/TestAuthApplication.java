package com.ticketmaster.auth;

import org.springframework.boot.SpringApplication;

public class TestAuthApplication {

	public static void main(String[] args) {
		SpringApplication.from(AuthApplication::main).run(args);
	}

}
