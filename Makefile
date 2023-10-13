# Compiler
JAVAC = javac
JAVA = java

# Source Files Directory
SRC_DIR = src

# Binaries Directory
BIN_DIR = bin

# Classpath including the JAR files and the binaries directory
CLASSPATH = ".;$(BIN_DIR);lib/*"

# Creating a list of source files
SRC_FILES = $(wildcard $(SRC_DIR)/*.java)

all: compile

# Creates the bin directory if it doesn’t exist
prepare:
	@mkdir -p $(BIN_DIR)

# Compiles the Java source files
compile: prepare
	$(JAVAC) -d $(BIN_DIR) -cp $(CLASSPATH) $(SRC_FILES)

# Running different components, adjust as per your specific needs
run-aggregation: compile
	$(JAVA) -cp $(CLASSPATH) AggregationServer $(filter-out $@,$(MAKECMDGOALS))

run-content: compile
	@echo "Using paths: $(filter-out $@,$(MAKECMDGOALS))"
	$(JAVA) -cp $(CLASSPATH) ContentServer $(filter-out $@,$(MAKECMDGOALS))

run-modify: compile
	$(JAVA) -cp $(CLASSPATH) Modify

run-getclient: compile
	$(JAVA) -cp $(CLASSPATH) GETClient $(filter-out $@,$(MAKECMDGOALS))

# Clean up the binaries directory
clean:
	@rm -rf $(BIN_DIR)

# This is a workaround to pass additional arguments with the command
%:
	@:

run-test: compile
	chmod +x test.sh
	./test.sh

run-test3: compile
	chmod +x test3.sh
	./test3.sh

run-test2: compile
	chmod +x test2.sh
	./test2.sh