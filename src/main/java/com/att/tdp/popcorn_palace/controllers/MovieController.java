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
}
