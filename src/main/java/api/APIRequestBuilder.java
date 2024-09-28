package api;

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
    private Map<String, Object> queryParams;
    private Map<String, Object> pathParams;
    private String bodyTemplateKey;
    private String headersTemplateKey;
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
    public APIRequestBuilder addQueryParam(String name, Object value) {
        queryParams.put(name, value);
        logger.debug("Added query parameter: {} = {}", name, value);
        return this;
    }

    public APIRequestBuilder addPathParam(String name, Object value) {
        pathParams.put(name, value);
        logger.debug("Added path parameter: {} = {}", name, value);
        return this;
    }

    private void buildRequestBody() {
        if (bodyTemplateKey != null) {
            try {
                String body = APIRequestTemplateProcessor.renderTemplate(bodyTemplateKey, bodyOverride);
                request.body(body);
                logger.debug("Built request body using template: {} and overrides", bodyTemplateKey);
            } catch (Exception e) {
                logger.error("Failed to build request body", e);
                throw new TestException.RequestPreparationException("Failed to build request body", e);
            }
        }
    }

    private void buildRequestHeaders() {
        if (headersTemplateKey != null) {
            try {
                String headersString = APIRequestTemplateProcessor.renderTemplate(headersTemplateKey, headerOverride);
                Map<String, String> headers = APIRequestTemplateProcessor.parseHeaderString(headersString);
                request.headers(headers);
                logger.debug("Built request headers using template: {} and overrides", headersTemplateKey);
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
        request.queryParams(queryParams);
        request.pathParams(pathParams);

        logRequest();

        logger.info("Executing {} request to {}", method, endpoint);
        Response response = request.request(method, endpoint);
        logger.debug("Received response with status code: {}", response.getStatusCode());

        return response;
    }

    private void logRequest() {
        logger.info("Request details:");
        logger.info("URL: {}", endpoint);
        logger.info("Method: {}", method);
        logger.info("Header Override: {}", headerOverride);
        logger.info("Body Override: {}", bodyOverride);
        logger.info("Query Parameters: {}", queryParams);
        logger.info("Path Parameters: {}", pathParams);

        logTemplate(headersTemplateKey, headerOverride, "Headers");
        logTemplate(bodyTemplateKey, bodyOverride, "Body");
    }

    private void logTemplate(String templateKey, Map<String, String> override, String type) {
        if (templateKey != null) {
            try {
                String content = APIRequestTemplateProcessor.renderTemplate(templateKey, override);
                logger.info("Request {}:\n{}", type, content);
            } catch (Exception e) {
                logger.error("Failed to log request {}", type, e);
            }
        }
    }
}