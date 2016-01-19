package com.asadmshah.drawnearby.serialization;

import com.asadmshah.drawnearby.models.DrawEvent;

public interface DrawEventSerializer {
    String serialize(DrawEvent drawEvent);
}
