package com.amit.crud.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShowDto {
    private Long movieId;
    private LocalDateTime startTime;
    private double price;
}
