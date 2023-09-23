# Weather Aggregation System

## Overview

This project simulates a weather aggregation system that gathers data from multiple content servers. The aggregation server then uses the Lamport clock to decide which content to serve upon a GET request.

## File Breakdown

1. **`GETClient.java`**: 
    - Represents a client requesting the latest weather data.
    - Sends a GET request to the `AggregationServer`.
    - Receives and prints the most recent weather data.

2. **`ContentServer.java`**:
    - Represents a content server sending weather data to the `AggregationServer`.
    - Sends a PUT request to the `AggregationServer` with the latest weather data and a Lamport clock value.

3. **`AggregationServer.java`**:
    - Listens for incoming PUT requests from content servers and GET requests from clients.
    - Uses the Lamport clock to determine the order of received data.
    - Serves the most recent weather data upon a GET request.
    - Has a shutdown hook to create a backup of the most recent data when the server is interrupted.

4. **`LamportClock.java`**:
    - Represents a Lamport logical clock.
    - Provides methods to increment the clock and adjust its value based on messages from other processes.

## Key Features

### 1. HTTP Protocol

- The system uses a simplified HTTP protocol for communication.
- The `ContentServer` sends data using a PUT request, while the `GETClient` fetches data using a GET request.
- The `AggregationServer` listens to these requests, processes them accordingly, and sends appropriate HTTP responses.

### 2. Lamport Clock Sync and Implementation

- Lamport clocks are logical clocks used to order events in a distributed system.
- Each `ContentServer` has its instance of a Lamport clock, which it increments and sends along with its data to the `AggregationServer`.
- The `AggregationServer` uses these clock values to order the incoming data, ensuring the latest data is determined by the logical order rather than the actual time of receipt.
- This implementation prevents issues that arise due to unsynchronized system clocks across different servers.

### 3. Testing

To test the system:

1. Start the `AggregationServer` using the Makefile command: `mingw32-make run-aggregation`.
2. Start multiple instances of `ContentServer` using the Makefile command: `mingw32-make run-content`. This will simulate different content servers sending data.
3. Start the `GETClient` using the Makefile command: `mingw32-make run-getclient` to fetch the latest weather data.
4. Observe the output on the `GETClient` terminal to verify that it receives the most recent data according to the Lamport clock order.

## Lamport Clock - Deep Dive

A Lamport Clock is a logical clock, devised by Leslie Lamport, used to order events in a distributed system without relying on synchronized physical clocks between processes.

**Working:**

1. **Increment**: Every time a process (in our case, the `ContentServer`) does work, it increments its local Lamport Clock.

2. **Sending a Message**: When a process sends a message (data in our scenario), it includes its current Lamport clock value in the message.

3. **Receiving a Message**: When a message is received, the receiver (the `AggregationServer` in our scenario) sets its clock to be the maximum of its current clock and the received clock from the message, and then it increments its clock by 1.

This ensures that the order of events can be determined based on their logical clocks, not the actual time they happened.

## Planning on Testing

1. **Initial Tests**:
    - Start the aggregation server and multiple content servers.
    - Observe how the aggregation server processes and orders the incoming data.
    - Use a GET client to fetch the latest data and verify its accuracy.

2. **Lamport Clock Testing**:
    - Deliberately introduce delays in sending data from some content servers to observe how the Lamport clock keeps the logical order intact.

3. **Backup Testing**:
    - After running the servers and sending some data, manually interrupt the aggregation server (CTRL+C).
    - Ensure the backup file (`backup.json`) is correctly created with the most recent data.

4. **Fault Tolerance**:
    - Ensure that if a content server doesn't send data for a certain period, its previous data is removed from consideration for serving to GET clients.
![Description of Image](https://github.com/Tanjir16/weather_aggregation/blob/main/CaptureFF.JPG)
