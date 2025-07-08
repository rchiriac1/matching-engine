# matching-engine
Real-time order matching engine implemented in Java, inspired by real-world trading systems.

This project simulates the core functionality of a financial exchange's matching engine. It supports:

- Limit Orders (GTC, IOC, FOK)

- Market Orders

- Priority Queues for buy/sell matching (price-time priority)

- engine.Order Book Management with efficient insertion, cancellation, and matching

TO DOs: 
- Spring Boot REST API for submitting and querying orders

- Thread-safe implementation using synchronization primitives

- Unit tests to validate order-matching behavior

This is a foundational component of any electronic trading platform. I built it to deepen my understanding of concurrent data structures, low-latency trading logic, and backend system design.
