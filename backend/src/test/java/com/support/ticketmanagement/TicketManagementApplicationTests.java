package com.support.ticketmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TicketManagementApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring context, Flyway baseline migration, and H2 test profile start.
    }
}
