package com.amit.crud.service;

import com.amit.crud.dto.BookingRequest;
import com.amit.crud.entity.Booking;
import com.amit.crud.entity.Seat;
import com.amit.crud.entity.Show;
import com.amit.crud.entity.User;
import com.amit.crud.exception.BadRequestException;
import com.amit.crud.exception.NotFoundException;
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
                if (attempts > 3)
                    throw new BadRequestException("Concurrent booking failure, please try again");
            }
        }
    }

    @Transactional
    protected Booking attemptBooking(Long userId, BookingRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Show show = showRepository.findById(req.getShowId()).orElseThrow(() -> new NotFoundException("Show not found with id: " + req.getShowId()));

        // load seats
        List<String> seatNumbers = req.getSeats();
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new BadRequestException("At least one seat must be selected");
        }
        List<Seat> seats = new ArrayList<>();
        for (String s : seatNumbers) {
            Seat seat = seatRepository.findByShowIdAndSeatNumber(req.getShowId(), s)
                    .orElseThrow(() -> new NotFoundException("Seat not found: " + s));
            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new BadRequestException("Seat not available: " + s);
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
            var p = promoService.findByCode(req.getPromoCode()).orElseThrow(() -> new BadRequestException("Invalid promo code"));
            if (!promoService.isValid(p)) throw new RuntimeException("Promo invalid/expired");


            boolean eligible = isEligible(user);
            if (!eligible) {
                new BadRequestException("User not eligible for promo");
                ;
            }

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
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}
