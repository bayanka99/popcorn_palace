##1. Overview
   This document provides instructions on how to set up, build, and run the Movie Ticket Booking System ("Popcorn Palace") backend. The backend is developed using Spring Boot (Java) and is designed to handle movie management, showtimes, and ticket booking functionality. This system also ensures input validation, error handling, and prevents double booking of seats.


##2. Prerequisites
   Before starting, ensure you have the following installed:

    * Java 11 or higher

    * Maven for project build and dependency management

    * Docker for running PostgreSQL



##3. Setup Instructions

   3.1 Clone the Repository, Start by cloning the repository from GitHub:
   ```bash
  git clone <repository-url>
   ```
   

   3.2 Set Up PostgreSQL, use Docker to run a PostgreSQL database locally. Ensure Docker is installed, and use the following command to run PostgreSQL:

   ```bash
   docker-compose -f compose.yml up -d
   ```


   This will spin up a PostgreSQL instance on the default port 5432. You can modify the connection details in the application.yaml if needed.

   you can access the database through the terminal using these commands:
   ```bash
   docker exec -it popcorn-palace-db-1  bash
      
    psql -U popcorn-palace -d popcorn-palace
   ```


   3.3 Build the Project, Navigate to the project directory and build the project using Maven:
   ```bash
   mvn clean install
   ```


   This command will compile the project and download the necessary dependencies.

   3.4 Run the Application, Once the build is complete, run the Spring Boot application with the following command:
   ```bash
   mvn spring-boot:run
   ```


   The application should now be running on http://localhost:8080


##4. API Endpoints
   The project exposes the following RESTful API endpoints:
   
   **for all post requests you must add Content-Type:application/json to the header and in body add the coresponding JSON.**

   4.1 Movie Management
   * POST /movies – Add a new movie.

     URL: http://localhost:8080/movies  
example request body: 
      ```json 
        {"title": "Sample Movie Title", 
         "genre": "Action", 
         "duration": 120, 
         "rating": 8.7, 
         "releaseYear": 2025 } 
     ```


   * POST /movies/update/{movieTitle} – Update movie details.

     URL:http://localhost:8080/movies/update/myMovie

     example request body:
      ```json 
        {"title": "something", 
         "genre": "Action", 
         "duration": 120, 
         "rating": 8.7, 
         "releaseYear": 2020 } 
     ```

   * DELETE /movies/{movieTitle} – Delete a movie.

     URL:http://localhost:8080/movies/myMovie


   * GET /movies/all – Fetch all movies.

     URL:http://localhost:8080/movies/all

   4.2 Showtime Management
   * POST /showtimes – Add a new showtime for a movie.

     URL: http://localhost:8080/showtimes

     example request body:
      ```json 
     { "movieId":2 , 
     "price":20.2, 
     "theater": "Sample Theater", 
     "startTime": "2025-02-14T14:48:46.125405Z", 
     "endTime": "2025-02-14T14:50:46.125405Z" }
     ```
     
   * POST /showtimes/update/{showtimeId} – Update a showtime.

     URL: http://localhost:8080/showtimes/update/1

     example request body:
      ```json 
      { "movieId":3 , 
     "price":30.2, 
     "theater": "Sample Theater", 
     "startTime": "2025-02-14T14:48:46.125405Z", 
     "endTime": "2025-02-14T14:50:46.125405Z" }
      ```

   * DELETE /showtimes/{showtimeId} – Delete a showtime.

     URL: http://localhost:8080/showtimes/1


   * GET /showtimes/{showtimeId} – Fetch showtime by ID.

     URL: http://localhost:8080/showtimes/1

   
   4.3 Ticket Booking
   * POST /bookings – Book a ticket for a showtime.

     URL: http://localhost:8080/bookings
      ```json 
      { "showtimeId": 3, 
     "seatNumber": 554 , 
     "userId":"84438967-f68f-4fa0-b620-0f08217e76af"} 
      ```
   
   The API follows strict validation rules for inputs. Error messages will be returned for invalid inputs, such as missing fields, invalid data types, or double bookings.

##5.Running Tests
   The project uses JUnit 5 for testing. To run the unit tests, use the following Maven command:
   ```bash 
   mvn test
   ```

    