package com.att.tdp.popcorn_palace.repositories;

import com.att.tdp.popcorn_palace.models.Showtime;
import com.att.tdp.popcorn_palace.models.TicketBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketBookingRepository extends JpaRepository<TicketBooking, UUID> {
    boolean existsByShowtimeIdAndSeatNumber(long showtimeId, int seatNumber);
}
