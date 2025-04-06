package com.karol;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Robot {
    @JsonProperty("x")
    private int x;
    
    @JsonProperty("y")
    private int y;
    
    @JsonProperty("direction")
    private Direction direction;

    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    public Robot() {
        // Default constructor for Jackson
    }

    public Robot(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
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

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
} 