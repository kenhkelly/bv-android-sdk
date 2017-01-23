/*
 * Copyright 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bazaarvoice.bvandroidsdk;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * {@link android.support.v7.widget.RecyclerView} container for
 * many {@link ReviewView}s providing usage Analytic events.
 */
public final class ReviewsRecyclerView extends ConversationsDisplayRecyclerView<ReviewsRequest, ReviewResponse> {

    public ReviewsRecyclerView(Context context) {
        super(context);
    }

    public ReviewsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ReviewsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    String getProductIdFromRequest(ReviewsRequest reviewsRequest) {
        return reviewsRequest.getProductId();
    }

    @Override
    MagpieBvProduct getMagpieBvProduct() {
        return MagpieBvProduct.RATINGS_AND_REVIEWS;
    }

}