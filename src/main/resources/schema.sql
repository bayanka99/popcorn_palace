CREATE TABLE IF NOT EXISTS task (
                                    description VARCHAR(64) NOT NULL,
    completed   VARCHAR(30) NOT NULL);

CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    duration INT,
    rating DECIMAL(3, 1),
    release_year INT
);

CREATE TABLE IF NOT EXISTS showtimes (
    id BIGSERIAL PRIMARY KEY,
    price DECIMAL(5, 2),
    movie_id BIGINT REFERENCES movies(id) ON DELETE CASCADE,
    theater VARCHAR(255),
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

