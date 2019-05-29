package mycontroller;

import controller.CarController;
import org.lwjgl.Sys;
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
public class fuelFocus extends CarController{
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 1;

	public Exit exit = null;
	
	// Car Speed to move at
	private final int CAR_MAX_SPEED = 1;
	
	private static world.WorldSpatial.RelativeDirection turn;
	
	private static ArrayList<Coordinate> parcels_to_collect;
	
	private static HashMap<Coordinate, world.WorldSpatial.RelativeDirection> prevTurns;
	
	private static ArrayList<Coordinate> finalDestination;

	private ArrayList<Coordinate> recordedSteps = new ArrayList<>();

	private Coordinate nextBackTrackCoord = null;

	private boolean startedExit = false;
	private int moveXDir = 0;
	private int moveYDir = 0;
	private boolean wentLeft = false;
	public fuelFocus(Car car) {
		super(car);
		
		// parcels that need to be collected
		parcels_to_collect = new ArrayList<Coordinate>();
		prevTurns = new HashMap<Coordinate, world.WorldSpatial.RelativeDirection>();
		finalDestination = new ArrayList<Coordinate>();
	}

	void goToPos(Coordinate coord, Coordinate currentPos) {
		if(coord.x > currentPos.x) {

		}else if(coord.x < currentPos.x) {

		}

	}
	
