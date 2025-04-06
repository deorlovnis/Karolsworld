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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private static final int CELL_SIZE = 50;
    private ListView<String> assignmentList;
    private TextArea descriptionArea;
    private TextArea programArea;
    private Canvas worldCanvas;
    private List<Assignment> assignments;
    private AssignmentLoader loader;
    private World world;
    private Karol karol;
    private WorldView worldView;
    private ObservableList<String> assignmentNames;
    private Stage programWindow;

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
            assignmentNames = FXCollections.observableArrayList();
            for (Assignment assignment : assignments) {
                assignmentNames.add(assignment.getName());
            }
            assignmentList.setItems(assignmentNames);
            
            descriptionArea = new TextArea();
            descriptionArea.setEditable(false);
            descriptionArea.setWrapText(true);
            descriptionArea.setPrefRowCount(4);

            Button createProblemButton = new Button("Create Problem");
            createProblemButton.setMaxWidth(Double.MAX_VALUE);
            createProblemButton.setOnAction(e -> createNewProblem());

            Button deleteButton = new Button("Delete Assignment");
            deleteButton.setMaxWidth(Double.MAX_VALUE);
            deleteButton.setOnAction(e -> deleteSelectedAssignment());

            leftPanel.getChildren().addAll(
                assignmentsLabel, assignmentList,
                new Label("Description:"), descriptionArea,
                createProblemButton, deleteButton
            );
            
            // Center - World view and program editor
            VBox centerPanel = new VBox(10);
            centerPanel.setPadding(new Insets(10));
            
            // World canvas at the top
            worldCanvas = new Canvas(500, 500);
            centerPanel.getChildren().add(worldCanvas);
            
            // Program editor section (initially hidden)
            VBox programSection = new VBox(10);
            programSection.setVisible(false);
            
            Label programLabel = new Label("Program:");
            programArea = new TextArea();
            programArea.setWrapText(true);
            programArea.setPrefRowCount(15);
            programArea.setStyle("-fx-font-family: monospace;");
            
            // Add example program
            programArea.setText("""
                package com.karol.userprograms;

                import com.karol.KarolProgram;
                import com.karol.Karol;
                import com.karol.KarolLibrary;

                public class MyProgram implements KarolProgram {
                    @Override
                    public void run(Karol karol) {
                        // Move to wall and put a beeper
                        KarolLibrary.moveUntilWall(karol);
                        karol.putBeeper();
                        
                        // Turn around and return to start
                        KarolLibrary.turnAround(karol);
                        KarolLibrary.moveUntilWall(karol);
                    }
                }
                """);

            Button runProgramButton = new Button("Run Program");
            runProgramButton.setMaxWidth(Double.MAX_VALUE);
            runProgramButton.setOnAction(e -> runProgram());

            // Command library section
            TitledPane libraryPane = new TitledPane();
            libraryPane.setText("Command Library");
            TextArea libraryArea = new TextArea();
            libraryArea.setEditable(false);
            libraryArea.setWrapText(true);
            libraryArea.setPrefRowCount(8);
            libraryArea.setStyle("-fx-font-family: monospace;");
            libraryArea.setText("""
                Basic Commands:
                --------------
                karol.move()           - Move forward one step
                karol.turnLeft()       - Turn 90 degrees left
                karol.turnRight()      - Turn 90 degrees right
                karol.putBeeper()      - Put a beeper at current position
                karol.pickBeeper()     - Pick up a beeper from current position
                karol.frontIsClear()   - Check if path ahead is clear
                karol.beeperPresent()  - Check if beeper is at current position

                Library Commands:
                ----------------
                KarolLibrary.turnRight(karol)      - Turn right using left turns
                KarolLibrary.turnAround(karol)     - Turn 180 degrees
                KarolLibrary.moveSteps(karol, n)   - Move forward n steps
                KarolLibrary.moveUntilWall(karol)  - Move until hitting a wall
                KarolLibrary.putBeepers(karol, n)  - Put n beepers at current position
                KarolLibrary.pickAllBeepers(karol) - Pick up all beepers at current position
                KarolLibrary.faceNorth(karol)      - Turn until facing north
                KarolLibrary.faceEast(karol)       - Turn until facing east
                KarolLibrary.faceSouth(karol)      - Turn until facing south
                KarolLibrary.faceWest(karol)       - Turn until facing west
                """);
            libraryPane.setContent(libraryArea);
            libraryPane.setExpanded(false);

            programSection.getChildren().addAll(
                new Separator(),
                programLabel,
                programArea,
                runProgramButton,
                libraryPane
            );

            centerPanel.getChildren().add(programSection);
            
            // Right side - Controls
            VBox controlPanel = new VBox(10);
            controlPanel.setPadding(new Insets(10));
            controlPanel.setPrefWidth(150);
            
            Button moveButton = new Button("Move");
            Button turnLeftButton = new Button("Turn Left");
            Button turnRightButton = new Button("Turn Right");
            Button pickBeeperButton = new Button("Pick Beeper");
            Button putBeeperButton = new Button("Put Beeper");
            Button resetButton = new Button("Reset World");
            
            // Initially disable control buttons until assignment is selected
            moveButton.setDisable(true);
            turnLeftButton.setDisable(true);
            turnRightButton.setDisable(true);
            pickBeeperButton.setDisable(true);
            putBeeperButton.setDisable(true);
            resetButton.setDisable(true);
            
            // Set max width for all buttons
            moveButton.setMaxWidth(Double.MAX_VALUE);
            turnLeftButton.setMaxWidth(Double.MAX_VALUE);
            turnRightButton.setMaxWidth(Double.MAX_VALUE);
            pickBeeperButton.setMaxWidth(Double.MAX_VALUE);
            putBeeperButton.setMaxWidth(Double.MAX_VALUE);
            resetButton.setMaxWidth(Double.MAX_VALUE);
            
            controlPanel.getChildren().addAll(
                new Label("Controls:"),
                moveButton, turnLeftButton, turnRightButton,
                pickBeeperButton, putBeeperButton,
                new Separator(),
                resetButton
            );
            
            // Event handlers
            assignmentList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                boolean hasSelection = (newVal != null);
                
                // Enable/disable UI elements based on selection
                moveButton.setDisable(!hasSelection);
                turnLeftButton.setDisable(!hasSelection);
                turnRightButton.setDisable(!hasSelection);
                pickBeeperButton.setDisable(!hasSelection);
                putBeeperButton.setDisable(!hasSelection);
                resetButton.setDisable(!hasSelection);
                programSection.setVisible(hasSelection);
                
                if (hasSelection) {
                    Assignment selected = assignments.stream()
                        .filter(a -> a.getName().equals(newVal))
                        .findFirst()
                        .orElse(null);
                    if (selected != null) {
                        descriptionArea.setText(selected.getDescription());
                        loadAssignment(selected);
                    }
                } else {
                    descriptionArea.clear();
                    if (worldCanvas != null) {
                        GraphicsContext gc = worldCanvas.getGraphicsContext2D();
                        gc.clearRect(0, 0, worldCanvas.getWidth(), worldCanvas.getHeight());
                    }
                }
            });
            
            moveButton.setOnAction(e -> {
                if (karol != null) {
                    try {
                        karol.move();
                        drawWorld();
                    } catch (IllegalStateException ex) {
                        showError("Cannot move in that direction!");
                    }
                }
            });
            
            turnLeftButton.setOnAction(e -> {
                if (karol != null) {
                    karol.turnLeft();
                    drawWorld();
                }
            });
            
            turnRightButton.setOnAction(e -> {
                if (karol != null) {
                    karol.turnRight();
                    drawWorld();
                }
            });
            
            pickBeeperButton.setOnAction(e -> {
                if (karol != null) {
                    try {
                        karol.pickBeeper();
                        drawWorld();
                    } catch (IllegalStateException ex) {
                        showError("No beeper to pick up!");
                    }
                }
            });
            
            putBeeperButton.setOnAction(e -> {
                if (karol != null) {
                    karol.putBeeper();
                    drawWorld();
                }
            });
            
            resetButton.setOnAction(e -> {
                if (assignmentList.getSelectionModel().getSelectedItem() != null) {
                    // Find and reload the current assignment
                    String selectedName = assignmentList.getSelectionModel().getSelectedItem();
                    for (Assignment assignment : assignments) {
                        if (assignment.getName().equals(selectedName)) {
                            loadAssignment(assignment);
                            break;
                        }
                    }
                }
            });
            
            root.setLeft(leftPanel);
            root.setCenter(centerPanel);
            root.setRight(controlPanel);
            
            Scene scene = new Scene(root, 1200, 800);
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
        // Create a new world with the correct dimensions
        world = new World(assignment.getWorldWidth(), assignment.getWorldHeight());
        
        // Clear any existing state
        world.clearWalls();
        world.clearBeepers();
        
        // Load walls
        for (Wall wall : assignment.getWalls()) {
            world.addWall(wall);
        }
        
        // Load beepers
        for (Beeper beeper : assignment.getBeepers()) {
            world.addBeeper(beeper);
        }
        
        // Load robot
        if (!assignment.getInitialRobots().isEmpty()) {
            Robot initialRobot = assignment.getInitialRobots().get(0);
            karol = new Karol(initialRobot.getX(), initialRobot.getY(), 
                                   initialRobot.getDirection(), world);
        }
        
        // Resize canvas to fit world
        worldCanvas.setWidth(assignment.getWorldWidth() * CELL_SIZE);
        worldCanvas.setHeight(assignment.getWorldHeight() * CELL_SIZE);
        
        drawWorld();
    }

    private void drawWorld() {
        if (world == null) return;
        
        GraphicsContext gc = worldCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, worldCanvas.getWidth(), worldCanvas.getHeight());
        
        // Draw grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int x = 0; x <= world.getWidth(); x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, world.getHeight() * CELL_SIZE);
        }
        for (int y = 0; y <= world.getHeight(); y++) {
            gc.strokeLine(0, y * CELL_SIZE, world.getWidth() * CELL_SIZE, y * CELL_SIZE);
        }
        
        // Draw walls
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        for (Wall wall : world.getWalls()) {
            if (wall.isVertical()) {
                gc.strokeLine(wall.getX() * CELL_SIZE, 
                            (world.getHeight() - wall.getY() - 1) * CELL_SIZE,
                            wall.getX() * CELL_SIZE, 
                            (world.getHeight() - wall.getY()) * CELL_SIZE);
            } else {
                gc.strokeLine(wall.getX() * CELL_SIZE, 
                            (world.getHeight() - wall.getY()) * CELL_SIZE,
                            (wall.getX() + 1) * CELL_SIZE, 
                            (world.getHeight() - wall.getY()) * CELL_SIZE);
            }
        }
        
        // Draw beepers
        gc.setFill(Color.GREEN);
        for (Beeper beeper : world.getBeepers()) {
            gc.fillOval(beeper.getX() * CELL_SIZE + CELL_SIZE/4,
                       (world.getHeight() - beeper.getY() - 1) * CELL_SIZE + CELL_SIZE/4,
                       CELL_SIZE/2, CELL_SIZE/2);
            // Draw beeper count
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(beeper.getCount()),
                       beeper.getX() * CELL_SIZE + CELL_SIZE/2,
                       (world.getHeight() - beeper.getY() - 1) * CELL_SIZE + CELL_SIZE/2);
            gc.setFill(Color.GREEN);
        }
        
        // Draw Karol
        if (karol != null) {
            gc.setFill(Color.BLUE);
            gc.fillOval(karol.getX() * CELL_SIZE + CELL_SIZE/4,
                       (world.getHeight() - karol.getY() - 1) * CELL_SIZE + CELL_SIZE/4,
                       CELL_SIZE/2, CELL_SIZE/2);
            
            // Draw direction indicator
            gc.setStroke(Color.WHITE);
            double centerX = karol.getX() * CELL_SIZE + CELL_SIZE/2;
            double centerY = (world.getHeight() - karol.getY() - 1) * CELL_SIZE + CELL_SIZE/2;
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
    }

    private void createNewProblem() {
        // Create a dialog for problem details
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Problem");
        dialog.setHeaderText("Enter problem details");

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show the dialog and wait for response
        if (dialog.showAndWait().filter(response -> response == ButtonType.OK).isPresent()) {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            if (!name.isEmpty()) {
                // Open the world editor
                Stage editorStage = new Stage();
                WorldEditor editor = new WorldEditor();
                editor.setOnSave(worldEditor -> {
                    try {
                        // Create the assignment JSON
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode assignmentNode = mapper.createObjectNode();
                        assignmentNode.put("name", name);
                        assignmentNode.put("description", description);
                        assignmentNode.put("worldWidth", worldEditor.getWorldWidth());
                        assignmentNode.put("worldHeight", worldEditor.getWorldHeight());
                        assignmentNode.set("initialRobots", worldEditor.getRobotsNode());
                        assignmentNode.set("walls", worldEditor.getWallsNode());
                        assignmentNode.set("beepers", worldEditor.getBeepersNode());

                        // Save to file
                        String filename = name.toLowerCase().replace(" ", "_") + ".json";
                        File file = new File("src/main/resources/assignments/" + filename);
                        mapper.writerWithDefaultPrettyPrinter().writeValue(file, assignmentNode);

                        // Reload assignments
                        assignments = loader.loadAllAssignments();
                        assignmentNames.clear();
                        for (Assignment assignment : assignments) {
                            assignmentNames.add(assignment.getName());
                        }

                        editorStage.close();
                    } catch (IOException ex) {
                        showError("Error saving problem: " + ex.getMessage());
                    }
                });

                Scene editorScene = new Scene(editor, 800, 600);
                editorStage.setTitle("World Editor - " + name);
                editorStage.setScene(editorScene);
                editorStage.show();
            }
        }
    }

    private void deleteSelectedAssignment() {
        String selectedName = assignmentList.getSelectionModel().getSelectedItem();
        if (selectedName == null) {
            showError("Please select an assignment to delete");
            return;
        }

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Assignment");
        confirm.setHeaderText("Delete " + selectedName + "?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().filter(response -> response == ButtonType.OK).isPresent()) {
            try {
                // Find the assignment file by matching the name in the JSON
                File assignmentsDir = new File("src/main/resources/assignments");
                File[] files = assignmentsDir.listFiles((dir, name) -> name.endsWith(".json"));
                
                boolean deleted = false;
                if (files != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    for (File file : files) {
                        try {
                            Assignment assignment = mapper.readValue(file, Assignment.class);
                            if (assignment.getName().equals(selectedName)) {
                                if (file.delete()) {
                                    deleted = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            // Skip files that can't be parsed
                            continue;
                        }
                    }
                }

                if (deleted) {
                    // Reload assignments
                    assignments = loader.loadAllAssignments();
                    assignmentNames.clear();
                    for (Assignment assignment : assignments) {
                        assignmentNames.add(assignment.getName());
                    }
                    
                    // Clear description if the deleted assignment was selected
                    descriptionArea.clear();
                } else {
                    showError("Could not delete the assignment file");
                }
            } catch (IOException ex) {
                showError("Error reloading assignments: " + ex.getMessage());
            }
        }
    }

    private void runProgram() {
        if (karol == null) {
            showError("No assignment loaded!");
            return;
        }

        try {
            // Get the program source code
            String sourceCode = programArea.getText();
            
            // Extract class name from source code
            String className = "UserProgram"; // Default name
            if (sourceCode.contains("class")) {
                int classIndex = sourceCode.indexOf("class") + 6;
                int braceIndex = sourceCode.indexOf("{", classIndex);
                if (classIndex > 5 && braceIndex > classIndex) {
                    className = sourceCode.substring(classIndex, braceIndex).trim();
                }
            }

            // Compile and load the program
            Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, className);
            
            // Execute the program
            ProgramExecutor.executeProgram(programClass, karol);
            
            // Update the world view
            drawWorld();
            
        } catch (Exception e) {
            showError("Error running program: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up temporary files
            ProgramExecutor.cleanup();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 