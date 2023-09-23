# Compiler
JAVAC = javac

# Source Files
GETCLIENT_SRC = src/GETClient.java
AGGREGATION_SRC = src/AggregationServer.java
LAMPORT_SRC = src/LamportClock.java
CONTENT_SRC = src/ContentServer.java

# Output Directories
BIN = bin/

# Libraries
LIBS = lib/
JARS = $(LIBS)Java-WebSocket-1.5.4.jar:$(LIBS)json-20230618.jar:$(LIBS)slf4j-api-2.0.9.jar:$(LIBS)slf4j-simple-2.0.9.jar
CLASSPATH = ".;$(BIN);$(JARS)"

# Targets
all: directories getclient aggregation content

directories:
	mkdir -p $(BIN)

getclient:
	$(JAVAC) -d $(BIN) -cp $(CLASSPATH) $(GETCLIENT_SRC)

aggregation:
	$(JAVAC) -d $(BIN) -cp $(CLASSPATH) $(AGGREGATION_SRC) $(LAMPORT_SRC)

content:
	$(JAVAC) -d $(BIN) -cp $(CLASSPATH) $(CONTENT_SRC) $(LAMPORT_SRC)

clean:
	rm -rf $(BIN)

run-getclient:
	java -cp $(CLASSPATH) GETClient

run-aggregation:
	java -cp $(CLASSPATH) AggregationServer

run-content:
	java -cp $(CLASSPATH) ContentServer

.PHONY: all directories getclient aggregation content clean run-getclient run-aggregation run-content
