package com.amit.crud.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingResponse {
    private Long bookingId;
    private double totalPrice;
    private List<String> seats;
    private String promoApplied;
}
