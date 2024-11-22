-- Create the EMAT database and switch to it
CREATE DATABASE IF NOT EXISTS emat;
USE emat;
-- Users table with constraints and ENUM role included directly in the table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL CHECK (CHAR_LENGTH(password) >= 8),
    role ENUM('president', 'admin', 'regular_user') NOT NULL DEFAULT 'regular_user',
    email VARCHAR(100) UNIQUE NOT NULL CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    full_name VARCHAR(100) NOT NULL,
    department VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT valid_username CHECK (CHAR_LENGTH(username) >= 3)
);
select * from users;

-- Adding indexes for the 'users' table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_department ON users(department);

-- Events table
CREATE TABLE events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(100) NOT NULL,
    event_description TEXT,
    event_date DATETIME NOT NULL,
    registration_deadline DATETIME NOT NULL,
    location VARCHAR(100) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    event_type ENUM('conference', 'workshop', 'seminar', 'meeting', 'other') NOT NULL,
    event_status ENUM('upcoming', 'ongoing', 'completed', 'cancelled') DEFAULT 'upcoming',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT valid_deadline CHECK (registration_deadline < event_date)
);

-- Adding indexes for the 'events' table
CREATE INDEX idx_events_date ON events(event_date);
CREATE INDEX idx_events_status ON events(event_status);
CREATE INDEX idx_events_type ON events(event_type);

-- Attendees table
CREATE TABLE attendees (
    attendee_id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attendance_status ENUM('registered', 'attended', 'cancelled', 'no_show') DEFAULT 'registered',
    check_in_time TIMESTAMP NULL,
    check_out_time TIMESTAMP NULL,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_event_attendee UNIQUE (event_id, user_id)
);

-- Feedback table
CREATE TABLE feedback (
    feedback_id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_feedback UNIQUE (event_id, user_id)
);

-- Resources table
CREATE TABLE resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    resource_type ENUM('presentation', 'document', 'video', 'other') NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    uploaded_by INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

-- Analytics table
CREATE TABLE analytics (
    analytics_id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    total_registrations INT DEFAULT 0,
    actual_attendance INT DEFAULT 0,
    satisfaction_score DECIMAL(3,2) CHECK (satisfaction_score BETWEEN 0 AND 5),
    feedback_count INT DEFAULT 0,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);

-- Views for different access levels
CREATE VIEW public_events AS
SELECT 
    event_id,
    event_name,
    event_description,
    event_date,
    location,
    event_type,
    event_status
FROM events
WHERE event_status = 'upcoming'
ORDER BY event_date;

-- Event statistics view (accessible to president and admins)
CREATE VIEW event_statistics AS
SELECT 
    e.event_id,
    e.event_name,
    e.event_date,
    COUNT(DISTINCT a.attendee_id) as total_registrations,
    SUM(CASE WHEN a.attendance_status = 'attended' THEN 1 ELSE 0 END) as actual_attendance,
    AVG(f.rating) as average_rating,
    COUNT(DISTINCT f.feedback_id) as feedback_count
FROM events e
LEFT JOIN attendees a ON e.event_id = a.event_id
LEFT JOIN feedback f ON e.event_id = f.event_id
GROUP BY e.event_id, e.event_name, e.event_date;

-- Example stored procedure to check if a user is a president
DELIMITER //
CREATE PROCEDURE is_president(IN p_user_id INT, OUT is_pres BOOLEAN)
BEGIN
    SELECT role = 'president' INTO is_pres
    FROM users
    WHERE user_id = p_user_id;
END //
DELIMITER ;

-- Insert initial president user
INSERT INTO users (username, password, role, email, full_name, department)
VALUES ('president',SHA2( 'securepass123',256), 'president', 'president@organization.com', 'System President', 'Management');

-- CRUD Stored Procedures for Users Table

-- Create User
DELIMITER //
CREATE PROCEDURE create_user (
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255),
    IN p_role ENUM('president', 'admin', 'regular_user'),
    IN p_email VARCHAR(100),
    IN p_full_name VARCHAR(100),
    IN p_department VARCHAR(50)
)
BEGIN
    INSERT INTO users (username, password, role, email, full_name, department)
    VALUES (p_username, p_password, p_role, p_email, p_full_name, p_department);
END //
DELIMITER ;

-- Read User
DELIMITER //
CREATE PROCEDURE get_user_by_id (IN p_user_id INT)
BEGIN
    SELECT * FROM users WHERE user_id = p_user_id;
END //
DELIMITER ;

-- Update User
DELIMITER //
CREATE PROCEDURE update_user (
    IN p_user_id INT,
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255),
    IN p_role ENUM('president', 'admin', 'regular_user'),
    IN p_email VARCHAR(100),
    IN p_full_name VARCHAR(100),
    IN p_department VARCHAR(50)
)
BEGIN
    UPDATE users
    SET 
        username = p_username,
        password = p_password,
        role = p_role,
        email = p_email,
        full_name = p_full_name,
        department = p_department
    WHERE user_id = p_user_id;
END //
DELIMITER ;

-- Delete User
DELIMITER //
CREATE PROCEDURE delete_user (IN p_user_id INT)
BEGIN
    DELETE FROM users WHERE user_id = p_user_id;
END //
DELIMITER ;

-- CRUD Stored Procedures for Events Table

-- Create Event
DELIMITER //
CREATE PROCEDURE create_event (
    IN p_event_name VARCHAR(100),
    IN p_event_description TEXT,
    IN p_event_date DATETIME,
    IN p_registration_deadline DATETIME,
    IN p_location VARCHAR(100),
    IN p_capacity INT,
    IN p_event_type ENUM('conference', 'workshop', 'seminar', 'meeting', 'other'),
    IN p_created_by INT
)
BEGIN
    INSERT INTO events (event_name, event_description, event_date, registration_deadline, location, capacity, event_type, created_by)
    VALUES (p_event_name, p_event_description, p_event_date, p_registration_deadline, p_location, p_capacity, p_event_type, p_created_by);
END //
DELIMITER ;

-- Read Event
DELIMITER //
CREATE PROCEDURE get_event_by_id (IN p_event_id INT)
BEGIN
    SELECT * FROM events WHERE event_id = p_event_id;
END //
DELIMITER ;

-- Update Event
DELIMITER //
CREATE PROCEDURE update_event (
    IN p_event_id INT,
    IN p_event_name VARCHAR(100),
    IN p_event_description TEXT,
    IN p_event_date DATETIME,
    IN p_registration_deadline DATETIME,
    IN p_location VARCHAR(100),
    IN p_capacity INT,
    IN p_event_type ENUM('conference', 'workshop', 'seminar', 'meeting', 'other')
)
BEGIN
    UPDATE events
    SET 
        event_name = p_event_name,
        event_description = p_event_description,
        event_date = p_event_date,
        registration_deadline = p_registration_deadline,
        location = p_location,
        capacity = p_capacity,
        event_type = p_event_type
    WHERE event_id = p_event_id;
END //
DELIMITER ;

-- Delete Event
DELIMITER //
CREATE PROCEDURE delete_event (IN p_event_id INT)
BEGIN
    DELETE FROM events WHERE event_id = p_event_id;
END //
DELIMITER ;

-- View the users table
SELECT * FROM users;