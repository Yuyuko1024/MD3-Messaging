/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.messaging.md3.ui.appsettings;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragmentCompat;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.SwitchPreference;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.android.messaging.md3.R;
import com.android.messaging.md3.ui.LicenseActivity;
import com.android.messaging.md3.ui.UIIntents;
import com.android.messaging.md3.util.BuglePrefs;
import com.android.messaging.md3.util.DebugUtils;
import com.android.messaging.md3.util.OsUtil;
import com.android.messaging.md3.util.PhoneUtils;

import org.exthmui.settingslib.collapsingtoolbar.ExthmCollapsingToolbarBaseActivity;

public class ApplicationSettingsActivity extends ExthmCollapsingToolbarBaseActivity {
    Fragment applicationSettingsFragment = new ApplicationSettingsFragment();
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        final boolean topLevel = getIntent().getBooleanExtra(
                UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false);
        if (topLevel) {
            setTitle(getString(R.string.settings_activity_title));
        }

        Window window = getWindow();
        if (isDarkMode()){
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else{
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, applicationSettingsFragment)
                .commit();
    }

    private boolean isDarkMode(){
        UiModeManager modeManager = (UiModeManager)getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
        return modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (super.onCreateOptionsMenu(menu)) {
            return true;
        }
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        case R.id.action_license:
            final Intent intent = new Intent(this, LicenseActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ApplicationSettingsFragment extends PreferenceFragmentCompat {

        private String mNotificationsPreferenceKey;
        private Preference mNotificationsPreference;
        private String mSmsDisabledPrefKey;
        private Preference mSmsDisabledPreference;
        private String mSmsEnabledPrefKey;
        private Preference mSmsEnabledPreference;
        private Preference mLicensePreference;
        private String mLicensePrefKey;
        private boolean mIsSmsPreferenceClicked;
        private String mSwipeRightToDeleteConversationkey;
        private SwitchPreference mSwipeRightToDeleteConversationPreference;

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            getPreferenceManager().setSharedPreferencesName(BuglePrefs.SHARED_PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.preferences_application);

            mNotificationsPreferenceKey =
                    getString(R.string.notifications_pref_key);
            mNotificationsPreference = findPreference(mNotificationsPreferenceKey);
            mSmsDisabledPrefKey = getString(R.string.sms_disabled_pref_key);
            mSmsDisabledPreference = findPreference(mSmsDisabledPrefKey);
            mSmsEnabledPrefKey = getString(R.string.sms_enabled_pref_key);
            mSmsEnabledPreference = findPreference(mSmsEnabledPrefKey);
            mLicensePrefKey = getString(R.string.key_license);
            mLicensePreference = findPreference(mLicensePrefKey);
            mSwipeRightToDeleteConversationkey = getString(
                    R.string.swipe_right_deletes_conversation_key);
            mSwipeRightToDeleteConversationPreference = findPreference(mSwipeRightToDeleteConversationkey);
            mIsSmsPreferenceClicked = false;

            if (!DebugUtils.isDebugEnabled()) {
                final Preference debugCategory = findPreference(getString(
                        R.string.debug_pref_key));
                getPreferenceScreen().removePreference(debugCategory);
            }

            final PreferenceScreen advancedScreen = (PreferenceScreen) findPreference(
                    getString(R.string.advanced_pref_key));
            final boolean topLevel = getActivity().getIntent().getBooleanExtra(
                    UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false);
            if (topLevel) {
                advancedScreen.setIntent(UIIntents.get()
                        .getAdvancedSettingsIntent(getPreferenceScreen().getContext()));
            } else {
                // Hide the Advanced settings screen if this is not top-level; these are shown at
                // the parent SettingsActivity.
                getPreferenceScreen().removePreference(advancedScreen);
            }
        }

        public ApplicationSettingsFragment() {
            // Required empty constructor
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            if (preference.getKey() == mNotificationsPreferenceKey) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
                startActivity(intent);
            }
            if (preference.getKey() ==  mSmsDisabledPrefKey ||
                    preference.getKey() == mSmsEnabledPrefKey) {
                mIsSmsPreferenceClicked = true;
            }
            if (preference.getKey() == mLicensePrefKey){
                final Intent intent = new Intent(getActivity(), LicenseActivity.class);
                startActivity(intent);
            }
            return super.onPreferenceTreeClick(preference);
        }

        private void updateSmsEnabledPreferences() {
            if (!OsUtil.isAtLeastKLP()) {
                getPreferenceScreen().removePreference(mSmsDisabledPreference);
                getPreferenceScreen().removePreference(mSmsEnabledPreference);
            } else {
                final String defaultSmsAppLabel = getString(R.string.default_sms_app,
                        PhoneUtils.getDefault().getDefaultSmsAppLabel());
                boolean isSmsEnabledBeforeState;
                boolean isSmsEnabledCurrentState;
                if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                    if (getPreferenceScreen().findPreference(mSmsEnabledPrefKey) == null) {
                        getPreferenceScreen().addPreference(mSmsEnabledPreference);
                        isSmsEnabledBeforeState = false;
                    } else {
                        isSmsEnabledBeforeState = true;
                    }
                    isSmsEnabledCurrentState = true;
                    getPreferenceScreen().removePreference(mSmsDisabledPreference);
                    mSmsEnabledPreference.setSummary(defaultSmsAppLabel);
                } else {
                    if (getPreferenceScreen().findPreference(mSmsDisabledPrefKey) == null) {
                        getPreferenceScreen().addPreference(mSmsDisabledPreference);
                        isSmsEnabledBeforeState = true;
                    } else {
                        isSmsEnabledBeforeState = false;
                    }
                    isSmsEnabledCurrentState = false;
                    getPreferenceScreen().removePreference(mSmsEnabledPreference);
                    mSmsDisabledPreference.setSummary(defaultSmsAppLabel);
                }
            }
            mIsSmsPreferenceClicked = false;
        }


        @Override
        public void onResume() {
            super.onResume();
            updateSmsEnabledPreferences();
        }
    }
}
