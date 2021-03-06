/*
 * Copyright (C) 2017 Citrus-AOSP Project
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import com.android.internal.util.custom.CustomUtils;

import com.cardinal.settings.preference.SystemSettingSwitchPreference;
import com.cardinal.settings.preference.SecureSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFpKeystore;
    private SystemSettingSwitchPreference mFingerprintVib;
    private SecureSettingSwitchPreference mFaceUnlock;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.cardinal_settings_lockscreen);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFpKeystore = (SwitchPreference) findPreference(FP_UNLOCK_KEYSTORE);
        mFingerprintVib = (SystemSettingSwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()){
            prefSet.removePreference(mFpKeystore);
            prefSet.removePreference(mFingerprintVib);
        } else {
            mFpKeystore.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FP_UNLOCK_KEYSTORE, 0) == 1));
            mFpKeystore.setOnPreferenceChangeListener(this);
        }

        mFaceUnlock = (SecureSettingSwitchPreference) findPreference("face_auto_unlock");
        if (!CustomUtils.isAppInstalled(getContext(), "com.android.facelock")) {
            prefSet.removePreference(mFaceUnlock);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mFpKeystore) {
         boolean value = (Boolean) objValue;
         Settings.System.putInt(getActivity().getContentResolver(),
                  Settings.System.FP_UNLOCK_KEYSTORE, value ? 1 : 0);
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
                    sir.xmlResId = R.xml.cardinal_settings_lockscreen;
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
