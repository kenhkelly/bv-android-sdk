/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvandroidsdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.bazaarvoice.bvandroidsdk.internal.Utils.checkMain;

/**
 * Bazaarvoice Provided {@link FrameLayout} to display {@link RecommendationView} objects
 */
public class RecommendationsContainerView extends BVContainerView implements BVRecommendations.BVRecommendationsLoader {
    private static final String TAG = RecommendationsContainerView.class.getSimpleName();

    private String productId, categoryId;
    private WeakReference<BVRecommendations.BVRecommendationsCallback> delegateCbRef;

    public RecommendationsContainerView(Context context) {
        super(context);
    }

    public RecommendationsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecommendationsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecommendationsContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void loadRecommendations(RecommendationsRequest request, BVRecommendations.BVRecommendationsCallback callback) {
        checkMain();
        updateRecCallInfo(request.getProductId(), request.getCategoryId(), callback);
        BVRecommendations recommendations = new BVRecommendations();
        recommendations.getRecommendedProducts(request, receiverCb);
    }

    private void updateRecCallInfo(final String productId, final String categoryId, final BVRecommendations.BVRecommendationsCallback delegateCb) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.delegateCbRef = new WeakReference<BVRecommendations.BVRecommendationsCallback>(delegateCb);
    }

    private BVRecommendations.BVRecommendationsCallback receiverCb = new BVRecommendations.BVRecommendationsCallback() {
        @Override
        public void onSuccess(List<BVProduct> recommendedProducts) {
            BVRecommendations.BVRecommendationsCallback delegateCb = delegateCbRef.get();
            if (delegateCb == null) {
                return;
            }
            delegateCbRef.clear();
            delegateCb.onSuccess(recommendedProducts);
            if (recommendedProducts != null) {
                RecommendationsAnalyticsManager.sendEmbeddedPageView(ReportingGroup.CUSTOM, productId, categoryId, recommendedProducts.size());
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            BVRecommendations.BVRecommendationsCallback delegateCb = delegateCbRef.get();
            if (delegateCb == null) {
                return;
            }
            delegateCbRef.clear();
            delegateCb.onFailure(throwable);
        }
    };

    @Override
    public void onAddedToViewHierarchy() {
        RecommendationsAnalyticsManager.sendBvViewGroupAddedToHierarchyEvent(ReportingGroup.CUSTOM);
    }
}
