package com.asadmshah.drawnearby.screens.room;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.asadmshah.drawnearby.R;
import com.asadmshah.drawnearby.models.DrawEvent;
import com.asadmshah.drawnearby.network.NearbyDrawerRoom;
import com.asadmshah.drawnearby.network.NearbyDrawerRoomListener;
import com.asadmshah.drawnearby.network.nearbyconnections.NearbyConnectionsRoom;
import com.asadmshah.drawnearby.network.websocket.WebSocketRoom;
import com.asadmshah.drawnearby.serialization.DrawEventDeserializer;
import com.asadmshah.drawnearby.serialization.DrawEventJSONSerializer;
import com.asadmshah.drawnearby.serialization.DrawEventSJSONDeserializer;
import com.asadmshah.drawnearby.serialization.DrawEventSerializer;
import com.asadmshah.drawnearby.utils.NetworkHelper;
import com.asadmshah.drawnearby.utils.SettingsManager;
import com.asadmshah.drawnearby.widgets.HorizontalColorPickerView;
import com.asadmshah.drawnearby.widgets.HorizontalColorPickerView.OnColorSelectedListener;
import com.asadmshah.drawnearby.widgets.PainterView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.neovisionaries.ws.client.WebSocketException;

import java.io.IOException;

import timber.log.Timber;

public class RoomActivity extends AppCompatActivity implements PainterView.OnRemoteDrawListener, NearbyDrawerRoomListener {

    private static final String AUTHORITY = "192.168.100.5:8080";

    public static final String KEY_IS_CREATOR = "is_creator";
    public static final String KEY_ROOM_NAME = "room_name";
    public static final String KEY_STROKE_WIDTH = "radius";
    public static final String KEY_COLOR = "color";
    public static final String KEY_RESULT_MESSAGE = "result_message";

    private PainterView viewPainter;
    private HorizontalColorPickerView viewColorChooser;
    private SeekBar viewSeekBar;
    private ProgressBar viewProgressBar;

    private NearbyDrawerRoom room;

