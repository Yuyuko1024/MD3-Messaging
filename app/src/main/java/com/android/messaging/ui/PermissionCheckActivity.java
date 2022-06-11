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

package com.android.messaging.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.util.Assert;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Activity to check if the user has required permissions. If not, it will try to prompt the user
 * to grant permissions. However, the OS may not actually prompt the user if the user had
 * previously checked the "Never ask again" checkbox while denying the required permissions.
 */
public class PermissionCheckActivity extends Activity {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 250;
    private static final String PACKAGE_URI_PREFIX = "package:";
    private static final String PROP_MIUI_VER_CODE = "ro.miui.ui.version.name";
    private long mRequestTimeMillis;
    private TextView mNextView;
    private TextView mSettingsView;
    private boolean isMiui;
    private int mSubId ;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (redirectIfNeeded()) {
            return;
        }
        if(TextUtils.isEmpty(getSystemPropertyValue(PROP_MIUI_VER_CODE))){
            Log.d("MIUICheck","This device is NOT Mi Phone");
            isMiui=false;
        }else{
            Log.d("MIUICheck","Mi Phone deteced!");
            isMiui=true;
        }

        setContentView(R.layout.permission_check_activity);
        UiUtils.setStatusBarColor(this, getColor(R.color.permission_check_activity_background));

        findViewById(R.id.exit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                finish();
            }
        });

        mNextView = (TextView) findViewById(R.id.next);
        mNextView.setOnClickListener(view -> tryRequestPermission());

        mSettingsView = (TextView) findViewById(R.id.settings);
        mSettingsView.setOnClickListener(view -> {
            final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (redirectIfNeeded()) {
            return;
        }
    }

    private static String getSystemPropertyValue(String key) {
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            String value = (String) getMethod.invoke(classType, new Object[]{key});
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void tryRequestPermission() {
        final String[] missingPermissions = OsUtil.getMissingRequiredPermissions();
        if (missingPermissions.length == 0) {
            if (isMiui){
                final Intent intent = new Intent(this,MiuiPermissionGrantActivity.class);
                startActivity(intent);
            }else{
                redirect();
            }
            return;
        }

        mRequestTimeMillis = SystemClock.elapsedRealtime();
        requestPermissions(missingPermissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            // We do not use grantResults as some of the granted permissions might have been
            // revoked while the permissions dialog box was being shown for the missing permissions.
            if (!redirectIfNeeded()) {
                final long currentTimeMillis = SystemClock.elapsedRealtime();
                // If the permission request completes very quickly, it must be because the system
                // automatically denied. This can happen if the user had previously denied it
                // and checked the "Never ask again" check box.
                if ((currentTimeMillis - mRequestTimeMillis) < AUTOMATED_RESULT_THRESHOLD_MILLLIS) {
                    mNextView.setVisibility(View.GONE);

                    mSettingsView.setVisibility(View.VISIBLE);
                    findViewById(R.id.enable_permission_procedure).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /** Returns true if the redirecting was performed */
    private boolean redirectIfNeeded() {
        if (!OsUtil.hasRequiredPermissions()) {
            return false;
        }
        Intent intent = getIntent();
        Assert.notNull(intent);
        mSubId = (intent != null) ? intent.getIntExtra(UIIntents.UI_INTENT_EXTRA_SUB_ID,
                ParticipantData.DEFAULT_SELF_SUB_ID) : ParticipantData.DEFAULT_SELF_SUB_ID;
        Log.e("DEBUG PhoneNum ", PhoneUtils.get(mSubId).getCanonicalForSelf(false));
        if (TextUtils.isEmpty(PhoneUtils.get(mSubId).getCanonicalForSelf(false))) {
            return false;
        }

        Factory.get().onRequiredPermissionsAcquired();
        redirect();
        return true;
    }

    private void redirect() {
        UIIntents.get().launchConversationListActivity(this);
        finish();
    }
}
