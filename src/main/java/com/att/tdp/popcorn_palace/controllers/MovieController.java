package com.att.tdp.popcorn_palace.controllers;

import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.regex.Pattern;

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
    public ResponseEntity<?> addMovie(@RequestBody JsonNode movie) {
        String error_input=validate_input(movie);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }
        if(movieRepository.existsByTitle(movie.path("title").asText()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("this movie already exists");
        }

        String title=movie.path("title").asText();
        String genre=movie.path("genre").asText();
        int duration=movie.path("duration").asInt();
        double rating=movie.path("rating").asDouble();
        int year=movie.path("releaseYear").asInt();
        Movie savedMovie = movieRepository.save(new Movie(title,genre,duration,rating,year));
        return ResponseEntity.ok(savedMovie);
    }

    private String validate_input(JsonNode movie) {

        if (movie.size() != 5) {
            return "The input JSON should contain exactly 5 fields: title, genre, duration, rating, and releaseYear.";
        }
        if (movie.path("title").isMissingNode() || movie.path("title").asText().trim().isEmpty()) {
            return "Please enter a valid title.";
        }

        if (movie.path("genre").isMissingNode() || !Pattern.matches("^[A-Za-z\\s-]+$", movie.path("genre").asText())) {
            return "Please enter a valid genre.";
        }

        if (movie.path("duration").isMissingNode() || movie.path("duration").asInt() <= 0) {
            return "Please enter a valid duration.";
        }

        if (movie.path("rating").isMissingNode()) {
            return "Please enter a valid rating between 0 and 10.";
        }
        try {
            double rating = Double.parseDouble(movie.path("rating").asText());
            if (rating < 0 || rating > 10) {
                return "Please enter a valid rating between 0 and 10.";
            }
        } catch (NumberFormatException e) {
            return "Please enter a valid numeric value for rating.";
        }


        if (movie.path("releaseYear").isMissingNode() || !movie.path("releaseYear").canConvertToExactIntegral() ||movie.path("releaseYear").asInt() <= 0) {
            return "Please enter a valid release year.";
        }


        return "";

    }


    @DeleteMapping("/delete/{movieTitle}")
    public ResponseEntity<?> deleteMovie(@PathVariable String movieTitle) {


        Movie movie = movieRepository.findByTitle(movieTitle);

        if (movie != null) {
            movieRepository.delete(movie);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no movie exists with the provided title");
    }
    @PostMapping("/update/{movieTitle}")
    public ResponseEntity<?> updateMovie(@PathVariable String movieTitle, @RequestBody JsonNode updatedMovie) {
        String error_input=validate_input(updatedMovie);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }
        Movie movie = movieRepository.findByTitle(movieTitle);
        if (movie==null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no movie exists with the provided title");
        }

        if(!movieTitle.equals(updatedMovie.path("title").asText()) && movieRepository.existsByTitle(updatedMovie.path("title").asText()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("a movie with the same title already exists. Please provide a different title.");
        }

        // Update the movie details
        movie.setTitle(updatedMovie.path("title").asText());
        movie.setGenre(updatedMovie.path("genre").asText());
        movie.setDuration(updatedMovie.path("duration").asInt());
        movie.setRating(updatedMovie.path("rating").asDouble());
        movie.setRelease_year(updatedMovie.path("releaseYear").asInt());

        // Save the updated movie to the database
        movieRepository.save(movie);

        return ResponseEntity.ok().build();




    }
    }

