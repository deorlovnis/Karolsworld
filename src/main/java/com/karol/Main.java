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
import java.nio.file.Files;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
    private ObservableList<String> assignmentNames;
    private String lastSavedProgram;

    @Override
    public void start(Stage primaryStage) {
        try {
            loader = new AssignmentLoader();
            assignments = loader.loadAllAssignments();
            
            // Initialize with example program
            lastSavedProgram = """
                package com.karol.userprograms;

                import com.karol.KarolProgram;
                import com.karol.Karol;

                public class MyProgram implements KarolProgram {
                    @Override
                    public void run(Karol karol) {
                        // TODO: Write your program here!
                        // Here are some commands you can use:
                        // karol.move() - Move forward one step
                        // karol.turnLeft() - Turn 90 degrees left
                        // karol.turnRight() - Turn 90 degrees right
                        // karol.putBeeper() - Put a beeper (if you have one!)
                        // karol.pickBeeper() - Pick up a beeper
                        // karol.frontIsClear() - Check if path ahead is clear
                        // karol.beeperPresent() - Check if beeper is here
                        // karol.hasBeeper() - Check if you have a beeper to put down
                        // karol.getBeepersInBag() - Check how many beepers you have
                    }
                }
                """;
            
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

            // Create a HBox to hold line numbers and program text
            HBox editorBox = new HBox();
            editorBox.setStyle("-fx-font-family: monospace; -fx-background-color: white; -fx-border-color: lightgray;");

            // Line numbers
            VBox lineNumbers = new VBox();
            lineNumbers.setAlignment(Pos.TOP_RIGHT);
            lineNumbers.setPadding(new Insets(5, 5, 5, 5));
            lineNumbers.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");
            lineNumbers.setPrefWidth(40);

            // Program text area
            programArea = new TextArea();
            programArea.setWrapText(true);
            programArea.setPrefRowCount(15);
            programArea.setStyle("-fx-font-family: monospace;");

            // Update line numbers when text changes
            programArea.textProperty().addListener((obs, oldText, newText) -> {
                lastSavedProgram = newText;
                updateLineNumbers(lineNumbers, newText);
            });

            // Handle tab key
            programArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume(); // Prevent default tab behavior
                    int caretPosition = programArea.getCaretPosition();
                    programArea.insertText(caretPosition, "    "); // Insert 4 spaces
                }
            });

            editorBox.getChildren().addAll(lineNumbers, programArea);
            HBox.setHgrow(programArea, Priority.ALWAYS);

            // Set initial text and line numbers
            programArea.setText("""
                package com.karol.userprograms;

                import com.karol.KarolProgram;
                import com.karol.Karol;

                public class MyProgram implements KarolProgram {
                    @Override
                    public void run(Karol karol) {
                        // TODO: Write your program here!
                        // Here are some commands you can use:
                        // karol.move() - Move forward one step
                        // karol.turnLeft() - Turn left
                        // karol.turnRight() - Turn right
                        // karol.putBeeper() - Put down a beeper (if you have one!)
                        // karol.pickBeeper() - Pick up a beeper
                        // karol.frontIsClear() - Check if path ahead is clear
                        // karol.beeperPresent() - Check if beeper is here
                        // karol.hasBeeper() - Check if you have a beeper to put down
                        // karol.getBeepersInBag() - Check how many beepers you have
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
                Karol's Basic Commands (Things Karol Already Knows):
                ------------------------------------------------
                karol.move()           - Take one step forward
                karol.turnLeft()       - Turn left
                karol.turnRight()      - Turn right
                karol.putBeeper()      - Put down a beeper
                karol.pickBeeper()     - Pick up a beeper
                karol.frontIsClear()   - Check if Karol can move forward
                karol.beeperPresent()  - Check if there's a beeper here

                Karol's Extra Commands:
                ---------------------
                karol.moveUntilWall()  - Keep moving until hitting a wall
                karol.turnAround()     - Turn around (face the other way)
                karol.moveSteps(3)     - Move forward 3 steps (or any number)
                karol.putBeepers(3)    - Put down 3 beepers (or any number)

                Teaching Karol New Tricks!
                ------------------------
                You can teach Karol new tricks by creating your own commands!
                Here's how to do it:

                1. Add a new method (trick) to your program:
                   private void moveTwice(Karol karol) {
                       karol.move();  // First step
                       karol.move();  // Second step
                   }

                2. Use your new trick in the run method:
                   public void run(Karol karol) {
                       moveTwice(karol);  // Karol moves twice!
                   }

                More Examples of New Tricks:
                --------------------------
                // Make Karol dance
                private void dance(Karol karol) {
                    karol.turnLeft();
                    karol.turnRight();
                    karol.turnAround();
                }

                // Make Karol put down 3 beepers in a row
                private void putBeeperLine(Karol karol) {
                    karol.putBeeper();
                    karol.move();
                    karol.putBeeper();
                    karol.move();
                    karol.putBeeper();
                }
                """);
            libraryPane.setContent(libraryArea);
            libraryPane.setExpanded(false);

            // Add Save and Run buttons
            HBox buttonBox = new HBox(10);
            Button saveButton = new Button("Save Solution");
            saveButton.setMaxWidth(Double.MAX_VALUE);
            saveButton.setOnAction(e -> saveSolution());
            runProgramButton.setMaxWidth(Double.MAX_VALUE);
            buttonBox.getChildren().addAll(runProgramButton, saveButton);
            HBox.setHgrow(runProgramButton, Priority.ALWAYS);
            HBox.setHgrow(saveButton, Priority.ALWAYS);

            programSection.getChildren().addAll(
                new Separator(),
                programLabel,
                editorBox,  // Use editorBox instead of directly adding programArea
                buttonBox,
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
            
            // Add beeper count label to control panel
            Label beeperCountLabel = new Label("Beepers: 0");
            controlPanel.getChildren().addAll(
                new Label("Controls:"),
                moveButton, turnLeftButton, turnRightButton,
                pickBeeperButton, putBeeperButton,
                new Separator(),
                resetButton,
                beeperCountLabel
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
                        beeperCountLabel.setText("Beepers: " + karol.getBeepersInBag());
                        drawWorld();
                    } catch (IllegalStateException ex) {
                        showError("No beeper to pick up!");
                    }
                }
            });
            
            putBeeperButton.setOnAction(e -> {
                if (karol != null) {
                    try {
                        karol.putBeeper();
                        beeperCountLabel.setText("Beepers: " + karol.getBeepersInBag());
                        drawWorld();
                    } catch (IllegalStateException ex) {
                        showError("No beepers in bag to put down!");
                    }
                }
            });
            
            resetButton.setOnAction(e -> {
                if (assignmentList.getSelectionModel().getSelectedItem() != null) {
                    // Find and reload the current assignment
                    String selectedName = assignmentList.getSelectionModel().getSelectedItem();
                    for (Assignment assignment : assignments) {
                        if (assignment.getName().equals(selectedName)) {
                            loadAssignment(assignment);
                            // Update beeper count label
                            Node beeperLabel = controlPanel.getChildren().get(1);
                            if (beeperLabel instanceof Label) {
                                ((Label) beeperLabel).setText("Beepers: 0");
                            }
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
        
        // Load saved solution if it exists
        loadSolution(assignment.getName());
        
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

    private void saveSolution() {
        String selectedName = assignmentList.getSelectionModel().getSelectedItem();
        if (selectedName == null) {
            showError("Please select an assignment first");
            return;
        }

        try {
            // Create solutions directory if it doesn't exist
            File solutionsDir = new File("src/main/resources/solutions");
            if (!solutionsDir.exists()) {
                solutionsDir.mkdirs();
            }

            // Create solution file named after the assignment
            String filename = selectedName.toLowerCase().replace(" ", "_") + "_solution.java";
            File solutionFile = new File(solutionsDir, filename);
            Files.writeString(solutionFile.toPath(), programArea.getText());

            showInfo("Solution saved successfully!");
        } catch (IOException e) {
            showError("Error saving solution: " + e.getMessage());
        }
    }

    private void loadSolution(String assignmentName) {
        try {
            // Try to load saved solution
            String filename = assignmentName.toLowerCase().replace(" ", "_") + "_solution.java";
            File solutionFile = new File("src/main/resources/solutions", filename);
            
            if (solutionFile.exists()) {
                String savedSolution = Files.readString(solutionFile.toPath());
                programArea.setText(savedSolution);
                lastSavedProgram = savedSolution;
            } else {
                // If no solution exists, load the default template
                programArea.setText("""
                    package com.karol.userprograms;

                    import com.karol.KarolProgram;
                    import com.karol.Karol;

                    public class MyProgram implements KarolProgram {
                        @Override
                        public void run(Karol karol) {
                            // TODO: Write your program here!
                            // Here are some commands you can use:
                            // karol.move() - Move forward one step
                            // karol.turnLeft() - Turn left
                            // karol.turnRight() - Turn right
                            // karol.putBeeper() - Put down a beeper (if you have one!)
                            // karol.pickBeeper() - Pick up a beeper
                            // karol.frontIsClear() - Check if path ahead is clear
                            // karol.beeperPresent() - Check if beeper is here
                            // karol.hasBeeper() - Check if you have a beeper to put down
                            // karol.getBeepersInBag() - Check how many beepers you have
                        }
                    }
                    """);
            }
        } catch (IOException e) {
            showError("Error loading solution: " + e.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateLineNumbers(VBox lineNumbers, String text) {
        // Clear existing line numbers
        lineNumbers.getChildren().clear();
        
        // Count lines
        int lines = text.split("\n", -1).length;
        
        // Add new line numbers
        for (int i = 1; i <= lines; i++) {
            Label lineNum = new Label(String.format("%3d", i));
            lineNum.setStyle("-fx-font-family: monospace; -fx-text-fill: #666666;");
            lineNumbers.getChildren().add(lineNum);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 