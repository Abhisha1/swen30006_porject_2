package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.HashMap;

public class TurnHelper {
    /**
     * Check if the wall is on your left hand side given your orientation
     * @param orientation
     * @param currentView
     * @return
     */
    public static boolean checkLeftWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView, Coordinate currentPos, int wallSense) {

        switch(orientation){
            case EAST:
                return checkNorth(currentView, currentPos, wallSense);
            case NORTH:
                return checkWest(currentView, currentPos, wallSense);
            case SOUTH:
                return checkEast(currentView, currentPos, wallSense);
            case WEST:
                return checkSouth(currentView, currentPos, wallSense);
            default:
                return false;
        }
    }

    /**
     * Check if the wall is on your ahead given your orientation
     * @param orientation
     * @param currentView
     * @return
     */
    public static boolean checkWallAhead(WorldSpatial.Direction orientation, Coordinate currentPos, HashMap<Coordinate, MapTile> currentView, int wallSensitivity){
        switch(orientation){
            case EAST:
                return TurnHelper.checkEast(currentView, currentPos, wallSensitivity);
            case NORTH:
                return TurnHelper.checkNorth(currentView, currentPos, wallSensitivity);
            case SOUTH:
                return TurnHelper.checkSouth(currentView, currentPos, wallSensitivity);
            case WEST:
                return TurnHelper.checkWest(currentView, currentPos, wallSensitivity);
            default:
                return false;
        }
    }

    /**
     * Check if the wall is on your left hand side given your orientation
     * @param orientation
     * @param currentView
     * @return
     */
    public static boolean checkLeftWall(WorldSpatial.Direction orientation, Coordinate currentPos, HashMap<Coordinate, MapTile> currentView, int wallSensitivity) {
        switch(orientation){
            case EAST:
                return TurnHelper.checkNorth(currentView, currentPos, wallSensitivity);
            case NORTH:
                return TurnHelper.checkWest(currentView, currentPos, wallSensitivity);
            case SOUTH:
                return TurnHelper.checkEast(currentView, currentPos, wallSensitivity);
            case WEST:
                return TurnHelper.checkSouth(currentView, currentPos, wallSensitivity);
            default:
                return false;
        }
    }
    /**
     * Check if the wall is on your right hand side given your orientation
     * @param orientation
     * @param currentView
     * @return
     */
    public static boolean checkRightWall(WorldSpatial.Direction orientation, Coordinate currentPos, HashMap<Coordinate, MapTile> currentView, int wallSensitivity) {
        switch(orientation){
            case EAST:
                return TurnHelper.checkSouth(currentView, currentPos, wallSensitivity);
            case NORTH:
                return TurnHelper.checkEast(currentView, currentPos, wallSensitivity);
            case SOUTH:
                return TurnHelper.checkWest(currentView, currentPos, wallSensitivity);
            case WEST:
                return TurnHelper.checkNorth(currentView, currentPos, wallSensitivity);
            default:
                return false;
        }
    }

    /**
     * Method below just iterates through the list and check in the correct coordinates.
     * i.e. Given your current position is 10,10
     * checkEast will check up to wallSensitivity amount of tiles to the right.
     * checkWest will check up to wallSensitivity amount of tiles to the left.
     * checkNorth will check up to wallSensitivity amount of tiles to the top.
     * checkSouth will check up to wallSensitivity amount of tiles below.
     */
    public static boolean checkEast(HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition, int wallSensitivity){

        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public static boolean checkWest(HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition, int wallSensitivity){
        // Check tiles to my left
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public static boolean checkNorth(HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition, int wallSensitivity){
        // Check tiles to towards the top
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public static boolean checkSouth(HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition, int wallSensitivity){
        // Check tiles towards the bottom
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }
}
