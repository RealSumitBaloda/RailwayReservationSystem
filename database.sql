-- =========================================================
-- PLATRA Railway Reservation System - Database Setup Script
-- =========================================================

CREATE DATABASE IF NOT EXISTS railway_reservation;
USE railway_reservation;

-- ---------------------------------------------------------
-- 1. USERS TABLE
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) DEFAULT 'System User',
    role VARCHAR(20) DEFAULT 'admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default Admin User (username: admin, password: 1234)
INSERT INTO users (username, password, full_name, role)
VALUES ('admin', '1234', 'PLATRA Administrator', 'admin')
ON DUPLICATE KEY UPDATE username = username;

-- ---------------------------------------------------------
-- 2. TRAINS TABLE
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS trains (
    train_number VARCHAR(10) PRIMARY KEY,
    train_name VARCHAR(100) NOT NULL,
    source VARCHAR(50) NOT NULL,
    destination VARCHAR(50) NOT NULL,
    departure_time VARCHAR(20) NOT NULL,
    arrival_time VARCHAR(20) NOT NULL,
    duration VARCHAR(20) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    base_fare INT NOT NULL,
    status VARCHAR(50) DEFAULT 'On Time'
);

-- Populate Trains
INSERT INTO trains (train_number, train_name, source, destination, departure_time, arrival_time, duration, total_seats, available_seats, base_fare, status)
VALUES
('12951', 'Mumbai Rajdhani', 'Mumbai', 'New Delhi', '17:00', '08:35', '15h 35m', 120, 48, 1450, 'On Time'),
('12009', 'Shatabdi Express', 'Mumbai', 'Ahmedabad', '06:25', '11:10', '4h 45m', 100, 32, 950, 'On Time'),
('12957', 'Swarna Jayanti Rajdhani', 'Ahmedabad', 'New Delhi', '18:20', '07:35', '13h 15m', 120, 65, 1350, 'On Time'),
('12423', 'Dibrugarh Rajdhani', 'New Delhi', 'Dibrugarh', '16:10', '07:00', '38h 50m', 150, 91, 2100, 'Delayed 10 min'),
('12904', 'Golden Temple Mail', 'Mumbai', 'Amritsar', '18:45', '22:30', '27h 45m', 160, 73, 1250, 'On Time'),
('12431', 'Rajdhani Express', 'New Delhi', 'Thiruvananthapuram', '11:00', '14:00', '51h 00m', 180, 110, 2400, 'On Time'),
('12910', 'Garib Rath', 'Bengaluru', 'New Delhi', '21:30', '18:45', '45h 15m', 180, 57, 1100, 'On Time'),
('12015', 'Ajmer Shatabdi', 'New Delhi', 'Ajmer', '06:05', '12:45', '6h 40m', 100, 41, 850, 'On Time'),
('12926', 'Paschim Express', 'Amritsar', 'Mumbai', '08:10', '13:45', '29h 35m', 150, 83, 1300, 'On Time'),
('12860', 'Gitanjali Express', 'Mumbai', 'Kolkata', '06:00', '10:00', '28h 00m', 160, 76, 1400, 'On Time')
ON DUPLICATE KEY UPDATE train_name = VALUES(train_name);

-- ---------------------------------------------------------
-- 3. BOOKINGS TABLE
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    pnr VARCHAR(20) PRIMARY KEY,
    train_number VARCHAR(10) NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    seats INT NOT NULL,
    fare_class VARCHAR(20) NOT NULL,
    total_fare DOUBLE NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (train_number) REFERENCES trains(train_number) ON DELETE CASCADE
);
