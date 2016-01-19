package com.asadmshah.drawnearby.network.websocket;

import android.net.Uri;

import com.asadmshah.drawnearby.models.DrawEvent;
import com.asadmshah.drawnearby.network.NearbyDrawerRoom;
import com.asadmshah.drawnearby.network.NearbyDrawerRoomListener;
import com.asadmshah.drawnearby.serialization.DrawEventDeserializer;
import com.asadmshah.drawnearby.serialization.DrawEventSerializer;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public class WebSocketRoom extends BaseConnection implements NearbyDrawerRoom {

    private final String address;
    private final DrawEventSerializer serializer;
    private final DrawEventDeserializer deserializer;
    private NearbyDrawerRoomListener listener;

    public WebSocketRoom(String authority, String room, DrawEventSerializer serializer, DrawEventDeserializer deserializer) {
        this.address = new Uri.Builder().scheme("ws").encodedAuthority(authority).appendPath("join").appendPath(room).build().toString();
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public void connect() {
        connectWebSocket(address);
    }

    @Override
    public void disconnect() {
        disconnectWebSocket();
    }

    @Override
    public void setListener(NearbyDrawerRoomListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendDrawEvent(DrawEvent event) {
        sendTextMessage(serializer.serialize(event));
    }

    @Override
    protected void onIOException(IOException e) {
        if (listener != null) {
            listener.onNearbyDrawerIOException(e);
        }
    }

    @Override
    protected void onWebSocketException(WebSocketException e) {
        if (listener != null) {
            listener.onNearbyDrawerWebSocketException(e);
        }
    }

    @Override
    protected void onWebSocketConnected() {
        if (listener != null) {
            listener.onNearbyDrawerRoomConnected();
        }
    }

    @Override
    protected void onWebSocketDisconnected() {
        if (listener != null) {
            listener.onNearbyDrawerRoomDisconnected();
        }
    }

    @Override
    protected void onWebSocketTextMessage(String text) {
        if (listener != null) {
            listener.onNearbyDrawerRoomDrawEvent(deserializer.deserialize(text));
        }
    }
}
