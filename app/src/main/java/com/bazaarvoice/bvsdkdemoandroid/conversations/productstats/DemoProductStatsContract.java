/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvsdkdemoandroid.conversations.productstats;

import com.bazaarvoice.bvandroidsdk.Product;

import java.util.List;

public interface DemoProductStatsContract {

    interface View {
        void showProductStats(List<Product> bazaarStatistics);
        void showLoadingProductStats(boolean show);
        void showNoProductStats();
        void transitionToProductStats();
        void showDialogWithMessage(String message);
    }

    interface UserActionsListener {
        void loadProductStats();
    }

}
