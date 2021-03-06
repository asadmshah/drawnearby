package com.asadmshah.drawnearby.serialization;

import com.asadmshah.drawnearby.models.DrawEvent;
import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import timber.log.Timber;

public class DrawEventSJSONDeserializer implements DrawEventDeserializer {
    @Override
    public DrawEvent deserialize(String response) {
        try {
            return LoganSquare.parse(response, DrawEvent.class);
        } catch (IOException e) {
            Timber.e(e, "Unable to parse JSON");
            return null;
        }
    }
}