	// Coordinate initialGuess;
	// boolean notSouth = true;
	@Override
	public void update() {


		//goToPos(new Coordinate(35, 1), new Coordinate(getPosition()));

		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		checkForExit(new Coordinate(getPosition()), currentView);
		// checkStateChange();
		if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
			 if((this.exit == null) && (!checkWallAhead(getOrientation(), currentView))) {
				 applyForwardAcceleration();
			 }
		}
		System.out.println(world.World.MAP_HEIGHT);
		Coordinate current_coord = new Coordinate(getPosition());
		if (this.numParcelsFound() < this.numParcels()) {
			// check if a parcel has been collected
			discovered_parcel(currentView, current_coord);

			// searches for any new parcels
			find_parcels();
			System.out.println("num of parcels located is" + parcels_to_collect.size());
			// tries navigating on short route to parcel
			if (parcels_to_collect.size() >0) {
				if (shortest_path_turn(parcels_to_collect, getOrientation(), currentView)) {

				}
			}
			loopAvoidance(current_coord);

			// car turns direction if path has a recommended turn
			permitted_Turn(current_coord, getOrientation(), currentView);
		}
		else {
			startedExit = true;
			if(exit != null) {
				System.out.println("FOUND EXIT");
				goToExit(current_coord);
			}else {

			}

		}

		
	}

	private void turnToOrient(WorldSpatial.Direction orientation, WorldSpatial.Direction neededOrientation) {
		if(orientation != neededOrientation) {
			if(checkLeftWall(orientation, getView())) {
				turnRight();
			}else {
				turnLeft();
			}
		}
	}

	private boolean moveX(float x) {
		boolean madeTurn = false;
		if(x > 0) {
			System.out.println("NEED TO MOVE EAST");

			if(getOrientation() == WorldSpatial.Direction.EAST) {
				if(getSpeed() == 0) {
					applyForwardAcceleration();
				}

			}else if(getOrientation() == WorldSpatial.Direction.WEST) {
				if(getSpeed() == 0) {
					applyReverseAcceleration();
				}

			}else {
				madeTurn = true;
				applyBrake();
				turnToOrient(getOrientation(), WorldSpatial.Direction.EAST);
			}


		}else if(x < 0){
			System.out.println("NEED TO MOVE WEST");
			System.out.println(getOrientation());
			if(getOrientation() == WorldSpatial.Direction.WEST) {
				applyForwardAcceleration();

			}else if(getOrientation() == WorldSpatial.Direction.EAST) {
				applyReverseAcceleration();

			}else {
				madeTurn = true;
				applyBrake();
				turnToOrient(getOrientation(), WorldSpatial.Direction.WEST);
			}

		}else {
			applyBrake();
		}
		return madeTurn;
	}

	private void moveY(float y) {
		if(y > 0) {
			System.out.println("NEED TO MOVE NORTH");
			if(getOrientation() == WorldSpatial.Direction.NORTH) {
				if(getSpeed() == 0) {
					applyForwardAcceleration();
				}

			}else if(getOrientation() == WorldSpatial.Direction.SOUTH) {
				if(getSpeed() == 0) {
					applyReverseAcceleration();
				}
			}else {
				applyBrake();
				turnToOrient(getOrientation(), WorldSpatial.Direction.NORTH);
			}

		}else {
			System.out.println("NEED TO MOVE SOUTH");
			if(getOrientation() == WorldSpatial.Direction.SOUTH) {
				applyForwardAcceleration();
			}else if(getOrientation() == WorldSpatial.Direction.NORTH) {
				applyReverseAcceleration();
			}else {
				applyBrake();
				turnToOrient(getOrientation(), WorldSpatial.Direction.SOUTH);
			}

		}
	}

	private void goToExit(Coordinate currentPos) {

		if(nextBackTrackCoord == null) {
			if(recordedSteps.size() == 0) {
				nextBackTrackCoord = exit.getCoord();
			}else {
				nextBackTrackCoord = recordedSteps.remove(recordedSteps.size()-1);
			}
		}
		if((nextBackTrackCoord.x == currentPos.x) && (nextBackTrackCoord.y == currentPos.y)) {
			nextBackTrackCoord = null;
		}else {
			System.out.println("COORD TO REACH is " + nextBackTrackCoord.toString());
			if(nextBackTrackCoord.x != currentPos.x) {
				System.out.println("NEED TO MOVE IN X");
				moveX(nextBackTrackCoord.x - currentPos.x);
			}
			if(nextBackTrackCoord.y != currentPos.y) {
				System.out.println("NEED TO MOVE IN Y");
				moveY(nextBackTrackCoord.y - currentPos.y);
			}
		}
	}

	private void checkForExit(Coordinate currentPos, HashMap<Coordinate, MapTile> currentView) {

		for(int i = currentPos.x-4; i <= currentPos.x+4; i++) {
			for (int j = currentPos.y - 4; j <= currentPos.y + 4; j++) {
				Coordinate coord = new Coordinate(i, j);
				MapTile tile = currentView.get(coord);
				if(tile.isType(MapTile.Type.FINISH) && !startedExit) {
					this.exit = new Exit();
					this.exit.setCoord(coord);

				}
			}
		}
	}


	private void permitted_Turn(Coordinate current_coord, WorldSpatial.Direction orientation,HashMap<Coordinate, MapTile> currentView) {
		if (turn == null) {
			if(!checkWallAhead(getOrientation(), currentView)) {
				prevTurns.put(current_coord, null);
			}
			else {
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				permitted_Turn(current_coord, orientation,currentView);
			}
		}
		else{
			System.out.print("WILL TURN"+turn.toString());
			switch(turn) {
			case LEFT:
				if (!checkLeftWall(getOrientation(), currentView)) {
					turnLeft();
					turn = null;
					prevTurns.put(current_coord, world.WorldSpatial.RelativeDirection.LEFT);
					if(exit != null) {
						recordedSteps.add(current_coord);
					}
					break;
				}
				else {
					turn = world.WorldSpatial.RelativeDirection.RIGHT;
					permitted_Turn(current_coord, orientation,currentView);
					break;
				}
			case RIGHT:
				if (!checkRightWall(getOrientation(), currentView)) {
					turnRight();
					prevTurns.put(current_coord, world.WorldSpatial.RelativeDirection.RIGHT);
					if(exit != null) {
						recordedSteps.add(current_coord);
					}
					turn = null;
					break;
				}
				else {
					turn = null;
					permitted_Turn(current_coord, orientation,currentView);
					break;
				}
			default:
				break;
			}
		}
	}
	
	private void loopAvoidance(Coordinate coord) {
		if (prevTurns.containsKey(coord) && prevTurns.get(coord)!= null) {
			turn = world.WorldSpatial.RelativeDirection.RIGHT;
		}
		
	}
	
	private void discovered_parcel(HashMap<Coordinate, MapTile> currentView, Coordinate coord) {
		for (Coordinate parcel_coordinates: parcels_to_collect) {
			if (coord.equals(parcel_coordinates)){
				parcels_to_collect.remove(coord);
//				prevTurns.clear();
				break;
			}
		}
	}
	
	
	private void find_parcels() {
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
	
	
	
	private boolean shortest_path_turn(ArrayList<Coordinate> destination,WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation) {
		case EAST:
			turn = advance_vertical(destination, orientation, currentView);
			return true;
		case NORTH:
			turn = advance_horizontal(destination, orientation, currentView);
			return true;
		case SOUTH:
			turn = advance_horizontal(destination, orientation, currentView);
			return true;
		case WEST:
			turn = advance_vertical(destination, orientation, currentView);
			return true;
		default:
			return false;
		}
	}
	
	public world.WorldSpatial.RelativeDirection advance_horizontal(ArrayList<Coordinate> destination,WorldSpatial.Direction orientation,HashMap<Coordinate, MapTile> currentView) {
		Coordinate currentPosition = new Coordinate(getPosition());
		System.out.print("the parcel x is "+destination.get(0).x+"but xar"+currentPosition.x);
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
	
	public world.WorldSpatial.RelativeDirection advance_vertical(ArrayList<Coordinate> destination, WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		Coordinate currentPosition = new Coordinate(getPosition());
		System.out.print("the parcel y is "+destination.get(0).y+"but yar"+currentPosition.y);
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

