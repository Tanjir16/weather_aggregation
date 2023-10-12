#!/bin/bash

CLASSPATH=".;bin;lib/*"

#TestCase3
# Starting the Aggregation Server
echo "Starting AggregationServer..."
java -cp $CLASSPATH AggregationServer 5002 &
AGGREGATION_SERVER_PID=$!
sleep 2

# Starting the Content Server with both weather_data.txt and weather_data2.txt
echo "Starting ContentServer with both weather_data.txt and weather_data2.txt..."
java -cp $CLASSPATH ContentServer 5001 src/weather_data.txt src/weather_data2.txt &
CONTENT_SERVER_PID=$!
sleep 60  # Adjust the sleep duration as needed

# Killing the ContentServer after a delay
echo "Stopping ContentServer after processing some data..."
kill -STOP $CONTENT_SERVER_PID
sleep 5

# Print the process to check its status; can remove after confirming it works
ps -p $CONTENT_SERVER_PID


# First GETClient request
echo "Starting GETClient for the first time..."
firstResponse=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
echo "First Response: $firstResponse"
sleep 10

# Second GETClient request after 30 seconds delay
echo "Waiting for 30 seconds before starting GETClient for the second time..."
sleep 30
secondResponse=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
echo "Second Response: $secondResponse"

# Checking if the second response is empty, and considering the test passed in that case
if [ -z "$secondResponse" ]; then
    echo "Test PASSED - No data received in the second request"
else
    echo "Test FAILED - Data received in the second request"
fi

# Cleanup - killing all processes
echo "Killing processes..."
kill $AGGREGATION_SERVER_PID
