# 🍕 Food Delivery Application

A comprehensive **Food Delivery System** built with Java, featuring REST API, SOAP Web Service (WSDL), SQLite database, and both Console & Web UI. Developed as the final project for **SWE 4302 - Object Oriented Concepts** lab.

---

## 📋 Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Architecture](#project-architecture)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [SOAP Web Service](#soap-web-service)
- [Web Interface](#web-interface)
- [Database Schema](#database-schema)
- [Project Structure](#project-structure)
- [OOP Principles](#oop-principles)
- [Contributors](#contributors)
- [License](#license)

---

## ✨ Features

### 👤 Customer Portal
- Register/Login with password hashing (SHA-256)
- Browse nearby restaurants based on delivery area
- Search restaurants by name or cuisine type
- View restaurant menus with prices and descriptions
- Add items to cart with quantity selection
- Apply discount coupons (SAVE10, SAVE20)
- Place orders (Cash on Delivery / Card Payment)
- Track order status (PLACED → PREPARING → READY → DELIVERED)
- View order history

### 🍽️ Restaurant Portal
- Register new restaurant (name, address, cuisine, contact, hours, delivery radius)
- Add menu items with price, description, stock quantity
- Update item availability (Available/Unavailable)
- Update stock quantity
- View incoming orders
- Update order status (Accept → Prepare → Ready)
- Assign delivery riders to ready orders

### 👑 Admin Portal (Console)
- View all registered users
- View all restaurants
- Manage orders across all restaurants

### 🌐 Web Interface
- Responsive HTML/CSS/JS frontend
- Customer portal with restaurant browsing, cart, and orders
- Restaurant portal with menu management and order handling
- Real-time updates via REST API calls

### 🔌 API & Web Services
- **REST API** on port 8000 (JSON responses)
- **SOAP Web Service** on port 8080 with WSDL contract
- CORS enabled for cross-origin requests

---

## 🛠️ Technologies Used

| Category | Technologies |
|----------|--------------|
| **Language** | Java 17+ |
| **Database** | SQLite (JDBC) |
| **REST API** | com.sun.net.httpserver |
| **SOAP/WSDL** | JAX-WS (jakarta.xml.ws) |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **Build Tool** | Maven (optional) |
| **Version Control** | Git & GitHub |




