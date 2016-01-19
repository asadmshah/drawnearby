package com.asadmshah.drawnearby.network;

import com.asadmshah.drawnearby.models.DrawEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public interface NearbyDrawerRoomListener {
    void onNearbyDrawerRoomConnected();
    void onNearbyDrawerRoomDisconnected();
    void onNearbyDrawerRoomDrawEvent(DrawEvent event);
    void onNearbyDrawerIOException(IOException exception);
    void onNearbyDrawerWebSocketException(WebSocketException exception);
    void onNearbyDrawerNearbyConnectionsError(Status status);
    void onNearbyDrawerNearbyConnectionsNetworkError();
    void onNearbyDrawerNearbyConnectionSuspended(int cause);
    void onNearbyDrawerNearbyConnectionFailed(ConnectionResult connectionResult);
}
