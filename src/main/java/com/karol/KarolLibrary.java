package com.karol;

/**
 * Library of common commands and patterns for Karol programs.
 */
public class KarolLibrary {
    /**
     * Turns Karol right by making three left turns.
     */
    public static void turnRight(Karol karol) {
        for (int i = 0; i < 3; i++) {
            karol.turnLeft();
        }
    }

    /**
     * Makes Karol turn around (180 degrees).
     */
    public static void turnAround(Karol karol) {
        karol.turnLeft();
        karol.turnLeft();
    }

    /**
     * Makes Karol move forward multiple steps.
     */
    public static void moveSteps(Karol karol, int steps) {
        for (int i = 0; i < steps; i++) {
            karol.move();
        }
    }

    /**
     * Makes Karol move until it hits a wall.
     */
    public static void moveUntilWall(Karol karol) {
        while (karol.frontIsClear()) {
            karol.move();
        }
    }

    /**
     * Makes Karol put down multiple beepers.
     */
    public static void putBeepers(Karol karol, int count) {
        for (int i = 0; i < count; i++) {
            karol.putBeeper();
        }
    }

    /**
     * Makes Karol pick up all beepers at the current position.
     */
    public static void pickAllBeepers(Karol karol) {
        while (karol.beeperPresent()) {
            karol.pickBeeper();
        }
    }

    /**
     * Makes Karol face north.
     */
    public static void faceNorth(Karol karol) {
        while (karol.getDirection() != Robot.Direction.NORTH) {
            karol.turnLeft();
        }
    }

    /**
     * Makes Karol face east.
     */
    public static void faceEast(Karol karol) {
        while (karol.getDirection() != Robot.Direction.EAST) {
            karol.turnLeft();
        }
    }

    /**
     * Makes Karol face south.
     */
    public static void faceSouth(Karol karol) {
        while (karol.getDirection() != Robot.Direction.SOUTH) {
            karol.turnLeft();
        }
    }

    /**
     * Makes Karol face west.
     */
    public static void faceWest(Karol karol) {
        while (karol.getDirection() != Robot.Direction.WEST) {
            karol.turnLeft();
        }
    }
} 