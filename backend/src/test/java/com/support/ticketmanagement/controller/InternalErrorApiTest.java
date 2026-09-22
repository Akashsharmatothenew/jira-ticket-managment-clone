package com.support.ticketmanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.support.ticketmanagement.dto.ErrorCodes;
import com.support.ticketmanagement.service.TicketService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * DEC-005 / TEST-041: unexpected exception maps to HTTP 500 INTERNAL_ERROR.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternalErrorApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void unexpectedExceptionReturns500InternalError() throws Exception {
        when(ticketService.getById(any(UUID.class)))
                .thenThrow(new RuntimeException("simulated failure"));

        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCodes.INTERNAL_ERROR))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/tickets/" + id))
                .andExpect(jsonPath("$.details").isArray());
    }
}
