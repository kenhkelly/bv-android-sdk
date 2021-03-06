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

package com.bazaarvoice.bvsdkdemoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bazaarvoice.bvandroidsdk.BVProduct;
import com.bazaarvoice.bvandroidsdk.CurationsPostCallback;
import com.bazaarvoice.bvandroidsdk.CurationsPostResponse;
import com.bazaarvoice.bvsdkdemoandroid.ads.BannerAdActivity;
import com.bazaarvoice.bvsdkdemoandroid.ads.DemoAdFragment;
import com.bazaarvoice.bvsdkdemoandroid.ads.InterstitialAdActivity;
import com.bazaarvoice.bvsdkdemoandroid.ads.NativeAdActivity;
import com.bazaarvoice.bvsdkdemoandroid.cart.DemoCartActivity;
import com.bazaarvoice.bvsdkdemoandroid.configs.DemoClientConfigUtils;
import com.bazaarvoice.bvsdkdemoandroid.conversations.DemoConvApiFragment;
import com.bazaarvoice.bvsdkdemoandroid.conversations.DemoConvApiPresenter;
import com.bazaarvoice.bvsdkdemoandroid.conversations.DemoConvApiPresenterModule;
import com.bazaarvoice.bvsdkdemoandroid.conversations.DemoConversationsStoresAPIFragment;
import com.bazaarvoice.bvsdkdemoandroid.conversations.reviews.DemoStoreReviewsActivity;
import com.bazaarvoice.bvsdkdemoandroid.curations.DemoCurationsFragment;
import com.bazaarvoice.bvsdkdemoandroid.curations.DemoCurationsPostActivity;
import com.bazaarvoice.bvsdkdemoandroid.curations.feed.DemoCurationsFeedActivity;
import com.bazaarvoice.bvsdkdemoandroid.location.DemoLocationFragment;
import com.bazaarvoice.bvsdkdemoandroid.pin.DemoPinFragment;
import com.bazaarvoice.bvsdkdemoandroid.recommendations.DemoRecommendationsFragment;
import com.bazaarvoice.bvsdkdemoandroid.recommendations.detail.DemoProductDetailActivity;
import com.bazaarvoice.bvsdkdemoandroid.settings.DemoSettingsActivity;
import com.bazaarvoice.bvsdkdemoandroid.utils.DemoLaunchIntentUtil;

import javax.inject.Inject;

public class DemoMainActivity extends AppCompatActivity implements CurationsPostCallback {
    @IdRes
    private int fragContainerId;

    private DrawerLayout mDrawerLayout;

    private MenuItem prevItem;

    private Toolbar toolbar;

    @Inject DemoClientConfigUtils demoClientConfigUtils;
    @Inject DemoConvApiPresenter demoConvApiPresenter;

    private DemoConvApiFragment demoConvApiFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onToolbarMenuItemClickListener);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        fragContainerId = R.id.main_content;

        if (savedInstanceState == null) {
            transitionTo(DemoHomePageFragment.newInstance());
        }

        // Uncomment me for iovation suppoort
        //start(getApplicationContext());

        demoConvApiFragment = DemoConvApiFragment.newInstance();

        DaggerDemoCodeHomeComponent.builder()
            .demoAppComponent(DemoApp.getAppComponent(this))
            .demoActivityModule(new DemoActivityModule(this))
            .demoConvApiPresenterModule(new DemoConvApiPresenterModule(demoConvApiFragment))
            .build()
            .inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_actions, menu);

        if (!demoClientConfigUtils.otherSourcesExist()) {
            menu.findItem(R.id.settings_action).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private Toolbar.OnMenuItemClickListener onToolbarMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.settings_action:
                    DemoSettingsActivity.transitionTo(DemoMainActivity.this, DemoLaunchIntentUtil.LaunchCode.API);
                    break;
                case R.id.cart_action:
                    DemoCartActivity.transitionTo(DemoMainActivity.this);
                    break;

            }
            return false;
        }
    };

    private void setupDrawerContent(final NavigationView navigationView) {

        MenuItem item = navigationView.getMenu().getItem(0);
        item.setChecked(true);
        prevItem = item;

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                prevItem.setChecked(false);
                prevItem = item;

                switch (item.getItemId()) {
                    case R.id.home_page:
                        toolbar.setTitle(getString(R.string.app_name));
                        transitionTo(DemoHomePageFragment.newInstance());
                        break;
                    case R.id.recommendations:
                        toolbar.setTitle(getString(R.string.demo_recommendations));
                        transitionTo(DemoRecommendationsFragment.newInstance());
                        break;
                    case R.id.advertising:
                        toolbar.setTitle("Advertising");
                        transitionTo(DemoAdFragment.newInstance());
                        break;
                    case R.id.conversations_demo:
                        toolbar.setTitle(getString(R.string.demo_conversations) + ": API Demo");
                        transitionTo(demoConvApiFragment);
                        break;
                    case R.id.conversations_demo_stores:
                        toolbar.setTitle(getString(R.string.demo_conversations_stores) + ": API Demo");
                        transitionTo(DemoConversationsStoresAPIFragment.newInstance());
                        break;
                    case R.id.curations:
                        toolbar.setTitle(getString(R.string.demo_curations));
                        transitionTo(DemoCurationsFragment.newInstance());
                        break;
                    case R.id.location:
                        toolbar.setTitle(getString(R.string.demo_location));
                        transitionTo(DemoLocationFragment.newInstance());
                        break;
                    case R.id.pin_demo:
                        toolbar.setTitle(getString(R.string.demo_pin) + ": Demo");
                        transitionTo(DemoPinFragment.newInstance());
                }

                // Close the navigation drawer when an item is selected.
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void transitionTo(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(fragContainerId, fragment);
        transaction.commit();
    }

    public void transitionToBvProductDetail(BVProduct bvProduct) {
        DemoProductDetailActivity.start(this, bvProduct);
    }

    public void transitionToNativeAd() {
        Intent intent = new Intent(this, NativeAdActivity.class);
        startActivity(intent);
    }

    public void transitionToInterstitialAd(){
        Intent intent = new Intent(this, InterstitialAdActivity.class);
        startActivity(intent);
    }

    public void transitionToBannerAd(){
        Intent intent = new Intent(this, BannerAdActivity.class);
        startActivity(intent);
    }

    public void transitionToCurationsFeed(){
        Intent intent = new Intent(this, DemoCurationsFeedActivity.class);
        startActivity(intent);
    }

    public void transitionToCurationsPost(){
        Intent intent = new Intent(this, DemoCurationsPostActivity.class);
        startActivity(intent);
    }

    public void transitionToStoreReviewsActivity(String productId){
        Intent intent = new Intent(this, DemoStoreReviewsActivity.class);
        intent.putExtra("extra_product_id", productId);
        intent.putExtra("extra_force_api_load", true);
        startActivity(intent);
    }

    @Override
    public void onSuccess(CurationsPostResponse response) {
        Log.d("success", response.getRemoteUrl());
    }

    @Override
    public void onFailure(Throwable throwable) {
        Log.d("asd", throwable.getMessage());
    }
}

