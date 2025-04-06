package com.karol.examples;

import com.karol.Karol;

/**
 * This class shows how you can extend Karol's functionality by creating your own methods.
 * You can use these methods in your programs to make them more readable and reusable.
 */
public class ExtendingKarol {
    
    /**
     * Example of how to create your own method that uses Karol's basic commands.
     * This method makes Karol move in a square pattern.
     * 
     * @param karol The robot to control
     * @param size The size of the square (number of steps per side)
     */
    public static void moveInSquare(Karol karol, int size) {
        // Move in a square pattern
        for (int i = 0; i < 4; i++) {
            // Move forward 'size' steps
            for (int j = 0; j < size; j++) {
                karol.move();
            }
            // Turn right to make a corner
            karol.turnRight();
        }
    }

    /**
     * Example of how to create a method that checks multiple conditions.
     * This method checks if Karol is in a corner (has walls on two sides).
     * 
     * @param karol The robot to check
     * @return true if Karol is in a corner, false otherwise
     */
    public static boolean isInCorner(Karol karol) {
        // Check if there's a wall in front
        boolean frontWall = !karol.frontIsClear();
        
        // Turn right to check the side
        karol.turnRight();
        boolean sideWall = !karol.frontIsClear();
        
        // Turn back to original direction
        karol.turnLeft();
        
        // Return true only if both front and side have walls
        return frontWall && sideWall;
    }

    /**
     * Example of how to create a method that uses Karol's state.
     * This method makes Karol move to the nearest wall in any direction.
     * 
     * @param karol The robot to control
     */
    public static void moveToNearestWall(Karol karol) {
        // Try each direction
        for (int i = 0; i < 4; i++) {
            if (!karol.frontIsClear()) {
                // Found a wall in this direction
                return;
            }
            karol.turnRight();
        }
        
        // If we get here, there are no walls in any direction
        throw new IllegalStateException("No walls found in any direction!");
    }
} 