/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvsdkdemoandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bazaarvoice.bvsdkdemoandroid.cart.DemoCartActivity;
import com.bazaarvoice.bvsdkdemoandroid.curations.detail.DemoCurationsDetailActivity;
import com.bazaarvoice.bvsdkdemoandroid.curations.map.DemoCurationsMapsActivity;
import com.bazaarvoice.bvsdkdemoandroid.detail.DemoFancyProductDetailActivity;
import com.bazaarvoice.bvsdkdemoandroid.settings.DemoSettingsActivity;

public class DemoRouter {
    private final Context currentActivityContext;

    public DemoRouter(Context currentActivityContext) {
        this.currentActivityContext = currentActivityContext;
    }

    private static void transitionToActivityWithExtras(Context fromContext, Class toActivityClass, @Nullable Bundle extras) {
        Intent intent = new Intent(fromContext, toActivityClass);
        if (extras != null) {
            intent.putExtras(extras);
        }
        fromContext.startActivity(intent);
    }

    /**
     * @deprecated TODO: Refactor to be part of instance rather than static
     *
     * @param fromContext
     * @param productId
     */
    public static void transitionToCurationsMapView(Context fromContext, String productId) {
        Bundle bundle = new Bundle();
        bundle.putString(DemoCurationsMapsActivity.EXTRA_PRODUCT_ID, productId);
        transitionToActivityWithExtras(fromContext, DemoCurationsMapsActivity.class, bundle);
    }

    /**
     * @deprecated TODO: Refactor to be part of instance rather than static
     * @param fromContext
     * @param productId
     * @param startFeedItemId
     */
    public static void transitionToCurationsFeedItem(Context fromContext, String productId, String startFeedItemId) {
        Bundle extras = new Bundle();
        extras.putString(DemoCurationsDetailActivity.KEY_PRODUCT_ITEM, productId);
        extras.putString(DemoCurationsDetailActivity.KEY_CURATIONS_ITEM_ID, startFeedItemId);
        transitionToActivityWithExtras(fromContext, DemoCurationsDetailActivity.class, extras);
    }

    public void transitionToProductDetail(String productId) {
        DemoFancyProductDetailActivity.transitionTo(currentActivityContext, productId);
    }

    public void transitionToSettings(int launchCode) {
        DemoSettingsActivity.transitionTo(currentActivityContext, launchCode);
    }

    public void transitionToCart() {
        DemoCartActivity.transitionTo(currentActivityContext);
    }
}