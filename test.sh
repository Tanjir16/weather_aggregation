#!/bin/bash

CLASSPATH=".;bin;lib/*"

echo "Starting AggregationServer..."
java -cp $CLASSPATH AggregationServer 5002 &
AGGREGATION_SERVER_PID=$!
sleep 2

echo "Starting ContentServer..."
java -cp $CLASSPATH ContentServer 5001 src/weather_data.txt src/weather_data2.txt &
CONTENT_SERVER_PID=$!
sleep 2

echo "Starting Modify..."
java -cp $CLASSPATH Modify &
MODIFY_PID=$!
sleep 2

echo "Starting GETClient..."
java -cp $CLASSPATH GETClient 5002 IDS1877550 &
GETCLIENT_PID=$!
sleep 2

# HTTP Request
echo "Executing HTTP request..."
response=$(curl -s "http://localhost:5002/?stationId=IDS60901")

echo "Response from HTTP server:"
echo $response

# Basic validation logic to check if the response contains a specified string
# Replace 'apparent_t' with the actual expected string in the HTTP response
expected_string="apparent_t"
if [[ $response == *"$expected_string"* ]]; then
  echo "Test case PASSED"
else
  echo "Test case FAILED"
fi

# Cleanup
echo "Killing processes..."
kill $AGGREGATION_SERVER_PID
kill $CONTENT_SERVER_PID
kill $MODIFY_PID
kill $GETCLIENT_PID


