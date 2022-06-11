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
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.util.Assert;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.google.android.material.button.MaterialButton;

/**
 * Activity to check if the user has required permissions. If not, it will try to prompt the user
 * to grant permissions. However, the OS may not actually prompt the user if the user had
 * previously checked the "Never ask again" checkbox while denying the required permissions.
 */
public class MiuiPermissionGrantActivity extends Activity {
    private int mSubId ;
    private MaterialButton mNextView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (redirectIfNeeded()) {
            return;
        }

        setContentView(R.layout.miui_permission_grant_activity);
        UiUtils.setStatusBarColor(this, getColor(R.color.permission_check_activity_background));

        findViewById(R.id.exit).setOnClickListener(view -> finish());

        mNextView = findViewById(R.id.next);
        mNextView.setOnClickListener(view -> tryRequestPermission());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (redirectIfNeeded()) {
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void tryRequestPermission() {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    /** Returns true if the redirecting was performed */
    private boolean redirectIfNeeded() {
        Intent intent = getIntent();
        Assert.notNull(intent);
        mSubId = (intent != null) ? intent.getIntExtra(UIIntents.UI_INTENT_EXTRA_SUB_ID,
                ParticipantData.DEFAULT_SELF_SUB_ID) : ParticipantData.DEFAULT_SELF_SUB_ID;
        Log.e("DEBUG PhoneNum ",PhoneUtils.get(mSubId).getCanonicalForSelf(false));
        if (TextUtils.isEmpty(PhoneUtils.get(mSubId).getCanonicalForSelf(false))) {
            return false;
        }

        redirect();
        return true;
    }

    private void redirect() {
        UIIntents.get().launchConversationListActivity(this);
        finish();
    }
}
