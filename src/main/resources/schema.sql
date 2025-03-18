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
