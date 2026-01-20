package com.ticketmaster.auth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires Docker/Testcontainers to start Postgres; enable when Docker is available")
class TicketingEngineApplicationTests {

	@Test
	void contextLoads() {
	}

}
