package com.support.ticketmanagement.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticketmanagement.dto.ErrorCodes;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * MockMvc + H2 API integration tests (DEC-004 / DEC-007).
 * Full 25-cell state-machine matrix remains in domain unit tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("API-001 Create")
    class Create {

        @Test
        void createsTicketSuccessfully() throws Exception {
            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Cannot reset password",
                                      "description": "Reset email never arrives",
                                      "priority": "HIGH",
                                      "assignee": "alex"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.title").value("Cannot reset password"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.priority").value("HIGH"))
                    .andExpect(jsonPath("$.assignee").value("alex"))
                    .andExpect(jsonPath("$.comments").isArray())
                    .andExpect(jsonPath("$.comments", empty()));
        }

        @Test
        void validationFailureReturns400ValidationError() throws Exception {
            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "title": "   " }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.path").value("/api/tickets"))
                    .andExpect(jsonPath("$.details", not(empty())))
                    .andExpect(jsonPath("$.details[0].field").value("title"));
        }

        @Test
        void malformedJsonReturns400MalformedRequest() throws Exception {
            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ title: missing quotes }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.MALFORMED_REQUEST))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.path").value("/api/tickets"));
        }

        @Test
        void invalidEnumValueReturns400() throws Exception {
            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Bad priority",
                                      "priority": "URGENT"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                    .andExpect(jsonPath("$.details[0].field").value("priority"))
                    .andExpect(jsonPath("$.path").value("/api/tickets"));
        }

        @Test
        void clientSuppliedStatusOnCreateIsRejected() throws Exception {
            mockMvc.perform(post("/api/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "With status",
                                      "status": "CLOSED"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
        }
    }

    @Nested
    @DisplayName("API-002 List / search / filter")
    class ListTickets {

        @Test
        @DisplayName("null keyword + no status → list all (PostgreSQL null-keyword safe)")
        void listsTickets() throws Exception {
            createTicket("List me", "body", "MEDIUM", null);

            mockMvc.perform(get("/api/tickets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", not(empty())))
                    .andExpect(jsonPath("$.items[0].id").isNotEmpty())
                    .andExpect(jsonPath("$.items[0].title").isNotEmpty())
                    .andExpect(jsonPath("$.items[0].status").isNotEmpty());
        }

        @Test
        @DisplayName("keyword only → title/description search")
        void keywordSearch() throws Exception {
            createTicket("Password reset broken", "detail", "LOW", null);
            createTicket("Unrelated billing", "other", "LOW", null);

            mockMvc.perform(get("/api/tickets").param("keyword", "password"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].title", containsString("Password")));
        }

        @Test
        @DisplayName("null keyword + status → status filter only (PostgreSQL null-keyword safe)")
        void statusFilter() throws Exception {
            String id = createTicket("Open ticket", null, "MEDIUM", null);
            changeStatus(id, "IN_PROGRESS");

            mockMvc.perform(get("/api/tickets").param("status", "OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[*].status").value(everyItem(is("OPEN"))));
        }

        @Test
        @DisplayName("keyword + status → AND semantics")
        void combinedKeywordAndStatus() throws Exception {
            createTicket("VPN password issue", "open vpn", "HIGH", null);
            String inProgressId = createTicket("Password in progress", "vpn", "HIGH", null);
            changeStatus(inProgressId, "IN_PROGRESS");
            createTicket("Billing", "no match", "LOW", null);

            mockMvc.perform(get("/api/tickets")
                            .param("keyword", "password")
                            .param("status", "OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].title").value("VPN password issue"))
                    .andExpect(jsonPath("$.items[0].status").value("OPEN"));
        }

        @Test
        void invalidStatusQueryReturns400() throws Exception {
            mockMvc.perform(get("/api/tickets").param("status", "DONE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                    .andExpect(jsonPath("$.details[0].field").value("status"));
        }
    }

    @Nested
    @DisplayName("API-003 Get by id")
    class GetById {

        @Test
        void returnsTicketDetailWithComments() throws Exception {
            String id = createTicket("Detail subject", "desc", "MEDIUM", "sam");
            mockMvc.perform(post("/api/tickets/{id}/comments", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "body": "First note" }
                                    """))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/tickets/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.title").value("Detail subject"))
                    .andExpect(jsonPath("$.comments", hasSize(1)))
                    .andExpect(jsonPath("$.comments[0].body").value("First note"));
        }

        @Test
        void missingTicketReturns404() throws Exception {
            UUID missing = UUID.randomUUID();
            mockMvc.perform(get("/api/tickets/{id}", missing))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.TICKET_NOT_FOUND))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").isNotEmpty())
                    .andExpect(jsonPath("$.path").value("/api/tickets/" + missing));
        }

        @Test
        void invalidUuidPathReturns400() throws Exception {
            mockMvc.perform(get("/api/tickets/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
        }
    }

    @Nested
    @DisplayName("API-004 Update")
    class Update {

        @Test
        void successfulPartialUpdate() throws Exception {
            String id = createTicket("Old title", "old desc", "LOW", null);

            mockMvc.perform(patch("/api/tickets/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "New title",
                                      "assignee": "jordan"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("New title"))
                    .andExpect(jsonPath("$.assignee").value("jordan"))
                    .andExpect(jsonPath("$.description").value("old desc"))
                    .andExpect(jsonPath("$.priority").value("LOW"))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        void validationFailureOnBlankTitle() throws Exception {
            String id = createTicket("Keep", null, "MEDIUM", null);

            mockMvc.perform(patch("/api/tickets/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "title": "  " }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
        }

        @Test
        void statusCannotBeChangedThroughPatch() throws Exception {
            String id = createTicket("Guard", null, "MEDIUM", null);

            mockMvc.perform(patch("/api/tickets/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Still open",
                                      "status": "CLOSED"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));

            mockMvc.perform(get("/api/tickets/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.title").value("Guard"));
        }
    }

    @Nested
    @DisplayName("API-005 Comments")
    class Comments {

        @Test
        void createsCommentSuccessfully() throws Exception {
            String id = createTicket("Comment parent", null, "MEDIUM", null);

            mockMvc.perform(post("/api/tickets/{id}/comments", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "body": "Asked user to retry" }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.ticketId").value(id))
                    .andExpect(jsonPath("$.body").value("Asked user to retry"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        void missingTicketReturns404() throws Exception {
            UUID missing = UUID.randomUUID();
            mockMvc.perform(post("/api/tickets/{id}/comments", missing)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "body": "orphan" }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.TICKET_NOT_FOUND));
        }

        @Test
        void validationFailureReturns400() throws Exception {
            String id = createTicket("Comment validation", null, "MEDIUM", null);

            mockMvc.perform(post("/api/tickets/{id}/comments", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "body": "" }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                    .andExpect(jsonPath("$.details[0].field").value("body"));
        }
    }

    @Nested
    @DisplayName("API-006 Status transitions")
    class StatusTransitions {

        @Test
        void openToInProgress() throws Exception {
            assertValidTransition("OPEN", "IN_PROGRESS");
        }

        @Test
        void openToCancelled() throws Exception {
            assertValidTransition("OPEN", "CANCELLED");
        }

        @Test
        void inProgressToResolved() throws Exception {
            String id = createTicket("To resolved", null, "MEDIUM", null);
            changeStatus(id, "IN_PROGRESS");
            changeStatusExpectOk(id, "RESOLVED");
        }

        @Test
        void inProgressToCancelled() throws Exception {
            String id = createTicket("To cancelled", null, "MEDIUM", null);
            changeStatus(id, "IN_PROGRESS");
            changeStatusExpectOk(id, "CANCELLED");
        }

        @Test
        void resolvedToClosed() throws Exception {
            String id = createTicket("To closed", null, "MEDIUM", null);
            changeStatus(id, "IN_PROGRESS");
            changeStatus(id, "RESOLVED");
            changeStatusExpectOk(id, "CLOSED");
        }

        @Test
        void invalidTransitionReturns409() throws Exception {
            String id = createTicket("Invalid jump", null, "MEDIUM", null);

            mockMvc.perform(post("/api/tickets/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "CLOSED" }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.INVALID_TRANSITION))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.details", hasSize(2)))
                    .andExpect(jsonPath("$.details[0].field").value("currentStatus"))
                    .andExpect(jsonPath("$.details[0].message").value("OPEN"))
                    .andExpect(jsonPath("$.details[1].field").value("targetStatus"))
                    .andExpect(jsonPath("$.details[1].message").value("CLOSED"))
                    .andExpect(jsonPath("$.path").value("/api/tickets/" + id + "/status"));

            mockMvc.perform(get("/api/tickets/{id}", id))
                    .andExpect(jsonPath("$.status").value("OPEN"));
        }

        @Test
        void selfTransitionReturns409() throws Exception {
            String id = createTicket("Self", null, "MEDIUM", null);

            mockMvc.perform(post("/api/tickets/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "OPEN" }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.INVALID_TRANSITION));
        }

        @Test
        void backwardTransitionReturns409() throws Exception {
            String id = createTicket("Backward", null, "MEDIUM", null);
            changeStatus(id, "IN_PROGRESS");

            mockMvc.perform(post("/api/tickets/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "OPEN" }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.INVALID_TRANSITION));
        }

        @Test
        void fromCancelledReturns409() throws Exception {
            String id = createTicket("Cancelled terminal", null, "MEDIUM", null);
            changeStatus(id, "CANCELLED");

            mockMvc.perform(post("/api/tickets/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "OPEN" }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.INVALID_TRANSITION));
        }

        @Test
        void missingTicketReturns404() throws Exception {
            UUID missing = UUID.randomUUID();
            mockMvc.perform(post("/api/tickets/{id}/status", missing)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "IN_PROGRESS" }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.TICKET_NOT_FOUND));
        }

        @Test
        void invalidStatusEnumReturns400() throws Exception {
            String id = createTicket("Bad enum", null, "MEDIUM", null);

            mockMvc.perform(post("/api/tickets/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "status": "DONE" }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
        }
    }

    private void assertValidTransition(String from, String to) throws Exception {
        assertThat(from).isEqualTo("OPEN");
        String id = createTicket("Edge " + to, null, "MEDIUM", null);
        changeStatusExpectOk(id, to);
    }

    private void changeStatusExpectOk(String id, String status) throws Exception {
        mockMvc.perform(post("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.id").value(id));
    }

    private void changeStatus(String id, String status) throws Exception {
        changeStatusExpectOk(id, status);
    }

    private String createTicket(String title, String description, String priority, String assignee)
            throws Exception {
        StringBuilder json = new StringBuilder("{");
        json.append("\"title\":").append(objectMapper.writeValueAsString(title));
        if (description != null) {
            json.append(",\"description\":").append(objectMapper.writeValueAsString(description));
        }
        if (priority != null) {
            json.append(",\"priority\":").append(objectMapper.writeValueAsString(priority));
        }
        if (assignee != null) {
            json.append(",\"assignee\":").append(objectMapper.writeValueAsString(assignee));
        }
        json.append("}");

        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(node.get("id").asText()).isNotBlank();
        assertThat(node.get("status").asText()).isEqualTo("OPEN");
        return node.get("id").asText();
    }
}
