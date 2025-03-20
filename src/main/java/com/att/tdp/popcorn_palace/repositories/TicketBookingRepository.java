package com.att.tdp.popcorn_palace.repositories;

import com.att.tdp.popcorn_palace.models.TicketBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TicketBookingRepository extends JpaRepository<TicketBooking, UUID> {
    boolean existsByShowtimeIdAndSeatNumber(long showtimeId, int seatNumber);
}
