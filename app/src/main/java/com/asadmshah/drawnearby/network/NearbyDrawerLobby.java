package com.asadmshah.drawnearby.network;

public interface NearbyDrawerLobby {
    void connect();
    void disconnect();
    void setListener(NearbyDrawerLobbyListener listener);
}
