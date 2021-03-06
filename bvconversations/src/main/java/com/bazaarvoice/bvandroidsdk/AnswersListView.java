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
import android.util.AttributeSet;

/**
 * @deprecated Used the {@link AnswersRecyclerView} instead
 *
 * {@link android.widget.ListView} container for many
 * {@link AnswerView}s providing usage Analytic events.
 */
public final class AnswersListView extends BVListView {
    private String productId = "";

    public AnswersListView(Context context) {
        super(context);
    }

    public AnswersListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnswersListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnswersListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onFirstTimeOnScreen() {
        ConversationsAnalyticsManager convAnalyticsManager = ConversationsAnalyticsManager.getInstance(BVSDK.getInstance());
        convAnalyticsManager.sendUsedFeatureInViewEvent(
            productId, "AnswersListView", BVEventValues.BVProductType.CONVERSATIONS_QANDA);
    }

    @Override
    public void onViewGroupInteractedWith() {
        ConversationsAnalyticsManager convAnalyticsManager = ConversationsAnalyticsManager.getInstance(BVSDK.getInstance());
        convAnalyticsManager.sendUsedFeatureScrolledEvent(productId, BVEventValues.BVProductType.CONVERSATIONS_QANDA);
    }

    @Override
    public String getProductId() {
        return productId;
    }
}
