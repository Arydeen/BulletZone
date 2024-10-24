package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TurnEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;

    public TurnEvent() {}

    public TurnEvent(int rawServerValue, int pos) {
        this.position = pos;
        this.rawServerValue = rawServerValue;
    }

    @Override
    public String toString() {
        return "Turn " + rawServerValue +
                " at " + position +
                super.toString();
    }

}