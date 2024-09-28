package api.model;

import api.util.APIResponseConverter;
import api.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.Map;
import java.util.Optional;

public class APIResponse {
    private static final Logger logger = LoggerFactory.getLogger(APIResponse.class);
    private final Response response;
    private JsonPath jsonPath;

    public APIResponse(Response response) {
        this.response = response;
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public String getBodyAsString() {
        return response.getBody().asString();
    }

    public JsonPath jsonPath() {
        if (jsonPath == null) {
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("application/xml")) {
                String json = APIResponseConverter.convertXmlToJson(response.getBody().asString());
                jsonPath = new JsonPath(json);
            } else {
                jsonPath = response.jsonPath();
            }
        }
        return jsonPath;
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

    public boolean hasJsonPath(String path) {
        return jsonPath().get(path) != null;
    }

    public void logResponse() {
        logger.info("Response Status Code: {}", getStatusCode());
        logger.info("Response Headers:\n{}", getHeaders());

        String contentType = response.getContentType();
        if (contentType != null && contentType.contains("application/xml")) {
            logger.info("Response Body (XML):\n{}", Utils.formatXml(getBodyAsString()));
        } else {
            logger.info("Response Body:\n{}", getBodyAsString());
        }

        logger.info("Response Time: {} ms", getResponseTime());
    }

}