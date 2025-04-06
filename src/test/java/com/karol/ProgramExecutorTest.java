package com.karol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

public class ProgramExecutorTest {
    private World world;
    private Karol karol;

    @BeforeEach
    void setUp() {
        world = new World(5, 5);
        karol = new Karol(2, 2, Robot.Direction.EAST, world);
    }

    @AfterEach
    void cleanup() {
        ProgramExecutor.cleanup();
    }

    @Test
    void testBasicProgramCompilation() throws Exception {
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
        assertNotNull(programClass);
        assertEquals("com.karol.userprograms.TestProgram", programClass.getName());
    }

    @Test
    void testLibraryProgramCompilation() throws Exception {
        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            import com.karol.KarolLibrary;
            
            public class LibraryTest implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    KarolLibrary.turnAround(karol);
                }
            }
            """;

        Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, "LibraryTest");
        assertNotNull(programClass);
        
        // Test program execution
        ProgramExecutor.executeProgram(programClass, karol);
        assertEquals(Robot.Direction.WEST, karol.getDirection());
    }

    @Test
    void testComplexProgram() throws Exception {
        // Add a wall and a beeper to test interaction
        world.addWall(new Wall(3, 2, true));  // Vertical wall at x=3
        world.putBeeper(2, 2);  // Beeper at robot's position

        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            import com.karol.KarolLibrary;
            
            public class ComplexTest implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    // Pick up beeper at current position
                    if (karol.beeperPresent()) {
                        karol.pickBeeper();
                    }
                    
                    // Try to move until wall
                    while (karol.frontIsClear()) {
                        karol.move();
                    }
                    
                    // Turn around and return
                    KarolLibrary.turnAround(karol);
                    KarolLibrary.moveUntilWall(karol);
                }
            }
            """;

        Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, "ComplexTest");
        assertNotNull(programClass);
        
        // Test program execution
        ProgramExecutor.executeProgram(programClass, karol);
        
        // Verify final state
        assertFalse(karol.beeperPresent(), "Beeper should have been picked up");
        assertEquals(0, karol.getX(), "Robot should be at x=0");
        assertEquals(2, karol.getY(), "Robot should be at y=2");
        assertEquals(Robot.Direction.WEST, karol.getDirection());
    }

    @Test
    void testCompilationError() {
        String invalidCode = """
            package com.karol.userprograms;
            
            public class InvalidProgram {
                // Missing implementation of KarolProgram interface
                public void run() {
                    // This should fail because it doesn't implement KarolProgram
                }
            }
            """;

        Exception exception = assertThrows(Exception.class, () -> {
            Class<?> programClass = ProgramExecutor.compileAndLoad(invalidCode, "InvalidProgram");
            ProgramExecutor.executeProgram(programClass, karol);
        });

        String errorMessage = exception.getMessage();
        assertTrue(
            errorMessage.contains("must implement KarolProgram interface") || 
            errorMessage.contains("Compilation failed"),
            "Error message should indicate interface implementation or compilation failure"
        );
    }
} 