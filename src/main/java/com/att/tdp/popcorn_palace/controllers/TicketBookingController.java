package com.att.tdp.popcorn_palace.controllers;


import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.models.TicketBooking;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repositories.TicketBookingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;


@RestController
@RequestMapping("/bookings")
public class TicketBookingController {

    @Autowired
    private TicketBookingRepository ticketbookingrepository;
    @Autowired
    private ShowtimeRepository showtimeRepository;



    private String validate_input(JsonNode booking) {
        //{ "showtimeId": 1, "seatNumber": 15 , userId:"84438967-f68f-4fa0-b620-0f08217e76af"}

        if (booking.path("showtimeId").isMissingNode() || booking.path("showtimeId").isDouble() || booking.path("showtimeId").asLong() <= 0) {
            return "Please enter a valid showtimeId value.";
        }
        if (booking.path("seatNumber").isMissingNode() || booking.path("seatNumber").isDouble() || booking.path("seatNumber").asInt() <= 0) {
            return "Please enter a valid seatNumber value.";
        }
        if (booking.path("userId").isMissingNode())
        {
            return "Please enter a valid userId value.";
        }
        if (booking.size() != 3) {
            return "The input JSON should contain exactly 3 fields: showtimeId, seatNumber, and userId.";
        }
        return "";

    }
    @PostMapping
    public ResponseEntity<?> bookTicket(@RequestBody JsonNode booking) {
        String error_input=validate_input(booking);
        if (error_input!="")
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error_input);
        }
        long showtimeid=booking.get("showtimeId").asLong();
        int seat_number=booking.get("seatNumber").asInt();
        Optional<Showtime> showtime = showtimeRepository.findById(booking.get("showtimeId").asLong());
        if (!showtime.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no Showtime exists with the provided id.");
        }
        if (ticketbookingrepository.existsByShowtimeIdAndSeatNumber(showtimeid,seat_number))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Seat already booked for this showtime.");
        }

        TicketBooking saved_booking = ticketbookingrepository.save(new TicketBooking(showtimeid,seat_number,booking.get("userId").asText()));
        Dictionary<String,String> respone=new Hashtable<>();
        respone.put("bookingId",saved_booking.getBookingId().toString());


        return ResponseEntity.ok(respone);
    }
}
