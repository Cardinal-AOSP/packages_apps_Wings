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
import android.os.UserHandle;
import android.support.v7.preference.ListPreference; 
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;

public class NotificationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "NotificationSettings";

    private ListPreference mNoisyNotification;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cardinal_settings_notification);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        boolean mChargingLedsEnabled = (getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed));
 
        boolean mNotificationLedsEnabled = (getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveNotificationLed));
 
        PreferenceCategory mLedsCategory = (PreferenceCategory) findPreference("custom_leds");
        Preference mChargingLeds = (Preference) findPreference("charging_light");
        Preference mNotificationLeds = (Preference) findPreference("notification_light");
 
        if (mChargingLeds != null && mNotificationLeds != null) {
            if (!mChargingLedsEnabled) {
                mLedsCategory.removePreference(mChargingLeds);
            } else if (!mNotificationLedsEnabled) {
                mLedsCategory.removePreference(mNotificationLeds);
            } else if (!mChargingLedsEnabled && !mNotificationLedsEnabled) {
                prefSet.removePreference(mLedsCategory);
            }
        }

        mNoisyNotification = (ListPreference) findPreference("notification_sound_vib_screen_on");
        mNoisyNotification.setOnPreferenceChangeListener(this);
        int mode = Settings.System.getIntForUser(resolver,
                Settings.System.NOTIFICATION_SOUND_VIB_SCREEN_ON,
                1, UserHandle.USER_CURRENT);
        mNoisyNotification.setValue(String.valueOf(mode));
        mNoisyNotification.setSummary(mNoisyNotification.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNoisyNotification)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_SOUND_VIB_SCREEN_ON, mode, UserHandle.USER_CURRENT);
            int index = mNoisyNotification.findIndexOfValue((String) newValue);
            mNoisyNotification.setSummary(
                    mNoisyNotification.getEntries()[index]);
            return true;
        }
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.cardinal_settings_notification;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
