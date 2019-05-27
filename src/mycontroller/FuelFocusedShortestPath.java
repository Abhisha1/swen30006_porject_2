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
public class FuelFocusedShortestPath extends CarController{


	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 1;
	
	private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.
	
	// Car Speed to move at
	private final int CAR_MAX_SPEED = 1;
	
	private static world.WorldSpatial.RelativeDirection turn;
	
	private static ArrayList<Coordinate> parcels_to_collect;
	
	public FuelFocusedShortestPath(Car car) {
		super(car);
//		HashMap<Coordinate, MapTile> currentView = getView();
		parcels_to_collect = new ArrayList<Coordinate>();
//		for(HashMap.Entry<Coordinate, MapTile> entry: currentView.entrySet()) {
//			MapTile tile = entry.getValue();
//			if (tile.isType(MapTile.Type.TRAP)){
//				TrapTile trap = (TrapTile) tile;
//				if (trap.getTrap() == "parcel") {
//					System.out.println("got a parcel");
//					parcels_to_collect.add(entry.getKey());
//					if (parcels_to_collect.size() == this.numParcels()) {
//						break;
//					}
//				}
//			}
//		}
	}
	
	// Coordinate initialGuess;
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
		discovered_parcel(currentView, current_coord);
		if (collisionAvoidance(getOrientation(),currentView)) {
			
		}
		else if (parcels_to_collect.size() >0) {
			if (shortest_path_turn(getOrientation(), currentView)) {
				
			}	
		}
		else {
			find_parcels();
		}
		if (turn == null) {
			
		}
		else{
			System.out.print("WILL TURN"+turn.toString());
			switch(turn) {
			case LEFT:
				turnLeft();
				turn = null;
				break;
			case RIGHT:
				turnRight();
				turn = null;
				break;
			default:
				break;
			}
		}
		
	}
	
	private void discovered_parcel(HashMap<Coordinate, MapTile> currentView, Coordinate coord) {
		MapTile tile = currentView.get(coord);
		if (tile.isType(MapTile.Type.TRAP)){
			TrapTile trap = (TrapTile) currentView.get(coord);
			System.out.println("checkie tile is "+ trap.getTrap());
			if (trap.getTrap() == "parcel" && parcels_to_collect.contains(coord)) {
				System.out.println("GHEJGEHJGEGJEFGEGJGRE");
				parcels_to_collect.remove(coord);
			}
		}
	}
	
	
	private void find_parcels() {
		HashMap<Coordinate, MapTile> currentView = getView();
		
		Coordinate current_pos = new Coordinate(getPosition());
		for(int i = current_pos.x-4; i <= current_pos.x+4; i++) {
			for(int j = current_pos.y-4; j <= current_pos.y+4; j++) {
				Coordinate test_coord = new Coordinate(i,j);
				MapTile tile = currentView.get(test_coord);
				if (tile.isType(MapTile.Type.TRAP)){
					TrapTile trap = (TrapTile) tile;
					if (trap.getTrap() == "parcel" && !parcels_to_collect.contains(test_coord)) {
						System.out.println("got a parcel");
						parcels_to_collect.add(test_coord);
						if (parcels_to_collect.size() == this.numParcels()) {
							break;
						}
					}
				}
			}
		}
	}
	
	
	
	private boolean shortest_path_turn(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation) {
		case EAST:
			turn = advance_vertical(orientation, currentView);
			return true;
		case NORTH:
			turn = advance_horizontal(orientation, currentView);
			return true;
		case SOUTH:
			turn = advance_horizontal(orientation, currentView);
			return true;
		case WEST:
			turn = advance_vertical(orientation, currentView);
			return true;
		default:
			return false;
		}
	}
	public world.WorldSpatial.RelativeDirection advance_horizontal(WorldSpatial.Direction orientation,HashMap<Coordinate, MapTile> currentView) {
		Coordinate currentPosition = new Coordinate(getPosition());
		Coordinate nextPosition;
		System.out.print("the parcel x is "+parcels_to_collect.get(0).x+"but xar"+currentPosition.x);
		switch(orientation) {
		case NORTH:
			if (currentPosition.x < parcels_to_collect.get(0).x) {
				nextPosition = new Coordinate(currentPosition.x-1, currentPosition.y);
				return world.WorldSpatial.RelativeDirection.RIGHT; 
			}
			else if (currentPosition.x > parcels_to_collect.get(0).x) {
				nextPosition = new Coordinate(currentPosition.x-1, currentPosition.y);
				return world.WorldSpatial.RelativeDirection.LEFT;
			}return null;
		case SOUTH:
			if (currentPosition.x < parcels_to_collect.get(0).x) {
				nextPosition = new Coordinate(currentPosition.x-1, currentPosition.y);
				return world.WorldSpatial.RelativeDirection.LEFT;
			}
			else if (currentPosition.x > parcels_to_collect.get(0).x) {
				nextPosition = new Coordinate(currentPosition.x-1, currentPosition.y);
				return world.WorldSpatial.RelativeDirection.RIGHT;
			}return null;
		default:
			return null;
		}
	}
	public world.WorldSpatial.RelativeDirection advance_vertical(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		Coordinate currentPosition = new Coordinate(getPosition());
		Coordinate nextPosition;
		System.out.print("the parcel y is "+parcels_to_collect.get(0).y+"but yar"+currentPosition.y);
		switch(orientation) {
		case EAST:
			if (currentPosition.y < parcels_to_collect.get(0).y) {
				nextPosition = new Coordinate(currentPosition.x, currentPosition.y+1);
				return world.WorldSpatial.RelativeDirection.LEFT; 
			}
			else if (currentPosition.y > parcels_to_collect.get(0).y) {
				nextPosition = new Coordinate(currentPosition.x, currentPosition.y-1);
				return world.WorldSpatial.RelativeDirection.RIGHT;
			}return null;
		case WEST:
			if (currentPosition.y < parcels_to_collect.get(0).y) {
				nextPosition = new Coordinate(currentPosition.x, currentPosition.y+1);
				return world.WorldSpatial.RelativeDirection.RIGHT;
			}
			else if (currentPosition.y > parcels_to_collect.get(0).y) {
				nextPosition = new Coordinate(currentPosition.x, currentPosition.y-1);
				return world.WorldSpatial.RelativeDirection.LEFT;
			}return null;
		default:
			return null;
		}
	}
	
	public boolean isWall(Coordinate coord, HashMap<Coordinate, MapTile> currentView) {
		MapTile tile = currentView.get(coord);
		if(tile.isType(MapTile.Type.WALL)) {
			return true;
		}return false;
	}
	
	public boolean collisionAvoidance(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		Coordinate currentPosition = new Coordinate(getPosition());
		switch(orientation) {
		case EAST:
			if (isWall(new Coordinate(currentPosition.x+1, currentPosition.y), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		case NORTH:
			if (isWall(new Coordinate(currentPosition.x, currentPosition.y+1), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		case SOUTH:
			if (isWall(new Coordinate(currentPosition.x, currentPosition.y-1), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		case WEST:
			if (isWall(new Coordinate(currentPosition.x-1, currentPosition.y), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		default:
			return false;
		}
	}
}

