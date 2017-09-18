/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cardinal.settings.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.cardinal.settings.preference.CustomSeekBarPreference;
import com.cardinal.settings.preference.SystemSettingSwitchPreference;


public class Traffic extends SettingsPreferenceFragment
    implements OnPreferenceChangeListener {

    private static final String TAG = "Traffic";
    private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
    private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";

    private CustomSeekBarPreference mThreshold;
    private ListPreference mNetTrafficState;

    private int mNetTrafficVal;
    private int MASK_UP;
    private int MASK_DOWN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.traffic);
        loadResources();

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        int value = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (CustomSeekBarPreference) findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
        mThreshold.setValue(value);
        mThreshold.setOnPreferenceChangeListener(this);

        mNetTrafficState = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_STATE);
        if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED &&
                TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
        mNetTrafficVal = Settings.System.getInt(getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0);
        int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
        intIndex = mNetTrafficState.findIndexOfValue(String.valueOf(intIndex));
        updateNetworkTrafficState(intIndex);

        mNetTrafficState.setValueIndex(intIndex >= 0 ? intIndex : 0);
        mNetTrafficState.setSummary(mNetTrafficState.getEntry());
        mNetTrafficState.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public int getMetricsCategory() {
       return MetricsEvent.WINGS;
   }

    private void updateNetworkTrafficState(int mIndex) {
        if (mIndex <= 0) {
            mThreshold.setEnabled(false);
        } else {
            mThreshold.setEnabled(true);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficState) {
            int intState = Integer.valueOf((String)newValue);
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
            mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
            int index = mNetTrafficState.findIndexOfValue((String) newValue);
            mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
            updateNetworkTrafficState(index);
            return true;
        } else if (preference == mThreshold) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, val,
                    UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private void loadResources() {
        Resources resources = getActivity().getResources();
        MASK_UP = resources.getInteger(R.integer.maskUp);
        MASK_DOWN = resources.getInteger(R.integer.maskDown);
    }

    private int setBit(int intNumber, int intMask, boolean blnState) {
        if (blnState) {
            return (intNumber | intMask);
        }
        return (intNumber & ~intMask);
    }

    private boolean getBit(int intNumber, int intMask) {
        return (intNumber & intMask) == intMask;
    }
}
