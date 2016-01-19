package com.asadmshah.drawnearby.screens.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.asadmshah.drawnearby.R;
import com.asadmshah.drawnearby.models.LobbyStatus;
import com.asadmshah.drawnearby.network.NearbyDrawerLobby;
import com.asadmshah.drawnearby.network.NearbyDrawerLobbyListener;
import com.asadmshah.drawnearby.network.nearbyconnections.NearbyConnectionsLobby;
import com.asadmshah.drawnearby.network.websocket.WebSocketLobby;
import com.asadmshah.drawnearby.screens.room.RoomActivity;
import com.asadmshah.drawnearby.screens.settings.SettingsActivity;
import com.asadmshah.drawnearby.serialization.LobbyStatusJSONDeserializer;
import com.asadmshah.drawnearby.utils.NetworkHelper;
import com.asadmshah.drawnearby.utils.SettingsManager;
import com.asadmshah.drawnearby.widgets.EditTextDialogFragment;
import com.asadmshah.drawnearby.widgets.OnDialogFragmentResultListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements RoomsAdapter.Listener, OnDialogFragmentResultListener, NearbyDrawerLobbyListener {

    private static final int RC_CREATE_ROOM_DIALOG = 1;
    private static final int RC_ROOM_ACTIVITY = 2;

    private RoomsAdapter viewListAdapter = new RoomsAdapter();
    private RecyclerView viewList;
    private FloatingActionButton viewFAB;

    private NearbyDrawerLobby lobby;

    private final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkHelper.canConnectToNearby(context)) {
                if (lobby != null) {
                    lobby.setListener(null);
                    lobby.disconnect();
                }
                setUpNearbyLobby();
            } else {
                showErrorMessage(R.string.unable_to_connect_to_wifi);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.lobby);
        }

        viewList = (RecyclerView) findViewById(R.id.list);
        viewList.setLayoutManager(new LinearLayoutManager(this));
        viewList.setAdapter(viewListAdapter);

        viewFAB = (FloatingActionButton) findViewById(R.id.fab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(SettingsActivity.getIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewListAdapter.setListener(this);
        viewListAdapter.setLobbyStatus(null);
        viewListAdapter.notifyDataSetChanged();

        if (NetworkHelper.canConnectToNearby(this)) {
            setUpNearbyLobby();
        } else {
            showErrorMessage(R.string.unable_to_connect_to_wifi);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);

        viewListAdapter.setListener(null);
        viewListAdapter.setLobbyStatus(null);
        viewListAdapter.notifyDataSetChanged();

        tearDownNearbyLobby();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_ROOM_ACTIVITY:
                if (resultCode == Activity.RESULT_OK) {
                    String message = data.getStringExtra(RoomActivity.KEY_RESULT_MESSAGE);
                    if (message != null) {
                        Snackbar.make(viewFAB, message, Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onRoomNameClicked(String roomName) {
        startRoomActivity(false, roomName);
    }

    @Override
    public void onDialogFragmentResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CREATE_ROOM_DIALOG && resultCode == Activity.RESULT_OK) {
            String room = data.getStringExtra(EditTextDialogFragment.KEY_RESULT_TEXT);
            if (room.length() > 0) {
                startRoomActivity(true, room);
            }
        }
    }

    @Override
    public void onNearbyDrawerLobbyConnected() {
        Timber.d("onNearbyDrawerLobbyConnected");

        viewFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DialogFragment dialog = EditTextDialogFragment.newInstance(RC_CREATE_ROOM_DIALOG);
                dialog.show(fm, null);
            }
        });
    }

    @Override
    public void onNearbyDrawerLobbyDisconnected() {
        Timber.d("onNearbyDrawerLobbyDisconnected");
        showErrorMessage(R.string.disconnected_from_lobby);

        viewFAB.setOnClickListener(null);
    }

    @Override
    public void onNearbyDrawerLobbyUpdate(final LobbyStatus status) {
        Timber.d("onNearbyDrawerLobbyUpdate: %d rooms", status.size());
        viewListAdapter.setLobbyStatus(status);
        viewListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNearbyDrawerIOException(IOException exception) {
        Timber.e(exception, "onNearbyDrawerIOException");
        showErrorMessage(R.string.unable_to_connect_to_lobby);
        tearDownNearbyLobby();
    }

    @Override
    public void onNearbyDrawerWebSocketException(WebSocketException exception) {
        Timber.e(exception, "onNearbyDrawerWebSocketException");
        showErrorMessage(R.string.unable_to_connect_to_lobby);
        tearDownNearbyLobby();
    }

    @Override
    public void onNearbyDrawerNearbyConnectionsError(Status status) {
        Timber.e("onNearbyDrawerNearbyConnectionsError: %s" + status.getStatusMessage());
        showErrorMessage(R.string.unable_to_connect_to_lobby);
        tearDownNearbyLobby();
    }

    @Override
    public void onNearbyDrawerNearbyConnectionsNetworkError() {
        Timber.e("onNearbyDrawerNearbyConnectionsNetworkError");
        showErrorMessage(R.string.unable_to_connect_to_lobby);
        tearDownNearbyLobby();
    }

    @Override
    public void onNearbyDrawerNearbyConnectionSuspended(int cause) {
        Timber.e("onNearbyDrawerNearbyConnectionSuspended: %d", cause);
        showErrorMessage(R.string.unable_to_connect_to_lobby);
        tearDownNearbyLobby();
    }

    @Override
    public void onNearbyDrawerNearbyConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("onNearbyDrawerNearbyConnectionFailed: %s", connectionResult.getErrorMessage());
        showErrorMessage(R.string.unable_to_connect_to_lobby);
        tearDownNearbyLobby();
    }

    private void showErrorMessage(int stringResId) {
        Snackbar.make(viewFAB, stringResId, Snackbar.LENGTH_LONG).show();
    }

    private void startRoomActivity(boolean isCreator, String roomName) {
        Intent intent = RoomActivity.getIntent(this, isCreator, roomName);
        startActivityForResult(intent, RC_ROOM_ACTIVITY);
    }

    private void setUpNearbyLobby() {
        SettingsManager sm = SettingsManager.getInstance(this);
        switch (sm.getServiceToUse()) {
            case SettingsManager.VALUE_USE_NEARBY_CONNECTIONS:
                lobby = new NearbyConnectionsLobby(this);
                break;
            case SettingsManager.VALUE_USE_LOCAL_SERVER:
                lobby = new WebSocketLobby(sm.getLocalServerAddress(), new LobbyStatusJSONDeserializer());
                break;
        }
        lobby.setListener(this);
        lobby.connect();
    }

    private void tearDownNearbyLobby() {
        if (lobby != null) {
            lobby.setListener(null);
            lobby.disconnect();
        }

        viewListAdapter.setLobbyStatus(null);
        viewListAdapter.notifyDataSetChanged();
        viewFAB.setOnClickListener(null);
    }

}
