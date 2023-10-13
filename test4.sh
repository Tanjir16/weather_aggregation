#!/bin/bash

CLASSPATH=".;bin;lib/*"

# TestCase3
# Starting the Aggregation Server
echo "Starting AggregationServer..."
java -cp $CLASSPATH AggregationServer 5000 5001 &
AGGREGATION_SERVER_PID=$!
sleep 2

# Starting the Content Server to send data
echo "Starting ContentServer..."
java -cp $CLASSPATH ContentServer 5001 &
CONTENT_SERVER_PID=$!
sleep 5  # Allow Content Server to send data

# Killing the Content Server
echo "Killing ContentServer..."
taskkill //PID $CONTENT_SERVER_PID //F


# First GET request from the client (within 5 seconds)
echo "First GET request from the client (within 5 seconds):"
java -cp $CLASSPATH GETClient 5002 IDS1877550 &
CLIENT_PID=$!
sleep 5  # Allow Client to make the first GET request

# Second GET request from the client (after 30 seconds)
echo "Waiting for 10 seconds before the second GET request..."
sleep 10
OUTPUT=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
CLIENT_PID2=$!
sleep 5  # Allow Client to make the second GET request

# Check if the second GET request received empty data
if [ -z "$OUTPUT" ]; then
    echo "Test Failed: Multiple GET client received empty data."
else
    echo "Test Passed: Multiple GET client received data."
fi

# Killing all processes for cleanup
echo "Killing all processes..."
kill -9 $AGGREGATION_SERVER_PID
wait $AGGREGATION_SERVER_PID 2>/dev/null
kill -9 $CLIENT_PID
wait $CLIENT_PID 2>/dev/null
kill -9 $CLIENT_PID2
wait $CLIENT_PID2 2>/dev/null
echo "All processes killed, test completed."
