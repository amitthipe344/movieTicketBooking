package com.amit.crud.service;

import com.amit.crud.dto.MovieSetupRequest;
import com.amit.crud.dto.PromoCodeRequest;
import com.amit.crud.entity.Movie;
import com.amit.crud.entity.PromoCode;
import com.amit.crud.entity.Seat;
import com.amit.crud.entity.Show;
import com.amit.crud.exception.BadRequestException;
import com.amit.crud.exception.NotFoundException;
import com.amit.crud.repository.MovieRepository;
import com.amit.crud.repository.PromoCodeRepository;
import com.amit.crud.repository.SeatRepository;
import com.amit.crud.repository.ShowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final PromoCodeRepository promoCodeRepository;

    public MovieService(MovieRepository movieRepository, ShowRepository showRepository, SeatRepository seatRepository,PromoCodeRepository promoCodeRepository) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional
    public Movie createMovieWithShowsAndSeats(MovieSetupRequest req) {
        // 1. Create movie
        Movie movie = Movie.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .build();
        movie = movieRepository.save(movie);

        // 2. Create shows
        for (MovieSetupRequest.ShowRequest showReq : req.getShows()) {
            Show show = Show.builder()
                    .movie(movie)
                    .startTime(showReq.getStartTime())
                    .price(showReq.getPrice())
                    .build();
            show = showRepository.save(show);

            // 3. Create seats for each show
            for (String seatNumber : showReq.getSeatNumbers()) {
                Seat seat = Seat.builder()
                        .seatNumber(seatNumber)
                        .show(show)
                        .status(Seat.SeatStatus.AVAILABLE)
                        .build();
                seatRepository.save(seat);
            }
        }

        return movie;
    }

    public Movie addMovie(Movie m) {
        if (m.getTitle() == null || m.getTitle().isBlank()) {
            throw new BadRequestException("Movie title cannot be empty");
        }
        return movieRepository.save(m);
    }

    public Movie updateMovie(Long id, Movie m) {
        return movieRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(m.getTitle());
                    existing.setDescription(m.getDescription());
                    return movieRepository.save(existing);
                })
                .orElseThrow(() -> new NotFoundException("Movie not found with id: " + m.getId()));
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new NotFoundException("Movie not found with id: " + id);
        }
        List<Show> shows = showRepository.findByMovieId(id);
        for (Show show : shows) {
            seatRepository.deleteAll(seatRepository.findByShowId(show.getId()));
            showRepository.delete(show);
        }

        movieRepository.deleteById(id);
    }

    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    public Show addShow(Show s) {
        if (s.getId() == null) {
            throw new BadRequestException("Show must be linked to a movie");
        }
        return showRepository.save(s);
    }

    public void deleteShow(Long id) {
        if (!showRepository.existsById(id)) {
            throw new NotFoundException("Show not found with id: " + id);
        }
        showRepository.deleteById(id);
    }

    public List<Show> getShowsByMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new NotFoundException("Movie not found with id: " + movieId);
        }
        return showRepository.findByMovieId(movieId);
    }

    public PromoCode addPromoCode(PromoCodeRequest promoCodeRequest) {
        PromoCode promoCode = PromoCode.builder()
                .code(promoCodeRequest.getCode())
                .type(promoCodeRequest.getType())
                .active(promoCodeRequest.isActive())
                .expiryDate(promoCodeRequest.getExpiryDate())
                .build();
        return promoCodeRepository.save(promoCode);

    }
}

