package com.android.messaging.md3.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.android.messaging.md3.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaptchaKeywordsUtils {

    private Context mContext;

    private List<String> mKeywords = new ArrayList<>();

    private BuglePrefs mPrefs;
    private String mDefaultKeywordString;
    private String mKeywordsPrefsKey;

    public CaptchaKeywordsUtils(Context context) {
        this.mContext = context;

        mPrefs = BuglePrefs.getApplicationPrefs();

        mDefaultKeywordString = this.mContext.getString(R.string.captcha_keywords_default);
        mKeywordsPrefsKey = this.mContext.getString(R.string.captcha_keywords_key);

        parseKeywordsList();
    }

    private void parseKeywordsList() {
        mKeywords.clear();
        String keyWordsData = mPrefs.getString(this.mKeywordsPrefsKey, mDefaultKeywordString);
        if (TextUtils.isEmpty(keyWordsData)) keyWordsData = mDefaultKeywordString;

        String[] keyWordsListArray = keyWordsData.split("\n");
        mKeywords.addAll(Arrays.asList(keyWordsListArray));
    }

    public List<String> getKeywordsList() {
        return this.mKeywords;
    }

    private void saveKeywordsList() {
        String keywordListData = TextUtils.join("\n", mKeywords);
        mPrefs.putString(this.mKeywordsPrefsKey, keywordListData);
    }

    public int addKeywordToList(String str) {
        if (!mKeywords.contains(str)) {
            mKeywords.add(str);
            return 0;
        } else {
            return -1;
        }
    }

    public void removeStringFromList(String str) {
        mKeywords.remove(str);
        saveKeywordsList();
    }

    public void addKeywords(EditText editText) {
        if (!TextUtils.isEmpty(editText.getText())) {
            String[] keywordArr = editText.getText().toString().split("\n");
            for (String keyword : keywordArr) {
                int addKeywordsResult = addKeywordToList(keyword);
                if (addKeywordsResult == -1) {
                    new MaterialAlertDialogBuilder(this.mContext)
                            .setTitle(R.string.error)
                            .setMessage(R.string.captcha_keyword_duplicate)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    return;
                }
            }
            saveKeywordsList();
            Toast.makeText(this.mContext, R.string.captcha_keyword_add_successsful_tip, Toast.LENGTH_SHORT).show();

        } else {
            new MaterialAlertDialogBuilder(this.mContext)
                    .setTitle(R.string.error)
                    .setMessage(R.string.captcha_keyword_add_empty)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

}