package com.asadmshah.drawnearby.network.websocket;

import android.os.Handler;
import android.os.Looper;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

abstract class BaseConnection {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final WebSocketFactory factory = new WebSocketFactory();
    private WebSocket connection;

    private final WebSocketListener socketListener = new WebSocketAdapter() {
        @Override
        public void onTextMessage(WebSocket websocket, final String text) throws Exception {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketTextMessage(text);
                }
            });
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketConnected();
                }
            });
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketDisconnected();
                }
            });
        }

        @Override
        public void onError(WebSocket websocket, final WebSocketException cause) throws Exception {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onWebSocketException(cause);
                }
            });
        }
    };

    protected void connectWebSocket(String address) {
        try {
            connection = factory.createSocket(address);
            connection.addListener(socketListener);
            connection.setPingInterval(60 * 1000);
            connection.connectAsynchronously();
        } catch (final IOException e) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onIOException(e);
                }
            });
        }
    }

    protected void disconnectWebSocket() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    protected void sendTextMessage(String message) {
        if (message != null && connection != null && connection.isOpen()) {
            connection.sendText(message);
        }
    }

    abstract protected void onIOException(IOException e);
    abstract protected void onWebSocketException(WebSocketException e);
    abstract protected void onWebSocketConnected();
    abstract protected void onWebSocketDisconnected();
    abstract protected void onWebSocketTextMessage(String text);
}
