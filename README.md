# Karol the Robot

A JavaFX-based educational program that simulates a robot named Karol moving in a grid world. This program is designed to help students learn programming concepts through visual representation.

## About

Karol the Robot is inspired by the classic "Karel the Robot" educational programming language, which was created by Richard E. Pattis in 1981. This JavaFX implementation provides a modern, visual interface for learning fundamental programming concepts.

## Features

- Visual grid-based world representation
- Robot movement simulation with wall detection
- Beeper placement and collection
- World editor for creating custom assignments
- Assignment management system
- Java program execution environment
- JavaFX-based user interface
- Educational tool for learning programming concepts

## Requirements

- Java 17 or later
- Maven 3.8.1 or later
- JavaFX 17

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

## Writing Programs for Karol

Karol programs are written in Java and must implement the `KarolProgram` interface. Here's an example program:

```java
package com.karol.userprograms;

import com.karol.KarolProgram;
import com.karol.Karol;

public class MyProgram implements KarolProgram {
    @Override
    public void run(Karol karol) {
        // Move forward until a wall is in front
        while (karol.frontIsClear()) {
            karol.move();
        }
        
        // Turn left and pick up a beeper if present
        karol.turnLeft();
        if (karol.beeperPresent()) {
            karol.pickBeeper();
        }
    }
}
```

### Available Robot Commands

- `move()` - Move forward one step
- `turnLeft()` - Turn 90 degrees to the left
- `turnRight()` - Turn 90 degrees to the right
- `pickBeeper()` - Pick up a beeper from the current position
- `putBeeper()` - Put down a beeper at the current position
- `frontIsClear()` - Check if the path ahead is clear
- `beeperPresent()` - Check if there's a beeper at the current position
- `getBeepersInBag()` - Get the number of beepers in the robot's bag
- `hasBeeper()` - Check if the robot has any beepers in its bag
- `moveUntilWall()` - Move forward until a wall is encountered
- `turnAround()` - Turn 180 degrees
- `moveSteps(int steps)` - Move forward a specified number of steps

## Project Structure

- `src/main/java/com/karol/`
  - `Main.java` - Main application entry point and UI
  - `Karol.java` - Robot implementation
  - `World.java` - World grid implementation
  - `WorldView.java` - JavaFX visualization
  - `WorldEditor.java` - World editor implementation
  - `Assignment.java` - Assignment data structure
  - `AssignmentLoader.java` - Assignment file management
  - `Robot.java` - Robot data structure
  - `Wall.java` - Wall implementation
  - `Beeper.java` - Beeper implementation
  - `KarolProgram.java` - Interface for user programs
  - `ProgramExecutor.java` - Program compilation and execution

## Dependencies

- JavaFX 17
- Jackson Databind 2.16.1
- JUnit 5.10.1 (for testing)
- Maven build system

## Credits

This project is based on the original "Karel the Robot" concept by Richard E. Pattis. The JavaFX implementation was created as an educational tool to help students learn programming concepts in a visual and interactive way.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## Acknowledgments

- Richard E. Pattis for creating the original Karel the Robot concept
- The JavaFX team for providing the UI framework
- All contributors who have helped improve this project 