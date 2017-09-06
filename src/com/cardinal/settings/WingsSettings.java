package com.cardinal.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class WingsSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.cardinal_settings_wings);
        Preference prefSet = getPreferenceScreen();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WINGS;
    }
}
