# Compiler
JAVAC = javac

# Base Directories
BASE_DIR = E:/weather_aggregation/weather_aggregation-3/
SRC_DIR = $(BASE_DIR)src/
LIB_DIR = $(BASE_DIR)lib/
BIN_DIR = $(BASE_DIR)bin/

# Source Files
GETCLIENT_SRC = $(SRC_DIR)GETClient.java
AGGREGATION_SRC = $(SRC_DIR)AggregationServer.java
LAMPORT_SRC = $(SRC_DIR)LamportClock.java
CONTENT_SRC = $(SRC_DIR)ContentServer.java

# Libraries
JARS = $(LIB_DIR)Java-WebSocket-1.5.4.jar;$(LIB_DIR)json-20230618.jar;$(LIB_DIR)slf4j-api-2.0.9.jar;$(LIB_DIR)slf4j-simple-2.0.9.jar
CLASSPATH = ".;$(BIN_DIR);$(JARS)"

# Targets
all: directories getclient aggregation content

directories:
	mkdir -p $(BIN_DIR)

getclient: 
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(GETCLIENT_SRC)

aggregation: 
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(AGGREGATION_SRC) $(LAMPORT_SRC)

content:
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(CONTENT_SRC) $(LAMPORT_SRC)

clean:
	rm -rf $(BIN_DIR)

run-getclient:
	java -cp $(CLASSPATH) GETClient

run-aggregation:
	java -cp $(CLASSPATH) AggregationServer

run-content:
	java -cp $(CLASSPATH) ContentServer

.PHONY: all directories getclient aggregation content clean run-getclient run-aggregation run-content
