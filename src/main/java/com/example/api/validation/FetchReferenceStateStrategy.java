package com.example.api.validation;

import com.example.api.request.HttpRequestBuilder;
import com.example.api.model.ApiResponse;
import java.util.Map;

public class FetchReferenceStateStrategy implements ValidationStrategy {
    private final String endpoint;
    private Map<String, Object> state;

    public FetchReferenceStateStrategy(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void execute(Object... args) {
        HttpRequestBuilder request = new HttpRequestBuilder().setEndpoint(endpoint);
        ApiResponse response = new ApiResponse(request.build().get());
        state = response.getBody(Map.class);
    }

    public Map<String, Object> getState() {
        return state;
    }
}