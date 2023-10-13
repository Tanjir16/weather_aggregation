#!/bin/bash
#Test Cases for data deletion after 30 seconds// Second GetClient did not receive data as its removed from the Aggregation Server after 30 seconds
CLASSPATH=".;bin;lib/*"

echo "Starting AggregationServer..."
java -cp $CLASSPATH AggregationServer 5002 5001 &
AGGREGATION_SERVER_PID=$!
sleep 5

echo "Starting ContentServer..."
java -cp $CLASSPATH ContentServer 5001 "src/weather_data.txt" "src/weather_data2.txt" &
CONTENT_SERVER_PID=$!
sleep 5

client1Data=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
echo "First GET request from the client:"
echo "$client1Data"
sleep 5

client2Data=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
echo "Second GET request from the client:"
echo "$client2Data"
sleep 5

if [ -n "$client1Data" ] && [[ "$client1Data" == *IDS1877550* ]] &&
   [ -n "$client2Data" ] && [[ "$client2Data" == *IDS1877550* ]]; then
  echo "Test Failed: Both GET clients received data after 30 second."
else
  echo "Test Passed: One of the GET clients did not receive data after 30 seconds."
fi
echo "Killing all processes..."
kill -9 $AGGREGATION_SERVER_PID
kill -9 $CONTENT_SERVER_PID
echo "All processes killed, test completed."
