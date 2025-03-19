package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.controllers.MovieController;
import com.att.tdp.popcorn_palace.models.Movie;
import com.att.tdp.popcorn_palace.repositories.MovieRepository;

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

import org.springframework.http.HttpStatus;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
public class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Movie validMovie;

    @BeforeEach
    public void setUp() {
        validMovie = new Movie("Inception", "Sci-Fi", 148, 8.8, 2010);
    }

    @Test
    public void testGetAllMovies() throws Exception {
        when(movieRepository.findAll()).thenReturn(List.of(validMovie));

        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(validMovie.getTitle()))
                .andExpect(jsonPath("$[0].genre").value(validMovie.getGenre()))
                .andExpect(jsonPath("$[0].duration").value(validMovie.getDuration()))
                .andExpect(jsonPath("$[0].rating").value(validMovie.getRating()))
                .andExpect(jsonPath("$[0].release_year").value(validMovie.getRelease_year()));

        verify(movieRepository, times(1)).findAll();
    }


    @Test
    public void testAddMovie_Success() throws Exception {
        String validMovieJson = "{ \"title\": \"Inception\", \"genre\": \"Sci-Fi\", \"duration\": 148, \"rating\": 8.8, \"releaseYear\": 2010 }";


        when(movieRepository.existsByTitle(validMovie.getTitle())).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenReturn(validMovie);

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovieJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(validMovie.getTitle()))
                .andExpect(jsonPath("$.genre").value(validMovie.getGenre()))
                .andExpect(jsonPath("$.duration").value(validMovie.getDuration()))
                .andExpect(jsonPath("$.rating").value(validMovie.getRating()))
                .andExpect(jsonPath("$.release_year").value(validMovie.getRelease_year()));

        verify(movieRepository, times(1)).existsByTitle(validMovie.getTitle());
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    public void testAddMovie_InvalidInput() throws Exception {
        // Invalid movie data (missing required fields)
        String invalidMovieJson = "{ \"random_stuff\": \"random_val\",\"title\": \"\", \"genre\": \"\", \"duration\": -1, \"rating\": -1, \"releaseYear\": -2025 }";

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMovieJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The input JSON should contain exactly 5 fields: title, genre, duration, rating, and releaseYear."));

        verify(movieRepository, times(0)).existsByTitle(anyString());
        verify(movieRepository, times(0)).save(any(Movie.class));
    }

    @Test
    public void testAddMovie_MissingTitle() throws Exception {
        // Invalid movie data (missing title)
        String missingTitleJson = "{ \"title\": \"\",\"genre\": \"Action\", \"duration\": 120, \"rating\": 8.0, \"releaseYear\": 2021 }";

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingTitleJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid title."));

        verify(movieRepository, times(0)).existsByTitle(anyString());
        verify(movieRepository, times(0)).save(any(Movie.class));
    }

    @Test
    public void testAddMovie_invalid_ratings() throws Exception {
        // Invalid movie data (missing title)
        String missingTitleJson = "{ \"title\": \"good_title\",\"genre\": \"Action\", \"duration\": 120, \"rating\": -8.0, \"releaseYear\": 2021 }";

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingTitleJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid rating between 0 and 10."));

        verify(movieRepository, times(0)).existsByTitle(anyString());
        verify(movieRepository, times(0)).save(any(Movie.class));
    }




    @Test
    public void testAddMovie_DuplicateTitle() throws Exception {
        when(movieRepository.existsByTitle(validMovie.getTitle())).thenReturn(true);
        String validMovieJson = "{ \"title\": \"Inception\", \"genre\": \"Sci-Fi\", \"duration\": 148, \"rating\": 8.8, \"releaseYear\": 2010 }";
        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovieJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("this movie already exists"));

        verify(movieRepository, times(1)).existsByTitle(validMovie.getTitle());
        verify(movieRepository, times(0)).save(any(Movie.class));
    }



    @Test
    public void testDeleteMovie_Success() throws Exception {
        when(movieRepository.findByTitle(validMovie.getTitle())).thenReturn(validMovie);

        mockMvc.perform(delete("/movies/delete/{movieTitle}", validMovie.getTitle()))
                .andExpect(status().isOk());

        verify(movieRepository, times(1)).findByTitle(validMovie.getTitle());
        verify(movieRepository, times(1)).delete(validMovie);
    }


    @Test
    public void testDeleteMovie_NotFound() throws Exception {
        when(movieRepository.findByTitle(validMovie.getTitle())).thenReturn(null);

        mockMvc.perform(delete("/movies/delete/{movieTitle}", validMovie.getTitle()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("no movie exists with the provided title"));

        verify(movieRepository, times(1)).findByTitle(validMovie.getTitle());
        verify(movieRepository, times(0)).delete(any(Movie.class));
    }

    @Test
    public void testUpdateMovie_Success() throws Exception {
        String updatedMovieJson = "{ \"title\": \"Inception\", \"genre\": \"Sci-Fi\", \"duration\": 148, \"rating\": 5, \"releaseYear\": 2010 }";
        Movie updatedMovie = new Movie("Inception", "Sci-Fi", 148, 5, 2010);

        when(movieRepository.findByTitle(validMovie.getTitle())).thenReturn(validMovie);
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

        mockMvc.perform(post("/movies/update/{movieTitle}", validMovie.getTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMovieJson))
                .andExpect(status().isOk());

        verify(movieRepository, times(1)).findByTitle(validMovie.getTitle());
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    public void testUpdateMovie_NotFound() throws Exception {
        String updatedMovieJson = "{ \"title\": \"Inception\", \"genre\": \"Sci-Fi\", \"duration\": 148, \"rating\": 9.0, \"releaseYear\": 2010 }";

        when(movieRepository.findByTitle(validMovie.getTitle())).thenReturn(null);

        mockMvc.perform(post("/movies/update/{movieTitle}", validMovie.getTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMovieJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("no movie exists with the provided title"));

        verify(movieRepository, times(1)).findByTitle(validMovie.getTitle());
        verify(movieRepository, times(0)).save(any(Movie.class));
    }

    @Test
    public void testUpdateMovie_DuplicateTitle() throws Exception {
        String updatedMovieJson = "{ \"title\": \"The Matrix\", \"genre\": \"Sci-Fi\", \"duration\": 148, \"rating\": 9.0, \"releaseYear\": 2010 }";

        // Mock repository behavior
        when(movieRepository.findByTitle("Inception")).thenReturn(validMovie);
        when(movieRepository.existsByTitle("Inception")).thenReturn(true);
        when(movieRepository.existsByTitle("The Matrix")).thenReturn(true);

        mockMvc.perform(post("/movies/update/{movieTitle}", "Inception")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMovieJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("a movie with the same title already exists. Please provide a different title."));


    }

}
