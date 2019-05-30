package mycontroller;

import controller.CarController;
import world.Car;

import java.util.ArrayList;
import java.util.HashMap;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial;

public class FuelFocus extends CarController implements IStrategy {
	// Car Speed to move at
	private static final int CAR_MAX_SPEED = 1;

	private static final String BACKTRACK_CLASS = "BackTrack";
	private static final String BACKTRACK_CLASS_RESTEPS = "";
	private static  final String BACKTRACK_CLASS_EXIT = "";

	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 1;

	public Exit exit = null;

	private world.WorldSpatial.RelativeDirection turn;

	private ArrayList<Coordinate> parcelsToCollect;

	private HashMap<Coordinate, world.WorldSpatial.RelativeDirection> prevTurns;

	private ArrayList<Coordinate> finalDestination;

	private ArrayList<Coordinate> recordedSteps = new ArrayList<>();

	private int counter = 1;

	private Coordinate nextBackTrackCoord = null;

	private static boolean retraceHome = false;

	private boolean startedExit = false;

	private ControllerContext context;

	public FuelFocus(Car car, ControllerContext context) {
		super(car);
		this.context = context;
		// Store parcels needing to be collected
		parcelsToCollect = new ArrayList<Coordinate>();
		// Store previous turns and their coordinates
		prevTurns = new HashMap<Coordinate, world.WorldSpatial.RelativeDirection>();

		// Store all destination tiles
		finalDestination = new ArrayList<Coordinate>();
	}

	public void switchStrategy(String strategyName) {
		Bundle bundle = new Bundle();
		bundle.addCoordArray("backtrackTurns", recordedSteps);
		bundle.addCoordinate("exit", exit.getCoord());
		context.switchStrategy("BackTrack", bundle);
	}

	public void parseBundle(Bundle strategyBundle) {

	}

