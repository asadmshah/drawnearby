package com.asadmshah.drawnearby.network.nearbyconnections;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.asadmshah.drawnearby.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AppIdentifier;
import com.google.android.gms.nearby.connection.AppMetadata;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import java.util.ArrayList;
import java.util.List;

abstract class BaseConnection implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        Connections.EndpointDiscoveryListener, Connections.ConnectionRequestListener, Connections.MessageListener,
        Connections.ConnectionResponseCallback {

    private final GoogleApiClient connection;
    private final String serviceId;

    BaseConnection(Context context) {
        serviceId = context.getResources().getString(R.string.nearby_connections_service_id);
        connection = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.CONNECTIONS_API)
                .build();
    }

    protected void connectNearbyConnection() {
        connection.connect();
    }

    protected void disconnectNearbyConnection() {
        if (connection.isConnecting() || connection.isConnected()) {
            connection.disconnect();
        }
    }

    protected void startDiscovering() {
        Nearby.Connections.startDiscovery(connection, serviceId, Connections.DURATION_INDEFINITE, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                onNearbyConnectionDiscoveringSuccess();
                                break;
                            case ConnectionsStatusCodes.STATUS_NETWORK_NOT_CONNECTED:
                                onNearbyConnectionNetworkNotConnected();
                                break;
                            case ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING:
                                break;
                        }
                    }
                });
    }

    protected void startAdvertising(String name) {
        List<AppIdentifier> appIdentifierList = new ArrayList<>();
        appIdentifierList.add(new AppIdentifier(connection.getContext().getPackageName()));
        AppMetadata appMetadata = new AppMetadata(appIdentifierList);

        Nearby.Connections.startAdvertising(connection, name, appMetadata, Connections.DURATION_INDEFINITE, this)
                .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
                    @Override
                    public void onResult(@NonNull Connections.StartAdvertisingResult result) {
                        switch (result.getStatus().getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                onNearbyConnectionAdvertisingSuccess();
                                break;
                            case ConnectionsStatusCodes.STATUS_NETWORK_NOT_CONNECTED:
                                onNearbyConnectionNetworkNotConnected();
                                break;
                            case ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING:
                                break;
                            case ConnectionsStatusCodes.STATUS_ERROR:
                                onNearbyConnectionError(result.getStatus());
                                break;
                        }
                    }
                });
    }

    protected void sendConnectionRequest(String remoteEndpointId) {
        Nearby.Connections.sendConnectionRequest(connection, null, remoteEndpointId, null, this, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                break;
                            case ConnectionsStatusCodes.STATUS_NETWORK_NOT_CONNECTED:
                                onNearbyConnectionNetworkNotConnected();
                                break;
                            case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                                break;
                        }
                    }
                });
    }

    protected void acceptConnectionRequest(final String remoteEndpointId) {
        Nearby.Connections.acceptConnectionRequest(connection, remoteEndpointId, null, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                onNearbyConnectionEndpointConnected(remoteEndpointId);
                                break;
                            case ConnectionsStatusCodes.STATUS_NETWORK_NOT_CONNECTED:
                                onNearbyConnectionNetworkNotConnected();
                                break;
                            case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                                break;
                        }
                    }
                });
    }

    protected void sendMessage(List<String> remoteEndpoints, byte[] message) {
        Nearby.Connections.sendReliableMessage(connection, remoteEndpoints, message);
    }

    protected void sendMessage(String remoteEndpoint, byte[] message) {
        Nearby.Connections.sendReliableMessage(connection, remoteEndpoint, message);
    }

    @Override
    public final void onConnectionResponse(String remoteEndpointId, Status status, byte[] bytes) {
        switch (status.getStatusCode()) {
            case ConnectionsStatusCodes.STATUS_OK:
                onNearbyConnectionEndpointConnected(remoteEndpointId);
                break;
            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                break;
            case ConnectionsStatusCodes.STATUS_NOT_CONNECTED_TO_ENDPOINT:
                break;
        }
    }

    @Override
    public final void onEndpointFound(String endpointId, String deviceId, String serviceId, String name) {
        onNearbyConnectionEndpointFound(endpointId, name);
    }

    @Override
    public final void onEndpointLost(String endpointId) {
        onNearbyConnectionEndpointLost(endpointId);
    }

    @Override
    public final void onConnectionRequest(String remoteEndpointId, String remoteDeviceId, String remoteEndpointName, byte[] bytes) {
        onNearbyConnectionEndpointConnectionRequest(remoteEndpointId);
    }

    @Override
    public final void onConnected(Bundle bundle) {
        onNearbyConnectionConnected();
    }

    @Override
    public final void onMessageReceived(String remoteEndpointId, byte[] bytes, boolean isReliable) {
        onNearbyConnectionMessageReceived(bytes);
    }

    @Override
    public final void onDisconnected(String remoteEndpointId) {
        onNearbyConnectionEndpointDisconnected(remoteEndpointId);
    }

    abstract void onNearbyConnectionConnected();
    abstract void onNearbyConnectionError(Status status);
    abstract void onNearbyConnectionDiscoveringSuccess();
    abstract void onNearbyConnectionAdvertisingSuccess();
    abstract void onNearbyConnectionNetworkNotConnected();
    abstract void onNearbyConnectionEndpointFound(String endpointId, String name);
    abstract void onNearbyConnectionEndpointLost(String endpointId);
    abstract void onNearbyConnectionEndpointConnectionRequest(String remoteEndpointId);
    abstract void onNearbyConnectionMessageReceived(byte[] bytes);
    abstract void onNearbyConnectionEndpointConnected(String remoteEndpointId);
    abstract void onNearbyConnectionEndpointDisconnected(String remoteEndpointId);

}
