# PLATRA - Smart Railway Reservation System (Java Swing + MySQL)

A modern, desktop-based Railway Reservation System developed in **Java Swing** connected directly with **MySQL Database** via JDBC.

---

## 📁 Project Structure

```
d:\College Project\
│
├── RailwayReservationSystem.java    # Main Swing GUI application & controller
├── DatabaseConnection.java          # JDBC Manager & auto schema initializer
├── db.properties                    # Database configuration (Host, Port, User, Password)
├── database.sql                     # Complete SQL schema & seed dataset
├── lib/
│   └── mysql-connector-j-8.3.0.jar  # Official MySQL JDBC Driver
├── compile.bat                      # 1-Click build script
├── run.bat                          # 1-Click execution script
└── README.md                        # Documentation
```

---

## 🗄️ Database Architecture

The system connects to database `railway_reservation` with three core tables:

1. **`users`**:
   - `id`, `username`, `password`, `full_name`, `role`, `created_at`
   - Default login: `admin` / `1234`

2. **`trains`**:
   - `train_number` (Primary Key), `train_name`, `source`, `destination`, `departure_time`, `arrival_time`, `duration`, `total_seats`, `available_seats`, `base_fare`, `status`
   - Pre-populated with 10 major express routes (Rajdhani, Shatabdi, Garib Rath, etc.)

3. **`bookings`**:
   - `pnr` (Primary Key), `train_number` (Foreign Key), `passenger_name`, `seats`, `fare_class`, `total_fare`, `booking_date`

---

## ⚙️ MySQL Configuration

Database credentials are stored in `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/railway_reservation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_ROOT_PASSWORD
```

> **Note**: You can also configure and test your MySQL credentials directly inside the application UI by clicking **"MySQL Config"** on the login screen or in the sidebar.

---

## 🚀 How to Run

### Method 1: Double-click Batch Files (Easiest)
1. Double-click `compile.bat` (if you modified any code).
2. Double-click `run.bat` to start the application.

### Method 2: Command Line
```powershell
# Navigate to project folder
cd "d:\College Project"

# Compile
javac -cp ".;lib/mysql-connector-j-8.3.0.jar" RailwayReservationSystem.java DatabaseConnection.java

# Run
java -cp ".;lib/mysql-connector-j-8.3.0.jar" RailwayReservationSystem
```

### Method 3: In IDEs (VS Code / IntelliJ / Eclipse)
1. Open folder `d:\College Project`.
2. Add `lib/mysql-connector-j-8.3.0.jar` to project libraries / classpath.
3. Run `RailwayReservationSystem.java`.

---

## 🔑 Default Credentials
- **Username:** `admin`
- **Password:** `1234`
