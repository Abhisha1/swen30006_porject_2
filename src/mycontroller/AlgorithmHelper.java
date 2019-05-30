package mycontroller;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;

import static world.WorldSpatial.Direction.*;

public class AlgorithmHelper {

    public static void discoveredParcel(HashMap<Coordinate, MapTile> currentView, ArrayList<Coordinate> parcelsToCollect, Coordinate coord) {
        /***
         * Checks if one of located parcels has been picked up by user and removes from parcels to collect
         * */
        for (Coordinate parcelCoordinates: parcelsToCollect) {
            if (coord.equals(parcelCoordinates)){
                parcelsToCollect.remove(coord);
                break;
            }
        }
    }

    public static boolean lavaAvoid(HashMap<Coordinate, MapTile> currentView, WorldSpatial.Direction orientation, Coordinate currentCoord) {
        /***
         * Checks if the direction straight ahead has a consecutive sequence of lava. If so, avoid.
         * */
        for(int i = 0; i < 4; i++) {
            switch(orientation) {
                case NORTH:
                    currentCoord = checkUp(currentCoord, currentView);
                    break;
                case WEST:
                    currentCoord = checkLeft(currentCoord,currentView);
                    break;
                case SOUTH:
                    currentCoord = checkDown(currentCoord, currentView);
                    break;
                case EAST:
                    currentCoord = checkRight(currentCoord, currentView);
                    break;
            }
            if(currentCoord == null) {
                return false;
            }

        }return true;
    }

    public static boolean loopAvoidance(HashMap<Coordinate, WorldSpatial.RelativeDirection> prevTurns, EnumReference<WorldSpatial.RelativeDirection> turn, Coordinate coord) {
        /***
         * Checks if player is in loop by seeing if they have made the same move at the same position.
         * If so return true and turn right
         * */
        if (prevTurns.containsKey(coord)) {
            if (prevTurns.get(coord)!= null) {
                turn.setTurn(world.WorldSpatial.RelativeDirection.RIGHT);
            }
            return true;
        }
        return false;
    }

    public static world.WorldSpatial.RelativeDirection advanceHorizontal(ArrayList<Coordinate> destination,WorldSpatial.Direction orientation,
                                                                  HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition) {
        /***
         * Moves horizontally, based on the current position of the car in relation to the destination coordinate
         * */
        switch(orientation) {
            case NORTH:
                if (currentPosition.x < destination.get(0).x) {
                    return world.WorldSpatial.RelativeDirection.RIGHT;
                }
                else if (currentPosition.x > destination.get(0).x) {
                    return world.WorldSpatial.RelativeDirection.LEFT;
                }return null;
            case SOUTH:
                if (currentPosition.x < destination.get(0).x) {
                    return world.WorldSpatial.RelativeDirection.LEFT;
                }
                else if (currentPosition.x > destination.get(0).x) {
                    return world.WorldSpatial.RelativeDirection.RIGHT;
                }return null;
            default:
                return null;
        }
    }

    public static world.WorldSpatial.RelativeDirection advanceVertical(ArrayList<Coordinate> destination, WorldSpatial.Direction orientation,
                                                                HashMap<Coordinate, MapTile> currentView, Coordinate currentPosition) {
        /***
         * Moves vertically, based on the current position of the car in relation to the destination coordinate
         * */
        switch(orientation) {
            case EAST:
                if (currentPosition.y < destination.get(0).y) {
                    return world.WorldSpatial.RelativeDirection.LEFT;
                }
                else if (currentPosition.y > destination.get(0).y) {
                    return world.WorldSpatial.RelativeDirection.RIGHT;
                }return null;
            case WEST:
                if (currentPosition.y < destination.get(0).y) {
                    return world.WorldSpatial.RelativeDirection.RIGHT;
                }
                else if (currentPosition.y > destination.get(0).y) {
                    return world.WorldSpatial.RelativeDirection.LEFT;
                }return null;
            default:
                return null;
        }
    }

    public static boolean shortestPathTurn(ArrayList<Coordinate> destination, WorldSpatial.Direction orientation,
                                     HashMap<Coordinate, MapTile> currentView, Coordinate currentPos,
                                        EnumReference<WorldSpatial.RelativeDirection> turn){
        /***
         * Finds the best turn to get to a specified destination coordinate given the current orientation
         * */
        switch(orientation) {
            case EAST:
                turn.setTurn(AlgorithmHelper.advanceVertical(destination, orientation, currentView, currentPos));
                return true;
            case NORTH:
                turn.setTurn(AlgorithmHelper.advanceHorizontal(destination, orientation, currentView, currentPos));
                return true;
            case SOUTH:
                turn.setTurn(AlgorithmHelper.advanceHorizontal(destination, orientation, currentView, currentPos));
                return true;
            case WEST:
                turn.setTurn(AlgorithmHelper.advanceVertical(destination, orientation, currentView, currentPos));
                return true;
            default:
                return false;
        }
    }

    private static Coordinate checkUp(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
        /***
         * Checks if the coordinate straight above is lava.
         * */
        Coordinate coord =  new Coordinate(currentCoord.x, currentCoord.y+1);
        MapTile tile = currentView.get(coord);
        if (tile.isType(MapTile.Type.TRAP)){
            TrapTile trap = (TrapTile) tile;
            if (trap.getTrap() == "lava") {
                return coord;
            }
        }return null;
    }

    private static Coordinate checkDown(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
        /***
         * Checks if the coordinate straight below is lava.
         * */
        Coordinate coord = new Coordinate(currentCoord.x, currentCoord.y-1);
        MapTile tile = currentView.get(coord);
        if (tile.isType(MapTile.Type.TRAP)){
            TrapTile trap = (TrapTile) tile;
            if (trap.getTrap().equals("lava")) {
                return coord;
            }
        }return null;
    }

    private static Coordinate checkLeft(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
        /***
         * Checks if the coordinate to the left is lava.
         * */
        Coordinate coord = new Coordinate(currentCoord.x+1, currentCoord.y);
        MapTile tile = currentView.get(coord);
        if (tile.isType(MapTile.Type.TRAP)){
            TrapTile trap = (TrapTile) tile;
            if (trap.getTrap().equals("lava")) {
                return coord;
            }
        }return null;
    }

    private static Coordinate checkRight(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
        /***
         * Checks if the coordinate to the right is lava.
         * */
        Coordinate coord = new Coordinate(currentCoord.x-1, currentCoord.y);
        MapTile tile = currentView.get(coord);
        if (tile.isType(MapTile.Type.TRAP)){
            TrapTile trap = (TrapTile) tile;
            if (trap.getTrap().equals("lava")) {
                return coord;
            }
        }return null;
    }


}
