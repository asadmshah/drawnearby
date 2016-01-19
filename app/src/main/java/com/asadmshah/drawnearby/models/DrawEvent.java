package com.asadmshah.drawnearby.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class DrawEvent {

    @JsonField(name = "c") public int color;
    @JsonField(name = "r") public float radius;
    @JsonField(name = "positions") public List<Float> positions;

    public DrawEvent() {
        super();
    }

    public DrawEvent(int color, float radius) {
        this.color = color;
        this.radius = radius;
        this.positions = new ArrayList<>();
    }

}
