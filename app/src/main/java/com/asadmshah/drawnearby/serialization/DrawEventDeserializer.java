package com.asadmshah.drawnearby.serialization;

import com.asadmshah.drawnearby.models.DrawEvent;

public interface DrawEventDeserializer {
    DrawEvent deserialize(String response);
}
