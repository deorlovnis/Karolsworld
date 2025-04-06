package com.karol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class World {
    private int width;
    private int height;
    private List<Wall> walls;
    private List<Beeper> beepers;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.walls = new ArrayList<>();
        this.beepers = new ArrayList<>();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Beeper> getBeepers() {
        return beepers;
    }

    public void addWall(Wall wall) {
        walls.add(wall);
    }

    public void clearWalls() {
        walls.clear();
    }

    public void clearBeepers() {
        beepers.clear();
    }

    public void addBeeper(Beeper beeper) {
        // Check if there's already a beeper at this location
        for (Beeper existing : beepers) {
            if (existing.getX() == beeper.getX() && existing.getY() == beeper.getY()) {
                existing.setCount(existing.getCount() + beeper.getCount());
                return;
            }
        }
        // If no existing beeper found, add the new one
        beepers.add(beeper);
    }

    public void putBeeper(int x, int y) {
        // Check if there's already a beeper at this location
        for (Beeper beeper : beepers) {
            if (beeper.getX() == x && beeper.getY() == y) {
                beeper.setCount(beeper.getCount() + 1);
                return;
            }
        }
        // If no existing beeper found, add a new one
        beepers.add(new Beeper(x, y, 1));
    }

    public void pickBeeper(int x, int y) {
        for (Beeper beeper : beepers) {
            if (beeper.getX() == x && beeper.getY() == y) {
                if (beeper.getCount() > 1) {
                    beeper.setCount(beeper.getCount() - 1);
                } else {
                    beepers.remove(beeper);
                }
                return;
            }
        }
        throw new IllegalStateException("No beeper to pick up!");
    }

    public boolean isValidMove(int fromX, int fromY, int toX, int toY) {
        // Check world boundaries
        if (toX < 0 || toX >= width || toY < 0 || toY >= height) {
            return false;
        }

        // Check for walls
        for (Wall wall : walls) {
            if (wall.isVertical()) {
                // Vertical wall blocks horizontal movement
                if (wall.getX() == Math.max(fromX, toX) && 
                    wall.getY() == fromY && 
                    Math.abs(fromX - toX) == 1) {
                    return false;
                }
            } else {
                // Horizontal wall blocks vertical movement
                if (wall.getY() == Math.max(fromY, toY) && 
                    wall.getX() == fromX && 
                    Math.abs(fromY - toY) == 1) {
                    return false;
                }
            }
        }

        return true;
    }
} 