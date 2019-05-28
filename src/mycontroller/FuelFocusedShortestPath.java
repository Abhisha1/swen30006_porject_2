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

	private static boolean loopAvoidance;
	
	// Car Speed to move at
	private final int CAR_MAX_SPEED = 1;
	
	private static world.WorldSpatial.RelativeDirection turn;
	
	private static ArrayList<Coordinate> parcels_to_collect;
	
	private static HashMap<Coordinate, world.WorldSpatial.RelativeDirection> prevTurns;
	
	public FuelFocusedShortestPath(Car car) {
		super(car);
		
		// parcels that need to be collected
		parcels_to_collect = new ArrayList<Coordinate>();
		prevTurns = new HashMap<Coordinate, world.WorldSpatial.RelativeDirection>();

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
		// check if a parcel has been collected
		discovered_parcel(currentView, current_coord);
		
		if (loop_detected(getOrientation(), current_coord)) {
			System.out.println("loop detection");
		}
		
		// checks if car will run into wall
		if (collisionAvoidance(current_coord,getOrientation(),currentView)) {
			
		}
		// tries navigating on short route to parcel
		else if (parcels_to_collect.size() >0) {
			if (shortest_path_turn(getOrientation(), currentView)) {
				
			}	
		}
		// searches for any new parcels
		else {
			find_parcels();
		}
		
		// car turns direction if path has a recommended turn
		if (turn == null) {
			prevTurns.put(current_coord, null);
		}
		else{
			System.out.print("WILL TURN"+turn.toString());
			switch(turn) {
			case LEFT:
				turnLeft();
				turn = null;
				prevTurns.put(current_coord, world.WorldSpatial.RelativeDirection.LEFT);
				break;
			case RIGHT:
				turnRight();
				prevTurns.put(current_coord, world.WorldSpatial.RelativeDirection.RIGHT);
				turn = null;
				break;
			default:
				break;
			}
		}
		
	}
	
	private boolean loop_detected(WorldSpatial.Direction orientation, Coordinate current_coord) {
		if (prevTurns.containsKey(current_coord)) {
			switch(orientation) {
			case EAST:
				if (current_coord.y == world.World.MAP_HEIGHT) {
					loopAvoidance = false;
				}
				return false;
			case WEST:
				if (current_coord.y == world.World.MAP_HEIGHT) {
					loopAvoidance = false;
				}return false;
			case NORTH:
				if (current_coord.x < world.World.MAP_WIDTH) {
					turn=world.WorldSpatial.RelativeDirection.RIGHT; 
				}
				if (current_coord.x == world.World.MAP_WIDTH) {
					loopAvoidance = false;
				}
				return false;
			case SOUTH:
				if (current_coord.x < world.World.MAP_WIDTH) {
					turn = world.WorldSpatial.RelativeDirection.LEFT;
					return true;
				}
				if (current_coord.x == world.World.MAP_WIDTH) {
					loopAvoidance = false;
				}return false;
			default:
				return false;
		}
	}return false;
}
	
	private void discovered_parcel(HashMap<Coordinate, MapTile> currentView, Coordinate coord) {
		for (Coordinate parcel_coordinates: parcels_to_collect) {
			if (coord.equals(parcel_coordinates)){
				parcels_to_collect.remove(coord);
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
						loopAvoidance = false;
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
		System.out.print("the parcel x is "+parcels_to_collect.get(0).x+"but xar"+currentPosition.x);
		switch(orientation) {
		case NORTH:
			if (currentPosition.x < parcels_to_collect.get(0).x) {
				return world.WorldSpatial.RelativeDirection.RIGHT; 
			}
			else if (currentPosition.x > parcels_to_collect.get(0).x) {
				return world.WorldSpatial.RelativeDirection.LEFT;
			}return null;
		case SOUTH:
			if (currentPosition.x < parcels_to_collect.get(0).x) {
				return world.WorldSpatial.RelativeDirection.LEFT;
			}
			else if (currentPosition.x > parcels_to_collect.get(0).x) {
				return world.WorldSpatial.RelativeDirection.RIGHT;
			}return null;
		default:
			return null;
		}
	}
	
	public world.WorldSpatial.RelativeDirection advance_vertical(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		Coordinate currentPosition = new Coordinate(getPosition());
		System.out.print("the parcel y is "+parcels_to_collect.get(0).y+"but yar"+currentPosition.y);
		switch(orientation) {
		case EAST:
			if (currentPosition.y < parcels_to_collect.get(0).y) {
				return world.WorldSpatial.RelativeDirection.LEFT; 
			}
			else if (currentPosition.y > parcels_to_collect.get(0).y) {
				return world.WorldSpatial.RelativeDirection.RIGHT;
			}return null;
		case WEST:
			if (currentPosition.y < parcels_to_collect.get(0).y) {
				return world.WorldSpatial.RelativeDirection.RIGHT;
			}
			else if (currentPosition.y > parcels_to_collect.get(0).y) {
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
	
	public boolean collisionAvoidance(Coordinate coordinate, WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation) {
		case EAST:
			if (isWall(new Coordinate(coordinate.x+1, coordinate.y), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		case NORTH:
			if (isWall(new Coordinate(coordinate.x, coordinate.y+1), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		case SOUTH:
			if (isWall(new Coordinate(coordinate.x, coordinate.y-1), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		case WEST:
			if (isWall(new Coordinate(coordinate.x-1, coordinate.y), currentView)){
				turn = world.WorldSpatial.RelativeDirection.LEFT;
				return true;
			}
			return false;
		default:
			return false;
		}
	}
}

