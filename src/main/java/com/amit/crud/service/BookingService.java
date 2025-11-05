package com.amit.crud.service;

import com.amit.crud.dto.BookingRequest;
import com.amit.crud.dto.BookingResponseDTO;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Show show = showRepository.findById(req.getShowId())
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + req.getShowId()));

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

        double total = seats.size() * show.getPrice();
        String applied = null;

        // ---- Promo Code Handling (only if provided) ----
        if (req.getPromoCode() != null && !req.getPromoCode().isBlank()) {
            var promo = promoService.findByCode(req.getPromoCode())
                    .orElseThrow(() -> new BadRequestException("Invalid promo code"));

            if (!promoService.isValid(promo)) {
                throw new BadRequestException("Promo invalid/expired");
            }

            // eligibility check: current booking >5 seats OR current total >1500
            if (!(seats.size() > 5 || total > 1500)) {
                throw new BadRequestException("User not eligible for promo");
            }

            // apply promo
            if ("FREE_SEAT".equalsIgnoreCase(promo.getType())) {
                total = Math.max(0, total - show.getPrice());
                applied = promo.getCode();
            } else if ("FLAT_250".equalsIgnoreCase(promo.getType())) {
                total = Math.max(0, total - 250);
                applied = promo.getCode();
            }
        }

        // mark booked
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seatRepository.save(seat);
        }

        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .seatNumbers(seatNumbers)
                .createdAt(LocalDateTime.now())
                .totalPrice(total)
                .promoCodeApplied(applied)
                .build();

        Booking saved = bookingRepository.save(booking);

        user.setTotalSpent(user.getTotalSpent() + total);
        userRepository.save(user);

        return saved;
    }

    public List<BookingResponseDTO> getUserBookings(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BookingResponseDTO mapToResponse(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .username(booking.getUser().getUsername())
                .showId(booking.getShow().getId())
                .seatNumbers(booking.getSeatNumbers())
                .numberOfSeats(booking.getSeatNumbers().size())
                .ticketPrice(booking.getShow().getPrice())
                .totalPrice(booking.getTotalPrice())
                .promoCodeApplied(booking.getPromoCodeApplied())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
