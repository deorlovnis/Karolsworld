package com.karol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import com.karol.Robot.Direction;

public class ProgramExecutorTest {
    private World world;
    private Karol karol;

    @BeforeEach
    void setUp() {
        // Make world bigger to avoid wall collisions
        world = new World(10, 10);
        karol = new Karol(2, 2, Direction.NORTH, world);
    }

    @AfterEach
    void tearDown() {
        ProgramExecutor.cleanup();
    }

    @Test
    void testValidProgramCompilation() throws Exception {
        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class TestProgram implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    karol.move();
                }
            }
            """;
        
        Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, "TestProgram");
        assertNotNull(programClass, "Program class should be compiled and loaded");
        
        // Test program execution
        ProgramExecutor.executeProgram(programClass, karol);
        assertEquals(3, karol.getY(), "Karol should move one step");
    }

    @Test
    void testInvalidProgramSyntax() {
        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class InvalidProgram implements KarolProgram {
                This is not valid Java syntax
            }
            """;
        
        Exception exception = assertThrows(Exception.class, () -> {
            ProgramExecutor.compileAndLoad(sourceCode, "InvalidProgram");
        });
        String errorMessage = exception.getMessage().toLowerCase();
        System.out.println("Actual error message: " + errorMessage);
        assertTrue(errorMessage.contains("compilation failed") && 
                  (errorMessage.contains("error") || errorMessage.contains("';' expected")),
                  "Error message should indicate syntax error. Got: " + errorMessage);
    }

    @Test
    void testMissingKarolProgramInterface() {
        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.Karol;
            
            public class MissingInterface {
                public void run(Karol karol) {
                    karol.move();
                }
            }
            """;
        
        Exception exception = assertThrows(Exception.class, () -> {
            ProgramExecutor.compileAndLoad(sourceCode, "MissingInterface");
        });
        assertTrue(exception.getMessage().toLowerCase().contains("implement") || 
                  exception.getMessage().toLowerCase().contains("interface") ||
                  exception.getMessage().toLowerCase().contains("karolprogram"),
                  "Error message should indicate missing interface implementation");
    }

    @Test
    void testProgramWithInvalidPackage() {
        String sourceCode = """
            package com.invalid.package;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class WrongPackage implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    karol.move();
                }
            }
            """;
        
        Exception exception = assertThrows(Exception.class, () -> {
            ProgramExecutor.compileAndLoad(sourceCode, "WrongPackage");
        });
        String errorMessage = exception.getMessage().toLowerCase();
        System.out.println("Invalid package error: " + errorMessage);
        assertTrue(errorMessage.contains("invalid package") || 
                  errorMessage.contains("must be: com.karol.userprograms"),
                  "Error message should indicate invalid package. Got: " + errorMessage);
    }

    @Test
    void testComplexProgram() throws Exception {
        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class ComplexProgram implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    karol.move();
                    karol.turnLeft();
                    karol.move();
                    karol.turnRight();
                }
            }
            """;
        
        Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, "ComplexProgram");
        assertNotNull(programClass, "Program class should be compiled and loaded");
        
        // Test program execution
        ProgramExecutor.executeProgram(programClass, karol);
        assertEquals(3, karol.getY(), "Karol should move one step up");
        assertEquals(1, karol.getX(), "Karol should move one step left");
    }
} 