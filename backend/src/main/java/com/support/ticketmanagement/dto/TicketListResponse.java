package com.support.ticketmanagement.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for API-002 list responses: {@code { "items": [ ... ] }} (API-DD-012).
 */
public class TicketListResponse {

    private List<TicketSummaryDto> items = new ArrayList<>();

    public TicketListResponse() {
    }

    public TicketListResponse(List<TicketSummaryDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public List<TicketSummaryDto> getItems() {
        return items;
    }

    public void setItems(List<TicketSummaryDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
