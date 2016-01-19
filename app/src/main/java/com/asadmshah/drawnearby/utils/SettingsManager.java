package com.asadmshah.drawnearby.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {

    public static final String KEY_SERVICE_TO_USE = "service_to_use";
    public static final String KEY_LOCAL_SERVER_ADDRESS = "local_server_address";

    public static final String VALUE_USE_NEARBY_CONNECTIONS = "use_nearby_connections";
    public static final String VALUE_USE_LOCAL_SERVER = "use_local_server";

    private static SettingsManager instance;

    private final SharedPreferences sharedPreferences;

    private SettingsManager(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    private void setDefaults() {
        SharedPreferences.Editor editor = instance.sharedPreferences.edit();
        if (!instance.sharedPreferences.contains(KEY_SERVICE_TO_USE)) {
            editor.putString(KEY_SERVICE_TO_USE, VALUE_USE_NEARBY_CONNECTIONS);
        }
        if (!instance.sharedPreferences.contains(KEY_LOCAL_SERVER_ADDRESS)) {
            editor.putString(KEY_LOCAL_SERVER_ADDRESS, "192.168.1.1:8080");
        }
        editor.apply();
    }

    public String getServiceToUse() {
        return sharedPreferences.getString(KEY_SERVICE_TO_USE, VALUE_USE_NEARBY_CONNECTIONS);
    }

    public String getLocalServerAddress() {
        return sharedPreferences.getString(KEY_LOCAL_SERVER_ADDRESS, null);
    }

    public synchronized static SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context);
            instance.setDefaults();
        }
        return instance;
    }

}
