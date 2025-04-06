package com.karol;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int CELL_SIZE = 50;
    private World world;
    private Karol karol;
    private Canvas canvas;

    @Override
    public void start(Stage primaryStage) {
        world = new World(10, 10);
        karol = new Karol(0, 0, Karol.Direction.EAST, world);
        
        // Add some beepers for testing
        world.addBeeper(2, 2);
        world.addBeeper(3, 3);
        world.addBeeper(4, 4);

        BorderPane root = new BorderPane();
        
        // Create canvas for drawing
        canvas = new Canvas(world.getWidth() * CELL_SIZE, world.getHeight() * CELL_SIZE);
        root.setCenter(canvas);
        
        // Create control buttons
        HBox controls = new HBox(10);
        Button moveButton = new Button("Move");
        Button turnLeftButton = new Button("Turn Left");
        Button turnRightButton = new Button("Turn Right");
        Button pickBeeperButton = new Button("Pick Beeper");
        Button putBeeperButton = new Button("Put Beeper");
        
        controls.getChildren().addAll(moveButton, turnLeftButton, turnRightButton, 
                                    pickBeeperButton, putBeeperButton);
        root.setBottom(controls);

        // Set up button actions
        moveButton.setOnAction(e -> {
            try {
                karol.move();
                drawWorld();
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        });

        turnLeftButton.setOnAction(e -> {
            karol.turnLeft();
            drawWorld();
        });

        turnRightButton.setOnAction(e -> {
            karol.turnRight();
            drawWorld();
        });

        pickBeeperButton.setOnAction(e -> {
            try {
                karol.pickBeeper();
                drawWorld();
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        });

        putBeeperButton.setOnAction(e -> {
            try {
                karol.putBeeper();
                drawWorld();
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        });

        Scene scene = new Scene(root);
        primaryStage.setTitle("Karol the Robot");
        primaryStage.setScene(scene);
        primaryStage.show();

        drawWorld();
    }

    private void drawWorld() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw grid
        gc.setStroke(Color.BLACK);
        for (int x = 0; x <= world.getWidth(); x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, world.getHeight() * CELL_SIZE);
        }
        for (int y = 0; y <= world.getHeight(); y++) {
            gc.strokeLine(0, y * CELL_SIZE, world.getWidth() * CELL_SIZE, y * CELL_SIZE);
        }
        
        // Draw beepers
        gc.setFill(Color.RED);
        for (World.Position pos : world.getBeepers()) {
            gc.fillOval(pos.getX() * CELL_SIZE + CELL_SIZE/4, pos.getY() * CELL_SIZE + CELL_SIZE/4, 
                       CELL_SIZE/2, CELL_SIZE/2);
        }
        
        // Draw Karol
        gc.setFill(Color.BLUE);
        double centerX = karol.getX() * CELL_SIZE + CELL_SIZE/2;
        double centerY = karol.getY() * CELL_SIZE + CELL_SIZE/2;
        gc.fillOval(centerX - CELL_SIZE/4, centerY - CELL_SIZE/4, CELL_SIZE/2, CELL_SIZE/2);
        
        // Draw Karol's direction
        gc.setStroke(Color.WHITE);
        double arrowLength = CELL_SIZE/3;
        double arrowX = centerX;
        double arrowY = centerY;
        
        switch (karol.getDirection()) {
            case NORTH -> arrowY -= arrowLength;
            case EAST -> arrowX += arrowLength;
            case SOUTH -> arrowY += arrowLength;
            case WEST -> arrowX -= arrowLength;
        }
        
        gc.strokeLine(centerX, centerY, arrowX, arrowY);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 