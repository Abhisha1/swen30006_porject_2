package mycontroller;

import org.apache.logging.log4j.core.Core;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

import java.util.HashMap;

public class Pledge {
    private Integer counter = 0;
    private int wallSensitivity;
    public static final int RIGHT = 1;
    public static final  int LEFT = -1;

    public Pledge(Integer counter, Integer wallSensitivity) {
        this.wallSensitivity = wallSensitivity;
        this.counter = counter;
    }

    public int pathExplorer(HashMap<Coordinate, MapTile> currentView, WorldSpatial.Direction orientation, Coordinate currentCoord) {
        /***
         * Uses the pledge algorithm to explore the grid, checking what moves are permitted.
         * Algorithm also avoids traversing through consecutive block of lava.
         * */
        while(counter != 0) {
            // Ensures player will not enter wall by traversing straight ahead nor enter a consective block of lava
            if (!TurnHelper.checkWallAhead(orientation, currentCoord, currentView, wallSensitivity)
                    && !AlgorithmHelper.lavaAvoid(currentView, orientation, currentCoord)) {
                return 0;
            }
            // Ensures player will not enter wall by traversing right
            else if (!TurnHelper.checkRightWall(orientation, currentCoord, currentView, wallSensitivity)) {
                counter +=90;
                return RIGHT;
            }
            // Ensures player will not enter wall by traversing left
            else if (!TurnHelper.checkLeftWall(orientation,currentCoord, currentView, wallSensitivity)) {
                counter -=90;
                return LEFT;
            }
        }
        return 0;
    }
}
