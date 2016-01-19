package com.asadmshah.drawnearby.network.nearbyconnections;

import android.content.Context;
import android.support.annotation.NonNull;

import com.asadmshah.drawnearby.models.LobbyStatus;
import com.asadmshah.drawnearby.network.NearbyDrawerLobby;
import com.asadmshah.drawnearby.network.NearbyDrawerLobbyListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NearbyConnectionsLobby extends BaseConnection implements NearbyDrawerLobby {

    private final Map<String, String> endpointsFound = new HashMap<>();
    private final LobbyStatus lobbyStatus = new LobbyStatus();

    private NearbyDrawerLobbyListener listener;

    public NearbyConnectionsLobby(Context context) {
        super(context);
        this.lobbyStatus.rooms = new ArrayList<>();
    }

    @Override
    public void connect() {
        connectNearbyConnection();
    }

    @Override
    public void disconnect() {
        disconnectNearbyConnection();
    }

    @Override
    public void setListener(NearbyDrawerLobbyListener listener) {
        this.listener = listener;
    }

    @Override
    void onNearbyConnectionConnected() {
        startDiscovering();
    }

    @Override
    void onNearbyConnectionError(Status status) {
        if (listener != null) {
            listener.onNearbyDrawerNearbyConnectionsError(status);
        }
    }

    @Override
    void onNearbyConnectionDiscoveringSuccess() {
        if (listener != null) {
            listener.onNearbyDrawerLobbyConnected();
        }
    }

    @Override
    void onNearbyConnectionAdvertisingSuccess() {

    }

    @Override
    void onNearbyConnectionNetworkNotConnected() {
        if (listener != null) {
            listener.onNearbyDrawerNearbyConnectionsNetworkError();
        }
    }

    @Override
    void onNearbyConnectionEndpointFound(String endpointId, String name) {
        if (!endpointsFound.containsKey(endpointId)) {
            endpointsFound.put(endpointId, name);
            lobbyStatus.rooms.add(name);
            Collections.sort(lobbyStatus.rooms);
            if (listener != null) {
                listener.onNearbyDrawerLobbyUpdate(lobbyStatus);
            }
        }
    }

    @Override
    void onNearbyConnectionEndpointLost(String endpointId) {
        String name = endpointsFound.remove(endpointId);
        if (name != null) {
            lobbyStatus.rooms.remove(name);
            Collections.sort(lobbyStatus.rooms);
            if (listener != null) {
                listener.onNearbyDrawerLobbyUpdate(lobbyStatus);
            }
        }
    }

    @Override
    void onNearbyConnectionEndpointConnectionRequest(String remoteEndpointId) {

    }

    @Override
    void onNearbyConnectionMessageReceived(byte[] bytes) {

    }

    @Override
    void onNearbyConnectionEndpointConnected(String remoteEndpointId) {

    }

    @Override
    void onNearbyConnectionEndpointDisconnected(String remoteEndpointId) {
        String name = endpointsFound.remove(remoteEndpointId);
        if (name != null) {
            lobbyStatus.rooms.remove(name);
            Collections.sort(lobbyStatus.rooms);
            if (listener != null) {
                listener.onNearbyDrawerLobbyUpdate(lobbyStatus);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (listener != null) {
            listener.onNearbyDrawerNearbyConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (listener != null) {
            listener.onNearbyDrawerNearbyConnectionFailed(connectionResult);
        }
    }

}
