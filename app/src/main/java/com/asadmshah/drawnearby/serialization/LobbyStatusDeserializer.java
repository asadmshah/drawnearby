package com.asadmshah.drawnearby.serialization;

import com.asadmshah.drawnearby.models.LobbyStatus;

public interface LobbyStatusDeserializer {
    LobbyStatus deserialize(String response);
}
