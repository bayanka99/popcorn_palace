package com.att.tdp.popcorn_palace.controllers;

import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    // Get all movies
    @GetMapping("/all")
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Add a new movie
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
        Movie savedMovie = movieRepository.save(movie);
        return ResponseEntity.ok(savedMovie);
    }

    @PostMapping("/update/{movieTitle}")
    public ResponseEntity<Movie> updateMovie(@PathVariable String movieTitle, @RequestBody Movie updatedMovie) {
        Movie movie = movieRepository.findByTitle(movieTitle);

        if (movie != null) {
            // Update the movie details
            movie.setTitle(updatedMovie.getTitle());
            movie.setGenre(updatedMovie.getGenre());
            movie.setDuration(updatedMovie.getDuration());
            movie.setRating(updatedMovie.getRating());
            movie.setRelease_year(updatedMovie.getRelease_year());

            // Save the updated movie to the database
            movieRepository.save(movie);

            // Return the updated movie with a 200 OK response
            return ResponseEntity.ok().build();
        }

        // If the movie with the given title doesn't exist, return 404
        return ResponseEntity.notFound().build();
    }
    }

