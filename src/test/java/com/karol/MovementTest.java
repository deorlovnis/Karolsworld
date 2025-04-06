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

    @Test
    void testBeepersInventory() {
        // Initially no beepers
        assertEquals(0, karol.getBeepersInBag(), "Should start with no beepers");
        assertFalse(karol.hasBeeper(), "Should not have any beepers initially");

        // Put a beeper in the world and pick it up
        world.putBeeper(2, 2);
        assertTrue(karol.beeperPresent(), "Should detect beeper at current position");
        karol.pickBeeper();
        assertEquals(1, karol.getBeepersInBag(), "Should have one beeper after picking up");
        assertTrue(karol.hasBeeper(), "Should have a beeper after picking up");

        // Try to put down the beeper
        karol.putBeeper();
        assertEquals(0, karol.getBeepersInBag(), "Should have no beepers after putting down");
        assertFalse(karol.hasBeeper(), "Should not have any beepers after putting down");

        // Try to put down a beeper without having any
        assertThrows(IllegalStateException.class, () -> karol.putBeeper(),
            "Putting beeper without having any should throw exception");
    }

    @Test
    void testAdvancedMovement() {
        // Test moveUntilWall
        karol.moveUntilWall();
        assertEquals(4, karol.getX(), "Should move until last position before wall");
        
        // Test turnAround
        karol.turnAround();
        assertEquals(Robot.Direction.WEST, karol.getDirection(), "Should be facing west after turn around");
        
        // Test moveSteps
        karol.moveSteps(2);
        assertEquals(2, karol.getX(), "Should move back 2 steps");
    }

    @Test
    void testMultipleBeepers() {
        // Put multiple beepers and pick them all
        world.putBeeper(2, 2);
        world.putBeeper(2, 2);
        world.putBeeper(2, 2);
        
        // Pick up beepers one by one
        karol.pickBeeper();
        karol.pickBeeper();
        karol.pickBeeper();
        assertEquals(3, karol.getBeepersInBag(), "Should have picked up all three beepers");

        // Try to put down more beepers than we have
        assertThrows(IllegalStateException.class, () -> karol.putBeepers(4),
            "Putting more beepers than available should throw exception");

        // Put down multiple beepers
        karol.putBeepers(2);
        assertEquals(1, karol.getBeepersInBag(), "Should have one beeper left after putting down two");
    }
} 