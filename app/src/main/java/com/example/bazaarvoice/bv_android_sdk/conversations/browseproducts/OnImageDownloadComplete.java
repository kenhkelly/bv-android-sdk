/**
 * OnImageDownloadComplete.java <br>
 * ReviewSubmissionExample<br>
 *
 * This is an interface used for passing callback functions for image downloads.
 *
 * <p>
 * Copyright 2016 Bazaarvoice Inc. All rights reserved.
 *
 * @author Bazaarvoice Engineering
 */

package com.example.bazaarvoice.bv_android_sdk.conversations.browseproducts;

import android.graphics.Bitmap;

/**
 * TODO: Description Here
 */
public interface OnImageDownloadComplete {
	
	/**
	 * Called when an image download has completed
	 * 
	 * @param image the downloaded image
	 */
	public void onFinish(Bitmap image);

}