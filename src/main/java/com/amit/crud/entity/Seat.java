package com.amit.crud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatNumber;
    @ManyToOne
    private Show show;
    @Enumerated(EnumType.STRING)
    private  SeatStatus status = SeatStatus.AVAILABLE;

    @Version
    private Long version;
    public  enum SeatStatus{
        AVAILABLE,BOOKED
    }

}
