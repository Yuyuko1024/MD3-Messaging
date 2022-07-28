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

package com.android.messaging.md3.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.messaging.md3.sms.SmsStorageStatusManager;

/**
 * Receiver that listens on storage status changes
 */
public class StorageStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(intent.getAction())) {
            SmsStorageStatusManager.handleStorageLow();
        } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(intent.getAction())) {
            SmsStorageStatusManager.handleStorageOk();
        }
    }
}
