package com.karol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovementTest {
    private World world;
    private Karol karol;
    
    @BeforeEach
    void setUp() {
        // Create a 5x5 world for testing
        world = new World(5, 5);
        karol = new Karol(2, 2, Robot.Direction.EAST, world);
    }
    
    @Test
    void testMoveIntoEmptySpace() {
        // Test moving east into empty space
        assertTrue(karol.frontIsClear(), "Should be able to move east into empty space");
        karol.move();
        assertEquals(3, karol.getX(), "Should have moved one step east");
        assertEquals(2, karol.getY(), "Y coordinate should not change when moving east");
        
        // Test moving north into empty space
        karol.turnLeft(); // Now facing north
        assertTrue(karol.frontIsClear(), "Should be able to move north into empty space");
        karol.move();
        assertEquals(3, karol.getX(), "X coordinate should not change when moving north");
        assertEquals(3, karol.getY(), "Should have moved one step north");
    }
    
    @Test
    void testMoveAgainstWorldBoundary() {
        // Move to edge of world
        karol = new Karol(4, 2, Robot.Direction.EAST, world);
        assertFalse(karol.frontIsClear(), "Should not be able to move beyond world boundary");
        assertThrows(IllegalStateException.class, () -> karol.move(), 
            "Moving beyond world boundary should throw exception");
    }
    
    @Test
    void testMoveAgainstWall() {
        // Place a wall in front of Karol
        world.addWall(new Wall(3, 2, true));
        assertFalse(karol.frontIsClear(), "Should not be able to move through wall");
        assertThrows(IllegalStateException.class, () -> karol.move(), 
            "Moving into wall should throw exception");
    }
    
    @Test
    void testCompleteRotation() {
        assertEquals(Robot.Direction.EAST, karol.getDirection());
        karol.turnLeft();
        assertEquals(Robot.Direction.NORTH, karol.getDirection());
        karol.turnLeft();
        assertEquals(Robot.Direction.WEST, karol.getDirection());
        karol.turnLeft();
        assertEquals(Robot.Direction.SOUTH, karol.getDirection());
        karol.turnLeft();
        assertEquals(Robot.Direction.EAST, karol.getDirection(), 
            "Should be back facing east after four left turns");
    }
    
    @Test
    void testMoveInAllDirections() {
        // Test moving in all four directions in empty space
        int startX = 2;
        int startY = 2;
        
        // Move east
        assertTrue(karol.frontIsClear());
        karol.move();
        assertEquals(startX + 1, karol.getX());
        assertEquals(startY, karol.getY());
        
        // Move north
        karol.turnLeft();
        assertTrue(karol.frontIsClear());
        karol.move();
        assertEquals(startX + 1, karol.getX());
        assertEquals(startY + 1, karol.getY());
        
        // Move west
        karol.turnLeft();
        assertTrue(karol.frontIsClear());
        karol.move();
        assertEquals(startX, karol.getX());
        assertEquals(startY + 1, karol.getY());
        
        // Move south
        karol.turnLeft();
        assertTrue(karol.frontIsClear());
        karol.move();
        assertEquals(startX, karol.getX());
        assertEquals(startY, karol.getY());
    }
} 