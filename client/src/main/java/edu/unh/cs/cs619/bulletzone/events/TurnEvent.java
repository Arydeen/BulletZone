package edu.unh.cs.cs619.bulletzone.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TurnEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;
    @JsonProperty
    private int direction;

    public TurnEvent() {}

    void applyTo(int [][]board) {
        board[position / 16][position % 16] = rawServerValue;
    }

    @Override
    public String toString() {
        return "Turn " + rawServerValue +
                " to face " + direction +
                super.toString();
    }

}
