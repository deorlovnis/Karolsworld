package com.karol;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private static final int CELL_SIZE = 50;
    private ListView<String> assignmentList;
    private TextArea descriptionArea;
    private Canvas worldCanvas;
    private List<Assignment> assignments;
    private AssignmentLoader loader;
    private World currentWorld;
    private Karol currentKarol;

    @Override
    public void start(Stage primaryStage) {
        try {
            loader = new AssignmentLoader();
            assignments = loader.loadAllAssignments();
            
            BorderPane root = new BorderPane();
            
            // Left side - Assignment list and description
            VBox leftPanel = new VBox(10);
            leftPanel.setPadding(new Insets(10));
            leftPanel.setPrefWidth(300);
            
            Label assignmentsLabel = new Label("Assignments:");
            assignmentList = new ListView<>();
            ObservableList<String> assignmentNames = FXCollections.observableArrayList();
            for (Assignment assignment : assignments) {
                assignmentNames.add(assignment.getName());
            }
            assignmentList.setItems(assignmentNames);
            
            descriptionArea = new TextArea();
            descriptionArea.setEditable(false);
            descriptionArea.setWrapText(true);
            descriptionArea.setPrefRowCount(4);
            
            Button loadButton = new Button("Load Assignment");
            loadButton.setMaxWidth(Double.MAX_VALUE);
            
            leftPanel.getChildren().addAll(assignmentsLabel, assignmentList, 
                                         new Label("Description:"), descriptionArea,
                                         loadButton);
            
            // Center - World view
            worldCanvas = new Canvas(500, 500);
            
            // Right side - Controls
            VBox controlPanel = new VBox(10);
            controlPanel.setPadding(new Insets(10));
            controlPanel.setPrefWidth(150);
            
            Button moveButton = new Button("Move");
            Button turnLeftButton = new Button("Turn Left");
            Button turnRightButton = new Button("Turn Right");
            Button pickBeeperButton = new Button("Pick Beeper");
            Button putBeeperButton = new Button("Put Beeper");
            
            moveButton.setMaxWidth(Double.MAX_VALUE);
            turnLeftButton.setMaxWidth(Double.MAX_VALUE);
            turnRightButton.setMaxWidth(Double.MAX_VALUE);
            pickBeeperButton.setMaxWidth(Double.MAX_VALUE);
            putBeeperButton.setMaxWidth(Double.MAX_VALUE);
            
            controlPanel.getChildren().addAll(
                new Label("Controls:"),
                moveButton, turnLeftButton, turnRightButton,
                pickBeeperButton, putBeeperButton
            );
            
            // Event handlers
            assignmentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    Assignment selected = assignments.stream()
                        .filter(a -> a.getName().equals(newVal))
                        .findFirst()
                        .orElse(null);
                    if (selected != null) {
                        descriptionArea.setText(selected.getDescription());
                    }
                }
            });
            
            loadButton.setOnAction(e -> {
                String selectedName = assignmentList.getSelectionModel().getSelectedItem();
                if (selectedName != null) {
                    Assignment selected = assignments.stream()
                        .filter(a -> a.getName().equals(selectedName))
                        .findFirst()
                        .orElse(null);
                    if (selected != null) {
                        loadAssignment(selected);
                    }
                }
            });
            
            moveButton.setOnAction(e -> {
                if (currentKarol != null) {
                    try {
                        currentKarol.move();
                        drawWorld();
                    } catch (IllegalStateException ex) {
                        showError("Cannot move in that direction!");
                    }
                }
            });
            
            turnLeftButton.setOnAction(e -> {
                if (currentKarol != null) {
                    currentKarol.turnLeft();
                    drawWorld();
                }
            });
            
            turnRightButton.setOnAction(e -> {
                if (currentKarol != null) {
                    currentKarol.turnRight();
                    drawWorld();
                }
            });
            
            pickBeeperButton.setOnAction(e -> {
                if (currentKarol != null) {
                    try {
                        currentKarol.pickBeeper();
                        drawWorld();
                    } catch (IllegalStateException ex) {
                        showError("No beeper to pick up!");
                    }
                }
            });
            
            putBeeperButton.setOnAction(e -> {
                if (currentKarol != null) {
                    currentKarol.putBeeper();
                    drawWorld();
                }
            });
            
            root.setLeft(leftPanel);
            root.setCenter(worldCanvas);
            root.setRight(controlPanel);
            
            Scene scene = new Scene(root, 1000, 600);
            primaryStage.setTitle("Karol the Robot - Assignments");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadAssignment(Assignment assignment) {
        currentWorld = new World(assignment.getWorldWidth(), assignment.getWorldHeight());
        
        // Load walls
        for (Wall wall : assignment.getWalls()) {
            currentWorld.addWall(wall);
        }
        
        // Load beepers
        for (Beeper beeper : assignment.getBeepers()) {
            currentWorld.addBeeper(beeper);
        }
        
        // Load robot
        if (!assignment.getInitialRobots().isEmpty()) {
            Robot initialRobot = assignment.getInitialRobots().get(0);
            currentKarol = new Karol(initialRobot.getX(), initialRobot.getY(), 
                                   initialRobot.getDirection(), currentWorld);
        }
        
        // Resize canvas to fit world
        worldCanvas.setWidth(assignment.getWorldWidth() * CELL_SIZE);
        worldCanvas.setHeight(assignment.getWorldHeight() * CELL_SIZE);
        
        drawWorld();
    }

    private void drawWorld() {
        if (currentWorld == null) return;
        
        GraphicsContext gc = worldCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, worldCanvas.getWidth(), worldCanvas.getHeight());
        
        // Draw grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int x = 0; x <= currentWorld.getWidth(); x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, currentWorld.getHeight() * CELL_SIZE);
        }
        for (int y = 0; y <= currentWorld.getHeight(); y++) {
            gc.strokeLine(0, y * CELL_SIZE, currentWorld.getWidth() * CELL_SIZE, y * CELL_SIZE);
        }
        
        // Draw walls
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        for (Wall wall : currentWorld.getWalls()) {
            if (wall.isVertical()) {
                gc.strokeLine(wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE,
                            wall.getX() * CELL_SIZE, (wall.getY() + 1) * CELL_SIZE);
            } else {
                gc.strokeLine(wall.getX() * CELL_SIZE, wall.getY() * CELL_SIZE,
                            (wall.getX() + 1) * CELL_SIZE, wall.getY() * CELL_SIZE);
            }
        }
        
        // Draw beepers
        gc.setFill(Color.GREEN);
        for (Beeper beeper : currentWorld.getBeepers()) {
            gc.fillOval(beeper.getX() * CELL_SIZE + CELL_SIZE/4,
                       beeper.getY() * CELL_SIZE + CELL_SIZE/4,
                       CELL_SIZE/2, CELL_SIZE/2);
            // Draw beeper count
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(beeper.getCount()),
                       beeper.getX() * CELL_SIZE + CELL_SIZE/2,
                       beeper.getY() * CELL_SIZE + CELL_SIZE/2);
            gc.setFill(Color.GREEN);
        }
        
        // Draw Karol
        if (currentKarol != null) {
            gc.setFill(Color.BLUE);
            gc.fillOval(currentKarol.getX() * CELL_SIZE + CELL_SIZE/4,
                       currentKarol.getY() * CELL_SIZE + CELL_SIZE/4,
                       CELL_SIZE/2, CELL_SIZE/2);
            
            // Draw direction indicator
            gc.setStroke(Color.WHITE);
            double centerX = currentKarol.getX() * CELL_SIZE + CELL_SIZE/2;
            double centerY = currentKarol.getY() * CELL_SIZE + CELL_SIZE/2;
            double arrowLength = CELL_SIZE/3;
            
            double arrowX = centerX;
            double arrowY = centerY;
            
            switch (currentKarol.getDirection()) {
                case NORTH -> arrowY -= arrowLength;
                case EAST -> arrowX += arrowLength;
                case SOUTH -> arrowY += arrowLength;
                case WEST -> arrowX -= arrowLength;
            }
            
            gc.strokeLine(centerX, centerY, arrowX, arrowY);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 