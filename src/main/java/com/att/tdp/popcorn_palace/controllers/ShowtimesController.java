package com.att.tdp.popcorn_palace.controllers;


import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public ResponseEntity<?> addShowtime(@RequestBody JsonNode showtime) {
        String error_input=validate_input(showtime);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }

        // Check if the movie exists in the database
        Optional<Movie> movieOptional = movieRepository.findById(showtime.path("movieId").asLong());
        if (!movieOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Movie with ID " + showtime.path("movieId").asLong() + " not found.");
        }
        Long movieId = showtime.path("movieId").asLong();
        Double price = showtime.path("price").asDouble();
        String theater = showtime.path("theater").asText();
        LocalDateTime startTime = LocalDateTime.parse(showtime.path("startTime").asText(), DateTimeFormatter.ISO_DATE_TIME);

        LocalDateTime endTime = LocalDateTime.parse(showtime.path("endTime").asText(), DateTimeFormatter.ISO_DATE_TIME);



        // Check for overlapping showtimes in the same theater
        if (hasOverlappingShowtime(null,theater, startTime, endTime)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Overlapping showtime found for the same theater.");
        }



        // Save the showtime if no overlap is found
        Showtime savedShowtime = showtimeRepository.save(new Showtime(price,movieId,theater,startTime,endTime));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedShowtime);
    }

    @PostMapping("/update/{showtimeId}")
    public ResponseEntity<?> updateMovie(@PathVariable String showtimeId, @RequestBody JsonNode updatedshowtime) {
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

        Long movieId = updatedshowtime.path("movieId").asLong();
        Double price = updatedshowtime.path("price").asDouble();
        String theater = updatedshowtime.path("theater").asText();

        LocalDateTime startTime = LocalDateTime.parse(updatedshowtime.path("startTime").asText(), DateTimeFormatter.ISO_DATE_TIME);

        LocalDateTime endTime = LocalDateTime.parse(updatedshowtime.path("endTime").asText(), DateTimeFormatter.ISO_DATE_TIME);
        boolean isTimeUpdated = !startTime.equals(showtime.get().getStartTime()) ||
                !endTime.equals(showtime.get().getEndTime());
        if (isTimeUpdated) {
            if (hasOverlappingShowtime(showtime.get().getId(), theater, startTime, endTime)) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Overlapping showtime found for the same theater.");
            }
        }
        showtime.get().setMovieId(movieId);
        showtime.get().setEndTime(endTime);
        showtime.get().setPrice(price);
        showtime.get().setTheater(theater);
        showtime.get().setStartTime(startTime);
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


    private String validate_input(JsonNode showtime) {
        //{ "movieId": 1, "price":20.2, "theater": "Sample Theater", "startTime": "2025-02-14T11:47:46.125405Z", "endTime": "2025-02-14T14:47:46.125405Z" }
        if (showtime.size() != 5) {
            return "The input JSON should contain exactly 5 fields: movieId, price, theater, startTime, and endTime.";
        }
        if (showtime.path("movieId").isMissingNode() || showtime.path("movieId").isDouble() || showtime.path("movieId").asLong() <= 0) {
            return "Please enter a valid movieId value.";
        }

        if (showtime.path("price").isMissingNode() || showtime.path("price").asLong() <= 0) {
            return "Please enter a valid price value.";
        }

        if (showtime.path("theater").isMissingNode() || showtime.path("theater").asText().trim().isEmpty()) {
            return "Please enter a valid theater.";
        }

        if (showtime.path("startTime").isMissingNode() || !showtime.path("startTime").isTextual()) {
            return "Please enter a valid startTime.";
        }
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(showtime.path("startTime").asText());
        } catch (DateTimeParseException e) {
            return "startTime should be a valid date-time string , example: 2025-02-14T14:58:46Z.";
        }

        // Validate endTime (should be a valid date-time string)
        if (showtime.path("endTime").isMissingNode() || !showtime.path("endTime").isTextual()) {
            return "Please enter a valid endTime.";
        }
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(showtime.path("endTime").asText());
        } catch (DateTimeParseException e) {
            return "endTime should be a valid date-time string , example: 2025-02-14T14:58:46Z.";
        }
        LocalDateTime startTime = LocalDateTime.parse(showtime.path("startTime").asText(), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(showtime.path("endTime").asText(), DateTimeFormatter.ISO_DATE_TIME);


        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            return "End time must be greater than start time.";
        }
        return "";

    }
}


