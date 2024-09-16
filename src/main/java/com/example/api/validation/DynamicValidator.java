package com.example.api.validation;

import com.example.api.request.HttpRequestBuilder;
import com.example.api.model.ApiResponse;
import io.restassured.response.Response;
import java.util.Map;

public class DynamicValidator {
    private final ValidationStrategy beforeRequestStrategy;
    private final ValidationStrategy afterRequestStrategy;

    public DynamicValidator(ValidationStrategy beforeRequestStrategy, ValidationStrategy afterRequestStrategy) {
        this.beforeRequestStrategy = beforeRequestStrategy;
        this.afterRequestStrategy = afterRequestStrategy;
    }

    public ApiResponse validate(HttpRequestBuilder requestBuilder, Map<String, String> expectedChanges) {
        beforeRequestStrategy.execute();

        Response restAssuredResponse = requestBuilder.build()
                .request(requestBuilder.getMethod(), requestBuilder.getEndpoint());
        ApiResponse response = new ApiResponse(restAssuredResponse);

        if (beforeRequestStrategy instanceof FetchReferenceStateStrategy &&
                afterRequestStrategy instanceof CompareStateStrategy) {
            FetchReferenceStateStrategy fetchStrategy = (FetchReferenceStateStrategy) beforeRequestStrategy;
            Map<String, Object> initialState = fetchStrategy.getState();

            fetchStrategy.execute(); // Fetch final state
            Map<String, Object> finalState = fetchStrategy.getState();

            afterRequestStrategy.execute(initialState, finalState, expectedChanges);
        }

        return response;
    }
}