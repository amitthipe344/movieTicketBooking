package com.amit.crud.service;

import com.amit.crud.entity.Movie;
import com.amit.crud.entity.Show;
import com.amit.crud.exception.BadRequestException;
import com.amit.crud.exception.NotFoundException;
import com.amit.crud.repository.MovieRepository;
import com.amit.crud.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;

    public MovieService(MovieRepository movieRepository, ShowRepository showRepository) {
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
    }

    public Movie addMovie(Movie m) {
        if (m.getTitle() == null || m.getTitle().isBlank()) {
            throw new BadRequestException("Movie title cannot be empty");
        }
        return movieRepository.save(m);
    }

    public Movie updateMovie(Long id, Movie m) {
        return movieRepository.findById(id)
                .map(existing -> movieRepository.save(m))
                .orElseThrow(() -> new NotFoundException("Movie not found with id: " + m.getId()));
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new NotFoundException("Movie not found with id: " + id);
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
}

