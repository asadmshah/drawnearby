package com.asadmshah.drawnearby.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

    private NetworkHelper() {
    }

    public static boolean isWiFiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI && ni.isConnected();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public static boolean canConnectToNearby(Context context) {
        SettingsManager sm = SettingsManager.getInstance(context);
        switch (sm.getServiceToUse()) {
            case SettingsManager.VALUE_USE_LOCAL_SERVER:
                return isNetworkConnected(context);
            default:
                return isWiFiConnected(context);
        }
    }
}
