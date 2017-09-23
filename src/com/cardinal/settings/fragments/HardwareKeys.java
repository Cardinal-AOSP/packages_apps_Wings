/*
 *  Copyright (C) 2017 Cardinal-AOSP
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.cardinal.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import android.provider.Settings;

import android.view.View;
import android.util.Log;
import android.app.AlertDialog;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.CustomUtils;

import com.cardinal.settings.preference.CustomSeekBarPreference;

public class HardwareKeys extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "HardwareKeys";

    private static final String KEYS_SHOW_NAVBAR_KEY = "navigation_bar_show";

    private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";

    private IPowerManager mPowerService;
    private CustomSeekBarPreference mButtonTimoutBar;
    private CustomSeekBarPreference mManualButtonBrightness;
    private SwitchPreference mEnableNavBar;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cardinal_settings_hardwarekeys);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();


        mEnableNavBar = (SwitchPreference) prefSet.findPreference(
                KEYS_SHOW_NAVBAR_KEY);

        boolean showNavBarDefault = CustomUtils.deviceSupportNavigationBar(getActivity());
        boolean showNavBar = Settings.System.getInt(resolver,
                    Settings.System.NAVIGATION_BAR_SHOW, showNavBarDefault ? 1:0) == 1;
        mEnableNavBar.setChecked(showNavBar);

        mManualButtonBrightness = (CustomSeekBarPreference) findPreference(KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        final int customButtonBrightness = getResources().getInteger(
                com.android.internal.R.integer.config_button_brightness_default);
        final int currentBrightness = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_BUTTON_BRIGHTNESS, customButtonBrightness);
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        mManualButtonBrightness.setMax(pm.getMaximumScreenBrightnessSetting());
        mManualButtonBrightness.setValue(currentBrightness);
        mManualButtonBrightness.setOnPreferenceChangeListener(this);

        mButtonTimoutBar = (CustomSeekBarPreference) findPreference(KEY_BUTTON_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
        mButtonTimoutBar.setValue(currentTimeout);
        mButtonTimoutBar.setOnPreferenceChangeListener(this);

        mPowerService = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mEnableNavBar) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW, checked ? 1:0);
            return true;
        }
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();

        if (preference == mButtonTimoutBar) {
            int buttonTimeout = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
        } else if (preference == mManualButtonBrightness) {
            int buttonBrightness = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, buttonBrightness);
        } else {
            return false;
        }
        return true;
    }
}
