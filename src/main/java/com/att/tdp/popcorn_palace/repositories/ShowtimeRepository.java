package com.att.tdp.popcorn_palace.repositories;

import com.att.tdp.popcorn_palace.models.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    @Query("SELECT s FROM Showtime s WHERE s.theater = :theater " +
            "AND s.startTime <= :endTime AND s.endTime >= :startTime")
    List<Showtime> findByTheaterAndStartTimeBeforeAndEndTimeAfter(
            String theater, LocalDateTime endTime, LocalDateTime startTime);

}
