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

import java.lang.ref.WeakReference;

/**
 * @deprecated Used the {@link QuestionsRecyclerView} instead
 *
 * {@link android.widget.GridView} container for many
 * {@link QuestionView}s providing usage Analytic events.
 */
public final class QuestionsGridView extends BVGridView implements BVConversationsClient.DisplayLoader<QuestionAndAnswerRequest, QuestionAndAnswerResponse> {
    private WeakReference<ConversationsCallback<QuestionAndAnswerResponse>> delegateCbWeakRef;
    private String productId;
    private boolean onScreen = false;

    public QuestionsGridView(Context context) {
        super(context);
    }

    public QuestionsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionsGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public QuestionsGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void loadAsync(LoadCallDisplay<QuestionAndAnswerRequest, QuestionAndAnswerResponse> call, ConversationsCallback<QuestionAndAnswerResponse> callback) {
        final QuestionAndAnswerRequest qAndARequest = call.getRequest();
        productId = qAndARequest.getProductId();
        delegateCbWeakRef = new WeakReference<ConversationsCallback<QuestionAndAnswerResponse>>(callback);
        call.loadAsync(receiverCb);
        trySendUsedFeatureInViewEvent();
    }

    private ConversationsCallback<QuestionAndAnswerResponse> receiverCb = new ConversationsCallback<QuestionAndAnswerResponse>() {
        @Override
        public void onSuccess(QuestionAndAnswerResponse response) {
            ConversationsCallback<QuestionAndAnswerResponse> delegateCb = delegateCbWeakRef.get();
            if (delegateCb == null) {
                return;
            }
            delegateCbWeakRef.clear();
            delegateCb.onSuccess(response);
        }

        @Override
        public void onFailure(BazaarException exception) {
            ConversationsCallback<QuestionAndAnswerResponse> delegateCb = delegateCbWeakRef.get();
            if (delegateCb == null) {
                return;
            }
            delegateCbWeakRef.clear();
            delegateCb.onFailure(exception);
        }
    };

    @Override
    public void onFirstTimeOnScreen() {
        this.onScreen = true;
        trySendUsedFeatureInViewEvent();
    }

    @Override
    public void onViewGroupInteractedWith() {
        ConversationsAnalyticsManager convAnalyticsManager = ConversationsAnalyticsManager.getInstance(BVSDK.getInstance());
        convAnalyticsManager.sendUsedFeatureScrolledEvent(productId, BVEventValues.BVProductType.CONVERSATIONS_QANDA);
    }

    private void trySendUsedFeatureInViewEvent() {
        if (onScreen && productId != null) {
            ConversationsAnalyticsManager convAnalyticsManager = ConversationsAnalyticsManager.getInstance(BVSDK.getInstance());
            convAnalyticsManager.sendUsedFeatureInViewEvent(
                productId, "QuestionsGridView", BVEventValues.BVProductType.CONVERSATIONS_QANDA);
        }
    }

    @Override
    public String getProductId() {
        return productId;
    }
}
