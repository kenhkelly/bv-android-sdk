/**
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 */
package com.bazaarvoice.bvsdkdemoandroid.conversations.reviews;

import android.support.v7.widget.RecyclerView;

import com.bazaarvoice.bvandroidsdk.Photo;
import com.bazaarvoice.bvandroidsdk.Store;
import com.bazaarvoice.bvandroidsdk.StoreReview;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

public class DemoStoreReviewsAdapter extends DemoReviewsAdapter<StoreReview> {

    public DemoStoreReviewsAdapter(Picasso picasso, PrettyTime prettyTime) {
        super(picasso, prettyTime);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StoreReview bazaarReview = reviews.get(position);
        ReviewRowViewHolder viewHolder = (ReviewRowViewHolder) holder;

        String title = bazaarReview.getTitle();
        String reviewText = bazaarReview.getReviewText();
        Integer rating = bazaarReview.getRating();
        Store store = bazaarReview.getIncludedIn().getStores().get(0);
        String location = store.getLocationAttribute(Store.StoreAttributeType.CITY);
        Date submissionDate= bazaarReview.getSubmissionDate();
        String nickName = bazaarReview.getUserNickname();
        List<Photo> photos = bazaarReview.getPhotos();
        String authorId = bazaarReview.getAuthorId();

        populateViewHolder(bazaarReview, viewHolder, title, reviewText, rating, location, submissionDate, nickName, photos, -1, -1, authorId);
    }
}