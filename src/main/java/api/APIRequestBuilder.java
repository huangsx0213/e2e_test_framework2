package api;

import api.model.TestContext;
import api.util.TestDataGenerator;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.restassured.http.Method;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIRequestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(APIRequestBuilder.class);
    private final APIConfigManager APIConfigManager;
    private RequestSpecification request;
    private Method method;
    private String endpoint;
    private Map<String, String> queryParams;
    private Map<String, String> pathParams;
    private String bodyTemplateKey;
    private String headersTemplateKey;
    private Map<String, String> headers;
    private String body;
    private Map<String, String> bodyOverride;
    private Map<String, String> headerOverride;
    private boolean relaxedHttps = false;

    public APIRequestBuilder(APIConfigManager APIConfigManager) {
        this.APIConfigManager = APIConfigManager;
        this.request = RestAssured.given();
        this.queryParams = new HashMap<>();
        this.pathParams = new HashMap<>();
        this.bodyOverride = new HashMap<>();
        this.headerOverride = new HashMap<>();

    }

    public APIRequestBuilder setEndpoint(String endpointKey) {
        this.endpoint = APIConfigManager.getEndpointUrl(endpointKey);
        this.method = Method.valueOf(APIConfigManager.getEndpointMethod(endpointKey).toUpperCase());
        logger.debug("Set endpoint: {} with method: {}", this.endpoint, this.method);
        return this;
    }

    public APIRequestBuilder setBodyTemplate(String templateKey) {
        this.bodyTemplateKey = templateKey;
        logger.debug("Set body template key: {}", templateKey);
        return this;
    }

    public APIRequestBuilder setHeadersTemplate(String templateKey) {
        this.headersTemplateKey = templateKey;
        logger.debug("Set headers template key: {}", templateKey);
        return this;
    }

    public APIRequestBuilder setBodyOverride(Map<String, String> override) {
        this.bodyOverride = override;
        logger.debug("Set body override: {}", override);
        return this;
    }

    public APIRequestBuilder setHeaderOverride(Map<String, String> override) {
        this.headerOverride = override;
        logger.debug("Set header override: {}", override);
        return this;
    }
    public APIRequestBuilder setRelaxedHTTPSValidation() {
        this.relaxedHttps = true;
        logger.debug("Enabled relaxed HTTPS validation");
        return this;
    }

    public APIRequestBuilder setQueryParams(Map<String, String> params) {
        this.queryParams = params;
        return this;
    }

    public APIRequestBuilder setPathParams(Map<String, String> params) {
        this.pathParams = params;
        return this;
    }

    private void processParams() {
        Map<String, String> savedFields = TestContext.getInstance().getAllDataAsString();

        queryParams.replaceAll((key, value) ->
                TestDataGenerator.generateDynamicData(value, savedFields));

        pathParams.replaceAll((key, value) ->
                TestDataGenerator.generateDynamicData(value, savedFields));
    }

    private void buildRequestBody() {
        if (bodyTemplateKey != null) {
            try {
                logger.debug("Building request body using template: {} and overrides {}", bodyTemplateKey, bodyOverride);
                body = APIRequestTemplateProcessor.renderTemplate(bodyTemplateKey, bodyOverride);
                request.body(body);
            } catch (Exception e) {
                logger.error("Failed to build request body", e);
                throw new TestException.RequestPreparationException("Failed to build request body", e);
            }
        }
    }

    private void buildRequestHeaders() {
        if (headersTemplateKey != null) {
            try {
                logger.debug("Building request headers using template: {} and overrides {}", headersTemplateKey, headerOverride);
                String headersString = APIRequestTemplateProcessor.renderTemplate(headersTemplateKey, headerOverride);
                headers = APIRequestTemplateProcessor.parseHeaderString(headersString);
                request.headers(headers);
            } catch (Exception e) {
                logger.error("Failed to build request headers", e);
                throw new TestException.RequestPreparationException("Failed to build request headers", e);
            }
        }
    }

    public Response execute() {
        if (endpoint == null || method == null) {
            throw new TestException.RequestPreparationException("Endpoint or method not set");
        }

        if (relaxedHttps) {
            RestAssured.useRelaxedHTTPSValidation();
            logger.warn("Using relaxed HTTPS validation. This should only be used for testing purposes.");
        }

        buildRequestBody();
        buildRequestHeaders();
        processParams(); // Process the query and path parameters before sending the request

        request.queryParams(queryParams);
        request.pathParams(pathParams);

        logRequest();

        logger.info("Executing {} request to {}", method, endpoint);
        Response response = request.request(method, endpoint);
        return response;
    }

    private void logRequest() {
        logger.info("Request details:");
        logger.info("URL: {}", endpoint);
        logger.info("Method: {}", method);
        logger.info("Query Parameters: {}", queryParams);
        logger.info("Path Parameters: {}", pathParams);
        logger.info("Headers: {}", headers);
        logger.info("Body: {}", body);
    }
}