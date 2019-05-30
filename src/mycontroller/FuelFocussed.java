package mycontroller;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;

import java.util.ArrayList;
import java.util.HashMap;

public class FuelFocussed extends CarController implements IStrategy {


    private ControllerContext context;

    // How many minimum units the wall is away from the player.
    private int wallSensitivity = 1;

    public Exit exit = null;

    private ArrayList<Coordinate> finalDestination = new ArrayList<>();

    private ArrayList<Coordinate> parcelsToCollect;

    private HashMap<Coordinate, world.WorldSpatial.RelativeDirection> prevTurns;

    private ArrayList<Coordinate> recordedSteps = new ArrayList<>();

    FuelFocussed(Car car, ControllerContext context) {
        super(car);
        this.context = context;
    }

    @Override
    public void update() {
        HashMap<Coordinate, MapTile> currentView = getView();
        Coordinate currentCoord = new Coordinate(getPosition());


    }

    public void switchStrategy(String strategyName) {

    }

    public void parseBundle(Bundle strategyBundle) {

    }
}
