package mycontroller;

import controller.CarController;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

import java.util.ArrayList;

public class BackTrack extends CarController implements IStrategy{

    private int wallSensitivity = 1;

    private Coordinate nextBackTrackCoord = null;
    private ArrayList<Coordinate> recordedSteps = null;
    private Coordinate exit = null;
    private ControllerContext context;
    BackTrack(Car car, ControllerContext context) {
        super(car);
        this.context = context;
    }

    @Override
    public void update() {
        goToExit(new Coordinate(getPosition()));
    }

    public void switchStrategy(String strategyName) {

    }

    public void parseBundle(Bundle strategyBundle) {
        recordedSteps = strategyBundle.getCoordArray("backtrackTurns");
        exit = strategyBundle.getCoordinate("exit");

    }

    private void goToExit(Coordinate currentPos) {

        if(nextBackTrackCoord == null) {
            if(recordedSteps.size() <= 1) {
                nextBackTrackCoord = exit;
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
                moveX(nextBackTrackCoord.x - currentPos.x, currentPos);
            }
            if(nextBackTrackCoord.y != currentPos.y) {
                System.out.println("NEED TO MOVE IN Y");
                moveY(nextBackTrackCoord.y - currentPos.y, currentPos);
            }
        }
    }

    private void turnToOrient(WorldSpatial.Direction orientation,Coordinate currentPos, WorldSpatial.Direction neededOrientation) {
        if(orientation != neededOrientation) {
            if(TurnHelper.checkLeftWall(orientation, currentPos, getView(), wallSensitivity)) {
                turnRight();
            }else {
                turnLeft();
            }
        }
    }

    private boolean moveX(float x, Coordinate currentPos) {
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
                turnToOrient(getOrientation(), currentPos, WorldSpatial.Direction.EAST);
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
                turnToOrient(getOrientation(), currentPos, WorldSpatial.Direction.WEST);
            }

        }else {
            applyBrake();
        }
        return madeTurn;
    }

    private void moveY(float y, Coordinate currentPos) {
        if(y > 0) {
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
                turnToOrient(getOrientation(), currentPos, WorldSpatial.Direction.NORTH);
            }

        }else {
            if(getOrientation() == WorldSpatial.Direction.SOUTH) {
                applyForwardAcceleration();
            }else if(getOrientation() == WorldSpatial.Direction.NORTH) {
                applyReverseAcceleration();
            }else {
                applyBrake();
                turnToOrient(getOrientation(), currentPos, WorldSpatial.Direction.SOUTH);
            }

        }
    }
}
