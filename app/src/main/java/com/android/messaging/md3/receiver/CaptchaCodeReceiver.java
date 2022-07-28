/*
 * Copyright 2021 The exTHmUI Open Source Project
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

import com.android.messaging.md3.R;
import com.android.messaging.md3.datamodel.action.MarkAsReadAction;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class CaptchaCodeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if (extras == null) {
            return; // do nothing
        }

        String chapataCode = extras.getString("chapataCode");

        if (!TextUtils.isEmpty(chapataCode)) {
            ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(chapataCode);
            Toast.makeText(context, context.getString(R.string.captcha_has_copied_toast), Toast.LENGTH_SHORT).show();

            /* Mark as read */
            String conversationId = extras.getString("conversationId");
            MarkAsReadAction.markAsRead(conversationId);
        }
    }
}