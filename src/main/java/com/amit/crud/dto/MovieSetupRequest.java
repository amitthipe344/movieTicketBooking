package com.amit.crud.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MovieSetupRequest {
    private String title;
    private String description;
    private List<ShowRequest> shows;

    @Data
    public static class ShowRequest {
        private LocalDateTime startTime;
        private double price;
        private List<String> seatNumbers; // e.g. ["A1","A2","B1"]
    }
}
