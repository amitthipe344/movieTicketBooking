package com.amit.crud.repository;

import com.amit.crud.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    Long countByUserId(Long userId);

    @Query("SELECT SUM(size(b.seatNumbers)) FROM Booking b WHERE b.user.id = :userId")
    Long countSeatsByUserId(@Param("userId") Long userId);
}
