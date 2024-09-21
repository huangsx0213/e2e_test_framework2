package api.steps;

import api.config.ConfigManager;
import api.context.TestContext;
import api.model.ApiResponse;
import api.request.HttpRequestBuilder;
import net.serenitybdd.annotations.Step;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class ApiSteps {
    private static final Logger logger = LoggerFactory.getLogger(ApiSteps.class);
    private final ConfigManager configManager = ConfigManager.getInstance();
    private HttpRequestBuilder requestBuilder;
    private ApiResponse apiResponse;
    private TestContext testContext = TestContext.getInstance();
    private JsonPath jsonPath;
    @Step("Set the environment to {0}")
    public void setEnvironment(String environment) {
        configManager.setEnvironment(environment);
        logger.info("Environment set to: {}", environment);
    }

    @Step("Set the project to {0}")
    public void setProject(String project) {
        configManager.setProject(project);
        logger.info("Project set to: {}", project);
    }
    @Step("Prepare a request to {0}")
    public ApiSteps prepareRequest(String endpointKey) {
        requestBuilder = new HttpRequestBuilder().setEndpoint(endpointKey);
        logger.info("Prepared request for endpoint: {}", endpointKey);
        return this;
    }

    @Step("Set request body using template {0}")
    public ApiSteps setRequestBody(String templateKey, Map<String, String> data) {
        try {
            requestBuilder.setBody(templateKey, data);
            logger.info("Set request body using template: {}", templateKey);
        } catch (Exception e) {
            logger.error("Failed to set request body", e);
            throw new RuntimeException("Failed to set request body", e);
        }
        return this;
    }

    @Step("Set request headers using template {0}")
    public ApiSteps setRequestHeaders(String templateKey, Map<String, String> data) {
        try {
            requestBuilder.setHeaders(templateKey, data);
            logger.info("Set request headers using template: {}", templateKey);
        } catch (Exception e) {
            logger.error("Failed to set request headers", e);
            throw new RuntimeException("Failed to set request headers", e);
        }
        return this;
    }

    @Step("Send the API request")
    public ApiSteps sendRequest() {
        apiResponse = new ApiResponse(requestBuilder.execute());
        logger.info("Sent API request. Response status code: {}", apiResponse.getStatusCode());
        logger.info("Sent API request. Response content: {}", apiResponse.getBodyAsString());
        return this;
    }

    @Step("Verify response status code is {0}")
    public ApiSteps verifyResponseStatusCode(int expectedStatusCode) {
        int actualStatusCode = apiResponse.getStatusCode();
        assert actualStatusCode == expectedStatusCode :
                String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode);
        logger.info("Verified response status code: {}", actualStatusCode);
        return this;
    }

    @Step("Verify response contains expected cases")
    public ApiSteps verifyResponseContent(Map<String, String> expectedData) {
        jsonPath = apiResponse.jsonPath();
        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            String key = entry.getKey(); // Use the key from the expectedData map
            String actualValue = jsonPath.get(key).toString();
            String expectedValue = entry.getValue();
            assert actualValue != null && actualValue.equals(expectedValue) :
                    String.format("Expected %s to be %s but got %s", key, expectedValue, actualValue);
            logger.info("Verified response field: {} = {}", key, actualValue);
        }
        return this;
    }

    @Step("Store response value in context")
    public ApiSteps storeResponseValue(List<String> keys) {
        jsonPath = apiResponse.jsonPath();
        for (String key : keys) {
            String value = jsonPath.getString(key).toString();
            testContext.setData(key, value);
            logger.info("Stored response value: {} = {}", key, value);
        }
        return this;
    }

    public HttpRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public ApiResponse getApiResponse() {
        return apiResponse;
    }
}