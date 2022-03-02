/*
 * Copyright (C) 2017 AospExtended ROM Project
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

package org.aospextended.extensions.fragments;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;
import com.android.internal.util.aospextended.AEXUtils;

import org.aospextended.support.preference.SecureSettingSwitchPreference;
import org.aospextended.support.preference.SystemSettingSwitchPreference;
import org.aospextended.support.preference.SystemSettingSeekBarPreference;

public class MiscExtensions extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_STATUS_BAR_LOGO = "status_bar_logo";
    private static final String COMBINED_STATUSBAR_ICONS = "show_combined_status_bar_signal_icons";
    private static final String CONFIG_RESOURCE_NAME = "flag_combined_status_bar_signal_icons";
    private static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    private static final String LOCATION_DEVICE_CONFIG = "location_indicators_enabled";
    private static final String CAMERA_DEVICE_CONFIG = "camera_indicators_enabled";
    private static final String LOCATION_INDICATOR = "enable_location_privacy_indicator";
    private static final String CAMERA_INDICATOR = "enable_camera_privacy_indicator";
    private static final String KEY_VOLTE_ICON_STYLE = "volte_icon_style";
    private static final String KEY_VOWIFI_ICON_STYLE = "vowifi_icon_style";
    private static final String KEY_VOLTE_VOWIFI_OVERRIDE = "volte_vowifi_override";

    private SwitchPreference mShowAexLogo;
    private SecureSettingSwitchPreference mLocationIndicator;
    SecureSettingSwitchPreference mCombinedIcons;
    private SecureSettingSwitchPreference mCamIndicator;
    private SystemSettingSeekBarPreference mVolteIconStyle;
    private SystemSettingSeekBarPreference mVowifiIconStyle;
    private SwitchPreference mOverride;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.misc_extensions);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

	mShowAexLogo = (SwitchPreference) findPreference(KEY_STATUS_BAR_LOGO);
        mShowAexLogo.setChecked((Settings.System.getInt(getContentResolver(),
             Settings.System.STATUS_BAR_LOGO, 0) == 1));
        mShowAexLogo.setOnPreferenceChangeListener(this);
        
        mCombinedIcons = (SecureSettingSwitchPreference)
                findPreference(COMBINED_STATUSBAR_ICONS);
        Resources sysUIRes = null;
        boolean def = false;
        int resId = 0;
        try {
            sysUIRes = getActivity().getPackageManager()
                    .getResourcesForApplication(SYSTEMUI_PACKAGE);
        } catch (Exception ignored) {
            // If you don't have system UI you have bigger issues
        }
        if (sysUIRes != null) {
            resId = sysUIRes.getIdentifier(
                    CONFIG_RESOURCE_NAME, "bool", SYSTEMUI_PACKAGE);
            if (resId != 0) def = sysUIRes.getBoolean(resId);
        }
        mCombinedIcons.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(), COMBINED_STATUSBAR_ICONS, def ? 1 : 0) == 1);
        mCombinedIcons.setOnPreferenceChangeListener(this);
        
        mLocationIndicator = (SecureSettingSwitchPreference) findPreference(LOCATION_INDICATOR);
        boolean locIndicator = DeviceConfig.getBoolean(DeviceConfig.NAMESPACE_PRIVACY,
                LOCATION_DEVICE_CONFIG, false);
        mLocationIndicator.setDefaultValue(locIndicator);
        mLocationIndicator.setChecked(Settings.Secure.getInt(resolver,
                LOCATION_INDICATOR, locIndicator ? 1 : 0) == 1);

        mCamIndicator = (SecureSettingSwitchPreference) findPreference(CAMERA_INDICATOR);
        boolean camIndicator = DeviceConfig.getBoolean(DeviceConfig.NAMESPACE_PRIVACY,
                CAMERA_DEVICE_CONFIG, false);
        mCamIndicator.setDefaultValue(camIndicator);
        mCamIndicator.setChecked(Settings.Secure.getInt(resolver,
                CAMERA_INDICATOR, camIndicator ? 1 : 0) == 1);
                
        mVolteIconStyle = (SystemSettingSeekBarPreference) findPreference(KEY_VOLTE_ICON_STYLE);
        mVowifiIconStyle = (SystemSettingSeekBarPreference) findPreference(KEY_VOWIFI_ICON_STYLE);
        mOverride = (SwitchPreference) findPreference(KEY_VOLTE_VOWIFI_OVERRIDE);
        
        if (!AEXUtils.isVoiceCapable(getActivity())) {
            prefSet.removePreference(mVolteIconStyle);
            prefSet.removePreference(mVowifiIconStyle);
            prefSet.removePreference(mOverride);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EXTENSIONS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if  (preference == mShowAexLogo) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, value ? 1 : 0);
            return true;
	} else if (preference == mCombinedIcons) {
            Settings.Secure.putInt(getActivity().getContentResolver(), COMBINED_STATUSBAR_ICONS, (boolean) objValue ? 1 : 0);
            return true;
        }
        return false;
    }
}
