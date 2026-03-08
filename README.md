# Cab Simulator 🚖

A **Java Swing based Cab Booking Simulator** that mimics the basic workflow of real-world ride-hailing applications. The system allows users to **register, login, book cabs, calculate fares, assign drivers using multithreading, and view trip history**. The application connects to **MySQL using JDBC** for persistent storage.

---

# 📌 Features

### 1️⃣ User Authentication

* User **Signup**
* User **Login**
* Username validation to prevent duplicates
* Data stored securely in the database

### 2️⃣ Cab Booking

* Select **Pickup Location**
* Select **Drop Location**
* Calculate **Ride Fare**
* Book Cab instantly

### 3️⃣ Driver Assignment

* Drivers are managed using a **Driver Pool**
* Uses **Multithreading and BlockingQueue**
* Assigns the first available driver automatically
* Driver becomes available again after trip completion

### 4️⃣ Trip Simulation

* Simulated ride duration using **Threads**
* Automatically completes trip
* Generates trip record

### 5️⃣ Profile Management

* View user profile
* Access trip history

### 6️⃣ Trip History

* Displays previous trips in a **table format**
* Shows:

  * Trip ID
  * Driver
  * Pickup
  * Drop
  * Fare
  * Start Time
  * End Time

---

# 🛠 Technologies Used

* **Java**
* **Java Swing (GUI)**
* **JDBC**
* **MySQL**
* **Multithreading**
* **Collections Framework**
* **BlockingQueue**
* **PreparedStatement**

---

# 🗄 Database Setup

Create a database named:

```sql
CREATE DATABASE cabsimulator;
USE cabsimulator;
```

---

## Users Table

```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50),
    name VARCHAR(100),
    phone VARCHAR(20)
);
```

---

## Drivers Table

```sql
CREATE TABLE drivers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    available BOOLEAN DEFAULT TRUE
);
```

Example driver data:

```sql
INSERT INTO drivers(name,phone) VALUES
('Ramesh','9876543210'),
('Suresh','9876543211'),
('Mahesh','9876543212');
```

---

## Trips Table

```sql
CREATE TABLE trips (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    drivername VARCHAR(100),
    pickup VARCHAR(100),
    dropLoc VARCHAR(100),
    fare DOUBLE,
    startTime TIMESTAMP,
    endTime TIMESTAMP
);
```

---

# ⚙️ MySQL Configuration

Update database credentials in the `DB` class if required:

```java
String url = "jdbc:mysql://localhost:3306/cabsimulator";
String user = "root";
String pass = "your_password";
```

---

# ▶️ How to Run the Project

### 1️⃣ Install Requirements

* Java JDK
* MySQL Server
* MySQL Workbench

---

### 2️⃣ Add MySQL JDBC Driver

Download **MySQL Connector/J** and add it to the project libraries.

---

### 3️⃣ Compile the Program

```bash
javac CabSimulator.java
```

---

### 4️⃣ Run the Program

```bash
java cabsimulator.CabSimulator
```

---

# 🖥 Application Workflow

1. User opens the application
2. User **signs up or logs in**
3. User selects **pickup and drop location**
4. Fare is calculated
5. System assigns an **available driver**
6. Trip starts and ends automatically
7. Trip is stored in **MySQL database**
8. User can view **trip history**

---

# 🧠 Concepts Demonstrated

This project demonstrates several important Java concepts:

* **Object Oriented Programming**
* **JDBC Database Connectivity**
* **Multithreading**
* **Java Swing GUI**
* **Collections Framework**
* **BlockingQueue**
* **Exception Handling**

---

# 📂 Project Structure

```
cabsimulator
│
├── CabSimulator.java
│
├── User
├── Driver
├── DB
├── DriverPool
├── AuthManager
│
├── LoginFrame
├── SignupFrame
├── BookingFrame
├── ProfileFrame
└── HistoryFrame
```

---

# 🚀 Possible Future Improvements

* Add **Google Maps API integration**
* Implement **distance-based fare calculation**
* Add **driver login system**
* Add **admin dashboard**
* Add **payment gateway simulation**
* Add **real-time driver tracking**
* Improve **UI/UX design**

---

# 👨‍💻 Author

**Srikesav M**

---

# 📄 License

This project is for **educational and learning purposes**.
