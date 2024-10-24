package edu.unh.cs.cs619.bulletzone.model.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoveEvent extends GameEvent {
    @JsonProperty
    private int rawServerValue;
    @JsonProperty
    private int position;

    public RemoveEvent() {}

    public RemoveEvent(int rawServerValue, int pos) {
        this.rawServerValue = rawServerValue;
        this.position = pos;
    }

    @Override
    public String toString() {
        return "Remove " + rawServerValue +
                " at " + position +
                super.toString();
    }

}
