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
    void testAdvancedProgramCompilation() throws Exception {
        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class AdvancedTest implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    karol.moveUntilWall();
                    karol.turnAround();
                }
            }
            """;

        Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, "AdvancedTest");
        assertNotNull(programClass);
        
        // Test program execution
        ProgramExecutor.executeProgram(programClass, karol);
        assertEquals(4, karol.getX(), "Should have moved to wall");
        assertEquals(Robot.Direction.WEST, karol.getDirection(), "Should be facing west");
    }

    @Test
    void testComplexProgram() throws Exception {
        // Add a wall and beepers to test interaction
        world.addWall(new Wall(3, 2, true));  // Vertical wall at x=3
        world.putBeeper(2, 2);  // Beeper at robot's position
        world.putBeeper(2, 2);  // Second beeper at same position

        String sourceCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class ComplexTest implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    // Pick up beepers at current position
                    while (karol.beeperPresent()) {
                        karol.pickBeeper();
                    }
                    
                    // Move until wall and put one beeper
                    while (karol.frontIsClear()) {
                        karol.move();
                    }
                    karol.putBeeper();
                    
                    // Turn around and return, putting last beeper
                    karol.turnAround();
                    karol.moveUntilWall();
                    karol.putBeeper();
                }
            }
            """;

        Class<?> programClass = ProgramExecutor.compileAndLoad(sourceCode, "ComplexTest");
        assertNotNull(programClass);
        
        // Test program execution
        ProgramExecutor.executeProgram(programClass, karol);
        
        // Verify final state
        assertEquals(0, karol.getBeepersInBag(), "Should have used all beepers");
        assertEquals(0, karol.getX(), "Should be at x=0");
        assertEquals(2, karol.getY(), "Should be at y=2");
        assertEquals(Robot.Direction.WEST, karol.getDirection(), "Should be facing west");
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

    @Test
    void testBeepersError() {
        String invalidBeeperCode = """
            package com.karol.userprograms;
            
            import com.karol.KarolProgram;
            import com.karol.Karol;
            
            public class BeepersTest implements KarolProgram {
                @Override
                public void run(Karol karol) {
                    // Try to put down a beeper without having any
                    karol.putBeeper();
                }
            }
            """;

        assertThrows(IllegalStateException.class, () -> {
            Class<?> programClass = ProgramExecutor.compileAndLoad(invalidBeeperCode, "BeepersTest");
            ProgramExecutor.executeProgram(programClass, karol);
        }, "Should throw exception when trying to put down beeper without having any");
    }
} 