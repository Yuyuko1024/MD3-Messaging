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
package com.android.messaging.md3.datamodel.media;

import android.content.Context;

import com.android.messaging.md3.util.ImageUtils;
import com.android.messaging.md3.util.UriUtil;

public class VideoThumbnailRequestDescriptor extends UriImageRequestDescriptor {
    public VideoThumbnailRequestDescriptor(final long id, int desiredWidth, int desiredHeight,
            int sourceWidth, int sourceHeight) {
        super(UriUtil.getContentUriForMediaStoreId(id), desiredWidth, desiredHeight, sourceWidth,
                sourceHeight, false /* canCompress */, false /* isStatic */,
                false /* cropToCircle */,
                ImageUtils.DEFAULT_CIRCLE_BACKGROUND_COLOR /* circleBackgroundColor */,
                ImageUtils.DEFAULT_CIRCLE_STROKE_COLOR /* circleStrokeColor */);
    }

    @Override
    public MediaRequest<ImageResource> buildSyncMediaRequest(Context context) {
        return new VideoThumbnailRequest(context, this);
    }
}
