package com.att.tdp.popcorn_palace.repositories;

import com.att.tdp.popcorn_palace.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Movie findByTitle(String title);
    boolean existsByTitle(String movie_title);
}
