package com.asadmshah.drawnearby.network;

import com.asadmshah.drawnearby.models.DrawEvent;

public interface NearbyDrawerRoom {
    void connect();
    void disconnect();
    void setListener(NearbyDrawerRoomListener listener);
    void sendDrawEvent(DrawEvent event);
}
