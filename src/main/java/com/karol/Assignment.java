package com.karol;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Assignment {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("worldWidth")
    private int worldWidth;
    
    @JsonProperty("worldHeight")
    private int worldHeight;
    
    @JsonProperty("initialRobots")
    private List<Robot> initialRobots;
    
    @JsonProperty("walls")
    private List<Wall> walls;
    
    @JsonProperty("beepers")
    private List<Beeper> beepers;

    public Assignment() {
        this.initialRobots = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.beepers = new ArrayList<>();
    }

    public Assignment(String name, String description, int worldWidth, int worldHeight) {
        this.name = name;
        this.description = description;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.initialRobots = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.beepers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public List<Robot> getInitialRobots() {
        return initialRobots;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Beeper> getBeepers() {
        return beepers;
    }

    public void addRobot(Robot robot) {
        initialRobots.add(robot);
    }

    public void addWall(Wall wall) {
        walls.add(wall);
    }

    public void addBeeper(Beeper beeper) {
        beepers.add(beeper);
    }
} 