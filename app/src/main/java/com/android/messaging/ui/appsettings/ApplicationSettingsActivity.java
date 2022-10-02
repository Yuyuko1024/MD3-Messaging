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

package com.android.messaging.ui.appsettings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.android.messaging.R;
import com.android.messaging.ui.LicenseActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.DebugUtils;
import org.exthmui.settingslib.collapsingtoolbar.ExthmCollapsingToolbarBaseActivity;

import java.util.Objects;

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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, applicationSettingsFragment)
                .commit();
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

    public static class ApplicationSettingsFragment extends PreferenceFragmentCompat implements com.android.messaging.ui.appsettings.ApplicationSettingsFragment {
        private String mNotificationsPreferenceKey;
        private Preference mNotificationsPreference;
        private Preference mLicensePreference;
        private String mLicensePrefKey;
        private String mSwipeRightToDeleteConversationkey;
        private SwitchPreference mSwipeRightToDeleteConversationPreference;

        public ApplicationSettingsFragment() {
            // Required empty constructor
        }

        @Override
        public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            getPreferenceManager().setSharedPreferencesName(BuglePrefs.SHARED_PREFERENCES_NAME);
            addPreferencesFromResource(R.xml.preferences_application);

            mNotificationsPreferenceKey =
                    getString(R.string.notifications_pref_key);
            mNotificationsPreference = findPreference(mNotificationsPreferenceKey);
            mLicensePrefKey = getString(R.string.key_license);
            mLicensePreference = findPreference(mLicensePrefKey);
            mSwipeRightToDeleteConversationkey = getString(
                    R.string.swipe_right_deletes_conversation_key);
            mSwipeRightToDeleteConversationPreference = findPreference(mSwipeRightToDeleteConversationkey);

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

        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            if (Objects.equals(preference.getKey(), mNotificationsPreferenceKey)) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
                startActivity(intent);
            }
            if (Objects.equals(preference.getKey(), mLicensePrefKey)){
                final Intent intent = new Intent(getActivity(), LicenseActivity.class);
                startActivity(intent);
            }
            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}
