package com.karol;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentLoader {
    private static final String ASSIGNMENTS_DIR = "src/main/resources/assignments";
    private final ObjectMapper objectMapper;

    public AssignmentLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Assignment> loadAllAssignments() throws IOException {
        List<Assignment> assignments = new ArrayList<>();
        Path assignmentsPath = Paths.get(ASSIGNMENTS_DIR);
        
        if (!Files.exists(assignmentsPath)) {
            Files.createDirectories(assignmentsPath);
        }

        List<File> assignmentFiles = Files.walk(assignmentsPath)
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".json"))
            .map(Path::toFile)
            .collect(Collectors.toList());

        for (File file : assignmentFiles) {
            assignments.add(loadAssignment(file));
        }

        return assignments;
    }

    public Assignment loadAssignment(File file) throws IOException {
        return objectMapper.readValue(file, Assignment.class);
    }
} 