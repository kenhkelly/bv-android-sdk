/**
 *  Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */

package com.bazaarvoice.bvandroidsdk;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.bazaarvoice.bvandroidsdk.internal.Utils;
import com.bazaarvoice.bvandroidsdk_common.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * <p> Main class for initializing the Bazaarvoice SDK, and changing global SDK state. </p>
 * <p> Must initialize the SDK upon Application startup
 * with the {@link Builder#build()} method. </p>
 * <p> API keys for Shopper Advertising and Conversation must be provided to
 * the {@link Builder#apiKeyShopperAdvertising(String)} and
 * {@link Builder#apiKeyConversations(String)} methods respectively for each of the
 * desired components to function</p>
 * <p> Your client name and the application object must also be provided when
 * constructing the {@link Builder}</p>
 */
public class BVSDK {
    // region Properties

    private static final String TAG = BVSDK.class.getSimpleName();
    private static final String SCHEDULED_BV_THREAD_NAME = "BV-ScheduledThread";
    private static final String IMMEDIATE_BV_THREAD_NAME = "BV-ImmediateThread";
    private static final String SHOPPER_MARKETING_API_ROOT_URL_STAGING = "https://my.network-stg.bazaarvoice.com/";
    private static final String SHOPPER_MARKETING_API_ROOT_URL_PRODUCTION = "https://my.network.bazaarvoice.com/";
    private static final String ANALYTICS_ROOT_URL_PRODUCTION = "https://network.bazaarvoice.com/";
    private static final String ANALYTICS_ROOT_URL_STAGING = "https://network-stg.bazaarvoice.com/";
    private static final String BAZAARVOICE_ROOT_URL_STAGING = "https://stg.api.bazaarvoice.com/";
    private static final String BAZAARVOICE_ROOT_URL_PRODUCTION = "https://api.bazaarvoice.com/";
    private static final String NOTIFICATION_CONFIG_URL = "https://s3.amazonaws.com/";
    private static final String BVSDK_USER_AGENT = "bvsdk-android/v"+ BuildConfig.BVSDK_VERSION_NAME;
    private static final String BACKGROUND_THREAD_NAME = "BackgroundThread";
    static final int BVHandlePayload = 123;
    static volatile BVSDK singleton;

    static final String SDK_VERSION = BuildConfig.BVSDK_VERSION_NAME;

    final BVUserProvidedData bvUserProvidedData;
    final BVWorkerData bvWorkerData;
    final BVActivityLifecycleCallbacks bvActivityLifecycleCallbacks;
    final BVAuthenticatedUser bvAuthenticatedUser;
    final Handler handler;
    final HandlerThread backgroundThread;
    final BVPixel bvPixel;
    final BVLogger bvLogger;

    // endregion

    // region Constructor

    BVSDK(BVUserProvidedData bvUserProvidedData, BVLogger bvLogger, BVActivityLifecycleCallbacks bvActivityLifecycleCallbacks, final BVAuthenticatedUser bvAuthenticatedUser, Handler handler, HandlerThread backgroundThread, BVPixel bvPixel, BVWorkerData bvWorkerData) {
        this.bvUserProvidedData = bvUserProvidedData;
        this.bvLogger = bvLogger;
        this.bvActivityLifecycleCallbacks = bvActivityLifecycleCallbacks;
        this.bvAuthenticatedUser = bvAuthenticatedUser;
        this.handler = handler;
        this.backgroundThread = backgroundThread;
        this.bvPixel = bvPixel;
        this.bvWorkerData = bvWorkerData;

        startAppLifecycleMonitoring();
    }

    // endregion

    // region Public API

    public static BVSDK getInstance() {
        confirmBVSDKCreated();
        return singleton;
    }

    /**
     * Tell BVSDK who the user is. This is the essential step in providing targeted, personalized
     * information about the user.
     * @param userAuthString The Bazaarvoice-specific User Auth String. See the online documentation
     *                       for information on where this auth string comes from, and why it is
     *                       necessary.
     */
    @MainThread
    public void setUserAuthString(@NonNull String userAuthString) {
        if (userAuthString == null || userAuthString.isEmpty()) {
            bvLogger.w(TAG, "userAuthString must not be empty");
            return;
        }

        bvAuthenticatedUser.setUserAuthString(userAuthString);
        BVPersonalizationEvent event = new BVPersonalizationEvent(userAuthString);
        bvPixel.track(event);

        bvAuthenticatedUser.updateUser("user auth string update");
    }

    /**
     * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)}
     *
     * @param application Application Object
     * @param clientId Your Bazaarvoice provided client id (e.g. "mycompany")
     * @return Builder to configure other options
     * @see #builder(Application, BazaarEnvironment)
     */
    public static Builder builder(Application application, String clientId) {
        return new Builder(application, clientId);
    }

    /**
     * Uses the configuration file generated at
     * <a href="https://bazaarvoice.github.io/bv-android-sdk/installation.html#configure-bvsdk">
     *   the installation and configuration page</a>
     * to set all of the options for the sdk.
     *
     * @param application The application object
     * @param bazaarEnvironment The environment that will be used for Bazaarvoice requests
     * @return Builder to configure other options
     */
    public static Builder builder(Application application, BazaarEnvironment bazaarEnvironment) {
        BVConfig bvConfig = BVConfig.BVConfigUtil.getBvConfig(application.getApplicationContext(), bazaarEnvironment);
        return builderWithConfig(application, bazaarEnvironment, bvConfig);
    }

    public static Builder builderWithConfig(Application application, BazaarEnvironment bazaarEnvironment, BVConfig bvConfig) {
        return new Builder(application, bazaarEnvironment, bvConfig);
    }

    // endregion

    // region Internal API

    private void startAppLifecycleMonitoring() {
        /*
         * Register with ActivityLifeCycleCallbacks for App lifecycle analytics, and
         * for triggering user profile updates
         */
        bvUserProvidedData.getApplication().registerActivityLifecycleCallbacks(bvActivityLifecycleCallbacks);

        /*
         * Send Magpie lifecycle analytics signaling app launch
         */
        BVMobileAppLifecycleEvent event = new BVMobileAppLifecycleEvent(BVEventValues.AppState.LAUNCHED);
        bvPixel.track(event);
    }

    private static void confirmBVSDKCreated() {
        if (singleton == null) {
            synchronized (BVSDK.class) {
                if (singleton == null) {
                    throw new IllegalStateException("Must initialize BVSDK first.");
                }
            }
        }
    }

    private static void confirmBVSDKNotCreated() {
        if (singleton != null) {
            synchronized (BVSDK.class) {
                if (singleton != null) {
                    throw new IllegalStateException("BVSDK singleton already exists");
                }
            }
        }
    }

    /**
     * Used for testing to emulate app restart when this singleton would be destroyed
     */
    static void destroy() {
        synchronized (BVSDK.class) {
            singleton = null;
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private String threadName;

        public NamedThreadFactory(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(threadName);
            return thread;
        }
    }

    /**
     * {@link HandlerThread} to be shared in the SDK to facilitate background
     * work. The {@link Looper} is accessible to libraries inheriting from common.
     */
    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super(Utils.THREAD_PREFIX + BACKGROUND_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        }
    }

    void postPayloadToMainThread(BVHandlerCallbackPayload payload) {
        Message message = handler.obtainMessage();
        message.what = BVSDK.BVHandlePayload;
        message.obj = payload;
        handler.sendMessage(message);
    }

    // endregion

    // region Builder

    /**
     * Initialization Builder that must be called in the {@link android.app.Application#onCreate()} method
     * before any Bazaarvoice SDKs can be used.
     */
    public static class Builder {
        private final Application application;
        private BazaarEnvironment bazaarEnvironment;
        private BVLogLevel logLevel;
        private OkHttpClient okHttpClient;
        private BVConfig.Builder bvConfigBuilder;

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} or
         * {@link BVSDK#builder(Application, BazaarEnvironment)}
         *
         * @param application Required Application Object
         * @param clientId Client id used to get custom results
         * @see BVSDK#builder(Application, BazaarEnvironment)
         */
        public Builder(Application application, String clientId) {
            if (application == null) {
                throw new IllegalArgumentException("Application must not be null");
            }
            if (clientId == null || clientId.isEmpty()) {
                throw new IllegalArgumentException("clientId must be valid");
            }
            this.application = application;
            bvConfigBuilder = new BVConfig.Builder();
            bvConfigBuilder.clientId(clientId);
        }

        /**
         * Uses the configuration file generated at
         * <a href="https://bazaarvoice.github.io/bv-android-sdk/installation.html#configure-bvsdk">
         *   the installation and configuration page</a>
         * to set all of the options for the sdk.
         *
         * @param application The application object
         * @param bazaarEnvironment The environment that will be used for Bazaarvoice requests
         * @return the builder object
         */
        Builder(Application application, BazaarEnvironment bazaarEnvironment, BVConfig bvConfig) {
            if (application == null) {
                throw new IllegalArgumentException("Application must not be null");
            }
            if (bazaarEnvironment == null) {
                throw new IllegalArgumentException("bazaarEnvironment must be valid");
            }
            this.application = application;
            this.bazaarEnvironment = bazaarEnvironment;
            this.bvConfigBuilder = bvConfig.newBuilder();
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)}
         *
         * @param bazaarEnvironment Bazaarvoice environment to get data from
         * @return the builder object
         */
        public Builder bazaarEnvironment(BazaarEnvironment bazaarEnvironment) {
            this.bazaarEnvironment = bazaarEnvironment;
            return this;
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} which
         * will set this value for you
         *
         * @param apiKeyShopperAdvertising API Key required to access Recommendations and Ads SDK
         * @return the builder object
         */
        public Builder apiKeyShopperAdvertising(String apiKeyShopperAdvertising) {
            if (apiKeyShopperAdvertising == null || apiKeyShopperAdvertising.isEmpty()) {
                throw new IllegalArgumentException("apiKeyShopperAdvertising must be valid");
            }
            bvConfigBuilder
                .apiKeyShopperAdvertising(apiKeyShopperAdvertising)
                .build();
            return this;
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} which
         * will set this value for you
         *
         * @param apiKeyConversations API Key required to access Conversations SDK
         * @return the builder object
         */
        public Builder apiKeyConversations(String apiKeyConversations) {
            if (apiKeyConversations == null || apiKeyConversations.isEmpty()) {
                throw new IllegalArgumentException("apiKeyConversations must be valid");
            }
            bvConfigBuilder
                .apiKeyConversations(apiKeyConversations)
                .build();
            return this;
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} which
         * will set this value for you
         *
         * @param apiKeyConversationsStores API Key required to access Conversations SDK
         * @return the builder object
         */
        public Builder apiKeyConversationsStores(String apiKeyConversationsStores) {
            if (apiKeyConversationsStores == null || apiKeyConversationsStores.isEmpty()) {
                throw new IllegalArgumentException("apiKeyConversationsStores must be valid");
            }
            bvConfigBuilder
                .apiKeyConversationsStores(apiKeyConversationsStores)
                .build();
            return this;
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} which
         * will set this value for you
         *
         * @param apiKeyCurations API Key required to access Curations SDK
         * @return the builder object
         */
        public Builder apiKeyCurations(String apiKeyCurations) {
            if (apiKeyCurations == null || apiKeyCurations.isEmpty()) {
                throw new IllegalArgumentException("apiKeyCurations must be valid");
            }
            bvConfigBuilder
                .apiKeyCurations(apiKeyCurations)
                .build();
            return this;
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} which
         * will set this value for you
         *
         * @param apiKeyLocation API Key required to access Location SDK
         * @return the builder object
         */
        public Builder apiKeyLocation(String apiKeyLocation) {
            if (apiKeyLocation == null || apiKeyLocation.isEmpty()) {
                throw new IllegalArgumentException("apiKeyCurations must be valid");
            }
            bvConfigBuilder
                .apiKeyLocation(apiKeyLocation)
                .build();
            return this;
        }

        /**
         * @deprecated Now use {@link BVSDK#builder(Application, BazaarEnvironment)} which
         * will set this value for you
         *
         * @param apiKeyPin API Key required to access Post Interaction Notifications API
         * @return the builder object
         */
        public Builder apiKeyPin(String apiKeyPin) {
            if (apiKeyPin == null || apiKeyPin.isEmpty()) {
                throw new IllegalArgumentException("apiKeyPin must be valid");
            }
            bvConfigBuilder
                .apiKeyPIN(apiKeyPin)
                .build();
            return this;
        }

        /**
         * @param logLevel The level at which the Bazaarvoice SDK will decide to log
         * @return the builder object
         */
        public Builder logLevel(BVLogLevel logLevel) {
            if (logLevel == null) {
                throw new IllegalArgumentException("logLevel must not be null");
            }
            this.logLevel = logLevel;
            return this;
        }

        /**
         * @param okHttpClient A custom client instance. This instance will be duplicated and an http cache will
         *                     be added.
         * @return the builder object
         */
        public Builder okHttpClient(OkHttpClient okHttpClient) {
            if (okHttpClient == null) {
                throw new IllegalArgumentException("OkHttpClient must not be null");
            }
            if (this.okHttpClient != null) {
                throw new IllegalStateException("OkHttpClient already set");
            }
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * Disables analytics, but still logs the events sent. Only used to avoid
         * hitting production with analytics while testing.
         *
         * @param dryRunAnalytics If true then analytics events will not be sent
         * @return the builder object
         */
        public Builder dryRunAnalytics(boolean dryRunAnalytics) {
            bvConfigBuilder
                .dryRunAnalytics(dryRunAnalytics)
                .build();
            return this;
        }

        public BVSDK build() {
            confirmBVSDKNotCreated();

            if (application == null) {
                throw new IllegalStateException("Must provide an application object");
            }

            BVConfig finalConfig = bvConfigBuilder.build();

            if (finalConfig.getClientId() == null) {
                throw new IllegalStateException("Must provide a client id");
            }

            if (bazaarEnvironment == null) {
                bazaarEnvironment = BazaarEnvironment.STAGING;
            }

            if (logLevel == null) {
                logLevel = BVLogLevel.ERROR;
            }

            if (okHttpClient == null) {
                this.okHttpClient = new OkHttpClient();
            }

            // Use their OkHttp instance or ours, and add the options we want
            File httpCacheFile = new File(application.getCacheDir(), "bvsdk_http_cache");
            long maxCacheSize = 1024 * 1024 * 10; // 10MiB
            Cache httpCache = new Cache(httpCacheFile, maxCacheSize);
            this.okHttpClient = okHttpClient
                    .newBuilder()
                    .cache(httpCache)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build();

            BVLogger bvLogger = new BVLogger(logLevel);
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(SCHEDULED_BV_THREAD_NAME));
            ExecutorService immediateExecutorService = Executors.newFixedThreadPool(1, new NamedThreadFactory(IMMEDIATE_BV_THREAD_NAME));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            UUID uuid = Utils.getUuid(application.getApplicationContext());
            String shopperMarketingApiRootUrl = bazaarEnvironment == BazaarEnvironment.STAGING ? SHOPPER_MARKETING_API_ROOT_URL_STAGING : SHOPPER_MARKETING_API_ROOT_URL_PRODUCTION;
            String analyticsRootUrl = bazaarEnvironment == BazaarEnvironment.STAGING ? ANALYTICS_ROOT_URL_STAGING : ANALYTICS_ROOT_URL_PRODUCTION;
            List<Integer> profilePollTimes = Arrays.asList(0, 5000, 12000, 24000);
            String bazaarvoiceApiRootUrl = bazaarEnvironment == BazaarEnvironment.STAGING ? BAZAARVOICE_ROOT_URL_STAGING : BAZAARVOICE_ROOT_URL_PRODUCTION;
            BVRootApiUrls endPoints = new BVRootApiUrls(shopperMarketingApiRootUrl, bazaarvoiceApiRootUrl, NOTIFICATION_CONFIG_URL);
            BVApiKeys apiKeys = new BVApiKeys(
                finalConfig.getApiKeyShopperAdvertising(),
                finalConfig.getApiKeyConversations(),
                finalConfig.getApiKeyConversationsStores(),
                finalConfig.getApiKeyCurations(),
                finalConfig.getApiKeyLocation(),
                finalConfig.getApiKeyPIN());
            BVMobileInfo bvMobileInfo = new BVMobileInfo(application.getApplicationContext());
            BVUserProvidedData bvUserProvidedData = new BVUserProvidedData(application, finalConfig.getClientId(), apiKeys, bvMobileInfo);
            BackgroundThread backgroundThread = new BackgroundThread();
            backgroundThread.start();
            BVAuthenticatedUser bvAuthenticatedUser = new BVAuthenticatedUser(
                application.getApplicationContext(),
                shopperMarketingApiRootUrl,
                finalConfig.getApiKeyShopperAdvertising(),
                okHttpClient,
                bvLogger,
                gson,
                profilePollTimes,
                backgroundThread);
            AnalyticsManager analyticsManager = new AnalyticsManager(
                application.getApplicationContext(),
                finalConfig.getClientId(),
                analyticsRootUrl,
                okHttpClient,
                immediateExecutorService,
                scheduledExecutorService,
                bvAuthenticatedUser,
                uuid,
                finalConfig.isDryRunAnalytics());
            BVPixel bvPixel = new BVPixel.Builder(application, finalConfig.getClientId(), bazaarEnvironment == BazaarEnvironment.STAGING)
                .bgHandlerThread(backgroundThread)
                .okHttpClient(okHttpClient)
                .dryRunAnalytics(finalConfig.isDryRunAnalytics())
                .build();
            BVActivityLifecycleCallbacks bvActivityLifecycleCallbacks = new BVActivityLifecycleCallbacks(bvPixel, bvLogger);

            BVWorkerData bvWorkerData = new BVWorkerData(
                analyticsManager,
                gson,
                endPoints,
                okHttpClient,
                BVSDK_USER_AGENT,
                backgroundThread.getLooper());

            Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == BVHandlePayload){
                        BVHandlerCallbackPayload payload = (BVHandlerCallbackPayload) msg.obj;
                        payload.getInternalCB().performOnMainThread(payload);
                        return true;
                    }
                    return false;
                }
            });

            singleton = new BVSDK(
                bvUserProvidedData,
                bvLogger,
                bvActivityLifecycleCallbacks,
                bvAuthenticatedUser,
                handler,
                backgroundThread,
                bvPixel,
                bvWorkerData);
            return singleton;
        }
    }

    // endregion

    // region BVSDK Helper Objects

    BVUserProvidedData getBvUserProvidedData() {
        return bvUserProvidedData;
    }

    BVAuthenticatedUser getAuthenticatedUser() {
        return bvAuthenticatedUser;
    }

    BVWorkerData getBvWorkerData() {
        return bvWorkerData;
    }

    BVLogger getBvLogger() {
        return bvLogger;
    }

    BVPixel getBvPixel() {
        return bvPixel;
    }

    static final class BVWorkerData {
        private final AnalyticsManager analyticsManager;
        private final Gson gson;
        private final BVRootApiUrls bvRootApiUrls;
        private final OkHttpClient okHttpClient;
        private final String bvSdkUserAgent;
        private final Looper backgroundLooper;

        public BVWorkerData(AnalyticsManager analyticsManager, Gson gson, BVRootApiUrls bvRootApiUrls, OkHttpClient okHttpClient, String bvSdkUserAgent, Looper backgroundLooper) {
            this.analyticsManager = analyticsManager;
            this.gson = gson;
            this.bvRootApiUrls = bvRootApiUrls;
            this.okHttpClient = okHttpClient;
            this.bvSdkUserAgent = bvSdkUserAgent;
            this.backgroundLooper = backgroundLooper;
        }

        public AnalyticsManager getAnalyticsManager() {
            return analyticsManager;
        }

        public Gson getGson() {
            return gson;
        }

        public BVRootApiUrls getRootApiUrls() {
            return bvRootApiUrls;
        }

        public OkHttpClient getOkHttpClient() {
            return okHttpClient;
        }

        public String getBvSdkUserAgent() {
            return bvSdkUserAgent;
        }

        public Looper getBackgroundLooper() {
            return backgroundLooper;
        }
    }

    // endregion
}
