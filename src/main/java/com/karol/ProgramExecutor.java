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
import java.util.Locale;

/**
 * Handles compilation and execution of user programs.
 */
public class ProgramExecutor {
    private static final String TEMP_DIR = "temp_programs";
    private static final String PACKAGE_NAME = "com.karol.userprograms";

    /**
     * Compiles and loads a Java program from source code.
     * @param sourceCode The Java source code to compile
     * @param className The name of the class to load
     * @return The loaded class
     * @throws Exception if compilation or loading fails
     */
    public static Class<?> compileAndLoad(String sourceCode, String className) throws Exception {
        // Create temp directory if it doesn't exist
        Path tempDir = Paths.get(TEMP_DIR);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // Create package directory structure
        Path packageDir = tempDir.resolve(PACKAGE_NAME.replace('.', '/'));
        if (!Files.exists(packageDir)) {
            Files.createDirectories(packageDir);
        }

        // Extract actual class name from source code
        String actualClassName = extractClassName(sourceCode);
        if (actualClassName == null) {
            throw new Exception("Could not find class name in source code");
        }

        // Create source file with correct name
        Path sourceFile = packageDir.resolve(actualClassName + ".java");
        Files.writeString(sourceFile, sourceCode);

        // Get the Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new Exception("Could not find Java compiler. Please ensure JDK is installed and JAVA_HOME is set correctly.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // Get classpath including the temp directory
        String classpath = System.getProperty("java.class.path") + File.pathSeparator + tempDir.toString();

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile.toFile()));

        List<String> options = Arrays.asList(
            "-classpath", classpath,
            "-d", tempDir.toString(),
            "-Xlint:all"  // Enable all warnings
        );

        // Compile
        JavaCompiler.CompilationTask task = compiler.getTask(
            null,
            fileManager,
            diagnostics,
            options,
            null,
            compilationUnits
        );

        boolean success = task.call();
        fileManager.close();

        if (!success) {
            StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    errorMsg.append(String.format(Locale.ENGLISH,
                        "Line %d: %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getMessage(null)
                    ));
                }
            }
            throw new Exception(errorMsg.toString());
        }

        // Verify the class implements KarolProgram
        try {
            URLClassLoader classLoader = new URLClassLoader(
                new URL[] { tempDir.toUri().toURL() },
                ProgramExecutor.class.getClassLoader()
            );
            
            Class<?> loadedClass = classLoader.loadClass(PACKAGE_NAME + "." + actualClassName);
            if (!KarolProgram.class.isAssignableFrom(loadedClass)) {
                throw new Exception("Class " + actualClassName + " must implement KarolProgram interface");
            }
            return loadedClass;
        } catch (ClassNotFoundException e) {
            throw new Exception("Could not load compiled class: " + e.getMessage());
        }
    }

    /**
     * Extracts the class name from the source code.
     * @param sourceCode The source code to analyze
     * @return The class name, or null if not found
     */
    private static String extractClassName(String sourceCode) {
        // Find the class declaration
        int classIndex = sourceCode.indexOf("class");
        if (classIndex == -1) return null;

        // Find the start of the class name
        int nameStart = classIndex + 5;
        while (nameStart < sourceCode.length() && Character.isWhitespace(sourceCode.charAt(nameStart))) {
            nameStart++;
        }

        // Find the end of the class name
        int nameEnd = nameStart;
        while (nameEnd < sourceCode.length() && 
               (Character.isJavaIdentifierPart(sourceCode.charAt(nameEnd)) || sourceCode.charAt(nameEnd) == '.')) {
            nameEnd++;
        }

        if (nameStart >= nameEnd) return null;
        return sourceCode.substring(nameStart, nameEnd).trim();
    }

    /**
     * Executes a Karol program.
     * @param programClass The class of the program to execute
     * @param karol The robot instance to use
     * @throws Exception if execution fails
     */
    public static void executeProgram(Class<?> programClass, Karol karol) throws Exception {
        Object program = programClass.getDeclaredConstructor().newInstance();
        if (program instanceof KarolProgram) {
            ((KarolProgram) program).run(karol);
        } else {
            throw new Exception("Program does not implement KarolProgram interface");
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