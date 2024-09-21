package api.request;

import api.config.ConfigManager;
import api.template.TemplateProcessor;
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
    private RequestSpecification request;
    private Method method;
    private String endpoint;
    private Map<String, Object> queryParams;
    private Map<String, Object> pathParams;

    public HttpRequestBuilder() {
        this.request = RestAssured.given();
        this.queryParams = new HashMap<>();
        this.pathParams = new HashMap<>();
    }

    public HttpRequestBuilder setEndpoint(String endpointKey) {
        ConfigManager configManager = ConfigManager.getInstance();
        this.endpoint = configManager.getEndpointUrl(endpointKey);
        this.method = Method.valueOf(configManager.getEndpointMethod(endpointKey).toUpperCase());
        logger.debug("Set endpoint: {} with method: {}", this.endpoint, this.method);
        return this;
    }

    public HttpRequestBuilder setBody(String templateKey, Map<String, String> data) {
        try {
            String body = TemplateProcessor.processTemplate(templateKey, data);
            request.body(body);
            logger.debug("Set request body using template: {}", templateKey);
        } catch (Exception e) {
            logger.error("Failed to set request body", e);
            throw new RuntimeException("Failed to set request body", e);
        }
        return this;
    }

    public HttpRequestBuilder setHeaders(String templateKey, Map<String, String> data) {
        try {

            String headersString = TemplateProcessor.processTemplate(templateKey, data);
            Map<String, String> headers = TemplateProcessor.parseHeaders(headersString);
            request.headers(headers);
            logger.debug("Set request headers using template: {}", templateKey);
        } catch (Exception e) {
            logger.error("Failed to set request headers", e);
            throw new RuntimeException("Failed to set request headers", e);
        }
        return this;
    }

    public HttpRequestBuilder setQueryParam(String name, Object value) {
        queryParams.put(name, value);
        logger.debug("Set query parameter: {} = {}", name, value);
        return this;
    }

    public HttpRequestBuilder setPathParam(String name, Object value) {
        pathParams.put(name, value);
        logger.debug("Set path parameter: {} = {}", name, value);
        return this;
    }

    public Response execute() {
        request.queryParams(queryParams);
        request.pathParams(pathParams);
        logger.info("Executing {} request to {}", method, endpoint);
        return request.request(method, endpoint);
    }

    public Method getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }
}