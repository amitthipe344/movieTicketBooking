package com.amit.crud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeRequest {
    private String code;        // e.g. "FREE1"
    private String type;        // e.g. "FREE_SEAT" or "FLAT_250"
    private boolean active;     // true/false
    private LocalDate expiryDate;

}
