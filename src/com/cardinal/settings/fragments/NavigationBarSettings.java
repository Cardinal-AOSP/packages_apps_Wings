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
import android.os.UserHandle;
import android.os.PowerManager;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import android.provider.SearchIndexableResource;
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
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.CustomUtils;

import com.cardinal.settings.preference.CustomSeekBarPreference;
import com.cardinal.settings.preference.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

public class NavigationBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "NavigationBar";

    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA = 0x20;

    private static final String KEYS_SHOW_NAVBAR_KEY = "navigation_bar_show";

    private static final String KEY_CATEGORY_BRIGHTNESS = "button_backlight";
    private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
    private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_BACKLIGHT_TOUCH = "button_backlight_on_touch_only";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";
    private static final String KEY_BUTTON_ANBI = "button_anbi";
    private static final String KEY_BUTTON_SWAP_KEYS = "swap_navigation_keys";

    private static final String KEY_HOME_LONG_PRESS        = "home_key_long_press";
    private static final String KEY_HOME_DOUBLE_TAP        = "home_key_double_tap";
    private static final String KEY_BACK_LONG_PRESS        = "back_key_long_press";
    private static final String KEY_BACK_DOUBLE_TAP        = "back_key_double_tap";
    private static final String KEY_MENU_LONG_PRESS        = "menu_key_long_press";
    private static final String KEY_MENU_DOUBLE_TAP        = "menu_key_double_tap";
    private static final String KEY_ASSIST_LONG_PRESS      = "assist_key_long_press";
    private static final String KEY_ASSIST_DOUBLE_TAP      = "assist_key_double_tap";
    private static final String KEY_APP_SWITCH_LONG_PRESS  = "app_switch_key_long_press";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP  = "app_switch_key_double_tap";
    private static final String KEY_CAMERA_LONG_PRESS      = "camera_key_long_press";
    private static final String KEY_CAMERA_DOUBLE_TAP      = "camera_key_double_tap";

    private static final String KEY_CATEGORY_HOME          = "home_key";
    private static final String KEY_CATEGORY_BACK          = "back_key";
    private static final String KEY_CATEGORY_MENU          = "menu_key";
    private static final String KEY_CATEGORY_ASSIST        = "assist_key";
    private static final String KEY_CATEGORY_APP_SWITCH    = "app_switch_key";
    private static final String KEY_CATEGORY_CAMERA        = "camera_key";

    private Handler mHandler;

    private int mDeviceHardwareKeys;
    private int mDeviceDefaultButtonBrightness;

    private boolean mDeviceHasButtonBrightnessSupport;
    private boolean mDeviceHasVariableButtonBrightness;

    private IPowerManager mPowerService;
    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mBackLongPressAction;
    private ListPreference mBackDoubleTapAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mMenuDoubleTapAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAssistDoubleTapAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mAppSwitchDoubleTapAction;
    private ListPreference mCameraLongPressAction;
    private ListPreference mCameraDoubleTapAction;
    private ListPreference mBacklightTimeout;
    private SwitchPreference mAnbiPreference;
    private SwitchPreference mEnableNavBar;
    private SwitchPreference mButtonBrightness;
    private SystemSettingSwitchPreference mButtonBacklightTouch;
    private SystemSettingSwitchPreference mSwapKeysPreference;
    private CustomSeekBarPreference mManualButtonBrightness;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cardinal_settings_navigationbar);

        mHandler = new Handler();

        final Resources res = getActivity().getResources();
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mDeviceHardwareKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        mDeviceHasVariableButtonBrightness = res.getBoolean(
                com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);

        mDeviceDefaultButtonBrightness = res.getInteger(
                com.android.internal.R.integer.config_buttonBrightnessSettingDefault);

        mDeviceHasButtonBrightnessSupport = res.getBoolean(
                com.android.internal.R.bool.config_button_brightness_support);

        mEnableNavBar = (SwitchPreference) prefSet.findPreference(
                KEYS_SHOW_NAVBAR_KEY);

        mSwapKeysPreference = (SystemSettingSwitchPreference) prefSet.findPreference(
                KEY_BUTTON_SWAP_KEYS);

        boolean showNavBarDefault = CustomUtils.deviceSupportNavigationBar(getActivity());
        boolean showNavBar = Settings.System.getInt(resolver,
                    Settings.System.NAVIGATION_BAR_SHOW, showNavBarDefault ? 1:0) == 1;
        mEnableNavBar.setChecked(showNavBar);

        final PreferenceCategory brightnessCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_BRIGHTNESS);

        mButtonBacklightTouch =
                (SystemSettingSwitchPreference) findPreference(KEY_BUTTON_BACKLIGHT_TOUCH);

        mManualButtonBrightness = (CustomSeekBarPreference) findPreference(KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        if (mManualButtonBrightness != null) {
            int ManualButtonBrightness = Settings.System.getInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, mDeviceDefaultButtonBrightness);
            mManualButtonBrightness.setValue(ManualButtonBrightness / 1);
            mManualButtonBrightness.setOnPreferenceChangeListener(this);
        }

        mButtonBrightness = (SwitchPreference) findPreference(KEY_BUTTON_BRIGHTNESS);
        if (mButtonBrightness != null) {
            mButtonBrightness.setChecked((Settings.System.getInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, 1) == 1));
            mButtonBrightness.setOnPreferenceChangeListener(this);
        }

        mBacklightTimeout = (ListPreference) findPreference(KEY_BUTTON_TIMEOUT);
        if (mBacklightTimeout != null) {
            int BacklightTimeout = Settings.System.getInt(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 5000);
            mBacklightTimeout.setValue(Integer.toString(BacklightTimeout));
            mBacklightTimeout.setSummary(mBacklightTimeout.getEntry());
            mBacklightTimeout.setOnPreferenceChangeListener(this);
        }

        mAnbiPreference = (SwitchPreference) findPreference(KEY_BUTTON_ANBI);
        if (mAnbiPreference != null) {
            mAnbiPreference.setChecked((Settings.System.getInt(resolver,
            		Settings.System.ANBI_ENABLED, 0) == 1));
            mAnbiPreference.setOnPreferenceChangeListener(this);
        }

        if (mDeviceHasVariableButtonBrightness) {
            brightnessCategory.removePreference(mButtonBrightness);
        } else {
            brightnessCategory.removePreference(mManualButtonBrightness);
        }

        if (!mDeviceHasButtonBrightnessSupport) {
            brightnessCategory.removePreference(mButtonBrightness);
            brightnessCategory.removePreference(mManualButtonBrightness);
            brightnessCategory.removePreference(mBacklightTimeout);
            brightnessCategory.removePreference(mButtonBacklightTouch);
            prefSet.removePreference(brightnessCategory);
        }

        if (mDeviceHardwareKeys == 0) {
            prefSet.removePreference(mAnbiPreference);
            prefSet.removePreference(mSwapKeysPreference);
            prefSet.removePreference(mEnableNavBar);
        }

        final boolean hasHome = (mDeviceHardwareKeys & KEY_MASK_HOME) != 0 || showNavBar;
        final boolean hasMenu = (mDeviceHardwareKeys & KEY_MASK_MENU) != 0;
        final boolean hasBack = (mDeviceHardwareKeys & KEY_MASK_BACK) != 0 || showNavBar;
        final boolean hasAssist = (mDeviceHardwareKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitch = (mDeviceHardwareKeys & KEY_MASK_APP_SWITCH) != 0 || showNavBar;
        final boolean hasCamera = (mDeviceHardwareKeys & KEY_MASK_CAMERA) != 0;

        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_HOME);

        if (hasHome && homeCategory != null) {
        /* Home Key Long Press */
        int defaultLongPressOnHomeKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnHomeKeyBehavior);
        int longPressOnHomeKeyBehavior = Settings.System.getIntForUser(resolver,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION,
                    defaultLongPressOnHomeKeyBehavior,
                    UserHandle.USER_CURRENT);
        mHomeLongPressAction = initActionList(KEY_HOME_LONG_PRESS, longPressOnHomeKeyBehavior);

        /* Home Key Double Tap */
        int defaultDoubleTapOnHomeKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnHomeKeyBehavior);
        int doubleTapOnHomeKeyBehavior = Settings.System.getIntForUser(resolver,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                    defaultDoubleTapOnHomeKeyBehavior,
                    UserHandle.USER_CURRENT);
        mHomeDoubleTapAction = initActionList(KEY_HOME_DOUBLE_TAP, doubleTapOnHomeKeyBehavior);
        } else {
            prefSet.removePreference(homeCategory);
        }

        final PreferenceCategory backCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_BACK);

        if (hasBack && backCategory != null) {
        /* Back Key Long Press */
        int defaultLongPressOnBackKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnBackKeyBehavior);
        int longPressOnBackKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultLongPressOnBackKeyBehavior,
                UserHandle.USER_CURRENT);
        mBackLongPressAction = initActionList(KEY_BACK_LONG_PRESS, longPressOnBackKeyBehavior);

        /* Back Key Double Tap */
        int defaultDoubleTapOnBackKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnBackKeyBehavior);
        int doubleTapOnBackKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_BACK_DOUBLE_TAP_ACTION,
                defaultDoubleTapOnBackKeyBehavior,
                UserHandle.USER_CURRENT);
        mBackDoubleTapAction = initActionList(KEY_BACK_DOUBLE_TAP, doubleTapOnBackKeyBehavior);
        } else {
            prefSet.removePreference(backCategory);
        }

        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_MENU);

        if (hasMenu && menuCategory != null) {
        /* Menu Key Long Press */
        int defaultLongPressOnMenuKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnMenuKeyBehavior);
        int longPressOnMenuKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_MENU_LONG_PRESS_ACTION,
                defaultLongPressOnMenuKeyBehavior,
                UserHandle.USER_CURRENT);
        mMenuLongPressAction = initActionList(KEY_MENU_LONG_PRESS, longPressOnMenuKeyBehavior);

        /* Menu Key Double Tap */
        int defaultDoubleTapOnMenuKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnMenuKeyBehavior);
        int doubleTapOnMenuKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_MENU_DOUBLE_TAP_ACTION,
                defaultDoubleTapOnMenuKeyBehavior,
                UserHandle.USER_CURRENT);
        mMenuDoubleTapAction = initActionList(KEY_MENU_DOUBLE_TAP, doubleTapOnMenuKeyBehavior);
        } else {
            prefSet.removePreference(menuCategory);
        }

        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_ASSIST);

        if (hasAssist && assistCategory != null) {
        /* Assist Key Long Press */
        int defaultLongPressOnAssistKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnAssistKeyBehavior);
        int longPressOnAssistKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_ASSIST_LONG_PRESS_ACTION,
                defaultLongPressOnAssistKeyBehavior,
                UserHandle.USER_CURRENT);
        mAssistLongPressAction = initActionList(KEY_ASSIST_LONG_PRESS, longPressOnAssistKeyBehavior);

        /* Assist Key Double Tap */
        int defaultDoubleTapOnAssistKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnAssistKeyBehavior);
        int doubleTapOnAssistKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION,
                defaultDoubleTapOnAssistKeyBehavior,
                UserHandle.USER_CURRENT);
        mAssistDoubleTapAction = initActionList(KEY_ASSIST_DOUBLE_TAP, doubleTapOnAssistKeyBehavior);
        } else {
            prefSet.removePreference(assistCategory);
        }

        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_APP_SWITCH);

        if (hasAppSwitch && appSwitchCategory != null) {
        /* AppSwitch Key Long Press */
        int defaultLongPressOnAppSwitchKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnAppSwitchKeyBehavior);
        int longPressOnAppSwitchKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultLongPressOnAppSwitchKeyBehavior,
                UserHandle.USER_CURRENT);
        mAppSwitchLongPressAction = initActionList(KEY_APP_SWITCH_LONG_PRESS, longPressOnAppSwitchKeyBehavior);

        /* AppSwitch Key Double Tap */
        int defaultDoubleTapOnAppSwitchKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnAppSwitchKeyBehavior);
        int doubleTapOnAppSwitchKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                defaultDoubleTapOnAppSwitchKeyBehavior,
                UserHandle.USER_CURRENT);
        mAppSwitchDoubleTapAction = initActionList(KEY_APP_SWITCH_DOUBLE_TAP, doubleTapOnAppSwitchKeyBehavior);
        } else {
            prefSet.removePreference(appSwitchCategory);
        }

        final PreferenceCategory cameraCategory =
                (PreferenceCategory) prefSet.findPreference(KEY_CATEGORY_CAMERA);

        if (hasCamera && cameraCategory != null) {
        /* Camera Key Long Press */
        int defaultLongPressOnCameraKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_longPressOnCameraKeyBehavior);
        int longPressOnCameraKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_CAMERA_LONG_PRESS_ACTION,
                defaultLongPressOnCameraKeyBehavior,
                UserHandle.USER_CURRENT);
        mCameraLongPressAction = initActionList(KEY_CAMERA_LONG_PRESS, longPressOnCameraKeyBehavior);

        /* Camera Key Double Tap */
        int defaultDoubleTapOnCameraKeyBehavior = res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnCameraKeyBehavior);
        int doubleTapOnCameraKeyBehavior = Settings.System.getIntForUser(resolver,
                Settings.System.KEY_CAMERA_DOUBLE_TAP_ACTION,
                defaultDoubleTapOnCameraKeyBehavior,
                UserHandle.USER_CURRENT);
        mCameraDoubleTapAction = initActionList(KEY_CAMERA_DOUBLE_TAP, doubleTapOnCameraKeyBehavior);
        } else {
            prefSet.removePreference(cameraCategory);
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list != null) {
            list.setValue(Integer.toString(value));
            list.setSummary(list.getEntry());
            list.setOnPreferenceChangeListener(this);
        }
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
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
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();

        if (preference == mBacklightTimeout) {
            String BacklightTimeout = (String) newValue;
            int BacklightTimeoutValue = Integer.parseInt(BacklightTimeout);
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, BacklightTimeoutValue);
            int BacklightTimeoutIndex = mBacklightTimeout.findIndexOfValue(BacklightTimeout);
            mBacklightTimeout.setSummary(mBacklightTimeout.getEntries()[BacklightTimeoutIndex]);
            return true;
        } else if (preference == mManualButtonBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, value * 1);
            return true;
        } else if (preference == mButtonBrightness) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, value ? 1 : 0);
            return true;
        } else if (preference == mHomeLongPressAction) {
            handleActionListChange(mHomeLongPressAction, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleActionListChange(mHomeDoubleTapAction, newValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mBackLongPressAction) {
            handleActionListChange(mBackLongPressAction, newValue,
                    Settings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mBackDoubleTapAction) {
            handleActionListChange(mBackDoubleTapAction, newValue,
                    Settings.System.KEY_BACK_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleActionListChange(mMenuLongPressAction, newValue,
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mMenuDoubleTapAction) {
            handleActionListChange(mMenuDoubleTapAction, newValue,
                    Settings.System.KEY_MENU_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleActionListChange(mAssistLongPressAction, newValue,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistDoubleTapAction) {
            handleActionListChange(mAssistDoubleTapAction, newValue,
                    Settings.System.KEY_ASSIST_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleActionListChange(mAppSwitchLongPressAction, newValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchDoubleTapAction) {
            handleActionListChange(mAppSwitchDoubleTapAction, newValue,
                    Settings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mCameraLongPressAction) {
            handleActionListChange(mCameraLongPressAction, newValue,
                    Settings.System.KEY_CAMERA_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mCameraDoubleTapAction) {
            handleActionListChange(mCameraDoubleTapAction, newValue,
                    Settings.System.KEY_CAMERA_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAnbiPreference) {
             boolean value = (Boolean) newValue;
             Settings.System.putInt(getActivity().getContentResolver(),
                      Settings.System.ANBI_ENABLED, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WINGS;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.cardinal_settings_navigationbar;
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
