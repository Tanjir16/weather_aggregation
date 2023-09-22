# Compiler
JAVAC = javac

# Source Files
GETCLIENT_SRC = C:/Users/Tanjir Ahmed/Desktop/weather_aggregation/weather_aggregation/src/GETClient.java
AGGREGATION_SRC = src/AggregationServer.java
LAMPORT_SRC = src/LamportClock.java
CONTENT_SRC = src/ContentServer.java

# Output Directories
BIN_DIR = bin/

# Libraries
LIBS = C:/Users/Tanjir Ahmed/Desktop/weather_aggregation/weather_aggregation/lib/
JARS = $(LIBS)Java-WebSocket-1.5.4.jar;$(LIBS)json-20230618.jar;$(LIBS)slf4j-api-2.0.9.jar;$(LIBS)slf4j-simple-2.0.9.jar
CLASSPATH = ".;$(BIN_DIR);$(JARS)"

# Targets
all: directories getclient aggregation content

directories:
	mkdir -p $(BIN_DIR)

getclient: directories
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(GETCLIENT_SRC)

aggregation: directories
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(AGGREGATION_SRC) $(LAMPORT_SRC)

content: directories
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
