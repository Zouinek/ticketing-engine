package com.ticketmaster.ticketing_engine;

import org.springframework.boot.SpringApplication;

public class TestTicketingEngineApplication {

	public static void main(String[] args) {
		SpringApplication.from(TicketingEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
