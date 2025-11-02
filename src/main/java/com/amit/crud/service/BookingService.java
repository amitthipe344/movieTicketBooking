package com.amit.crud.service;

import com.amit.crud.dto.BookingRequest;
import com.amit.crud.entity.Booking;
import com.amit.crud.entity.Seat;
import com.amit.crud.entity.Show;
import com.amit.crud.entity.User;
import com.amit.crud.repository.BookingRepository;
import com.amit.crud.repository.SeatRepository;
import com.amit.crud.repository.ShowRepository;
import com.amit.crud.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final PromoService promoService;

    public BookingService(BookingRepository bookingRepository, ShowRepository showRepository, SeatRepository seatRepository, UserRepository userRepository, PromoService promoService) {
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.promoService = promoService;
    }

    // attempt booking with retry on optimistic lock
    public Booking book(Long userId, BookingRequest req) {
        int attempts = 0;
        while (true) {
            try {
                return attemptBooking(userId, req);
            } catch (OptimisticLockException e) {
                attempts++;
                if (attempts > 3) throw new RuntimeException("Concurrent booking failure, please try again");
            }
        }
    }

    @Transactional
    protected Booking attemptBooking(Long userId, BookingRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Show show = showRepository.findById(req.getShowId()).orElseThrow(() -> new RuntimeException("Show not found"));

        // load seats
        List<String> seatNumbers = req.getSeats();
        List<Seat> seats = new ArrayList<>();
        for (String s : seatNumbers) {
            Seat seat = seatRepository.findByShowIdAndSeatNumber(req.getShowId(), s)
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + s));
            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat not available: " + s);
            }
            seats.add(seat);
        }

        // mark booked
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seatRepository.save(seat); // will trigger optimistic lock check via @Version
        }

        double total = seats.size() * show.getPrice();
        String applied = null;

        Booking b = Booking.builder()
                .user(user)
                .show(show)
                .seatNumbers(seatNumbers)
                .createdAt(LocalDateTime.now())
                .totalPrice(total)
                .promoCodeApplied(applied)
                .build();

        Booking saved = bookingRepository.save(b);

        user.setTotalSpent(user.getTotalSpent() + total);
        userRepository.save(user);

        if (req.getPromoCode() != null && !req.getPromoCode().isBlank()) {
            var p = promoService.findByCode(req.getPromoCode()).orElseThrow(() -> new RuntimeException("Invalid promo code"));
            if (!promoService.isValid(p)) throw new RuntimeException("Promo invalid/expired");


            boolean eligible = isEligible(user);
            if (!eligible) throw new RuntimeException("User not eligible for promo");

            if ("FREE_SEAT".equalsIgnoreCase(p.getType())) {
                // free seat: subtract one seat price (effectively make one of seats free)
                total = Math.max(0, total - show.getPrice());
                applied = p.getCode();
            } else if ("FLAT_250".equalsIgnoreCase(p.getType())) {
                total = Math.max(0, total - 250);
                applied = p.getCode();
            }
        }

        Booking a = Booking.builder()
                .user(user)
                .show(show)
                .seatNumbers(seatNumbers)
                .createdAt(LocalDateTime.now())
                .totalPrice(total)
                .promoCodeApplied(applied)
                .build();

        Booking savedD = bookingRepository.save(a);

        // update user spent
        user.setTotalSpent(user.getTotalSpent() + total);
        userRepository.save(user);

        return savedD;
    }

    public boolean isEligible(User user) {
        Long seatsBooked = bookingRepository.countSeatsByUserId(user.getId());
        if (seatsBooked != null && seatsBooked > 5) return true;
        if (user.getTotalSpent() > 1500) return true;
        return false;
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}
