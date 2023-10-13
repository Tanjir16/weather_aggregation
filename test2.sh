# TEST CASE 2
echo "TEST CASE 2: Multiple Content Servers Sending Data Simultaneously with Different Lamport Times & Checking the last data sent to aggregation server"
echo "Starting AggregationServer..."
java -cp $CLASSPATH AggregationServer 5002 &
AGGREGATION_SERVER_PID=$!
sleep 2

echo "Starting First ContentServer..."
java -cp $CLASSPATH ContentServer 5001 src/weather_data.txt &
FIRST_CONTENT_SERVER_PID=$!
sleep 2

echo "Starting Second ContentServer..."
java -cp $CLASSPATH ContentServer 5001 src/weather_data2.txt &
SECOND_CONTENT_SERVER_PID=$!
sleep 2

echo "Starting Modify..."
java -cp $CLASSPATH Modify &
MODIFY_PID=$!
sleep 2

echo "Waiting for 60 seconds before starting GETClient..."
sleep 10

# Starting First GETClient...
firstResponse=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
firstLamportClock=$(echo $firstResponse | grep -oP '"LamportClock":\K\d+')
echo "First Lamport Clock: $firstLamportClock"
echo "Debug: First Response: $firstResponse"   # Print the entire response
sleep 10

# Starting Second GETClient after a delay...
sleep 20  # Introducing a delay to allow potential updates
secondResponse=$(java -cp $CLASSPATH GETClient 5002 IDS1877550)
secondLamportClock=$(echo $secondResponse | grep -oP '"LamportClock":\K\d+')
echo "Second Lamport Clock: $secondLamportClock"
echo "Debug: Second Response: $secondResponse"  # Print the entire response
sleep 5

# Debugging lines to print out the variables, can be removed after debugging
echo "Debug: First Lamport Clock: '$firstLamportClock'"
echo "Debug: Second Lamport Clock: '$secondLamportClock'"

# Modification starts here
# Trim whitespace characters (like newline) from the variables
firstLamportClock=$(echo "$firstLamportClock" | tr -d '[:space:]')
secondLamportClock=$(echo "$secondLamportClock" | tr -d '[:space:]')
# Modification ends here

# Ensure the variables contain integers before attempting to compare them
if [[ "$firstLamportClock" =~ ^[0-9]+$ && "$secondLamportClock" =~ ^[0-9]+$ ]]; then
  if [ "$firstLamportClock" -lt "$secondLamportClock" ]; then
    echo "Test case 2 PASSED"
  else
    echo "Test case 2 FAILED"
  fi
else
  echo "Error: Non-integer value detected."
  echo "First Lamport Clock: '$firstLamportClock'"
  echo "Second Lamport Clock: '$secondLamportClock'"
fi

# Cleanup
echo "Killing processes..."
kill $AGGREGATION_SERVER_PID
kill $FIRST_CONTENT_SERVER_PID
kill $SECOND_CONTENT_SERVER_PID
kill $MODIFY_PID
