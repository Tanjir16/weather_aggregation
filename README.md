# Weather Aggregation System

Weather Aggregation System is a simulated project designed to aggregate weather data from multiple content servers, order the data using the Lamport clock, and serve the most recent data to clients upon request.

## Table of Contents
- [Overview](#overview)
- [File Breakdown](#file-breakdown)
- [Key Features](#key-features)
- [Lamport Clock - Deep Dive](#lamport-clock---deep-dive)
- [Testing](#testing)
- [Planning on Testing](#planning-on-testing)

## Overview
This project consists of multiple content servers sending data to an aggregation server. The aggregation server uses a Lamport clock to order the received data and serves the most recent data to clients upon a GET request.

![System Architecture](https://github.com/Tanjir16/weather_aggregation/blob/main/CaptureFF.JPG)

## File Breakdown

### `GETClient.java`
- Represents a client that sends GET requests.
- Fetches and prints the most recent weather data from the `AggregationServer`.

### `ContentServer.java`
- Represents a content server.
- Sends weather data and a Lamport clock value to the `AggregationServer` via PUT requests.

### `AggregationServer.java`
- Handles incoming PUT and GET requests.
- Uses a Lamport clock to order the received data.
- Has a shutdown hook to backup the most recent data when interrupted.

### `LamportClock.java`
- A class for the Lamport logical clock.
- Helps in ordering events in the distributed system.

## Key Features

### HTTP Protocol
The system adopts a simplified version of the HTTP protocol for communication. Content servers utilize PUT requests to transmit data, and clients use GET requests to retrieve the latest information. 

### Lamport Clock Sync and Implementation
The system employs Lamport clocks to logically order events in a distributed environment, avoiding complications arising from unsynchronized system clocks across different servers.

### Testing 
Utilize the provided Makefile commands to initiate the `AggregationServer`, multiple `ContentServer` instances, and the `GETClient`. Observe the `GETClient` terminal to confirm the retrieval of the latest data based on Lamport clock order.

## Lamport Clock - Deep Dive
A mechanism to order events logically in a distributed system without the need for synchronized physical clocks. It ensures a consistent order of events across all participating entities.

## Testing

### Initial Tests
- Initiate the aggregation server and multiple content servers.
- Observe the data processing and ordering by the aggregation server.
- Use a GET client to fetch and verify the latest data.

### Lamport Clock Testing
- Introduce delays in some content servers and observe how the Lamport clock maintains the logical order.

### Backup Testing
- Interrupt the aggregation server and check if the `backup.json` file is created with the recent data.

### Fault Tolerance
- Test the system’s ability to exclude data from servers that haven't updated within a specified period.

## Planning on Testing
1. **Unit Tests**: Ensure each module functions as intended individually.
2. **Integration Tests**: Test the system’s overall functionality and the interaction between different modules.
3. **Performance Testing**: Evaluate the system’s performance, focusing on response times and data accuracy.

---
**Note:** For a deeper understanding and hands-on experience, clone this repository and follow the instructions to execute various tests.

**Happy Coding!**
