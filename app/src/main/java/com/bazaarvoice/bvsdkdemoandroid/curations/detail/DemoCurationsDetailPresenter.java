/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvsdkdemoandroid.curations.detail;

import com.bazaarvoice.bvandroidsdk.BVCurations;
import com.bazaarvoice.bvandroidsdk.CurationsFeedCallback;
import com.bazaarvoice.bvandroidsdk.CurationsFeedItem;
import com.bazaarvoice.bvandroidsdk.CurationsFeedRequest;
import com.bazaarvoice.bvandroidsdk.CurationsMedia;
import com.bazaarvoice.bvsdkdemoandroid.DemoConstants;
import com.bazaarvoice.bvsdkdemoandroid.configs.DemoClient;
import com.bazaarvoice.bvsdkdemoandroid.configs.DemoMockDataUtil;
import com.bazaarvoice.bvsdkdemoandroid.utils.DemoUtils;

import java.util.Collections;
import java.util.List;

public class DemoCurationsDetailPresenter implements DemoCurationsDetailContract.UserActionsListener, CurationsFeedCallback {

    private DemoCurationsDetailContract.View view;
    private DemoClient demoClient;
    private DemoMockDataUtil demoMockDataUtil;
    private String productId;
    private List<CurationsFeedItem> curationsFeedItems = Collections.emptyList();
    private String currentFeedItemId;

    public DemoCurationsDetailPresenter(DemoCurationsDetailContract.View view, DemoClient demoClient, DemoMockDataUtil demoMockDataUtil, String productId, String startFeedItemId) {
        this.view = view;
        this.demoClient = demoClient;
        this.demoMockDataUtil = demoMockDataUtil;
        this.productId = productId;
        this.currentFeedItemId = startFeedItemId;
    }

    @Override
    public void loadCurationsFeed() {
        List<CurationsFeedItem> demoFeedItems = demoMockDataUtil.getCurationsFeedReponse().getUpdates();
        if (demoClient.isMockClient()) {
            showCurationsFeedItems(demoFeedItems);
            return;
        }

        if (!curationsFeedItems.isEmpty()) {
            showCurationsFeedItems(curationsFeedItems);
        }

        view.showLoadingCurations(true);
        BVCurations bvCurations = new BVCurations();
        CurationsFeedRequest request = new CurationsFeedRequest.Builder(DemoConstants.CURATIONS_GROUPS)
                .limit(20)
                .media(new CurationsMedia("photo", DemoUtils.MAX_IMAGE_WIDTH/2, DemoUtils.MAX_IMAGE_HEIGHT/2))
                .externalId(productId)
                .withProductData(true)
                .build();
        bvCurations.getCurationsFeedItems(request, this);
    }

    private void showCurationsFeedItems(List<CurationsFeedItem> feedItems) {
        curationsFeedItems = feedItems;

        if (feedItems.size() > 0) {
            view.showCurationsFeed(feedItems);
            view.showCurationsFeedItemInPager(getCurrentFeedItemIndex());
        } else {
            view.showNoCurations();
        }
    }

    private int getCurrentFeedItemIndex() {
        for (int i=0; i<curationsFeedItems.size(); i++) {
            CurationsFeedItem curationsFeedItem = curationsFeedItems.get(i);
            if (currentFeedItemId.equals(String.valueOf(curationsFeedItem.getId()))) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onSuccess(List<CurationsFeedItem> feedItems) {
        showCurationsFeedItems(feedItems);
    }

    @Override
    public void onFailure(Throwable throwable) {
        throwable.printStackTrace();
        view.showCurationsMessage("Failed to get curations feed");
        showCurationsFeedItems(Collections.<CurationsFeedItem>emptyList());
    }
}
