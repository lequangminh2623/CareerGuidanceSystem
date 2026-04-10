# Career Guidance System

This repository contains the source code for a comprehensive career guidance and student management platform. The system combines a microservices backend, a web-based portal for users, and hardware components for attendance tracking.

## Project Overview

The main goal of this system is to help students identify suitable career paths through assessment tools like the Holland Code (RIASEC). Beyond guidance, it also provides schools with tools to manage student data, track attendance via IoT devices, and enable real-time communication between users.

## System Architecture

The project is divided into three main parts:

- **Frontend**: A web application built with Next.js that serves as the primary interface for students and administrators.
- **Backend**: A set of Java microservices using Spring Boot and Spring Cloud for handling business logic, data persistence, and service orchestration.
- **IoT/Hardware**: ESP32-based devices used for physical attendance tracking, communicating with the backend over MQTT.

## Technology Stack

### Backend
- **Frameworks**: Spring Boot, Spring Cloud (Eureka for discovery, API Gateway for routing).
- **Databases**: PostgreSQL for primary storage and Redis for caching.
- **Infrastructure**: Docker and Docker Compose for containerization and deployment.
- **Messaging**: Mosquitto (MQTT) for hardware data and Firebase for real-time chat.

### Frontend
- **Framework**: Next.js (App Router) with TypeScript.
- **UI/UX**: Tailwind CSS for styling and Framer Motion for interface transitions.

### Hardware
- **Device**: ESP32 programmed with C++/Arduino.
- **Communication**: MQTT protocol for sending data to the attendance service.

## Core Features

- **Holland Code Assessment**: An automated tool to analyze student interests and suggest matching careers.
- **Microservices Architecture**: Decentralized services for users, chat, attendance, and academic records to ensure scalability.
- **Real-time Chat**: A messaging system integrated with Firebase for instant communication.
- **IoT Attendance tracking**: Hardware-based attendance system using ESP32 to monitor student presence automatically.
- **Academic Management**: Modules for tracking student scores and managing administrative data.

## Project Structure

- `careerguidanceweb/`: The Next.js frontend code.
- `careerguidanceapp/`: The microservices backend, including individual service folders like user-service, chat-service, and the api-gateway.
- `esp32project/`: Firmware for the attendance tracking hardware.
- `CrawlData/`: Scripts used for collecting and processing career-related data.
- `docker-compose.yml`: Configuration for running the entire infrastructure locally.

## Getting Started

### Prerequisites
You will need Java 17+, Node.js 18+, and Docker installed on your machine. For the hardware part, you'll need the Arduino IDE.

### Installation

1. **Clone the project**:
   ```bash
   git clone https://github.com/lequangminh2623/CareerGuidanceSystem.git
   ```

2. **Run the Backend**:
   Navigate to the `careerguidanceapp` folder and start the infrastructure using Docker:
   ```bash
   docker-compose up -d
   ```

3. **Run the Frontend**:
   Go to the `careerguidanceweb` folder, install dependencies, and start the development server:
   ```bash
   npm install
   npm run dev
   ```

4. **Hardware Setup**:
   The `esp32project` folder contains the `.ino` file for the ESP32. Make sure to update the credentials in `certs.h` before uploading.

## Configuration & Security

The project uses a root `.gitignore` to keep sensitive files like `.env` and private keys out of version control. Make sure to create your own environment configuration files based on the code requirements.

## Credits

This project was developed as part of a University Thesis.