/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */

package com.bazaarvoice.bvsdkdemoandroid;

import android.app.Application;
import android.content.Context;

import com.bazaarvoice.bvandroidsdk.BVSDK;
import com.bazaarvoice.bvsdkdemoandroid.configs.DemoClientConfigModule;
import com.bazaarvoice.bvsdkdemoandroid.conversations.questions.DemoQuestionsCache;
import com.bazaarvoice.bvsdkdemoandroid.conversations.reviews.DemoReviewsCache;
import com.bazaarvoice.bvsdkdemoandroid.products.DemoDisplayableProductsCache;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

import static com.bazaarvoice.bvsdkdemoandroid.BuildConfig.DEBUG;
import static com.bazaarvoice.bvsdkdemoandroid.BuildConfig.HAS_CRASHLYTICS_KEY;

public class DemoApp extends Application {
    private DemoAppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        if (HAS_CRASHLYTICS_KEY) {
            Fabric.with(this, crashlyticsKit);
        }

        Stetho.initializeWithDefaults(this);
        if (DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }

        appComponent = DaggerDemoAppComponent.builder()
                .demoAppModule(new DemoAppModule())
                .demoAndroidModule(new DemoAndroidModule(this))
                .demoClientConfigModule(new DemoClientConfigModule())
                .demoBvModule(new DemoBvModule())
                .build();
        BVSDK bvsdk = appComponent.getBvSdk();
        // Set user auth string which you may not have until after a user signs in
        bvsdk.setUserAuthString(DemoConstants.BV_USER_AUTH_STRING);
    }

    public static void cleanUp() {
        DemoDisplayableProductsCache.getInstance().clear();
        DemoReviewsCache.getInstance().clear();
        DemoQuestionsCache.getInstance().clear();
    }

    public static DemoAppComponent getAppComponent(Context context) {
        DemoApp demoApp = (DemoApp) context.getApplicationContext();
        return demoApp.appComponent;
    }
}
