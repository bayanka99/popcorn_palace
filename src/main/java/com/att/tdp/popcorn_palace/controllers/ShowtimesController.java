package com.att.tdp.popcorn_palace.controllers;


import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> getShowtimeById(@PathVariable String showtimeId) {

        try {
            Long id = Long.parseLong(showtimeId);
            Optional<Showtime> showtime = showtimeRepository.findById(id);
            if (showtime.isPresent()) {
                return ResponseEntity.ok(showtime.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No Showtime exists with the provided ID.");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The provided showtime ID is not a valid number.");
        }
    }

// Method to check for overlapping showtimes in the same theater
    private boolean hasOverlappingShowtime(Long showtime_id, String theater, LocalDateTime startTime, LocalDateTime endTime) {
        List<Showtime> existingShowtimes = showtimeRepository
                .findByTheaterAndStartTimeBeforeAndEndTimeAfter(theater,endTime, startTime);

        if (existingShowtimes.size()==1)
        {
            if (existingShowtimes.get(0).getId()==showtime_id){
                return false;
                }
        }
        return !existingShowtimes.isEmpty(); // If there are existing showtimes, it means there's an overlap
    }

    // Method to add a new showtime
    @PostMapping
    public ResponseEntity<?> addShowtime(@RequestBody Showtime showtime) {
        String error_input=validate_input(showtime);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }

        // Check if the movie exists in the database
        Optional<Movie> movieOptional = movieRepository.findById(showtime.getMovieId());
        if (!movieOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Movie with ID " + showtime.getMovieId() + " not found.");
        }

        // Check for overlapping showtimes in the same theater
        if (hasOverlappingShowtime(showtime.getId(),showtime.getTheater(), showtime.getStartTime(), showtime.getEndTime())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Overlapping showtime found for the same theater.");
        }

        // Save the showtime if no overlap is found
        Showtime savedShowtime = showtimeRepository.save(showtime);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedShowtime);
    }

    @PostMapping("/update/{showtimeId}")
    public ResponseEntity<?> updateMovie(@PathVariable String showtimeId, @RequestBody Showtime updatedshowtime) {
        String error_input=validate_input(updatedshowtime);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }

        try {
            Long id = Long.parseLong(showtimeId);

            }
         catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The provided showtime ID is not a valid number.");
        }

        Optional<Showtime> showtime = showtimeRepository.findById(Long.parseLong(showtimeId));
        if (!showtime.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("showtime with ID " + showtimeId + " not found.");
        }

        boolean isTimeUpdated = !updatedshowtime.getStartTime().equals(showtime.get().getStartTime()) ||
                !updatedshowtime.getEndTime().equals(showtime.get().getEndTime());
        if (isTimeUpdated) {
            if (hasOverlappingShowtime(showtime.get().getId(), updatedshowtime.getTheater(), updatedshowtime.getStartTime(), updatedshowtime.getEndTime())) {

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
    public ResponseEntity<?> deleteMovie(@PathVariable String showtimeId) {
        try {
            Long id = Long.parseLong(showtimeId);

        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("The provided showtime ID is not a valid number.");
        }
        Optional<Showtime> showtime = showtimeRepository.findById(Long.parseLong(showtimeId));
        if (!showtime.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("showtime with ID " + showtimeId + " not found.");
        }

        showtimeRepository.delete(showtime.get());
        return ResponseEntity.ok().build();

    }


    private String validate_input(Showtime showtime) {
        //{ "movieId": 1, "price":20.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }
        if (showtime.getMovieId() == null || showtime.getMovieId()<0 ) {
            return "please enter a valid Movie ID.";
        }
        if (showtime.getPrice()<0)
        {
            return "please enter a valid Price Value.";
        }

        if (showtime.getTheater() == null || showtime.getTheater().trim().isEmpty())
        {
            return "please enter a valid theater.";
        }
        if (showtime.getStartTime().isAfter(showtime.getEndTime())) {
            return "Start time must be before end time.";
        }
        return "";

    }
}


