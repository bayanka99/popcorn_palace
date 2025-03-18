package com.att.tdp.popcorn_palace.controllers;


import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/showtimes")
public class ShowtimesController {
    @Autowired
    private ShowtimeRepository showtimeRepository;
    @Autowired
    private MovieRepository movieRepository;

    // Get showtime by id
    @GetMapping("/{showtimeId}")
    public ResponseEntity<Showtime> getShowtimeById(@PathVariable Long showtimeId) {
        Optional<Showtime> showtime = showtimeRepository.findById(showtimeId);
        if (showtime.isPresent()) {
            return ResponseEntity.ok(showtime.get());
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found if showtime doesn't exist
        }
    }
//
//    // Add a new movie
@PostMapping
public ResponseEntity<?> addShowtime(@RequestBody Showtime showtime) {

    // Ensure that the movieId is not null in the request body
    if (showtime.getMovieId() == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Movie ID must be provided.");
    }

    // Check if the movie exists in the database using the movieId
    Optional<Movie> movieOptional = movieRepository.findById(showtime.getMovieId());

    if (!movieOptional.isPresent()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Movie with ID " + showtime.getMovieId() + " not found.");
    }

    // Set the Movie object in the showtime (if necessary)
    // showtime.setMovie(movieOptional.get());  // Uncomment if you need to set the full Movie object

    // Save the showtime
    Showtime savedShowtime = showtimeRepository.save(showtime);

    return ResponseEntity.status(HttpStatus.CREATED).body(savedShowtime);
}

//
//
//    @DeleteMapping("/delete/{movieTitle}")
//    public ResponseEntity<Void> deleteMovie(@PathVariable String movieTitle) {
//        Movie movie = movieRepository.findByTitle(movieTitle);
//
//        if (movie != null) {
//            movieRepository.delete(movie);
//            return ResponseEntity.ok().build();
//        }
//
//        // If the movie with the given title doesn't exist, return 404
//        return ResponseEntity.notFound().build();
//    }
//    @PostMapping("/update/{movieTitle}")
//    public ResponseEntity<Void> updateMovie(@PathVariable String movieTitle, @RequestBody Movie updatedMovie) {
//        Movie movie = movieRepository.findByTitle(movieTitle);
//
//        if (movie != null) {
//            // Update the movie details
//            movie.setTitle(updatedMovie.getTitle());
//            movie.setGenre(updatedMovie.getGenre());
//            movie.setDuration(updatedMovie.getDuration());
//            movie.setRating(updatedMovie.getRating());
//            movie.setRelease_year(updatedMovie.getRelease_year());
//
//            // Save the updated movie to the database
//            movieRepository.save(movie);
//
//            // Return the updated movie with a 200 OK response
//            return ResponseEntity.ok().build();
//        }
//
//        // If the movie with the given title doesn't exist, return 404
//        return ResponseEntity.notFound().build();
//    }
}

