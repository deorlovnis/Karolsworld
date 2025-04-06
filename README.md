# Karol the Robot

A JavaFX-based educational program that simulates a robot named Karol moving in a grid world. This program is designed to help students learn programming concepts through visual representation.

## Features

- Visual grid-based world representation
- Robot movement simulation
- JavaFX-based user interface
- Educational tool for learning programming concepts

## Requirements

- Java 23 or later
- Maven 3.8.1 or later
- JavaFX 23

## Installation

1. Clone the repository:
```bash
git clone https://github.com/deorlovnis/karoltherobot.git
cd karoltherobot
```

2. Build the project using Maven:
```bash
mvn clean package
```

## Running the Program

### Using Maven
```bash
mvn clean javafx:run
```

### Using the JAR file
```bash
java --module-path target/karol-the-robot-1.0-SNAPSHOT.jar --add-modules javafx.controls,javafx.fxml -jar target/karol-the-robot-1.0-SNAPSHOT.jar
```

## Project Structure

- `src/main/java/com/karol/`
  - `Main.java` - Main application entry point
  - `Robot.java` - Robot implementation
  - `World.java` - World grid implementation

## Dependencies

- JavaFX 23
- Maven build system

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. 