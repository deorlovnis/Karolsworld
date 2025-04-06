package com.karol;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.tools.*;
import java.util.List;
import java.net.URI;
import java.util.ArrayList;

/**
 * Handles compilation and execution of user programs.
 */
public class ProgramExecutor {
    private static final String TEMP_DIR = "temp_programs";
    private static final String REQUIRED_PACKAGE = "com.karol.userprograms";

    private static JavaFileObject createSourceFileObject(String sourceCode, String className) {
        // Create a safe URI by replacing any illegal characters
        String safeClassName = className.replaceAll("[^a-zA-Z0-9]", "_");
        return new SimpleJavaFileObject(
            URI.create("string:///" + safeClassName + ".java"),
            JavaFileObject.Kind.SOURCE
        ) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };
    }

    /**
     * Validates the source code before compilation.
     * @param sourceCode The source code to validate
     * @param className The name of the class being compiled
     * @throws Exception if validation fails
     */
    private static void validateSourceCode(String sourceCode, String className) throws Exception {
        // First check for package and other requirements
        if (!sourceCode.contains("package ")) {
            throw new Exception("Missing package declaration. Required package: " + REQUIRED_PACKAGE);
        }
        
        if (!sourceCode.contains("package " + REQUIRED_PACKAGE)) {
            throw new Exception("Invalid package. Must be: " + REQUIRED_PACKAGE);
        }

        if (!sourceCode.contains("implements KarolProgram")) {
            throw new Exception("Class must implement KarolProgram interface");
        }

        if (!sourceCode.contains("import com.karol.KarolProgram") || 
            !sourceCode.contains("import com.karol.Karol")) {
            throw new Exception("Missing required imports: com.karol.KarolProgram and com.karol.Karol");
        }

        // Now try to compile to catch syntax errors
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new Exception("Java compiler not available. Please run with JDK instead of JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        
        JavaFileObject sourceFile = createSourceFileObject(sourceCode, className);

        // Set up compilation options
        List<String> options = new ArrayList<>();
        String classesDir = new File("target/classes").getAbsolutePath();
        options.add("-cp");
        options.add(classesDir);

        JavaCompiler.CompilationTask task = compiler.getTask(
            null, fileManager, diagnostics, options, null, Arrays.asList(sourceFile)
        );

        if (!task.call()) {
            StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                errorMsg.append(diagnostic.getMessage(null)).append("\n");
            }
            throw new Exception(errorMsg.toString());
        }
    }

    /**
     * Compiles and loads a Java program from source code.
     * @param sourceCode The Java source code to compile
     * @param className The name of the class to load
     * @return The loaded class
     * @throws Exception if compilation or loading fails
     */
    public static Class<?> compileAndLoad(String sourceCode, String className) throws Exception {
        // Validate source code first
        validateSourceCode(sourceCode, className);

        // Create a temporary directory for compiled classes
        Path tempDir = Files.createTempDirectory("karol-classes");
        try {
            // Compile the source code
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            
            // Create a JavaFileObject for the source code
            JavaFileObject sourceFile = createSourceFileObject(sourceCode, className);
            
            // Set up compilation options
            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(tempDir.toString());
            
            // Add the project's classes to the classpath
            String classesDir = new File("target/classes").getAbsolutePath();
            options.add("-cp");
            options.add(classesDir);
            
            // Create a diagnostic collector to capture compilation errors
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            
            // Compile the source code
            JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, Arrays.asList(sourceFile)
            );
            
            if (!task.call()) {
                StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errorMsg.append(diagnostic.getMessage(null)).append("\n");
                }
                throw new Exception(errorMsg.toString());
            }
            
            // Create a class loader that includes both the temp directory and the project's classes
            URL[] urls = new URL[] { 
                tempDir.toUri().toURL(),
                new File(classesDir).toURI().toURL()
            };
            
            // Create a class loader with the parent class loader to ensure proper interface loading
            URLClassLoader classLoader = new URLClassLoader(urls, ProgramExecutor.class.getClassLoader());
            try {
                Class<?> loadedClass = classLoader.loadClass(REQUIRED_PACKAGE + "." + className);
                
                // Verify that the class implements KarolProgram
                if (!KarolProgram.class.isAssignableFrom(loadedClass)) {
                    throw new Exception("Class " + className + " does not implement KarolProgram interface");
                }
                
                return loadedClass;
            } finally {
                classLoader.close();
            }
        } finally {
            // Clean up temporary files
            cleanup();
        }
    }

    /**
     * Executes a Karol program.
     * @param programClass The class of the program to execute
     * @param karol The robot instance to use
     * @throws Exception if execution fails
     */
    public static void executeProgram(Class<?> programClass, Karol karol) throws Exception {
        try {
            Object program = programClass.getDeclaredConstructor().newInstance();
            if (!(program instanceof KarolProgram)) {
                throw new Exception("Program does not implement KarolProgram interface");
            }
            ((KarolProgram) program).run(karol);
        } catch (Exception e) {
            throw new Exception("Failed to execute program: " + e.getMessage());
        }
    }

    /**
     * Cleans up temporary files.
     */
    public static void cleanup() {
        try {
            Path tempDir = Paths.get(TEMP_DIR);
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))  // Sort in reverse order to delete files before directories
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 