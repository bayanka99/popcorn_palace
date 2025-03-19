package com.att.tdp.popcorn_palace.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "bookings")
public class TicketBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID bookingId;

    @Column(name = "showtime_id")
    private int showtimeId;

    @Column(name = "seat_number")
    private int seatNumber;

    @Column(name = "user_id")
    private String userId;

    public TicketBooking() {
    }

    public TicketBooking(int showtimeId, int seatNumber, String userId) {
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}