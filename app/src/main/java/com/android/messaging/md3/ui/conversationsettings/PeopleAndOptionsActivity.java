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

package com.android.messaging.md3.ui.conversationsettings;

import android.app.Fragment;
import android.app.UiModeManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.android.messaging.md3.R;
import com.android.messaging.md3.ui.UIIntents;
import com.android.messaging.md3.util.Assert;

import org.exthmui.settingslib.collapsingtoolbar.ExthmCollapsingToolbarBaseActivity;

/**
 * Shows a list of participants in a conversation.
 */
public class PeopleAndOptionsActivity extends ExthmCollapsingToolbarBaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_and_options_activity);

        Window window = getWindow();
        if (isDarkMode()){
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else{
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private boolean isDarkMode(){
        UiModeManager modeManager = (UiModeManager)getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
        return modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        if (fragment instanceof PeopleAndOptionsFragment) {
            final String conversationId =
                    getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            Assert.notNull(conversationId);
            final PeopleAndOptionsFragment peopleAndOptionsFragment =
                    (PeopleAndOptionsFragment) fragment;
            peopleAndOptionsFragment.setConversationId(conversationId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Treat the home press as back press so that when we go back to
                // ConversationActivity, it doesn't lose its original intent (conversation id etc.)
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
