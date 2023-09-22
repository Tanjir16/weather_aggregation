# Compiler
JAVAC = javac

# Source Files
SRC_DIR = src/
BIN_DIR = bin/
LIB_DIR = ../lib/
GETCLIENT_SRC = $(SRC_DIR)GETClient.java
AGGREGATION_SRC = $(SRC_DIR)AggregationServer.java
LAMPORT_SRC = $(SRC_DIR)LamportClock.java
CONTENT_SRC = $(SRC_DIR)ContentServer.java

# Determine OS type for separator in CLASSPATH
ifeq ($(OS),Windows_NT)
    SEP = ;
else
    SEP = :
endif

# Libraries
JARS = $(LIB_DIR)Java-WebSocket-1.5.4.jar$(SEP)$(LIB_DIR)json-20230618.jar$(SEP)$(LIB_DIR)slf4j-api-2.0.9.jar$(SEP)$(LIB_DIR)slf4j-simple-2.0.9.jar
CLASSPATH = .$(SEP)$(BIN_DIR)$(SEP)$(JARS)

# Targets
all: directories getclient aggregation content

directories:
	if not exist $(BIN_DIR) mkdir $(BIN_DIR)

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
