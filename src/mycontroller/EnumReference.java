package mycontroller;

import world.WorldSpatial;

public class EnumReference<T> {
    private T turn;

    public void setTurn(T turn) {
        this.turn = turn;
    }

    public T getTurn() {
        return turn;
    }
}
