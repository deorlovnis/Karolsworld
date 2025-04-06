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

    public void addBeeper(Beeper beeper) {
        Optional<Beeper> existingBeeper = beepers.stream()
            .filter(b -> b.getX() == beeper.getX() && b.getY() == beeper.getY())
            .findFirst();

        if (existingBeeper.isPresent()) {
            Beeper existing = existingBeeper.get();
            existing.setCount(existing.getCount() + beeper.getCount());
        } else {
            beepers.add(beeper);
        }
    }

    public void putBeeper(int x, int y) {
        Optional<Beeper> existingBeeper = beepers.stream()
            .filter(b -> b.getX() == x && b.getY() == y)
            .findFirst();

        if (existingBeeper.isPresent()) {
            Beeper beeper = existingBeeper.get();
            beeper.setCount(beeper.getCount() + 1);
        } else {
            beepers.add(new Beeper(x, y, 1));
        }
    }

    public void pickBeeper(int x, int y) {
        Optional<Beeper> existingBeeper = beepers.stream()
            .filter(b -> b.getX() == x && b.getY() == y)
            .findFirst();

        if (existingBeeper.isPresent()) {
            Beeper beeper = existingBeeper.get();
            if (beeper.getCount() > 1) {
                beeper.setCount(beeper.getCount() - 1);
            } else {
                beepers.remove(beeper);
            }
        } else {
            throw new IllegalStateException("No beeper to pick up at (" + x + ", " + y + ")");
        }
    }

    public boolean isValidMove(int x, int y) {
        // Check boundaries
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }

        // Check walls
        for (Wall wall : walls) {
            if (wall.isVertical()) {
                // For vertical walls, check if we're trying to pass through it
                if (wall.getX() == x || wall.getX() == x + 1) {
                    // Check if the wall spans this y-coordinate
                    if (wall.getY() <= y && wall.getY() + 1 >= y) {
                        return false;
                    }
                }
            } else {
                // For horizontal walls, check if we're trying to pass through it
                if (wall.getY() == y || wall.getY() == y + 1) {
                    // Check if the wall spans this x-coordinate
                    if (wall.getX() <= x && wall.getX() + 1 >= x) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
} 