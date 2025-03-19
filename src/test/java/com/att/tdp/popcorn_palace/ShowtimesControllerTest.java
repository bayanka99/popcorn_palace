package com.att.tdp.popcorn_palace;
import com.att.tdp.popcorn_palace.controllers.MovieController;
import com.att.tdp.popcorn_palace.controllers.ShowtimesController;
import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;

import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

}
