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
import android.provider.Telephony;

import com.android.messaging.md3.util.ContentType;
import com.android.messaging.md3.util.OsUtil;
import com.android.messaging.md3.util.PhoneUtils;

/**
 * This receiver is used to abort MMS WAP broadcasts pre-KLP when SMS is enabled.
 */
public class AbortMmsWapPushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION.equals(intent.getAction())
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            // If we are enabled, it's our job to stop the broadcast from continuing. This
            // receiver is not used on KLP but we do an extra check here just to make sure.
            if (!OsUtil.isAtLeastKLP() && PhoneUtils.getDefault().isSmsEnabled()) {
                abortBroadcast();
            }
        }
    }
}
