package com.karol;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.MouseEvent;
import java.util.function.Consumer;

public class WorldEditor extends VBox {
    private int worldWidth = 7;
    private int worldHeight = 5;
    private GridPane grid;
    private ToggleGroup toolGroup;
    private List<Wall> walls = new ArrayList<>();
    private List<Beeper> beepers = new ArrayList<>();
    private Robot robot = null;
    private static final int CELL_SIZE = 60;
    private Consumer<WorldEditor> onSave;
    
    public WorldEditor() {
        setPadding(new Insets(10));
        setSpacing(10);
        
        // Create toolbar
        HBox toolbar = new HBox(10);
        
        // World size controls
        TextField widthField = new TextField(String.valueOf(worldWidth));
        widthField.setPrefWidth(50);
        TextField heightField = new TextField(String.valueOf(worldHeight));
        heightField.setPrefWidth(50);
        Button resizeButton = new Button("Resize World");
        resizeButton.setOnAction(e -> {
            try {
                worldWidth = Integer.parseInt(widthField.getText());
                worldHeight = Integer.parseInt(heightField.getText());
                createGrid();
            } catch (NumberFormatException ex) {
                showAlert("Invalid dimensions");
            }
        });
        
        // Tool selection
        toolGroup = new ToggleGroup();
        
        ToggleButton horizontalWallButton = new ToggleButton("Horizontal Wall");
        horizontalWallButton.setToggleGroup(toolGroup);
        horizontalWallButton.setUserData("HORIZONTAL_WALL");
        
        ToggleButton verticalWallButton = new ToggleButton("Vertical Wall");
        verticalWallButton.setToggleGroup(toolGroup);
        verticalWallButton.setUserData("VERTICAL_WALL");
        
        ToggleButton beeperButton = new ToggleButton("Beeper");
        beeperButton.setToggleGroup(toolGroup);
        beeperButton.setUserData("BEEPER");
        
        ToggleButton robotButton = new ToggleButton("Robot");
        robotButton.setToggleGroup(toolGroup);
        robotButton.setUserData("ROBOT");
        
        ToggleButton eraseButton = new ToggleButton("Erase");
        eraseButton.setToggleGroup(toolGroup);
        eraseButton.setUserData("ERASE");
        
        horizontalWallButton.setSelected(true);
        
        // Save button
        Button saveButton = new Button("Save World");
        saveButton.setOnAction(e -> {
            if (onSave != null) {
                onSave.accept(this);
            } else {
                saveWorld();
            }
        });
        
        toolbar.getChildren().addAll(
            new Label("Width:"), widthField,
            new Label("Height:"), heightField,
            resizeButton,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            horizontalWallButton, verticalWallButton, beeperButton, robotButton, eraseButton,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            saveButton
        );
        
        getChildren().add(toolbar);
        
        createGrid();
    }
    
    public void setOnSave(Consumer<WorldEditor> callback) {
        this.onSave = callback;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public ArrayNode getRobotsNode() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode robotsNode = mapper.createArrayNode();
        if (robot != null) {
            ObjectNode robotNode = robotsNode.addObject();
            robotNode.put("x", robot.getX());
            robotNode.put("y", robot.getY());
            robotNode.put("direction", robot.getDirection().toString());
        }
        return robotsNode;
    }

