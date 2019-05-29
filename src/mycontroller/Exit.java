package mycontroller;

import utilities.Coordinate;

public class Exit {
    Coordinate coord = null;

    Exit() {

    }

    public boolean notFound() {
        return coord==null;
    }
    public Coordinate getCoord() {
        return coord;
    }
    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }
}
