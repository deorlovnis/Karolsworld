package com.karol;

public class Karol {
    private int x;
    private int y;
    private Robot.Direction direction;
    private World world;
    private int beepersInBag;  // Track how many beepers Karol is carrying

    public Karol(int x, int y, Robot.Direction direction, World world) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.world = world;
        this.beepersInBag = 0;  // Start with no beepers
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Robot.Direction getDirection() {
        return direction;
    }

    public void move() {
        int newX = x;
        int newY = y;

        switch (direction) {
            case NORTH -> newY++;
            case EAST -> newX++;
            case SOUTH -> newY--;
            case WEST -> newX--;
        }

        if (world.isValidMove(x, y, newX, newY)) {
            x = newX;
            y = newY;
        } else {
            throw new IllegalStateException("Cannot move in that direction!");
        }
    }

    public void turnLeft() {
        direction = switch (direction) {
            case NORTH -> Robot.Direction.WEST;
            case WEST -> Robot.Direction.SOUTH;
            case SOUTH -> Robot.Direction.EAST;
            case EAST -> Robot.Direction.NORTH;
        };
    }

    public void turnRight() {
        direction = switch (direction) {
            case NORTH -> Robot.Direction.EAST;
            case EAST -> Robot.Direction.SOUTH;
            case SOUTH -> Robot.Direction.WEST;
            case WEST -> Robot.Direction.NORTH;
        };
    }

    public void pickBeeper() {
        try {
            world.pickBeeper(x, y);
            beepersInBag++;  // Add beeper to Karol's bag
        } catch (IllegalStateException e) {
            throw new IllegalStateException("No beeper to pick up!");
        }
    }

    public void putBeeper() {
        if (beepersInBag > 0) {  // Only put beeper if Karol has one
            world.putBeeper(x, y);
            beepersInBag--;  // Remove beeper from Karol's bag
        } else {
            throw new IllegalStateException("No beepers in bag to put down!");
        }
    }

    public boolean frontIsClear() {
        int checkX = x;
        int checkY = y;

        switch (direction) {
            case NORTH -> checkY++;
            case EAST -> checkX++;
            case SOUTH -> checkY--;
            case WEST -> checkX--;
        }

        return world.isValidMove(x, y, checkX, checkY);
    }

    public boolean beeperPresent() {
        for (Beeper beeper : world.getBeepers()) {
            if (beeper.getX() == x && beeper.getY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check how many beepers Karol has in their bag.
     * @return number of beepers in bag
     */
    public int getBeepersInBag() {
        return beepersInBag;
    }

    /**
     * Check if Karol has any beepers to put down.
     * @return true if Karol has at least one beeper
     */
    public boolean hasBeeper() {
        return beepersInBag > 0;
    }

    /**
     * Moves forward until hitting a wall.
     */
    public void moveUntilWall() {
        while (frontIsClear()) {
            move();
        }
    }

    /**
     * Turns around (180 degrees).
     */
    public void turnAround() {
        turnLeft();
        turnLeft();
    }

    /**
     * Moves forward a certain number of steps.
     * @param steps How many steps to move forward
     */
    public void moveSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            move();
        }
    }

    /**
     * Puts down multiple beepers.
     * @param count How many beepers to put down
     * @throws IllegalStateException if Karol doesn't have enough beepers
     */
    public void putBeepers(int count) {
        if (beepersInBag >= count) {
            for (int i = 0; i < count; i++) {
                putBeeper();
            }
        } else {
            throw new IllegalStateException("Not enough beepers in bag! Have " + beepersInBag + ", need " + count);
        }
    }
} 