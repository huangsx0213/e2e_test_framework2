package com.example.api.request;

import com.example.api.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class HttpRequestBuilder {
    private final RequestSpecification request;
    private String method;
    private String endpoint;

    public HttpRequestBuilder() {
        this.request = RestAssured.given();
        RestAssured.baseURI = ConfigManager.getBaseUrl();
    }

    public HttpRequestBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBuilder setEndpoint(String endpointKey) {
        this.endpoint = ConfigManager.getEndpoint(endpointKey);
        return this;
    }

    public HttpRequestBuilder setBody(String body) {
        request.body(body);
        return this;
    }

    public HttpRequestBuilder setHeaders(Map<String, String> headers) {
        request.headers(headers);
        return this;
    }

    public HttpRequestBuilder addQueryParam(String key, String value) {
        request.queryParam(key, value);
        return this;
    }

    public RequestSpecification build() {
        return request;
    }

    public String getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
