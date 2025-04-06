package com.karol;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Wall {
    @JsonProperty("x")
    private int x;
    
    @JsonProperty("y")
    private int y;
    
    @JsonProperty("isVertical")
    private boolean isVertical;

    public Wall() {
        // Default constructor for Jackson
    }

    public Wall(int x, int y, boolean isVertical) {
        this.x = x;
        this.y = y;
        this.isVertical = isVertical;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVertical() {
        return isVertical;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setVertical(boolean vertical) {
        isVertical = vertical;
    }
} 