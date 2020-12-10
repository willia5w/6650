/*
 * Ski Data API for NEU Seattle distributed systems course
 * An API for an emulation of skier managment system for RFID tagged lift tickets. Basis for CS6650 Assignments for 2019
 *
 * OpenAPI spec version: 1.13
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.api;

import io.swagger.client.ApiCallback;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.Configuration;
import io.swagger.client.Pair;
import io.swagger.client.ProgressRequestBody;
import io.swagger.client.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import io.swagger.client.model.ResponseMsg;
import io.swagger.client.model.TopTen;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResortsApi {
    private ApiClient apiClient;

    public ResortsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ResortsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for getTopTenVert
     * @param resort resort to query by (required)
     * @param dayID day number in the season (required)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call getTopTenVertCall(List<String> resort, List<String> dayID, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;
        
        // create path and map variables
        String localVarPath = "/resort/day/top10vert";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (resort != null)
        localVarCollectionQueryParams.addAll(apiClient.parameterToPairs("multi", "resort", resort));
        if (dayID != null)
        localVarCollectionQueryParams.addAll(apiClient.parameterToPairs("multi", "dayID", dayID));

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) localVarHeaderParams.put("Accept", localVarAccept);

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);


        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] {  };

        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }
    
    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call getTopTenVertValidateBeforeCall(List<String> resort, List<String> dayID, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        // verify the required parameter 'resort' is set
        if (resort == null) {
            throw new ApiException("Missing the required parameter 'resort' when calling getTopTenVert(Async)");
        }
        // verify the required parameter 'dayID' is set
        if (dayID == null) {
            throw new ApiException("Missing the required parameter 'dayID' when calling getTopTenVert(Async)");
        }

        com.squareup.okhttp.Call call = getTopTenVertCall(resort, dayID, progressListener, progressRequestListener);
        return call;
    }

    /**
     * get the top 10 skier vertical totals for this day
     * get the top 10 skier vertical totals for this day
     * @param resort resort to query by (required)
     * @param dayID day number in the season (required)
     * @return TopTen
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public TopTen getTopTenVert(List<String> resort, List<String> dayID) throws ApiException {

        ApiResponse<TopTen> resp = getTopTenVertWithHttpInfo(resort, dayID);
        return resp.getData();
    }

    /**
     * get the top 10 skier vertical totals for this day
     * get the top 10 skier vertical totals for this day
     * @param resort resort to query by (required)
     * @param dayID day number in the season (required)
     * @return ApiResponse&lt;TopTen&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<TopTen> getTopTenVertWithHttpInfo(List<String> resort, List<String> dayID) throws ApiException {
        com.squareup.okhttp.Call call = getTopTenVertValidateBeforeCall(resort, dayID, null, null);
        Type localVarReturnType = new TypeToken<TopTen>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     * get the top 10 skier vertical totals for this day (asynchronously)
     * get the top 10 skier vertical totals for this day
     * @param resort resort to query by (required)
     * @param dayID day number in the season (required)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call getTopTenVertAsync(List<String> resort, List<String> dayID, final ApiCallback<TopTen> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = getTopTenVertValidateBeforeCall(resort, dayID, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<TopTen>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}
