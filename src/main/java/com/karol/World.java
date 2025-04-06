package com.karol;

import java.util.HashSet;
import java.util.Set;

public class World {
    private final int width;
    private final int height;
    private final Set<Position> beepers;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.beepers = new HashSet<>();
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public void addBeeper(int x, int y) {
        if (isValidPosition(x, y)) {
            beepers.add(new Position(x, y));
        } else {
            throw new RuntimeException("Invalid position for beeper!");
        }
    }

    public void removeBeeper(int x, int y) {
        beepers.remove(new Position(x, y));
    }

    public boolean hasBeeper(int x, int y) {
        return beepers.contains(new Position(x, y));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Set<Position> getBeepers() {
        return new HashSet<>(beepers);
    }

    public static class Position {
        private final int x;
        private final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }
} 