    public ArrayNode getWallsNode() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode wallsNode = mapper.createArrayNode();
        for (Wall wall : walls) {
            ObjectNode wallNode = wallsNode.addObject();
            wallNode.put("x", wall.getX());
            wallNode.put("y", wall.getY());
            wallNode.put("isVertical", wall.isVertical());
        }
        return wallsNode;
    }

    public ArrayNode getBeepersNode() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode beepersNode = mapper.createArrayNode();
        for (Beeper beeper : beepers) {
            ObjectNode beeperNode = beepersNode.addObject();
            beeperNode.put("x", beeper.getX());
            beeperNode.put("y", beeper.getY());
            beeperNode.put("count", beeper.getCount());
        }
        return beepersNode;
    }
    
    private void createGrid() {
        if (grid != null) {
            getChildren().remove(grid);
        }
        
        grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setStyle("-fx-background-color: lightgray;");
        
        for (int y = 0; y < worldHeight; y++) {
            for (int x = 0; x < worldWidth; x++) {
                Pane cell = createCell(x, worldHeight - 1 - y);
                grid.add(cell, x, y);
            }
        }
        
        getChildren().add(grid);
    }
    
    private Pane createCell(int x, int y) {
        Pane cell = new Pane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        
        cell.getProperties().put("logicalX", x);
        cell.getProperties().put("logicalY", y);
        
        cell.setOnMouseClicked(e -> {
            String tool = (String) toolGroup.getSelectedToggle().getUserData();
            if (e.getButton() == MouseButton.PRIMARY) {
                int logicalX = (int) cell.getProperties().get("logicalX");
                int logicalY = (int) cell.getProperties().get("logicalY");
                handleCellClick(cell, logicalX, logicalY, tool, e);
            }
        });
        
        return cell;
    }
    
    private void handleCellClick(Pane cell, int x, int y, String tool, MouseEvent e) {
        switch (tool) {
            case "HORIZONTAL_WALL":
                toggleWall(cell, x, y, false);
                break;
            case "VERTICAL_WALL":
                toggleWall(cell, x, y, true);
                break;
            case "BEEPER":
                toggleBeeper(cell, x, y);
                break;
            case "ROBOT":
                placeRobot(cell, x, y);
                break;
            case "ERASE":
                eraseCell(cell, x, y);
                break;
        }
    }
    
    private void toggleWall(Pane cell, int x, int y, boolean isVertical) {
        Wall wall = new Wall(x, y, isVertical);
        Wall existingWall = walls.stream()
            .filter(w -> w.getX() == x && w.getY() == y && w.isVertical() == isVertical)
            .findFirst()
            .orElse(null);
            
        if (existingWall != null) {
            walls.remove(existingWall);
            removeWallFromCell(cell);
        } else {
            walls.add(wall);
            addWallToCell(cell, isVertical);
        }
    }
    
    private void addWallToCell(Pane cell, boolean isVertical) {
        Rectangle wall = new Rectangle();
        if (isVertical) {
            wall.setWidth(4);
            wall.setHeight(CELL_SIZE);
            wall.setX(0);
            wall.setY(0);
        } else {
            wall.setWidth(CELL_SIZE);
            wall.setHeight(4);
            wall.setX(0);
            wall.setY(CELL_SIZE - 2);
        }
        wall.setFill(Color.BLACK);
        cell.getChildren().add(wall);
    }
    
    private void removeWallFromCell(Pane cell) {
        cell.getChildren().removeIf(node -> node instanceof Rectangle);
    }
    
    private void toggleBeeper(Pane cell, int x, int y) {
        Beeper beeper = new Beeper(x, y, 1);
        Beeper existingBeeper = beepers.stream()
            .filter(b -> b.getX() == x && b.getY() == y)
            .findFirst()
            .orElse(null);
            
        if (existingBeeper != null) {
            beepers.remove(existingBeeper);
            cell.getChildren().removeIf(node -> node instanceof Circle);
        } else {
            beepers.add(beeper);
            Circle beeperShape = new Circle(CELL_SIZE/2, CELL_SIZE/2, CELL_SIZE/4, Color.GREEN);
            cell.getChildren().add(beeperShape);
        }
    }
    
    private void placeRobot(Pane cell, int x, int y) {
        if (robot != null) {
            grid.getChildren().stream()
                .filter(node -> node instanceof Pane)
                .map(node -> (Pane) node)
                .filter(pane -> pane.getChildren().stream().anyMatch(child -> child instanceof Polygon))
                .findFirst()
                .ifPresent(oldCell -> oldCell.getChildren().removeIf(node -> node instanceof Polygon));
        }
        
        robot = new Robot(x, y, Robot.Direction.EAST);
        
        Polygon robotShape = new Polygon();
        robotShape.getPoints().addAll(
            CELL_SIZE * 0.2, CELL_SIZE * 0.2,
            CELL_SIZE * 0.8, CELL_SIZE * 0.5,
            CELL_SIZE * 0.2, CELL_SIZE * 0.8
        );
        robotShape.setFill(Color.BLUE);
        
        cell.getChildren().add(robotShape);
    }
    
    private void eraseCell(Pane cell, int x, int y) {
        walls.removeIf(w -> w.getX() == x && w.getY() == y);
        beepers.removeIf(b -> b.getX() == x && b.getY() == y);
        if (robot != null && robot.getX() == x && robot.getY() == y) {
            robot = null;
        }
        cell.getChildren().clear();
    }
    
    private void saveWorld() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save World");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode worldNode = mapper.createObjectNode();
                
                worldNode.put("worldWidth", worldWidth);
                worldNode.put("worldHeight", worldHeight);
                
                if (robot != null) {
                    ArrayNode robotsNode = worldNode.putArray("initialRobots");
                    ObjectNode robotNode = robotsNode.addObject();
                    robotNode.put("x", robot.getX());
                    robotNode.put("y", robot.getY());
                    robotNode.put("direction", robot.getDirection().toString());
                }
                
                ArrayNode wallsNode = worldNode.putArray("walls");
                for (Wall wall : walls) {
                    ObjectNode wallNode = wallsNode.addObject();
                    wallNode.put("x", wall.getX());
                    wallNode.put("y", wall.getY());
                    wallNode.put("isVertical", wall.isVertical());
                }
                
                ArrayNode beepersNode = worldNode.putArray("beepers");
                for (Beeper beeper : beepers) {
                    ObjectNode beeperNode = beepersNode.addObject();
                    beeperNode.put("x", beeper.getX());
                    beeperNode.put("y", beeper.getY());
                    beeperNode.put("count", beeper.getCount());
                }
                
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, worldNode);
                showAlert("World saved successfully!");
            } catch (Exception e) {
                showAlert("Error saving world: " + e.getMessage());
            }
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("World Editor");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 