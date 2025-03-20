package com.att.tdp.popcorn_palace;
import com.att.tdp.popcorn_palace.controllers.ShowtimesController;
import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;

import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShowtimesController.class)
public class ShowtimesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeRepository showtimeRepository;

    @MockBean
    private MovieRepository movieRepository;

    private Movie validMovie;

    @BeforeEach
    public void setUp() {
        validMovie = new Movie("Inception", "Sci-Fi", 148, 8.8, 2010);
    }

    @Test
    public void testGetShowtimeById_Valid() throws Exception {
        Showtime validShowtime = new Showtime(20.0, 1L, "Sample Theater", LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
        String formattedStartTime = validShowtime.getStartTime().format(formatter);
        String formattedEndTime = validShowtime.getEndTime().format(formatter);
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(validShowtime));

        mockMvc.perform(get("/showtimes/{showtimeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"price\":20.0,\"movieId\":1,\"theater\":\"Sample Theater\",\"startTime\":\"" +
                        formattedStartTime + "\",\"endTime\":\"" + formattedEndTime + "\"}"));
    }

    @Test
    public void testGetShowtimeById_NotFound() throws Exception {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/showtimes/{showtimeId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No Showtime exists with the provided ID."));
    }

    @Test
    public void testGetShowtimeById_Invalid() throws Exception {
        mockMvc.perform(get("/showtimes/{showtimeId}", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The provided showtime ID is not a valid number."));
    }

    @Test
    public void testAddShowtime_Valid() throws Exception {

        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";


        when(movieRepository.findById(any(Long.class))).thenReturn(Optional.of(validMovie));


        Showtime newShowtime = new Showtime(10.0, 1L, "Theater1", LocalDateTime.parse("2025-03-20T14:00:00"), LocalDateTime.parse("2025-03-20T16:00:00"));
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(newShowtime);

        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.movieId").value(1L))
                .andExpect(jsonPath("$.theater").value("Theater1"))
                .andExpect(jsonPath("$.startTime").value("2025-03-20T14:00:00"))
                .andExpect(jsonPath("$.endTime").value("2025-03-20T16:00:00"));
    }
    @Test
    public void testAddShowtime_InvalidInput() throws Exception {

        String invalidJson = "{}";

        // Act and Assert
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The input JSON should contain exactly 5 fields: movieId, price, theater, startTime, and endTime."));  // Modify this to the actual error message
    }
    @Test
    public void testAddShowtime_InvalidTheater() throws Exception {
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid theater."));
    }
    @Test
    public void testAddShowtime_MissingStartTime() throws Exception {
        // Arrange: Missing endTime in the JSON
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\",\"Random_key\": \"random_val\", \"endTime\": \"2025-03-20T14:00:00\"}";

        // Act and Assert
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid startTime."));
    }
    @Test
    public void testAddShowtime_InvalidStartTimeFormat() throws Exception {
        // Arrange: Invalid startTime format in the JSON
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"invalid-time\", \"endTime\": \"2025-03-20T16:00:00\"}";

        // Act and Assert
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("startTime should be a valid date-time string , example: 2025-02-14T14:58:46Z."));
    }
    @Test
    public void testAddShowtime_MissingEndTime() throws Exception {
        // Arrange: Missing endTime in the JSON
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\",\"Random_key\": \"random_val\", \"startTime\": \"2025-03-20T14:00:00\"}";

        // Act and Assert
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid endTime."));
    }
    @Test
    public void testAddShowtime_InvalidEndTimeFormat() throws Exception {
        // Arrange: Invalid endTime format in the JSON
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"invalid-time\"}";

        // Act and Assert
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("endTime should be a valid date-time string , example: 2025-02-14T14:58:46Z."));
    }
    @Test
    public void testAddShowtime_EndTimeBeforeStartTime() throws Exception {
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T16:00:00\", \"endTime\": \"2025-03-20T14:00:00\"}";

        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End time must be greater than start time."));
    }
    @Test
    public void testAddShowtime_StartTimeEqualToEndTime() throws Exception {
        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T14:00:00\"}";
        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End time must be greater than start time."));
    }
    @Test
    public void testAddShowtime_MovieNotFound() throws Exception {

        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";


        when(movieRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Movie with ID 1 not found."));
    }
    @Test
    public void testAddShowtime_OverlappingShowtime() throws Exception {

        String json = "{\"movieId\": 1, \"price\": 10.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";


        when(movieRepository.findById(any(Long.class))).thenReturn(Optional.of(validMovie));


        when(showtimeRepository.save(any(Showtime.class))).thenReturn(new Showtime(10.0, 1L, "Theater1",
                LocalDateTime.parse("2025-03-20T14:00:00"), LocalDateTime.parse("2025-03-20T16:00:00")));


        when(showtimeRepository.findByTheaterAndStartTimeBeforeAndEndTimeAfter(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new Showtime(10.0, 1L, "Theater1", LocalDateTime.parse("2025-03-20T10:00:00"), LocalDateTime.parse("2025-03-20T12:00:00")),
                        new Showtime(12.0, 2L, "Theater1", LocalDateTime.parse("2025-03-20T13:00:00"), LocalDateTime.parse("2025-03-20T15:00:00"))
                ));

        mockMvc.perform(post("/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Overlapping showtime found for the same theater."));
    }
    @Test
    public void testUpdateMovie_ValidRequest() throws Exception {
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        Showtime existingShowtime = new Showtime(10.0, 1L, "Theater1", LocalDateTime.parse("2025-03-20T12:00:00"), LocalDateTime.parse("2025-03-20T14:00:00"));
        when(showtimeRepository.findById(any(Long.class))).thenReturn(Optional.of(existingShowtime));

        Showtime updatedShowtime = new Showtime(12.0, 1L, "Theater1", LocalDateTime.parse("2025-03-20T14:00:00"), LocalDateTime.parse("2025-03-20T16:00:00"));
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(updatedShowtime);

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
    @Test
    public void testUpdateMovie_InvalidShowtimeId() throws Exception {
        //invalid showtimeId (not a number)
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/invalid-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The provided showtime ID is not a valid number."));
    }
    @Test
    public void testUpdateMovie_ShowtimeNotFound() throws Exception {
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        when(showtimeRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("showtime with ID 1 not found."));
    }
    @Test
    public void testUpdateMovie_Invalid_Input_extra_keys() throws Exception {
        //invalid JSON input extra keys
        String json = "{\"random_key\": \"random_val\",\"movieId\": 1, \"price\": 12.0, \"theater\": \"\",\"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The input JSON should contain exactly 5 fields: movieId, price, theater, startTime, and endTime."));
    }
    @Test
    public void testUpdateMovie_Invalid_Input_less_keys() throws Exception {
        //invalid JSON input less keys
        String json = "{\"price\": 12.0, \"theater\": \"\",\"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The input JSON should contain exactly 5 fields: movieId, price, theater, startTime, and endTime."));
    }
    @Test
    public void testUpdateMovie_Invalid_movieId_value() throws Exception {
        //invalid JSON input invalid movieId value
        String json = "{\"movieId\": 1.5, \"price\": 12.0, \"theater\": \"\",\"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid movieId value."));
    }
    @Test
    public void testUpdateMovie_Invalid_price_value() throws Exception {
        //invalid JSON input invalid price value
        String json = "{\"movieId\": 1, \"price\": -12.0, \"theater\": \"\",\"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid price value."));
    }
    @Test
    public void testUpdateMovie_Invalid_theater_value() throws Exception {
        //invalid JSON input invalid theater value
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"\",\"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid theater."));
    }

    @Test
    public void testUpdateMovie_Invalid_endtime_format() throws Exception {
        //invalid JSON input invalid endTime format
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"good_theater\",\"startTime\": \"2025-03-20T16:00:00\", \"endTime\": \"2025/03/20 16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("endTime should be a valid date-time string , example: 2025-02-14T14:58:46Z."));
    }
    @Test
    public void testUpdateMovie_Invalid_startTime_format() throws Exception {
        //invalid JSON input invalid startTime format
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"good_theater\",\"startTime\": \"2025/03/20 14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("startTime should be a valid date-time string , example: 2025-02-14T14:58:46Z."));
    }
    @Test
    public void testUpdateMovie_Invalid_endtime_value() throws Exception {
        //invalid JSON input invalid endTime value
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"good_theater\",\"startTime\": \"2025-03-20T16:00:00\", \"endTime\": 8}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid endTime."));
    }
    @Test
    public void testUpdateMovie_Invalid_startTime_value() throws Exception {
        //invalid JSON input invalid startTime value
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"good_theater\",\"startTime\": 7, \"endTime\": \"2025-03-20T16:00:00\"}";

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid startTime."));
    }
    @Test
    public void testUpdateMovie_OverlappingShowtime() throws Exception {
        // Valid JSON input with overlapping showtime
        String json = "{\"movieId\": 1, \"price\": 12.0, \"theater\": \"Theater1\", \"startTime\": \"2025-03-20T14:00:00\", \"endTime\": \"2025-03-20T16:00:00\"}";

        Showtime showtime_to_update = new Showtime(10.0, 1L, "Theater1", LocalDateTime.parse("2025-03-20T16:00:00"), LocalDateTime.parse("2025-03-20T19:00:00"));
        showtime_to_update.setId(1L);
        Showtime existingShowtime = new Showtime(10.0, 1L, "Theater1", LocalDateTime.parse("2025-03-20T13:00:00"), LocalDateTime.parse("2025-03-20T15:00:00"));
        existingShowtime.setId(2L);
        when(showtimeRepository.findById(any(Long.class))).thenReturn(Optional.of(showtime_to_update));

        when(showtimeRepository.findByTheaterAndStartTimeBeforeAndEndTimeAfter(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(existingShowtime));

        mockMvc.perform(post("/showtimes/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Overlapping showtime found for the same theater."));
    }


}
