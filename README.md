# Matching Engine
A real-time order matching engine implemented in Java, inspired by real-world trading systems.

This project simulates the core functionality of a financial exchange's matching engine and currently supports:

## Features
Limit Orders with support for:

- GTC (Good Till Cancel)

- IOC (Immediate or Cancel)

- FOK (Fill or Kill)

- Price-Time Priority matching via priority queues

- Efficient buy/sell order book management

- Matching logic for full and partial fills

- Expiration, update, and cancellation of orders

## To Do
⏳ Spring Boot REST API for submitting and querying orders (in progress)

⏳ Thread-safe design with synchronization primitives

⏳ Unit and integration tests to validate behavior under load

This is a foundational component of any electronic trading platform. I built it to deepen my understanding of:

- Concurrent data structures

- Algorithmics 

- Low-latency trading logic

- Backend system architecture and API design
