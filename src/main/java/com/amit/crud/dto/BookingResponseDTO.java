package com.amit.crud.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private String username;          // safe: only username, not password
    private Long showId;
    private List<String> seatNumbers;
    private int numberOfSeats;        // new field
    private double ticketPrice;       // new field
    private double totalPrice;
    private String promoCodeApplied;
    private LocalDateTime createdAt;
}