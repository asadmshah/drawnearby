package com.asadmshah.drawnearby.network.websocket;

import android.net.Uri;

import com.asadmshah.drawnearby.network.NearbyDrawerLobby;
import com.asadmshah.drawnearby.network.NearbyDrawerLobbyListener;
import com.asadmshah.drawnearby.serialization.LobbyStatusDeserializer;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

public class WebSocketLobby extends BaseConnection implements NearbyDrawerLobby {

    private final String address;
    private final LobbyStatusDeserializer deserializer;
    private NearbyDrawerLobbyListener listener;

    public WebSocketLobby(String authority, LobbyStatusDeserializer deserializer) {
        this.address = new Uri.Builder().scheme("ws").encodedAuthority(authority).build().toString();
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
    public void setListener(NearbyDrawerLobbyListener listener) {
        this.listener = listener;
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
            listener.onNearbyDrawerLobbyConnected();
        }
    }

    @Override
    protected void onWebSocketDisconnected() {
        if (listener != null) {
            listener.onNearbyDrawerLobbyDisconnected();
        }
    }

    @Override
    protected void onWebSocketTextMessage(String text) {
        if (listener != null) {
            listener.onNearbyDrawerLobbyUpdate(deserializer.deserialize(text));
        }
    }

}
