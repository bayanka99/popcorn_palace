package com.att.tdp.popcorn_palace.controllers;

import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.models.TicketBooking;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;
import com.att.tdp.popcorn_palace.repositories.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repositories.TicketBookingRepository;
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


    @PostMapping
    public ResponseEntity<?> bookTicket(@RequestBody TicketBooking booking) {
        Optional<Showtime> showtime = showtimeRepository.findById(booking.getShowtimeId());
        if (!showtime.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no Showtime exists with the provided id.");
        }
        if (ticketbookingrepository.existsByShowtimeIdAndSeatNumber(booking.getShowtimeId(),booking.getSeatNumber()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Seat already booked for this showtime.");
        }
        TicketBooking saved_booking = ticketbookingrepository.save(booking);
        Dictionary<String,String> respone=new Hashtable<>();
        respone.put("bookingId",saved_booking.getBookingId().toString());


        return ResponseEntity.ok(respone);
    }
}
