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

package com.android.messaging.md3.util;

import android.content.Context;
import android.text.TextUtils;

import com.android.messaging.md3.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptchaUtil {

    /* 匹配度 */
    private static final int RANK_DIGITAL_6 = 4;        // 6位纯数字
    private static final int RANK_DIGITAL_4 = 3;        // 4位纯数字
    private static final int RANK_DIGITAL_OTHERS = 2;   // 纯数字
    private static final int RANK_DIGITAL_LETTERS = 1;  // 数字 + 字母
    private static final int RANK_NONE = -1;            // 不是验证码

    /* 提取验证码的正则 */
    private static final Pattern CODE_REGEX = Pattern.compile("(?<![a-zA-Z0-9])[a-zA-Z0-9]{4,8}(?![a-zA-Z0-9])");

    public static String getCaptcha(Context context, String content) {
        if (TextUtils.isEmpty(content)) return null;

        content = content.replaceAll("[a-zA-z]+://[^\\s]*","");  // remove URL

        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

        String enableKey = context.getResources().getString(R.string.captcha_detector_key);
        boolean defValue = context.getResources().getBoolean(R.bool.captcha_detector_default);
        if (prefs.getBoolean(enableKey, defValue) == false) {
            return null;
        }

        String keywordsKey = context.getResources().getString(R.string.captcha_keywords_key);
        String defKeywords = context.getResources().getString(R.string.captcha_keywords_default);

        String keyWordText = prefs.getString(keywordsKey, defKeywords);
        if (TextUtils.isEmpty(keyWordText)) {
            keyWordText = defKeywords;
        }

        String[] keyWords = keyWordText.split("\n");

        // 判断是否包含关键字
        String keyword = null;
        for (String word : keyWords) {
            if (content.contains(word)) {
                keyword = word;
                break;
            }
        }
        if (keyword == null) {
            return null;
        }

        Matcher captchaMatcher = CODE_REGEX.matcher(content);

        ArrayList<String> matchedCodes = new ArrayList<>();
        while (captchaMatcher.find()) {
            matchedCodes.add(captchaMatcher.group());
        }
        if (matchedCodes.isEmpty()) {
            return null;
        }

        int maxRank = RANK_NONE;
        int minDistance = content.length();
        String curCode = null;

        for (String code : matchedCodes) {
            int curRank = getMatchRank(code);
            int curDistance = getDistanceToKeyword(keyword, code, content);
            if (curRank > maxRank) {
                maxRank = curRank;
                minDistance = curDistance;
                curCode = code;
            } else if (curRank == maxRank && curDistance < minDistance) {
                minDistance = curDistance;
                curCode = code;
            }
        }

        return curCode;
    }

    /**
     * 取得匹配度
     */
    private static int getMatchRank(String matchedStr) {
        if (matchedStr.matches("^[0-9]{6}$"))
            return RANK_DIGITAL_6;
        if (matchedStr.matches("^[0-9]{4}$"))
            return RANK_DIGITAL_4;
        if (matchedStr.matches("^[0-9]*$"))
            return RANK_DIGITAL_OTHERS;
        if (matchedStr.matches("^[a-zA-Z]*$"))
            return RANK_NONE;
        return RANK_DIGITAL_LETTERS;
    }

    /**
     * 计算验证码与关键字的距离
     */
    private static int getDistanceToKeyword(String keyword, String code, String content) {
        int keywordIdx = content.indexOf(keyword);
        int possibleCodeIdx = content.indexOf(code);
        return Math.abs(keywordIdx - possibleCodeIdx);
    }

}