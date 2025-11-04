package com.amit.crud.controller;


import com.amit.crud.dto.MovieSetupRequest;
import com.amit.crud.entity.Booking;
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
    @PostMapping("/movie-setup")
    public Movie createMovieSetup(@RequestBody MovieSetupRequest req) {
        return movieService.createMovieWithShowsAndSeats(req);
    }

    @PostMapping("/movies")
    public Movie addMovie(@RequestBody Movie m) {
        return movieService.addMovie(m);
    }

    @PutMapping("/movies/{id}")
    public Movie updateMovie(@PathVariable Long id, @RequestBody Movie m) {
        return movieService.updateMovie(id,m);
    }

    @DeleteMapping("/movies/{id}")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }

    @PostMapping("/shows")
    public Show addShow(@RequestBody Show s) {
        return movieService.addShow(s);
    }
    @GetMapping("/movies")
    public List<Movie> listMovies() {
        return movieService.getAll();
    }

    @DeleteMapping("/shows/{id}")
    public void deleteShow(@PathVariable Long id) {
        movieService.deleteShow(id);
    }

    @GetMapping("/bookings")
    public List<Booking> viewAllBookings() {
        return bookingService.getAllBookings();
    }
}

