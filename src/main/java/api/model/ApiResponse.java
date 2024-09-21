package api.model;

import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ApiResponse {
    private static final Logger logger = LoggerFactory.getLogger(ApiResponse.class);
    private final Response response;
    private JsonPath jsonPath;

    public ApiResponse(Response response) {
        this.response = response;
        logger.debug("Created ApiResponse with status code: {}", response.getStatusCode());
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public String getBodyAsString() {
        return response.getBody().asString();
    }

    public <T> T getBody(Class<T> clazz) {
        return response.getBody().as(clazz);
    }

    public JsonPath jsonPath() {
        if (jsonPath == null) {
            jsonPath = response.jsonPath();
        }
        return jsonPath;
    }

    public String getHeader(String headerName) {
        return response.getHeader(headerName);
    }

    public Map<String, String> getHeaders() {
        return response.getHeaders().asList().stream()
                .collect(java.util.stream.Collectors.toMap(
                        io.restassured.http.Header::getName,
                        io.restassured.http.Header::getValue,
                        (v1, v2) -> v1
                ));
    }

    public long getResponseTime() {
        return response.getTime();
    }

    public String getContentType() {
        return response.getContentType();
    }

    public void logResponse() {
        logger.info("Response Status Code: {}", getStatusCode());
        logger.info("Response Headers: {}", getHeaders());
        logger.info("Response Body: {}", getBodyAsString());
        logger.info("Response Time: {} ms", getResponseTime());
    }
}