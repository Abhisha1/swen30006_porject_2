package mycontroller;

import controller.CarController;
import world.Car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial;

public class pledgeFuel extends CarController{
	private static ArrayList<Coordinate> parcels_to_collect;
	
	private final int CAR_MAX_SPEED = 1;
	
	private static ArrayList<Coordinate> finalDestination;
	
	private static world.WorldSpatial.RelativeDirection turn;
	
	private static ArrayList<world.WorldSpatial.RelativeDirection> previousMove;
	
	private static boolean inMaze;
	
	private int wallSensitivity = 1;
	
	private static int counter;
	
	public pledgeFuel(Car car) {
		super(car);
		
		// parcels that need to be collected
		parcels_to_collect = new ArrayList<Coordinate>();
		finalDestination = new ArrayList<Coordinate>();
		previousMove = new ArrayList<world.WorldSpatial.RelativeDirection>();
		inMaze = true;
		counter = 1;
	}
	//Coordinate initialGuess;
	// boolean notSouth = true;
	@Override
	public void update() {
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		// checkStateChange();
		if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
			applyForwardAcceleration();   // Tough luck if there's a wall in the way
		}
		System.out.println(world.World.MAP_HEIGHT);
		Coordinate current_coord = new Coordinate(getPosition());
		findParcels();
		if (this.numParcelsFound() < this.numParcels()) {
			// check if a parcel has been collected
			if (isParcelNear(currentView, current_coord)) {
				// get the parcel
			}
			else{
				pathExplorer(currentView);
			}
			discoveredParcel(currentView, current_coord);
			
		}
		else {
			// go home
		}
		
	}
	private boolean isParcelNear(HashMap<Coordinate, MapTile> currentView, Coordinate currentCoord) {
		for(Coordinate parcels: parcels_to_collect) {
			if(Math.abs(currentCoord.x- parcels.x) < 5 && Math.abs(currentCoord.y- parcels.y) < 5) {
				return true;
			}
		}
		return false;
	}
	
	
	private void pathExplorer(HashMap<Coordinate, MapTile> currentView) {
		while(counter != 0) {
			System.out.println("in loop");
			if (!checkWallAhead(getOrientation(), currentView)) {
				System.out.println("STRAIGHT loop");
				previousMove.add(null);
				break;
			}
			else if (!checkRightWall(getOrientation(), currentView)) {
				System.out.println("RIGHT loop");
				turnRight();
				counter +=90;
				previousMove.add(world.WorldSpatial.RelativeDirection.RIGHT);
				break;
			}
			else if (!checkLeftWall(getOrientation(), currentView)) {
				System.out.println("LEFT loop");
				turnLeft();
				counter -=90;
				previousMove.add(world.WorldSpatial.RelativeDirection.LEFT);
				break;
			}
		}
	}
	
	
	private void discoveredParcel(HashMap<Coordinate, MapTile> currentView, Coordinate coord) {
		for (Coordinate parcel_coordinates: parcels_to_collect) {
			if (coord.equals(parcel_coordinates)){
				parcels_to_collect.remove(coord);
//				prevTurns.clear();
				break;
			}
		}
	}
	
	private void findParcels() {
		// searches for parcels
		HashMap<Coordinate, MapTile> currentView = getView();
		
		Coordinate current_pos = new Coordinate(getPosition());
		for(int i = current_pos.x-4; i <= current_pos.x+4; i++) {
			for(int j = current_pos.y-4; j <= current_pos.y+4; j++) {
				Coordinate test_coord = new Coordinate(i,j);
				MapTile tile = currentView.get(test_coord);
				if (tile.isType(MapTile.Type.TRAP)){
					TrapTile trap = (TrapTile) tile;
					if (trap.getTrap() == "parcel" && !parcels_to_collect.contains(test_coord)) {
						parcels_to_collect.add(0, test_coord);
						if (parcels_to_collect.size() == this.numParcels()) {
							break;
						}
					}
				}
				else if(tile.isType(MapTile.Type.FINISH) && !finalDestination.contains(test_coord)) {
					finalDestination.add(test_coord);
				}
			}
		}
	}
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
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
	private boolean checkLeftWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
		default:
			return false;
		}	
	}
	private boolean checkRightWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkSouth(currentView);
		case NORTH:
			return checkEast(currentView);
		case SOUTH:
			return checkWest(currentView);
		case WEST:
			return checkNorth(currentView);
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
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
}
