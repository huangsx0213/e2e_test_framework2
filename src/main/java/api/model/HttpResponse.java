package api.model;

import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private final Response response;
    private JsonPath jsonPath;

    public HttpResponse(Response response) {
        this.response = response;
        logger.debug("Created HttpResponse with status code: {}", response.getStatusCode());
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public String getBodyAsString() {
        return response.getBody().asString();
    }

    public <T> Optional<T> getBodyAs(Class<T> clazz) {
        try {
            T body = response.getBody().as(clazz);
            return Optional.ofNullable(body);
        } catch (Exception e) {
            logger.error("Failed to parse response body as {}", clazz.getSimpleName(), e);
            return Optional.empty();
        }
    }

    public JsonPath jsonPath() {
        if (jsonPath == null) {
            jsonPath = response.jsonPath();
        }
        return jsonPath;
    }

    public Optional<String> getHeader(String headerName) {
        return Optional.ofNullable(response.getHeader(headerName));
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

    public Optional<String> getContentType() {
        return Optional.ofNullable(response.getContentType());
    }

    public <T> Optional<T> extractValue(String jsonPath, Class<T> clazz) {
        try {
            T value = this.jsonPath().getObject(jsonPath, clazz);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            logger.error("Failed to extract value for path: {} as {}", jsonPath, clazz.getSimpleName(), e);
            return Optional.empty();
        }
    }

    public boolean hasJsonPath(String path) {
        return jsonPath().get(path) != null;
    }

    public void logResponse() {
        logger.info("Response Status Code: {}", getStatusCode());
        logger.info("Response Headers:\n{}", getHeaders());
        logger.info("Response Body:\n{}", getBodyAsString());
        logger.info("Response Time: {} ms", getResponseTime());
    }

    public Response getOriginalResponse() {
        return response;
    }
}