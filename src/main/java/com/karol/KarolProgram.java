package com.karol;

/**
 * Interface for Karol programs that users can implement.
 * Each program must implement the run() method which contains the robot's instructions.
 */
public interface KarolProgram {
    /**
     * The main method that contains the robot's instructions.
     * @param karol The robot instance that will execute the program
     */
    void run(Karol karol);
} 