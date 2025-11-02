package com.amit.crud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Show show;
    @ElementCollection
    private List<String> seatNumbers;
    private double totalPrice;
    private LocalDateTime createdAt;
    private String promoCodeApplied;
}
