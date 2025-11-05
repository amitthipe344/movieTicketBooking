package com.amit.crud.controller;

import com.amit.crud.dto.BookingRequest;
import com.amit.crud.dto.BookingResponse;
import com.amit.crud.dto.BookingResponseDTO;
import com.amit.crud.entity.Booking;
import com.amit.crud.entity.Movie;
import com.amit.crud.entity.Show;
import com.amit.crud.entity.User;
import com.amit.crud.exception.NotFoundException;
import com.amit.crud.repository.UserRepository;
import com.amit.crud.service.BookingService;
import com.amit.crud.service.MovieService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {
    private final MovieService movieService;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    public CustomerController(MovieService movieService, BookingService bookingService, UserRepository userRepository) {
        this.movieService = movieService;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/movies")
    public List<Movie> listMovies() {
        return movieService.getAll();
    }

    @GetMapping("/movies/{movieId}/shows")
    public List<Show> listShows(@PathVariable Long movieId) {
        return movieService.getShowsByMovie(movieId);
    }

    @PostMapping("/bookings")
    public BookingResponse book(@RequestBody BookingRequest req, Authentication auth) {
        var username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Booking b = bookingService.book(user.getId(), req);
        BookingResponse r = new BookingResponse();
        r.setBookingId(b.getId());
        r.setSeats(b.getSeatNumbers());
        r.setTotalPrice(b.getTotalPrice());
        r.setPromoApplied(b.getPromoCodeApplied());
        return r;
    }

    @GetMapping("/bookings")
    public List<BookingResponseDTO> myBookings(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        return bookingService.getUserBookings(user.getId());
    }
}
