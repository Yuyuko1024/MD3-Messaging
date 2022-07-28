package com.android.messaging.md3.ui.appsettings;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.messaging.md3.R;
import com.android.messaging.md3.util.CaptchaKeywordsUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.exthmui.settingslib.collapsingtoolbar.ExthmCollapsingToolbarBaseActivity;

import java.util.List;

public class CaptchaKeywordsSettingsActivity extends ExthmCollapsingToolbarBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Window window = getWindow();
        if (isDarkMode()){
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else{
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view, new CaptchaKeywordsSettingsFragment())
                .commit();
    }

    private boolean isDarkMode(){
        UiModeManager modeManager = (UiModeManager)getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
        return modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public static class CaptchaKeywordsSettingsFragment extends Fragment {

        private Context mContext;
        private CaptchaKeywordsUtils mCaptchaKeywordsUtils;

        public CaptchaKeywordsSettingsFragment() {
            // Required empty constructor
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            this.mContext = context;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View mView = inflater.inflate(R.layout.captcha_keywords_settings_container, null, false);
            this.mCaptchaKeywordsUtils = new CaptchaKeywordsUtils(this.mContext);

            RecyclerView recyclerView = mView.findViewById(R.id.keywords_container);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.mContext, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new KeywordsRVAdapter(this.mCaptchaKeywordsUtils.getKeywordsList()));

            ExtendedFloatingActionButton mAddKeywordBtn = mView.findViewById(R.id.add_keyword_btn);
            mAddKeywordBtn.setOnClickListener(v -> {
                final EditText editText = new EditText(CaptchaKeywordsSettingsFragment.this.mContext);
                new MaterialAlertDialogBuilder(getActivity())
                        .setTitle(R.string.add_captcha_keyword)
                        .setMessage(R.string.captcha_dialog_tip)
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                    CaptchaKeywordsSettingsFragment.this.mCaptchaKeywordsUtils.addKeywords(editText);
                                    recyclerView.setAdapter(new KeywordsRVAdapter(this.mCaptchaKeywordsUtils.getKeywordsList()));
                        })
                        .show();
            });
            return mView;
        }

        public TextView createKeywordsView() {
            TextView textView = new TextView(this.mContext);
            TypedValue typedValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            textView.setBackgroundResource(typedValue.resourceId);
            textView.setClickable(true);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setPadding(75, 36, 75, 36);
            textView.setTextColor(Color.BLACK);
            textView.setWidth(1080);
            textView.setMinHeight(156);
            return textView;
        }

        private class KeywordsRVAdapter extends RecyclerView.Adapter<KeywordsRVAdapter.KeywordsVH> {

            private List<String> mKeywordsList;

            public KeywordsRVAdapter(List<String> keywordsList) {
                this.mKeywordsList = keywordsList;
            }

            @NonNull
            @Override
            public KeywordsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new KeywordsVH(createKeywordsView());
            }

            @Override
            public void onBindViewHolder(@NonNull KeywordsVH holder, int position) {
                String keyword = this.mKeywordsList.get(position);
                holder.textView.setText(keyword);
                holder.textView.setOnLongClickListener(view -> {
                    new MaterialAlertDialogBuilder(getActivity())
                            .setTitle(R.string.dialog_delete_title)
                            .setMessage(R.string.dialog_delete_message)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                mCaptchaKeywordsUtils.removeStringFromList(keyword);
                                notifyItemRemoved(position);
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    return true;
                });
            }

            @Override
            public int getItemCount() {
                return mKeywordsList.size();
            }

            private class KeywordsVH extends RecyclerView.ViewHolder {

                private final TextView textView;

                public KeywordsVH(@NonNull View itemView) {
                    super(itemView);
                    this.textView = (TextView) itemView;
                    if (isDarkMode()){
                        textView.setTextColor(Color.WHITE);
                    }else{
                        textView.setTextColor(Color.BLACK);
                    }
                }
            }

            private boolean isDarkMode(){
                UiModeManager modeManager = (UiModeManager)getContext().getSystemService(Context.UI_MODE_SERVICE);
                return modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
            }

        }

    }

}