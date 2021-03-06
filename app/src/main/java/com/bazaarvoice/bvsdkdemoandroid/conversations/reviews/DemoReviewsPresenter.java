/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvsdkdemoandroid.conversations.reviews;

import com.bazaarvoice.bvandroidsdk.BVConversationsClient;
import com.bazaarvoice.bvandroidsdk.BVDisplayableProductContent;
import com.bazaarvoice.bvandroidsdk.BazaarException;
import com.bazaarvoice.bvandroidsdk.ConversationsCallback;
import com.bazaarvoice.bvandroidsdk.Review;
import com.bazaarvoice.bvandroidsdk.ReviewIncludeType;
import com.bazaarvoice.bvandroidsdk.ReviewOptions;
import com.bazaarvoice.bvandroidsdk.ReviewResponse;
import com.bazaarvoice.bvandroidsdk.ReviewsRequest;
import com.bazaarvoice.bvandroidsdk.SortOrder;
import com.bazaarvoice.bvsdkdemoandroid.configs.DemoClient;
import com.bazaarvoice.bvsdkdemoandroid.configs.DemoMockDataUtil;
import com.bazaarvoice.bvsdkdemoandroid.conversations.DemoConvResponseHandler;
import com.bazaarvoice.bvsdkdemoandroid.products.DemoDisplayableProductsCache;

import java.util.Collections;
import java.util.List;

public class DemoReviewsPresenter implements DemoReviewsContract.UserActionsListener, ConversationsCallback {

    protected DemoReviewsContract.View view;
    protected DemoClient demoClient;
    protected DemoMockDataUtil demoMockDataUtil;
    protected BVConversationsClient.DisplayLoader reviewsLoader;
    protected String productId;
    protected boolean fetched = false;
    protected final BVConversationsClient client = new BVConversationsClient();
    protected boolean forceAPICall;
    private final DemoConvResponseHandler demoConvResponseHandler;

    public DemoReviewsPresenter(DemoReviewsContract.View view, DemoClient demoClient, DemoMockDataUtil demoMockDataUtil, String productId, boolean forceAPICall, BVConversationsClient.DisplayLoader reviewsLoader, DemoConvResponseHandler demoConvResponseHandler) {
        this.view = view;
        this.demoClient = demoClient;
        this.demoMockDataUtil = demoMockDataUtil;
        this.reviewsLoader = reviewsLoader;
        this.productId = productId;
        this.forceAPICall = forceAPICall;
        this.demoConvResponseHandler = demoConvResponseHandler;

        if (productId != null && !productId.isEmpty()) {
            BVDisplayableProductContent bvProduct = DemoDisplayableProductsCache.getInstance().getDataItem(productId);
            String imageUrl = bvProduct == null ? null : bvProduct.getDisplayImageUrl();
            String productName = bvProduct == null ? "" : bvProduct.getDisplayName();
            float averageOverallRating = 5; //TODO RATING bvProduct == null ? -1 : bvProduct.getAverageRating();
            view.showHeaderView(imageUrl, productName, averageOverallRating);
        }
    }

    @Override
    public void loadReviews(boolean forceRefresh) {
        fetched = false;
        if (!forceAPICall && demoClient.isMockClient()) {
            showReviews(demoMockDataUtil.getConversationsReviews().getResults());
            return;
        }

        List<Review> cachedReviews = DemoReviewsCache.getInstance().getDataItem(productId);
        boolean haveLocalCache = cachedReviews!=null;
        boolean shouldHitNetwork = forceRefresh || !haveLocalCache;

        if (shouldHitNetwork) {
            ReviewsRequest request = new ReviewsRequest.Builder(productId, 20, 0)
                    .addSort(ReviewOptions.Sort.SubmissionTime, SortOrder.DESC)
                    .addIncludeContent(ReviewIncludeType.PRODUCTS)
                    .build();
            reviewsLoader.loadAsync(client.prepareCall(request), this);
        } else {
            showReviews(cachedReviews);
        }
    }

    @Override
    public void onReviewsTapped() {
        List<Review> cachedReviews = DemoReviewsCache.getInstance().getDataItem(productId);
        int numReviews = cachedReviews != null ? cachedReviews.size() : 0;
        if (fetched && numReviews > 0) {
            view.transitionToReviews();
        } else if (fetched) {
            view.showSubmitReviewDialog();
        }
    }

    private void showReviews(List<Review> bazaarReviews) {
        fetched = true;
        view.showLoadingReviews(false);
        DemoReviewsCache.getInstance().putDataItem(productId, bazaarReviews);

        if (bazaarReviews.size() > 0) {
            view.showReviews(bazaarReviews);
        } else {
            view.showNoReviews();
        }
    }

    @Override
    public void onSuccess(Object object) {
        ReviewResponse response = (ReviewResponse) object;
        demoConvResponseHandler.handleDisplaySuccessResponse(response, new DemoConvResponseHandler.DisplayMessage() {
            @Override
            public void onSuccessMessage(String message) {

            }

            @Override
            public void onErrorMessage(String errorMessage) {
                view.showDialogWithMessage(errorMessage);
            }
        });
        showReviews(response.getResults());
    }

    @Override
    public void onFailure(BazaarException exception) {
        view.showDialogWithMessage(exception.getMessage());
        exception.printStackTrace();
        showReviews(Collections.<Review>emptyList());
    }
}
