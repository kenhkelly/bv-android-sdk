package com.bazaarvoice.bvandroidsdk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

import static junit.framework.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

@RunWith(RobolectricTestRunner.class)
public class ConversationsStoresUnitTest extends BVBaseTest{

    @Override
    protected void modifyPropertiesToInitSDK() {
        bazaarvoiceApiBaseUrl = "https://examplesite/";
    }

    @Test
    public void testBulkStoreRatingsRequestValidLimit() {
        List<String> prodIds = getStoreIds(20);

        BulkStoreRequest request = new BulkStoreRequest.Builder(prodIds)
                .build();

        assertTrue("Request does not contain error but one was found", request.getError() == null);
    }

    @Test
    public void testBulkStoreRatingsLimitAndOffset() {
        BulkStoreRequest request = new BulkStoreRequest.Builder(20, 0)
                .build();

        assertTrue("Request does not contain error but one was found", request.getError() == null);
    }


    @Test
    public void testStoreReviewsRequestOverLimitError() {

        StoreReviewsRequest request = new StoreReviewsRequest.Builder("testStoreId", 101 , 0)
                .build();

        assertTrue("Request contains error but was not found", request.getError() != null);
    }

    @Test
    public void testStoreReviewsRequestValidLimit() {
        StoreReviewsRequest request = new StoreReviewsRequest.Builder("testStoreId", 20 , 0)
                .build();

        assertTrue("Request does not contain error but one was found", request.getError() == null);
    }

    @Test
    public void testStoreReviewSubmissionRequestApi() {
        StoreReviewSubmissionRequest request = new StoreReviewSubmissionRequest.Builder(Action.Preview, "productId")
                .addAdditionalField("key", "value")
                .addContextDataValueString("key", "value")
                .addContextDataValueString("key", true)
                .addFreeFormTag("questionId", "value")
                .addPhoto(new File("path/to/file"), "caption")
                .addPredefinedTag("questionId", "tagId", "value")
                .addRatingQuestion("questionName", 100)
                .addRatingSlider("questionName", "value")
                .isRecommended(true)
                .sendEmailAlertWhenCommented(true)
                .sendEmailAlertWhenPublished(true)
                .rating(5)
                .netPromoterScore(5)
                .title("title")
                .reviewText("review text")
                .netPromoterComment("comment")
                .campaignId("campaignId")
                .fingerPrint("fingerprint")
                .hostedAuthenticationEmail("hostedAuthEmail")
                .locale("locale")
                .user("user")
                .userEmail("useremail@email.com")
                .userId("userId")
                .userLocation("userLocation")
                .userNickname("userNickName")
                .agreedToTermsAndConditions(true)
                .build();
    }

    private Object testParsing(String filename, Class responseClass) throws Exception {
        String reviewsForProdResponse = jsonFileAsString(filename);
        return gson.fromJson(reviewsForProdResponse, responseClass);
    }

    @Test
    public void testReviewsForSingleStoreReviewIncludeProdStatsParsing() throws Exception {
        // Parse a json result where a single store is fetched and the Results contain Review objects
        // and the Includes contain the store and statistics for which the reviews are returned.
        StoreReviewResponse response = (StoreReviewResponse) testParsing("store_reviews_include_statistics.json", StoreReviewResponse.class);
        Map<String, Store> stores = response.getIncludes().getItemMap();
            assertTrue("Wrong store count", stores != null && stores.size() == 1);

        Store store = stores.get("1");
        checkTestStoreOneProperties(store);

        List<StoreReview> reviews = response.getResults();
        assertTrue("Wrong review count", reviews != null && reviews.size() == 12);
    }

    @Test
    public void reviewDisplayRequestWithIncludeProductShouldHaveStatsReviews() {
        // "Stats=Reviews must be used in conjunction with Include=Products"
        // - https://developer.bazaarvoice.com/conversations-api/reference/v5.4/reviews/review-display#requesting-all-reviews-for-a-particular-product-with-review-statistics-(inc.-average-rating)
        StoreReviewsRequest request = new StoreReviewsRequest.Builder("product123", 20, 0)
            .addIncludeContent(ReviewIncludeType.PRODUCTS)
            .build();
        HttpUrl httpUrl = request.toHttpUrl();
        assertTrue(httpUrl.queryParameterNames().contains("Stats"));
        assertTrue(httpUrl.queryParameterValues("Stats").get(0).equals("Reviews"));
    }

    @Test
    public void testBulkStoresResponseParsing() throws Exception {
        // Parse a json result where a single store is fetched by explicit it
        BulkStoreResponse response = (BulkStoreResponse) testParsing("store_product_feed_fetch.json", BulkStoreResponse.class);
        List<Store> stores = response.getResults();
        assertTrue("Wrong store count", stores != null && stores.size() == 1);

        Store store = stores.get(0);
        checkTestStoreOneProperties(store);

    }

    private void checkTestStoreOneProperties(Store store){
        assertTrue("State Property Test", store.getLocationAttribute(Store.StoreAttributeType.COUNTRY).equals("USA"));
        assertTrue("Address Property Test", store.getLocationAttribute(Store.StoreAttributeType.ADDRESS).equals("South, 10901 Stonelake Blvd"));
        assertTrue("City Property Test", store.getLocationAttribute(Store.StoreAttributeType.CITY).equals( "Austin"));
        assertTrue("Latitude Property Test", store.getLocationAttribute(Store.StoreAttributeType.LATITUDE).equals("30.3994293"));
        assertTrue("Longitude Property Test", store.getLocationAttribute(Store.StoreAttributeType.LONGITUDE).equals("-97.7380764"));
        assertTrue("Phone Property Test", store.getLocationAttribute(Store.StoreAttributeType.PHONE).equals("(512) 551-6000"));
        assertTrue("State Property Test", store.getLocationAttribute(Store.StoreAttributeType.STATE).equals("TX"));
        assertTrue("Post Code Property Test", store.getLocationAttribute(Store.StoreAttributeType.POSTALCODE).equals("78735"));
    }

    private List<String> getStoreIds(int limit){
        List<String> prodIds = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            prodIds.add("" + i);
        }

        return prodIds;
    }
}