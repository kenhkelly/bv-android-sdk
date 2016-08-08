/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvsdkdemoandroid.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bazaarvoice.bvsdkdemoandroid.R;

import java.util.List;

public class DemoConfigUtils {

    public static final String CONFIG_SHARED_PREFS = "config_shared_prefs";

    private static Context applicationContext;
    private static DemoConfigUtils instance;

    private DemoConfigUtils(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static DemoConfigUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DemoConfigUtils(context);
        }

        return instance;
    }

    private void putStringInPrefs(String key, String value) {
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(CONFIG_SHARED_PREFS, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).commit();
    }

    private  String getStringInPrefs(String key, String defaultValue) {
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(CONFIG_SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putClientId(String clientId) {
        String keyClientId = applicationContext.getString(R.string.key_client_id);
        putStringInPrefs(keyClientId, clientId);
    }

    public String getClientId() {
        String keyClientId = applicationContext.getString(R.string.key_client_id);
        String defaultClientId = DemoConfigXmlParser.getDefaultConfig(applicationContext).clientId;
        return getStringInPrefs(keyClientId, defaultClientId);
    }

    public String getShopperAdPasskey() {
        DemoConfig config = DemoConfigXmlParser.getConfigFromClientId(applicationContext, getClientId());
        return config.apiKeyShopperAdvertising;
    }

    public String getConversationsPasskey() {
        DemoConfig config = DemoConfigXmlParser.getConfigFromClientId(applicationContext, getClientId());
        return config.apiKeyConversations;
    }

    public String getCurationsPasskey() {
        DemoConfig config = DemoConfigXmlParser.getConfigFromClientId(applicationContext, getClientId());
        return config.apiKeyCurations;
    }

    public DemoConfig getCurrentConfig() {
        return DemoConfigXmlParser.getConfigFromClientId(applicationContext, getClientId());
    }

    public DemoConfig getConfigFromClientId(String clientId) {
        return DemoConfigXmlParser.getConfigFromClientId(applicationContext, clientId);
    }

    public CharSequence[] getDisplayNames() {
        List<DemoConfig> configList = DemoConfigXmlParser.getConfigList(applicationContext);
        CharSequence[] displayNames = new CharSequence[configList.size()];
        int i = 0;
        for (DemoConfig config : configList) {
            displayNames[i++] = config.displayName;
        }
        return displayNames;
    }

    public CharSequence[] getClientIdNames() {
        List<DemoConfig> configList = DemoConfigXmlParser.getConfigList(applicationContext);
        CharSequence[] clientIdNames = new CharSequence[configList.size()];
        int i = 0;
        for (DemoConfig config : configList) {
            clientIdNames[i++] = config.clientId;
        }
        return clientIdNames;
    }

    public boolean isDemoClient() {
        DemoConfig demoConfig = getCurrentConfig();
        return demoConfig.displayName.equals(DemoConfigXmlParser.demoDisplayName);
    }

    public boolean configFileExists() {
        return DemoConfigXmlParser.configFileExists(applicationContext);
    }
}