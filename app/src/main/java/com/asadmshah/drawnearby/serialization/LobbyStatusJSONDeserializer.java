package com.asadmshah.drawnearby.serialization;

import com.asadmshah.drawnearby.models.LobbyStatus;
import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import timber.log.Timber;

public class LobbyStatusJSONDeserializer implements LobbyStatusDeserializer {

    @Override
    public LobbyStatus deserialize(String response) {
        try {
            return LoganSquare.parse(response, LobbyStatus.class);
        } catch (IOException e) {
            Timber.e(e, "Unable to parse JSON: ");
            return null;
        }
    }

}
