package com.asadmshah.drawnearby.screens.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Patterns;

import com.asadmshah.drawnearby.R;
import com.asadmshah.drawnearby.utils.SettingsManager;

public class SettingsFragment extends PreferenceFragment {

    private ListPreference prefServiceToUse;
    private EditTextPreference prefLocalServerAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        prefServiceToUse = (ListPreference) getPreferenceManager().findPreference(SettingsManager.KEY_SERVICE_TO_USE);
        prefServiceToUse.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateServiceToUseSummary((String) newValue);
                return true;
            }
        });

        prefLocalServerAddress = (EditTextPreference) getPreferenceManager().findPreference(SettingsManager.KEY_LOCAL_SERVER_ADDRESS);
        prefLocalServerAddress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (Patterns.WEB_URL.matcher(value).matches()) {
                    updateLocalServerAddressSummary(value);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SettingsManager sm = SettingsManager.getInstance(getActivity());
        updateServiceToUseSummary(sm.getServiceToUse());
        updateLocalServerAddressSummary(sm.getLocalServerAddress());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefServiceToUse.setOnPreferenceChangeListener(null);
        prefLocalServerAddress.setOnPreferenceChangeListener(null);
    }

    private void updateServiceToUseSummary(String serviceToUseValue) {
        switch (serviceToUseValue) {
            case SettingsManager.VALUE_USE_LOCAL_SERVER:
                prefServiceToUse.setSummary(R.string.use_local_server);
                break;
            case SettingsManager.VALUE_USE_NEARBY_CONNECTIONS:
                prefServiceToUse.setSummary(R.string.use_nearby_connections);
                break;
        }
    }

    private void updateLocalServerAddressSummary(String summary) {
        prefLocalServerAddress.setSummary(summary);
    }

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
