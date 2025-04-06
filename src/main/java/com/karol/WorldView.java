package com.karol;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WorldView extends Canvas {
    private World world;
    private Karol karol;
    private double cellWidth;
    private double cellHeight;

    public WorldView(double width, double height) {
        super(width, height);
        setWidth(width);
        setHeight(height);
    }

    public void setWorld(World world) {
        this.world = world;
        update();
    }

    public void setKarol(Karol karol) {
        this.karol = karol;
        update();
    }

    public void update() {
        if (world == null) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        cellWidth = getWidth() / world.getWidth();
        cellHeight = getHeight() / world.getHeight();

        // Draw grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int x = 0; x <= world.getWidth(); x++) {
            gc.strokeLine(x * cellWidth, 0, x * cellWidth, getHeight());
        }
        for (int y = 0; y <= world.getHeight(); y++) {
            gc.strokeLine(0, y * cellHeight, getWidth(), y * cellHeight);
        }

        // Draw walls
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        for (Wall wall : world.getWalls()) {
            double x = wall.getX() * cellWidth;
            double y = (world.getHeight() - wall.getY()) * cellHeight;
            if (wall.isVertical()) {
                gc.strokeLine(x, y - cellHeight, x, y);
            } else {
                gc.strokeLine(x, y, x + cellWidth, y);
            }
        }

        // Draw beepers
        gc.setFill(Color.GREEN);
        for (Beeper beeper : world.getBeepers()) {
            double x = (beeper.getX() - 0.5) * cellWidth;
            double y = (world.getHeight() - beeper.getY() + 0.5) * cellHeight;
            double radius = Math.min(cellWidth, cellHeight) * 0.3;
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            
            // Draw beeper count if more than 1
            if (beeper.getCount() > 1) {
                gc.setFill(Color.WHITE);
                gc.fillText(String.valueOf(beeper.getCount()), x - 5, y + 5);
                gc.setFill(Color.GREEN);
            }
        }

        // Draw Karel
        if (karol != null) {
            double x = (karol.getX() - 0.5) * cellWidth;
            double y = (world.getHeight() - karol.getY() + 0.5) * cellHeight;
            double size = Math.min(cellWidth, cellHeight) * 0.8;

            gc.setFill(Color.BLUE);
            gc.save();
            gc.translate(x, y);
            
            // Rotate based on direction (0 degrees points EAST)
            double angle = switch (karol.getDirection()) {
                case NORTH -> 90;
                case EAST -> 0;
                case SOUTH -> 270;
                case WEST -> 180;
            };
            gc.rotate(angle);

            // Draw triangle pointing right (EAST)
            gc.beginPath();
            gc.moveTo(size/2, 0);  // Point at right
            gc.lineTo(-size/2, size/2);  // Bottom left
            gc.lineTo(-size/2, -size/2);  // Top left
            gc.closePath();
            gc.fill();
            
            gc.restore();
        }
    }
} 