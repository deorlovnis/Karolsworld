package com.karol;

public class Karol {
    private int x;
    private int y;
    private Direction direction;
    private int beepers;
    private World world;

    public enum Direction {
        NORTH, EAST, SOUTH, WEST;

        public Direction turnLeft() {
            return switch (this) {
                case NORTH -> WEST;
                case EAST -> NORTH;
                case SOUTH -> EAST;
                case WEST -> SOUTH;
            };
        }

        public Direction turnRight() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };
        }
    }

    public Karol(int x, int y, Direction direction, World world) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.world = world;
        this.beepers = 0;
    }

    public void move() {
        int newX = x;
        int newY = y;

        switch (direction) {
            case NORTH -> newY--;
            case EAST -> newX++;
            case SOUTH -> newY++;
            case WEST -> newX--;
        }

        if (world.isValidPosition(newX, newY)) {
            x = newX;
            y = newY;
        } else {
            throw new RuntimeException("Karol cannot move outside the world!");
        }
    }

    public void turnLeft() {
        direction = direction.turnLeft();
    }

    public void turnRight() {
        direction = direction.turnRight();
    }

    public void pickBeeper() {
        if (world.hasBeeper(x, y)) {
            world.removeBeeper(x, y);
            beepers++;
        } else {
            throw new RuntimeException("No beeper to pick up!");
        }
    }

    public void putBeeper() {
        if (beepers > 0) {
            world.addBeeper(x, y);
            beepers--;
        } else {
            throw new RuntimeException("No beepers to put down!");
        }
    }

    public boolean frontIsClear() {
        int newX = x;
        int newY = y;

        switch (direction) {
            case NORTH -> newY--;
            case EAST -> newX++;
            case SOUTH -> newY++;
            case WEST -> newX--;
        }

        return world.isValidPosition(newX, newY);
    }

    public boolean beepersPresent() {
        return world.hasBeeper(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getBeepers() {
        return beepers;
    }
} 