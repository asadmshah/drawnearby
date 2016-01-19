package com.asadmshah.drawnearby.serialization;

import com.asadmshah.drawnearby.models.DrawEvent;
import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import timber.log.Timber;

public class DrawEventJSONSerializer implements DrawEventSerializer {

    @Override
    public String serialize(DrawEvent drawEvent) {
        try {
            return LoganSquare.serialize(drawEvent);
        } catch (IOException e) {
            Timber.e(e, "Unable to serialize DrawEvent");
            return null;
        }
    }

}