    private boolean isColorPickerShowing = false;
    private boolean isRadiusSeekBarShowing = false;

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            float min = getResources().getDimensionPixelSize(R.dimen.stroke_radius_min);
            float max = getResources().getDimensionPixelSize(R.dimen.stroke_radius_max);
            float pos = seekBar.getProgress() / 100f;
            viewPainter.setStrokeWidth(Math.max(1, pos * (max-min)));
            hideRadiusSeekBar(true);
        }
    };

    private final OnColorSelectedListener onColorSelectedListener = new OnColorSelectedListener() {
        @Override
        public void onColorSelected(int color) {
            viewPainter.setColor(color);
            hideColorChooser(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        String roomName = getIntent().getStringExtra(KEY_ROOM_NAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24px);
            ab.setTitle(roomName);
        }

        viewPainter = (PainterView) findViewById(R.id.painter);
        viewPainter.setDrawingEnabled(false);

        viewColorChooser = (HorizontalColorPickerView) findViewById(R.id.color_chooser);
        viewColorChooser.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewColorChooser.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                hideColorChooser(false);
                viewColorChooser.setVisibility(View.VISIBLE);
            }
        });

        viewSeekBar = (SeekBar) findViewById(R.id.radius_seekbar);
        viewSeekBar.setMax(100);
        viewSeekBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewSeekBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                hideRadiusSeekBar(false);
                viewSeekBar.setVisibility(View.VISIBLE);
            }
        });

        if (savedInstanceState != null) {
            viewPainter.setStrokeWidth(savedInstanceState.getFloat(KEY_STROKE_WIDTH));
            viewPainter.setColor(savedInstanceState.getInt(KEY_COLOR));
        } else {
            viewPainter.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_radius_default));
            viewPainter.setColor(Color.BLACK);
        }

        float strokeWidthMax = getResources().getDimensionPixelSize(R.dimen.stroke_radius_max);
        float strokeWidthNow = viewPainter.getStrokeWidth();
        float progress = (strokeWidthNow/strokeWidthMax)*viewSeekBar.getMax();
        viewSeekBar.setProgress(Math.round(progress));

        viewProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_choose_color:
                if (isRadiusSeekBarShowing) {
                    hideRadiusSeekBar(true);
                }
                if (isColorPickerShowing) {
                    hideColorChooser(true);
                } else {
                    showColorChooser(true);
                }
                return true;
            case R.id.action_choose_radius:
                if (isColorPickerShowing) {
                    hideColorChooser(true);
                }
                if (isRadiusSeekBarShowing) {
                    hideRadiusSeekBar(true);
                } else {
                    showRadiusSeekBar(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewProgressBar.setVisibility(View.VISIBLE);

        if (NetworkHelper.canConnectToNearby(this)) {
            setUpNearbyRoom();
        } else {
            finishActivityWithMessage(getString(R.string.a_network_error_occurred));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        tearDownNearbyRoom();

        viewPainter.setDrawingEnabled(false);
        viewPainter.setOnRemoteDrawListener(null);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(KEY_STROKE_WIDTH, viewPainter.getStrokeWidth());
        outState.putInt(KEY_COLOR, viewPainter.getColor());
    }

    @Override
    public void onRemoteDraw(DrawEvent event) {
        room.sendDrawEvent(event);
    }

    @Override
    public void onNearbyDrawerRoomConnected() {
        Timber.d("onNearbyDrawerRoomConnected");
        viewPainter.setOnRemoteDrawListener(RoomActivity.this);
        viewPainter.setDrawingEnabled(true);
        viewProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNearbyDrawerRoomDisconnected() {
        Timber.d("onNearbyDrawerRoomDisconnected");
        viewPainter.setDrawingEnabled(false);
        viewPainter.setOnRemoteDrawListener(null);
        finishActivityWithMessage(getString(R.string.disconnected_from_room));
    }

    @Override
    public void onNearbyDrawerRoomDrawEvent(final DrawEvent event) {
        Timber.d("onNearbyDrawerRoomDrawEvent: %d items", event.positions.size());
        viewPainter.onRemoteTouchEvent(event);
    }

    @Override
    public void onNearbyDrawerIOException(IOException exception) {
        Timber.e(exception, "onNearbyDrawerIOException");
        finishActivityWithMessage(getString(R.string.an_unknown_error_occurred));
    }

    @Override
    public void onNearbyDrawerWebSocketException(WebSocketException exception) {
        Timber.e(exception, "onNearbyDrawerWebSocketException");
        finishActivityWithMessage(getString(R.string.an_unknown_error_occurred));
    }

    @Override
    public void onNearbyDrawerNearbyConnectionsError(Status status) {
        Timber.e("onNearbyDrawerNearbyConnectionsError: %s", status.getStatusMessage());
        finishActivityWithMessage(getString(R.string.an_unknown_error_occurred));
    }

    @Override
    public void onNearbyDrawerNearbyConnectionsNetworkError() {
        Timber.e("onNearbyDrawerNearbyConnectionsNetworkError");
        finishActivityWithMessage(getString(R.string.a_network_error_occurred));
    }

    @Override
    public void onNearbyDrawerNearbyConnectionSuspended(int cause) {
        Timber.e("onNearbyDrawerNearbyConnectionsSuspended: %d", cause);
        finishActivityWithMessage(getString(R.string.unable_to_connect_to_room));
    }

    @Override
    public void onNearbyDrawerNearbyConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("onNearbyDrawerNearbyConnectionsFailed: %s", connectionResult.getErrorMessage());
        finishActivityWithMessage(getString(R.string.unable_to_connect_to_room));
    }

    private void showColorChooser(final boolean animated) {
        isColorPickerShowing = true;
        viewColorChooser.setOnColorSelectedListener(onColorSelectedListener);
        if (animated) {
            animateControlViews(viewColorChooser, 0f, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    animation.removeAllListeners();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                }
            });
        } else {
            viewColorChooser.setTranslationY(0f);
        }
    }

    private void hideColorChooser(boolean animated) {
        isColorPickerShowing = false;
        viewColorChooser.setOnColorSelectedListener(null);
        if (animated) {
            animateControlViews(viewColorChooser, viewColorChooser.getHeight(), new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    animation.removeAllListeners();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                }
            });
        } else {
            viewColorChooser.setTranslationY(viewColorChooser.getHeight());
        }
    }

    private void showRadiusSeekBar(boolean animated) {
        isRadiusSeekBarShowing = true;
        viewSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        if (animated) {
            animateControlViews(viewSeekBar, 0f, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    animation.removeAllListeners();
                    viewSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    viewSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                }
            });
        } else {
            viewSeekBar.setTranslationY(0f);
        }
    }

    private void hideRadiusSeekBar(boolean animated) {
        isRadiusSeekBarShowing = false;
        viewSeekBar.setOnSeekBarChangeListener(null);
        if (animated) {
            animateControlViews(viewSeekBar, viewSeekBar.getHeight(), new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    animation.removeAllListeners();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                }
            });
        } else {
            viewSeekBar.setTranslationY(viewSeekBar.getHeight());
        }
    }

    private void animateControlViews(View view, float translationY, Animator.AnimatorListener listener) {
        view.animate()
                .translationY(translationY)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(listener)
                .start();
    }

    private void setUpNearbyRoom() {
        String roomName = getIntent().getStringExtra(KEY_ROOM_NAME);
        boolean isCreator = getIntent().getBooleanExtra(KEY_IS_CREATOR, false);

        DrawEventSerializer serializer = new DrawEventJSONSerializer();
        DrawEventDeserializer deserializer = new DrawEventSJSONDeserializer();

        SettingsManager sm = SettingsManager.getInstance(this);
        switch (sm.getServiceToUse()) {
            case SettingsManager.VALUE_USE_NEARBY_CONNECTIONS:
                room = new NearbyConnectionsRoom(this, isCreator, roomName, serializer, deserializer);
                break;
            case SettingsManager.VALUE_USE_LOCAL_SERVER:
                room = new WebSocketRoom(sm.getLocalServerAddress(), roomName, serializer, deserializer);
                break;
        }
        room.setListener(this);
        room.connect();
    }

    private void tearDownNearbyRoom() {
        if (room != null) {
            room.setListener(null);
            room.disconnect();
            room = null;
        }
    }

    private void finishActivityWithMessage(String message) {
        Intent intent = new Intent();
        intent.putExtra(KEY_RESULT_MESSAGE, message);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public static Intent getIntent(Context context, boolean isCreator, String roomName) {
        Intent intent = new Intent(context, RoomActivity.class);
        intent.putExtra(KEY_IS_CREATOR, isCreator);
        intent.putExtra(KEY_ROOM_NAME, roomName);
        return intent;
    }
}
