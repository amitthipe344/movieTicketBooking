package com.amit.crud.service;

import com.amit.crud.entity.Movie;
import com.amit.crud.entity.Show;
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

    public Movie addMovie(Movie m) { return movieRepository.save(m); }
    public Movie updateMovie(Movie m) { return movieRepository.save(m); }
    public void deleteMovie(Long id) { movieRepository.deleteById(id); }
    public List<Movie> getAll() { return movieRepository.findAll(); }

    public Show addShow(Show s) { return showRepository.save(s); }
    public void deleteShow(Long id) { showRepository.deleteById(id); }
    public List<Show> getShowsByMovie(Long movieId) { return showRepository.findByMovieId(movieId); }
}

