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

import java.time.LocalDateTime;
import java.util.List;
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
    public ResponseEntity<?> getShowtimeById(@PathVariable Long showtimeId) {
        Optional<Showtime> showtime = showtimeRepository.findById(showtimeId);
        if (showtime.isPresent()) {
            return ResponseEntity.ok(showtime.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no Showtime exists with the provided id."); // 404 Not Found if showtime doesn't exist
        }
    }
//
//    // Add a new movie
// Method to check for overlapping showtimes in the same theater
    private boolean hasOverlappingShowtime(String theater, LocalDateTime startTime, LocalDateTime endTime) {
        List<Showtime> existingShowtimes = showtimeRepository
                .findByTheaterAndStartTimeBeforeAndEndTimeAfter(theater,endTime, startTime);

        return !existingShowtimes.isEmpty(); // If there are existing showtimes, it means there's an overlap
    }

    // Method to add a new showtime
    @PostMapping
    public ResponseEntity<?> addShowtime(@RequestBody Showtime showtime) {

        // Ensure that the movieId is not null in the request body
        if (showtime.getMovieId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Movie ID must be provided.");
        }

        // Check if the movie exists in the database
        Optional<Movie> movieOptional = movieRepository.findById(showtime.getMovieId());
        if (!movieOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Movie with ID " + showtime.getMovieId() + " not found.");
        }

        // Check for overlapping showtimes in the same theater
        if (hasOverlappingShowtime(showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Overlapping showtime found for the same theater.");
        }

        // Save the showtime if no overlap is found
        Showtime savedShowtime = showtimeRepository.save(showtime);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedShowtime);
    }

    @PostMapping("/update/{showtimeId}")
    public ResponseEntity<?> updateMovie(@PathVariable Long showtimeId, @RequestBody Showtime updatedshowtime) {
        Optional<Showtime> showtime = showtimeRepository.findById(showtimeId);
        if (!showtime.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("showtime with ID " + showtimeId + " not found.");
        }

        boolean isTimeUpdated = !updatedshowtime.getStartTime().equals(showtime.get().getStartTime()) ||
                !updatedshowtime.getEndTime().equals(showtime.get().getEndTime());
        if (isTimeUpdated) {
            if (hasOverlappingShowtime(updatedshowtime.getTheater(), updatedshowtime.getStartTime(), updatedshowtime.getEndTime())) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Overlapping showtime found for the same theater.");
            }
        }
        showtime.get().setMovieId(updatedshowtime.getMovieId());
        showtime.get().setEndTime(updatedshowtime.getEndTime());
        showtime.get().setPrice(updatedshowtime.getPrice());
        showtime.get().setTheater(updatedshowtime.getTheater());
        showtime.get().setStartTime(updatedshowtime.getStartTime());
        Showtime savedShowtime = showtimeRepository.save(showtime.get());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{showtimeId}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long showtimeId) {
        Optional<Showtime> showtime = showtimeRepository.findById(showtimeId);
        if (!showtime.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("showtime with ID " + showtimeId + " not found.");
        }

        showtimeRepository.delete(showtime.get());
        return ResponseEntity.ok().build();

    }
}


