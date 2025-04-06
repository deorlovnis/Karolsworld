package com.karol;

public class Karol {
    private int x;
    private int y;
    private Robot.Direction direction;
    private World world;

    public Karol(int x, int y, Robot.Direction direction, World world) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.world = world;
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

        if (world.isValidMove(newX, newY)) {
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
        } catch (IllegalStateException e) {
            throw new IllegalStateException("No beeper to pick up!");
        }
    }

    public void putBeeper() {
        world.putBeeper(x, y);
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

        return world.isValidMove(checkX, checkY);
    }

    public boolean beeperPresent() {
        for (Beeper beeper : world.getBeepers()) {
            if (beeper.getX() == x && beeper.getY() == y) {
                return true;
            }
        }
        return false;
    }
} 