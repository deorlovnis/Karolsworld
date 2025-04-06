package com.karol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KarolLibraryTest {
    private World world;
    private Karol karol;
    
    @BeforeEach
    void setUp() {
        // Create a 5x5 world for testing
        world = new World(5, 5);
        karol = new Karol(2, 2, Robot.Direction.EAST, world);
    }
    
    @Test
    void testTurnRight() {
        KarolLibrary.turnRight(karol);
        assertEquals(Robot.Direction.SOUTH, karol.getDirection(), "Should turn right from EAST to SOUTH");
        
        KarolLibrary.turnRight(karol);
        assertEquals(Robot.Direction.WEST, karol.getDirection(), "Should turn right from SOUTH to WEST");
        
        KarolLibrary.turnRight(karol);
        assertEquals(Robot.Direction.NORTH, karol.getDirection(), "Should turn right from WEST to NORTH");
        
        KarolLibrary.turnRight(karol);
        assertEquals(Robot.Direction.EAST, karol.getDirection(), "Should turn right from NORTH to EAST");
    }
    
    @Test
    void testTurnAround() {
        // Test from each direction
        KarolLibrary.turnAround(karol); // From EAST
        assertEquals(Robot.Direction.WEST, karol.getDirection(), "Should turn from EAST to WEST");
        
        KarolLibrary.turnAround(karol); // Back to EAST
        assertEquals(Robot.Direction.EAST, karol.getDirection(), "Should turn from WEST to EAST");
        
        karol.turnLeft(); // Now facing NORTH
        KarolLibrary.turnAround(karol);
        assertEquals(Robot.Direction.SOUTH, karol.getDirection(), "Should turn from NORTH to SOUTH");
    }
    
    @Test
    void testMoveSteps() {
        KarolLibrary.moveSteps(karol, 2);
        assertEquals(4, karol.getX(), "Should move 2 steps east");
        assertEquals(2, karol.getY(), "Y coordinate should not change");
        
        // Test moving to wall
        assertThrows(IllegalStateException.class, () -> KarolLibrary.moveSteps(karol, 2),
            "Should throw exception when trying to move beyond world boundary");
    }
    
    @Test
    void testMoveUntilWall() {
        KarolLibrary.moveUntilWall(karol);
        assertEquals(4, karol.getX(), "Should move to last position before wall");
        assertEquals(2, karol.getY(), "Y coordinate should not change");
        
        // Test with wall in the middle
        karol = new Karol(2, 2, Robot.Direction.EAST, world);
        world.addWall(new Wall(3, 2, true));
        KarolLibrary.moveUntilWall(karol);
        assertEquals(2, karol.getX(), "Should stop before wall");
    }
    
    @Test
    void testPutBeepers() {
        KarolLibrary.putBeepers(karol, 3);
        assertTrue(karol.beeperPresent(), "Should have beepers at current position");
        
        // Pick up beepers one by one to verify count
        for (int i = 0; i < 3; i++) {
            karol.pickBeeper();
        }
        assertFalse(karol.beeperPresent(), "Should have picked up all beepers");
        assertThrows(IllegalStateException.class, () -> karol.pickBeeper(),
            "Should throw exception when no more beepers to pick");
    }
    
    @Test
    void testPickAllBeepers() {
        // Place multiple beepers
        KarolLibrary.putBeepers(karol, 3);
        assertTrue(karol.beeperPresent(), "Should have beepers at current position");
        
        KarolLibrary.pickAllBeepers(karol);
        assertFalse(karol.beeperPresent(), "Should have picked up all beepers");
    }
    
    @Test
    void testFaceDirections() {
        // Test faceNorth
        KarolLibrary.faceNorth(karol);
        assertEquals(Robot.Direction.NORTH, karol.getDirection(), "Should face north");
        
        // Test faceEast
        KarolLibrary.faceEast(karol);
        assertEquals(Robot.Direction.EAST, karol.getDirection(), "Should face east");
        
        // Test faceSouth
        KarolLibrary.faceSouth(karol);
        assertEquals(Robot.Direction.SOUTH, karol.getDirection(), "Should face south");
        
        // Test faceWest
        KarolLibrary.faceWest(karol);
        assertEquals(Robot.Direction.WEST, karol.getDirection(), "Should face west");
    }
} 