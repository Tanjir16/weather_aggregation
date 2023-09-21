# Compiler
JAVAC = javac

# Source Files
GETCLIENT_SRC = GETClient.java
AGGREGATION_SRC = AggregationServer.java
LAMPORT_SRC = LamportClock.java
CONTENT_SRC = ContentServer.java

# Output Directories
BIN = bin/

# Libraries
LIBS = ../lib/
JARS = $(LIBS)Java-WebSocket-1.5.4.jar;$(LIBS)json-20230618.jar;$(LIBS)slf4j-api-2.0.9.jar;$(LIBS)slf4j-simple-2.0.9.jar
CLASSPATH = ".;$(BIN);$(JARS)"

all: getclient aggregation content

getclient:
	mkdir -p $(BIN)
	$(JAVAC) -d $(BIN) -cp $(CLASSPATH) $(GETCLIENT_SRC)

aggregation:
	mkdir -p $(BIN)
	$(JAVAC) -d $(BIN) -cp $(CLASSPATH) $(AGGREGATION_SRC) $(LAMPORT_SRC)

content:
	mkdir -p $(BIN)
	$(JAVAC) -d $(BIN) -cp $(CLASSPATH) $(CONTENT_SRC) $(LAMPORT_SRC)

clean:
	rm -rf $(BIN)

run-getclient:
	java -cp $(CLASSPATH) GETClient

run-aggregation:
	java -cp $(CLASSPATH) AggregationServer

run-content:
	java -cp $(CLASSPATH) ContentServer
