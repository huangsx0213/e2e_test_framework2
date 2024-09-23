package api.request;

import api.config.ConfigManager;
import api.template.TemplateProcessor;
import api.exception.TestException;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.restassured.http.Method;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestBuilder.class);
    private final ConfigManager configManager;
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

    public HttpRequestBuilder(ConfigManager configManager) {
        this.configManager = configManager;
        this.request = RestAssured.given();
        this.queryParams = new HashMap<>();
        this.pathParams = new HashMap<>();
        this.bodyOverride = new HashMap<>();
        this.headerOverride = new HashMap<>();
    }

    public HttpRequestBuilder setEndpoint(String endpointKey) {
        this.endpoint = configManager.getEndpointUrl(endpointKey);
        this.method = Method.valueOf(configManager.getEndpointMethod(endpointKey).toUpperCase());
        logger.debug("Set endpoint: {} with method: {}", this.endpoint, this.method);
        return this;
    }

    public HttpRequestBuilder setBodyTemplate(String templateKey) {
        this.bodyTemplateKey = templateKey;
        logger.debug("Set body template key: {}", templateKey);
        return this;
    }

    public HttpRequestBuilder setHeadersTemplate(String templateKey) {
        this.headersTemplateKey = templateKey;
        logger.debug("Set headers template key: {}", templateKey);
        return this;
    }

    public HttpRequestBuilder setBodyOverride(Map<String, String> override) {
        this.bodyOverride = override;
        logger.debug("Set body override: {}", override);
        return this;
    }

    public HttpRequestBuilder setHeaderOverride(Map<String, String> override) {
        this.headerOverride = override;
        logger.debug("Set header override: {}", override);
        return this;
    }
    public HttpRequestBuilder setRelaxedHTTPSValidation() {
        this.relaxedHttps = true;
        logger.debug("Enabled relaxed HTTPS validation");
        return this;
    }
    public HttpRequestBuilder addQueryParam(String name, Object value) {
        queryParams.put(name, value);
        logger.debug("Added query parameter: {} = {}", name, value);
        return this;
    }

    public HttpRequestBuilder addPathParam(String name, Object value) {
        pathParams.put(name, value);
        logger.debug("Added path parameter: {} = {}", name, value);
        return this;
    }

    private void buildRequestBody() {
        if (bodyTemplateKey != null) {
            try {
                String body = TemplateProcessor.renderTemplate(bodyTemplateKey, bodyOverride);
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
                String headersString = TemplateProcessor.renderTemplate(headersTemplateKey, headerOverride);
                Map<String, String> headers = TemplateProcessor.parseHeaderString(headersString);
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


    public Method getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
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
                String content = TemplateProcessor.renderTemplate(templateKey, override);
                logger.info("Request {}:\n{}", type, content);
            } catch (Exception e) {
                logger.error("Failed to log request {}", type, e);
            }
        }
    }
}