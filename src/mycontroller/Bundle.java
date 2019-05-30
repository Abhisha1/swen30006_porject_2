package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class Bundle {
    private HashMap<String, ArrayList<Coordinate>> coordArrayData = new HashMap<>();
    private HashMap<String, Boolean> boolData = new HashMap<>();
    private HashMap<String, Integer> integerData = new HashMap<>();
    private HashMap<String, Coordinate> coordinateData = new HashMap();

    public void addCoordArray(String keyname, ArrayList<Coordinate> coords) {
        coordArrayData.put(keyname, coords);
    }

    public ArrayList<Coordinate> getCoordArray(String keyname) {
        return  coordArrayData.get(keyname);
    }

    public Coordinate getCoordinate(String keyname) {
        return coordinateData.get(keyname);
    }

    public void addCoordinate(String keyname, Coordinate coord) {
        coordinateData.put(keyname, coord);
    }


}
