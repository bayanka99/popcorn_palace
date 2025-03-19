package com.att.tdp.popcorn_palace.controllers;

import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import org.hibernate.annotations.NotFound;
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
    public ResponseEntity<?> addMovie(@RequestBody Movie movie) {
        String error_input=validate_input(movie);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }
        if(movieRepository.existsByTitle(movie.getTitle()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("this movie already exists");
        }

        Movie savedMovie = movieRepository.save(movie);
        return ResponseEntity.ok(savedMovie);
    }

    private String validate_input(Movie movie) {
        if (movie.getDuration()<=0)
        {
            return "Invalid Duration Value.";
        }
        if (!Pattern.matches("^[A-Za-z ]+$", movie.getGenre()) || movie.getGenre() == null || movie.getGenre().trim().isEmpty())
        {
            return "please enter a valid Genre.";
        }
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty())
        {
            return "please enter a valid title.";
        }
        if (movie.getRating() <0 || movie.getRating() >10 )
        {
            return "please enter a valid rating between 0 and 10.";
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

        // If the movie with the given title doesn't exist, return 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no movie exists with the provided title ");
    }
    @PostMapping("/update/{movieTitle}")
    public ResponseEntity<?> updateMovie(@PathVariable String movieTitle, @RequestBody Movie updatedMovie) {
        String error_input=validate_input(updatedMovie);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }
        Movie movie = movieRepository.findByTitle(movieTitle);
        if (movie==null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no movie exists with the provided title ");
        }

        if(!movieTitle.equals(updatedMovie.getTitle()) && movieRepository.existsByTitle(updatedMovie.getTitle()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("a movie with the same title already exists. Please provide a different title.");
        }

        // Update the movie details
        movie.setTitle(updatedMovie.getTitle());
        movie.setGenre(updatedMovie.getGenre());
        movie.setDuration(updatedMovie.getDuration());
        movie.setRating(updatedMovie.getRating());
        movie.setRelease_year(updatedMovie.getRelease_year());

        // Save the updated movie to the database
        movieRepository.save(movie);

        return ResponseEntity.ok().build();




    }
    }

