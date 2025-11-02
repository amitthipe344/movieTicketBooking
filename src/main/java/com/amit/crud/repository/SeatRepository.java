package com.amit.crud.repository;

import com.amit.crud.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByShowId(Long id);
    Optional<Seat> findByShowIdAndSeatNumber(Long showId,String seatNumber);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.show.id = :showId and s.seatNumber in :numbers")
    List<Seat> lockSeats(Long showId,List<String> numbers);
}
