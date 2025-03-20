package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.controllers.TicketBookingController;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.models.TicketBooking;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repositories.TicketBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setTheater("Sample Theater");
        showtime.setStartTime(LocalDateTime.parse("2025-03-19T19:00:55"));
        showtime.setEndTime(LocalDateTime.parse("2025-03-19T21:00:55"));
    }

    @Test
    void bookTicket_Success() throws Exception {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(ticketBookingRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(false);

        TicketBooking savedBooking = new TicketBooking(1l, 15, "84438967-f68f-4fa0-b620-0f08217e76af");
        savedBooking.setBookingId(UUID.randomUUID());
        when(ticketBookingRepository.save(any(TicketBooking.class))).thenReturn(savedBooking);
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").exists());

    }

    @Test
    void bookTicket_ShowtimeNotFound() throws Exception {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().string("no Showtime exists with the provided id."));
    }

    @Test
    void bookTicket_SeatAlreadyBooked() throws Exception {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(ticketBookingRepository.existsByShowtimeIdAndSeatNumber(1L, 15)).thenReturn(true);
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("Seat already booked for this showtime.")));
    }

    @Test
    void bookTicket_Invalid_Input_missing_keys() throws Exception {
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15 }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("The input JSON should contain exactly 3 fields: showtimeId, seatNumber, and userId.")));
    }
    @Test
    void bookTicket_Invalid_Input_extra_keys() throws Exception {

        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15 , \"random_key\": \"random_val\",\"random_key_2\": \"random_val_2\"}";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("The input JSON should contain exactly 3 fields: showtimeId, seatNumber, and userId.")));
    }


    @Test
    void bookTicket_Invalid_Input_missing_user_id() throws Exception {
        // missing user id
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15,\"random_key\": \"random_val\" }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("Please enter a valid userId value.")));
    }
    @Test
    void bookTicket_Invalid_Input_missing_seatNumber() throws Exception {
        // missing user seatNumber
        String bookingRequest = "{ \"showtimeId\": 1, \"random_key\": \"random_val\",\"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("Please enter a valid seatNumber value.")));
    }
    @Test
    void bookTicket_Invalid_Input_missing_showtimeid() throws Exception {
        // missing user showtimeid
        String bookingRequest = "{ \"seatNumber\": 1, \"random_key\": \"random_val\",\"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(("Please enter a valid showtimeId value.")));
    }
    @Test
    void bookTicket_InvalidShowtimeId_1() throws Exception {
        String bookingRequest = "{ \"showtimeId\": -1, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid showtimeId value."));
    }
    @Test
    void bookTicket_InvalidShowtimeId_2() throws Exception {
        String bookingRequest = "{ \"showtimeId\": 1.5, \"seatNumber\": 15, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid showtimeId value."));
    }
    @Test
    void bookTicket_Invalid_seat_number_1() throws Exception {
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": 15.5, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid seatNumber value."));
    }
    @Test
    void bookTicket_Invalid_seat_number_2() throws Exception {
        String bookingRequest = "{ \"showtimeId\": 1, \"seatNumber\": -50, \"userId\": \"84438967-f68f-4fa0-b620-0f08217e76af\" }";

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid seatNumber value."));
    }

}
