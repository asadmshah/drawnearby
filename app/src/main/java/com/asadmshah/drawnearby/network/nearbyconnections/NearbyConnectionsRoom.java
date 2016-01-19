package com.asadmshah.drawnearby.network.nearbyconnections;

import android.content.Context;
import android.support.annotation.NonNull;

import com.asadmshah.drawnearby.models.DrawEvent;
import com.asadmshah.drawnearby.network.NearbyDrawerRoom;
import com.asadmshah.drawnearby.network.NearbyDrawerRoomListener;
import com.asadmshah.drawnearby.serialization.DrawEventDeserializer;
import com.asadmshah.drawnearby.serialization.DrawEventSerializer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;

public class NearbyConnectionsRoom extends BaseConnection implements NearbyDrawerRoom {

    private final List<String> endpointsConnected = new ArrayList<>();
    private final List<String> drawnEvents = new ArrayList<>();

    private final DrawEventSerializer serializer;
    private final DrawEventDeserializer deserializer;
    private final boolean host;
    private final String room;
    private NearbyDrawerRoomListener listener;
    private String advertiserEndpointId;

    public NearbyConnectionsRoom(Context context, boolean host, String room, DrawEventSerializer serializer, DrawEventDeserializer deserializer) {
        super(context);
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.host = host;
        this.room = room;
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
    public void setListener(NearbyDrawerRoomListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendDrawEvent(DrawEvent event) {
        String text = serializer.serialize(event);
        if (text != null) {
            if (host) drawnEvents.add(text);
            if (endpointsConnected.size() > 0) {
                sendMessage(endpointsConnected, text.getBytes());
            }
        }
    }

    @Override
    void onNearbyConnectionConnected() {
        if (host) {
            startAdvertising(room);
        } else {
            startDiscovering();
        }
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
            listener.onNearbyDrawerRoomConnected();
        }
    }

    @Override
    void onNearbyConnectionAdvertisingSuccess() {
        if (listener != null) {
            listener.onNearbyDrawerRoomConnected();
        }
    }

    @Override
    void onNearbyConnectionNetworkNotConnected() {
        if (listener != null) {
            listener.onNearbyDrawerNearbyConnectionsNetworkError();
        }
    }

    @Override
    void onNearbyConnectionEndpointFound(String endpointId, String name) {
        if (!host && name.equals(room)) {
            advertiserEndpointId = endpointId;
            sendConnectionRequest(endpointId);
        }
    }

    @Override
    void onNearbyConnectionEndpointLost(String endpointId) {
        if (!host && endpointId.equals(advertiserEndpointId) && listener != null) {
            listener.onNearbyDrawerRoomDisconnected();
        }
    }

    @Override
    void onNearbyConnectionEndpointConnectionRequest(String remoteEndpointId) {
        if (host) {
            acceptConnectionRequest(remoteEndpointId);
        }
    }

    @Override
    void onNearbyConnectionEndpointConnected(String remoteEndpointId) {
        if (!endpointsConnected.contains(remoteEndpointId)) {
            endpointsConnected.add(remoteEndpointId);
            if (host) {
                for (String event : drawnEvents) {
                    sendMessage(remoteEndpointId, event.getBytes());
                }
            }
        }
    }

    @Override
    void onNearbyConnectionEndpointDisconnected(String remoteEndpointId) {
        if (endpointsConnected.contains(remoteEndpointId)) {
            endpointsConnected.remove(remoteEndpointId);
        }
    }

    @Override
    void onNearbyConnectionMessageReceived(byte[] bytes) {
        DrawEvent event = deserializer.deserialize(new String(bytes));
        if (event != null && listener != null) {
            listener.onNearbyDrawerRoomDrawEvent(event);
            if (host) {
                drawnEvents.add(new String(bytes));
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