	@Override
	public void update() {

		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		Coordinate currentCoord = new Coordinate(getPosition());
		checkForExit(currentCoord, currentView);
		// checkStateChange();
		if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
			if((this.exit == null) && (!TurnHelper.checkWallAhead(getOrientation(), currentCoord, currentView, wallSensitivity))) {
				applyForwardAcceleration();
			}
		}
		System.out.println(world.World.MAP_HEIGHT);
		if (this.numParcelsFound() < this.numParcels()) {
			// check if a parcel has been collected
			discoveredParcel(currentView, currentCoord);

			// searches for any new parcels
			findParcels();

			// If some parcels have been found already, go to the parcel
			if (parcelsToCollect.size() >0) {
				shortestPathTurn(parcelsToCollect, getOrientation(), currentView);
				loopAvoidance(currentCoord);
				permittedTurn(currentCoord, getOrientation(), currentView);
			}
			// Explore path for parcels
			else {
				pathExplorer(currentView);
			}
		}
		// Player has collected all parcels required and can try exit
		else {
			// An exit has been located
			if(exit != null) {
				// If route stuck in circuit, retrace steps to destination
				if (retraceHome) {
					switchStrategy("BackTrack");
				}
				// Route to destination using shortest path
				else {
					shortestPathTurn(finalDestination, getOrientation(), currentView);
					permittedTurn(currentCoord, getOrientation(), currentView);
					if (loopAvoidance(currentCoord)) {
						retraceHome = true;
					}
				}
			}
			// Explore path for exits
			else {
				pathExplorer(currentView);
				loopAvoidance(currentCoord);
				permittedTurn(currentCoord, getOrientation(), currentView);
			}

		}


	}


	private boolean lavaAvoid(HashMap<Coordinate, MapTile> currentView, Coordinate currentCoord) {
		/***
		 * Checks if the direction straight ahead has a consecutive sequence of lava. If so, avoid.
		 * */
		for(int i = 0; i < 4; i++) {
			switch(getOrientation()) {
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

	private Coordinate checkUp(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
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

	private Coordinate checkDown(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
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

	private Coordinate checkLeft(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
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

	private Coordinate checkRight(Coordinate currentCoord,HashMap<Coordinate, MapTile> currentView) {
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


	private void pathExplorer(HashMap<Coordinate, MapTile> currentView) {
		/***
		 * Uses the pledge algorithm to explore the grid, checking what moves are permitted.
		 * Algorithm also avoids traversing through consecutive block of lava.
		 * */
		Coordinate currentCoord = new Coordinate(getPosition());
		while(counter != 0) {
			// Ensures player will not enter wall by traversing straight ahead nor enter a consective block of lava
			if (!TurnHelper.checkWallAhead(getOrientation(), currentCoord, currentView, wallSensitivity)
					&& !lavaAvoid(currentView, currentCoord)) {
				break;
			}
			// Ensures player will not enter wall by traversing right
			else if (!TurnHelper.checkRightWall(getOrientation(), currentCoord, currentView, wallSensitivity)) {
				turnRight();
				counter +=90;
				break;
			}
			// Ensures player will not enter wall by traversing left
			else if (!TurnHelper.checkLeftWall(getOrientation(),currentCoord, currentView, wallSensitivity)) {
				turnLeft();
				counter -=90;
				break;
			}
		}
	}

	private void checkForExit(Coordinate currentPos, HashMap<Coordinate, MapTile> currentView) {

		for(int i = currentPos.x-4; i <= currentPos.x+4; i++) {
			for (int j = currentPos.y - 4; j <= currentPos.y + 4; j++) {
				Coordinate coord = new Coordinate(i, j);
				MapTile tile = currentView.get(coord);
				if(tile.isType(MapTile.Type.FINISH) && (!startedExit)) {
					System.out.println("found a tile");
					finalDestination.add(coord);
					this.exit = new Exit();
					this.exit.setCoord(coord);
					startedExit = true;
				}
			}
		}
	}


	private void permittedTurn(Coordinate currentCoord, WorldSpatial.Direction orientation,HashMap<Coordinate, MapTile> currentView) {
		/***
		 * Uses the suggested move and turns based on whether the move is permitted; player can move without encountering a wall
		 * If move is invalid, will try another direction until a valid move is found.
		 * */
		if (turn == null) {
			// Player traverses straight
			if(!TurnHelper.checkWallAhead(getOrientation(), currentCoord, currentView, wallSensitivity)) {
				prevTurns.put(currentCoord, null);
			}
			// Player cannot go straight, so try turning left
			else {
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				permittedTurn(currentCoord, orientation,currentView);
			}
		}
		else{
			System.out.print("WILL TURN"+turn.toString());
			switch(turn) {
				case LEFT:
					// Player traverses left
					if (!TurnHelper.checkLeftWall(getOrientation(), currentCoord, currentView, wallSensitivity)) {
						turnLeft();
						turn = null;
						prevTurns.put(currentCoord, world.WorldSpatial.RelativeDirection.LEFT);
						if(exit != null) {
							recordedSteps.add(currentCoord);
						}
						break;
					}
					// Player cannot go straight, so try turning right
					else {
						turn = world.WorldSpatial.RelativeDirection.RIGHT;
						permittedTurn(currentCoord, orientation,currentView);
						break;
					}
				case RIGHT:
					// Player traverses right
					if (!TurnHelper.checkRightWall(getOrientation(), currentCoord, currentView, wallSensitivity)) {
						turnRight();
						prevTurns.put(currentCoord, world.WorldSpatial.RelativeDirection.RIGHT);
						if(exit != null) {
							recordedSteps.add(currentCoord);
						}
						turn = null;
						break;
					}
					// Player cannot go straight, so try traversing straight
					else {
						turn = null;
						permittedTurn(currentCoord, orientation,currentView);
						break;
					}
				default:
					break;
			}
		}
	}

	private boolean loopAvoidance(Coordinate coord) {
		/***
		 * Checks if player is in loop by seeing if they have made the same move at the same position.
		 * If so return true and turn right
		 * */
		if (prevTurns.containsKey(coord)) {
			if (prevTurns.get(coord)!= null) {
				turn = world.WorldSpatial.RelativeDirection.RIGHT;
			}
			return true;
		}
		return false;

	}

	private void discoveredParcel(HashMap<Coordinate, MapTile> currentView, Coordinate coord) {
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


	private void findParcels() {
		/***
		 * Searches for parcels
		 * */
		HashMap<Coordinate, MapTile> currentView = getView();

		Coordinate currentCoord = new Coordinate(getPosition());
		for(int i = currentCoord.x-4; i <= currentCoord.x+4; i++) {
			for(int j = currentCoord.y-4; j <= currentCoord.y+4; j++) {
				Coordinate testCoord = new Coordinate(i,j);
				MapTile tile = currentView.get(testCoord);

				// Checks if tile is an undiscovered parcel
				if (tile.isType(MapTile.Type.TRAP)){
					TrapTile trap = (TrapTile) tile;
					if (trap.getTrap().equals("parcel") && !parcelsToCollect.contains(testCoord)) {
						parcelsToCollect.add(0, testCoord);
						if (parcelsToCollect.size() == this.numParcels()) {
							break;
						}
					}
				}
			}
		}
	}



	private boolean shortestPathTurn(ArrayList<Coordinate> destination,WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		/***
		 * Finds the best turn to get to a specified destination coordinate given the current orientation
		 * */
		switch(orientation) {
			case EAST:
				turn = advanceVertical(destination, orientation, currentView);
				return true;
			case NORTH:
				turn = advanceHorizontal(destination, orientation, currentView);
				return true;
			case SOUTH:
				turn = advanceHorizontal(destination, orientation, currentView);
				return true;
			case WEST:
				turn = advanceVertical(destination, orientation, currentView);
				return true;
			default:
				return false;
		}
	}

	public world.WorldSpatial.RelativeDirection advanceHorizontal(ArrayList<Coordinate> destination,WorldSpatial.Direction orientation,HashMap<Coordinate, MapTile> currentView) {
		/***
		 * Moves horizontally, based on the current position of the car in relation to the destination coordinate
		 * */
		Coordinate currentPosition = new Coordinate(getPosition());
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

	public world.WorldSpatial.RelativeDirection advanceVertical(ArrayList<Coordinate> destination, WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		/***
		 * Moves vertically, based on the current position of the car in relation to the destination coordinate
		 * */
		Coordinate currentPosition = new Coordinate(getPosition());
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

}