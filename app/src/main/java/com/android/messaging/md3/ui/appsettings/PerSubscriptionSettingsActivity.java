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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.mms.MmsManager;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.android.messaging.md3.Factory;
import com.android.messaging.md3.R;
import com.android.messaging.md3.datamodel.data.ParticipantData;
import com.android.messaging.md3.sms.ApnDatabase;
import com.android.messaging.md3.sms.MmsConfig;
import com.android.messaging.md3.sms.MmsUtils;
import com.android.messaging.md3.ui.UIIntents;
import com.android.messaging.md3.util.Assert;
import com.android.messaging.md3.util.BuglePrefs;
import com.android.messaging.md3.util.LogUtil;
import com.android.messaging.md3.util.PhoneUtils;

import org.exthmui.settingslib.collapsingtoolbar.ExthmCollapsingToolbarBaseActivity;

public class PerSubscriptionSettingsActivity extends ExthmCollapsingToolbarBaseActivity {
    Fragment perSubscriptionSettingsFragment = new PerSubscriptionSettingsFragment();
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        final String title = getIntent().getStringExtra(
                UIIntents.UI_INTENT_EXTRA_PER_SUBSCRIPTION_SETTING_TITLE);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        } else {
            // This will fall back to the default title, i.e. "Messaging settings," so No-op.
        }

        Window window = getWindow();
        if (isDarkMode()){
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else{
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, perSubscriptionSettingsFragment)
                .commit();
    }

    private boolean isDarkMode(){
        UiModeManager modeManager = (UiModeManager)getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
        return modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PerSubscriptionSettingsFragment extends PreferenceFragmentCompat
            implements OnSharedPreferenceChangeListener {
        private Preference mGroupMmsPreference;
        private String mGroupMmsPrefKey;
        private int mSubId;

        public PerSubscriptionSettingsFragment() {
            // Required empty constructor
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {

            // Get sub id from launch intent
            final Intent intent = getActivity().getIntent();
            Assert.notNull(intent);
            mSubId = (intent != null) ? intent.getIntExtra(UIIntents.UI_INTENT_EXTRA_SUB_ID,
                    ParticipantData.DEFAULT_SELF_SUB_ID) : ParticipantData.DEFAULT_SELF_SUB_ID;

            final BuglePrefs subPrefs = Factory.get().getSubscriptionPrefs(mSubId);
            getPreferenceManager().setSharedPreferencesName(subPrefs.getSharedPreferencesName());
            addPreferencesFromResource(R.xml.preferences_per_subscription);

            final PreferenceCategory advancedCategory = (PreferenceCategory)
                    findPreference(getString(R.string.advanced_category_pref_key));
            final PreferenceCategory mmsCategory = (PreferenceCategory)
                    findPreference(getString(R.string.mms_messaging_category_pref_key));

            mGroupMmsPrefKey = getString(R.string.group_mms_pref_key);
            mGroupMmsPreference = findPreference(mGroupMmsPrefKey);
            if (!MmsConfig.get(mSubId).getGroupMmsEnabled()) {
                // Always show group messaging setting even if the SIM has no number
                // If broadcast sms is selected, the SIM number is not needed
                // If group mms is selected, the phone number dialog will popup when message
                // is being sent, making sure we will have a self number for group mms.
                mmsCategory.removePreference(mGroupMmsPreference);
            } else {
                mGroupMmsPreference.setOnPreferenceClickListener(pref -> {
                    GroupMmsSettingDialog.showDialog(getActivity(), mSubId);
                    return true;
                });
                updateGroupMmsPrefSummary();
            }

            if (!MmsConfig.get(mSubId).getSMSDeliveryReportsEnabled()) {
                final Preference deliveryReportsPref = findPreference(
                        getString(R.string.delivery_reports_pref_key));
                advancedCategory.removePreference(deliveryReportsPref);
            }
            final Preference wirelessAlertPref = findPreference(getString(
                    R.string.wireless_alerts_key));
            if (!isCellBroadcastAppLinkEnabled()) {
                advancedCategory.removePreference(wirelessAlertPref);
            } else {
                wirelessAlertPref.setOnPreferenceClickListener(
                        preference -> {
                            try {
                                startActivity(UIIntents.get().getWirelessAlertsIntent());
                            } catch (final ActivityNotFoundException e) {
                                // Handle so we shouldn't crash if the wireless alerts
                                // implementation is broken.
                                LogUtil.e(LogUtil.BUGLE_TAG,
                                        "Failed to launch wireless alerts activity", e);
                            }
                            return true;
                        });
            }

            // Access Point Names (APNs)
            final PreferenceScreen apnsScreen =
                    (PreferenceScreen) findPreference(getString(R.string.sms_apns_key));

            if (!MmsManager.shouldUseLegacyMms()
                    || (MmsUtils.useSystemApnTable() && !ApnDatabase.doesDatabaseExist())) {
                // 1) Remove the ability to edit the local APN prefs if it doesn't use legacy APIs.
                // 2) Don't remove the ability to edit the local APN prefs if this device lets us
                // access the system APN, but we can't find the MCC/MNC in the APN table and we
                // created the local APN table in case the MCC/MNC was in there. In other words,
                // if the local APN table exists, let the user edit it.
                advancedCategory.removePreference((Preference) apnsScreen);
            } else {
                apnsScreen.setIntent(UIIntents.get()
                        .getApnSettingsIntent(getPreferenceScreen().getContext(), mSubId));
            }

            // We want to disable preferences if we are not the default app, but we do all of the
            // above first so that the user sees the correct information on the screen
            if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
                mGroupMmsPreference.setEnabled(false);
                final Preference autoRetrieveMmsPreference =
                        findPreference(getString(R.string.auto_retrieve_mms_pref_key));
                autoRetrieveMmsPreference.setEnabled(false);
                final Preference deliveryReportsPreference =
                        findPreference(getString(R.string.delivery_reports_pref_key));
                if (deliveryReportsPreference != null) {
                    deliveryReportsPreference.setEnabled(false);
                }
            }

            if (advancedCategory.getPreferenceCount() == 0) {
                getPreferenceScreen().removePreference(advancedCategory);
            }

        }

        private boolean isCellBroadcastAppLinkEnabled() {
            if (!MmsConfig.get(mSubId).getShowCellBroadcast()) {
                return false;
            }
            try {
                final PackageManager pm = getActivity().getPackageManager();
                return pm.getApplicationEnabledSetting(UIIntents.CMAS_COMPONENT)
                        != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            } catch (final IllegalArgumentException ignored) {
                // CMAS app not installed.
            }
            return false;
        }

        private void updateGroupMmsPrefSummary() {
            final boolean groupMmsEnabled = getPreferenceScreen().getSharedPreferences().getBoolean(
                    mGroupMmsPrefKey, getResources().getBoolean(R.bool.group_mms_pref_default));
            mGroupMmsPreference.setSummary(groupMmsEnabled ?
                    R.string.enable_group_mms : R.string.disable_group_mms);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                final String key) {
            if (key.equals(mGroupMmsPrefKey)) {
                updateGroupMmsPrefSummary();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
