package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.controllers.ShowtimesController;
import com.att.tdp.popcorn_palace.controllers.TicketBookingController;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.models.TicketBooking;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repositories.TicketBookingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketBookingController.class)
public class TicketBookingControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketBookingRepository ticketBookingRepository;

    @MockBean
    private ShowtimeRepository showtimeRepository;


    private Showtime showtime;

    @BeforeEach
    void setUp() {

        // Create a sample Showtime object
        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setTheater("Sample Theater");
        showtime.setStartTime(LocalDateTime.parse("2025-03-19T19:00:55"));
        showtime.setEndTime(LocalDateTime.parse("2025-03-19T21:00:55"));
    }

    @Test
    void bookTicket_Success() throws Exception {
        // Setup mock response for showtime lookup
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(ticketBookingRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(false);

        TicketBooking savedBooking = new TicketBooking(1l, 15, "84438967-f68f-4fa0-b620-0f08217e76af");
        savedBooking.setBookingId(UUID.randomUUID());
        when(ticketBookingRepository.save(any(TicketBooking.class))).thenReturn(savedBooking);

        // Sample booking request body
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";
        // Sample booking request body

        // Perform the POST request
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").exists());

    }

    @Test
    void bookTicket_ShowtimeNotFound() throws Exception {
        // Setup mock response for showtime lookup
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        // Sample booking request body
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        // Perform the POST request
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().string("no Showtime exists with the provided id."));
    }

    @Test
    void bookTicket_SeatAlreadyBooked() throws Exception {
        // Setup mock response for showtime lookup
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(ticketBookingRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(true);

        // Sample booking request body
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        // Perform the POST request
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("Seat already booked for this showtime.")));
    }

    @Test
    void bookTicket_InvalidInput() throws Exception {
        // Invalid booking request (missing userId)
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15 , \"random_key\": \"random_val\"}";

        // Perform the POST request
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("Please enter a valid userId value.")));
    }

    @Test
    void bookTicket_MissingFields() throws Exception {
        // Missing seat number and showtimeId
        String bookingRequest = "{ \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        // Perform the POST request
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("The input JSON should contain exactly 3 fields: showtimeId, seatNumber, and userId.")));
    }
}
