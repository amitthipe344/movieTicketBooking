package com.amit.crud.controller;



import com.amit.crud.entity.Movie;
import com.amit.crud.entity.Show;
import com.amit.crud.service.MovieService;
import com.amit.crud.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final MovieService movieService;
    private final BookingService bookingService;

    public AdminController(MovieService movieService, BookingService bookingService) {
        this.movieService = movieService;
        this.bookingService = bookingService;
    }

    @PostMapping("/movies")
    public Movie addMovie(@RequestBody Movie m){ return movieService.addMovie(m); }

    @PutMapping("/movies")
    public Movie updateMovie(@RequestBody Movie m){ return movieService.updateMovie(m); }

    @DeleteMapping("/movies/{id}")
    public void deleteMovie(@PathVariable Long id){ movieService.deleteMovie(id); }

    @PostMapping("/shows")
    public Show addShow(@RequestBody Show s){ return movieService.addShow(s); }

    @DeleteMapping("/shows/{id}")
    public void deleteShow(@PathVariable Long id){ movieService.deleteShow(id); }

    @GetMapping("/bookings")
    public List<?> viewAllBookings(){ return bookingService.getAllBookings(); }
}

