package com.asadmshah.drawnearby.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

@JsonObject
public class LobbyStatus {

    @JsonField(name = "rooms")
    public List<String> rooms;

    public String getRoomName(int position) {
        return rooms.get(position);
    }

    public int size() {
        return rooms.size();
    }

}
