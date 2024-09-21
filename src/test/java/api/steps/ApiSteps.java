package api.steps;

import api.config.ConfigManager;
import api.context.TestContext;
import api.model.ApiResponse;
import api.request.HttpRequestBuilder;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ApiSteps {
    private static final Logger logger = LoggerFactory.getLogger(ApiSteps.class);
    private final ConfigManager configManager = ConfigManager.getInstance();
    private HttpRequestBuilder requestBuilder;
    private ApiResponse apiResponse;
    private final TestContext testContext = TestContext.getInstance();

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
        requestBuilder = new HttpRequestBuilder(configManager).setEndpoint(endpointKey);
        logger.info("Prepared request for endpoint: {}", endpointKey);
        return this;
    }

    @Step("Set request body using template {0}")
    public ApiSteps setRequestBody(String templateKey, Map<String, String> data) {
        requestBuilder.setBodyTemplate(templateKey).setBodyOverride(data);
        logger.info("Set request body using template: {} with overrides", templateKey);
        return this;
    }

    @Step("Set request headers using template {0}")
    public ApiSteps setRequestHeaders(String templateKey, Map<String, String> data) {
        requestBuilder.setHeadersTemplate(templateKey).setHeaderOverride(data);
        logger.info("Set request headers using template: {} with overrides", templateKey);
        return this;
    }

    @Step("Send the API request")
    public ApiSteps sendRequest() {
        apiResponse = new ApiResponse(requestBuilder.execute());
        apiResponse.logResponse();
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

    @Step("Verify response contains expected data")
    public ApiSteps verifyResponseContent(Map<String, String> expectedData) {
        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            String key = entry.getKey();
            String actualValue = apiResponse.jsonPath().getString(key);
            String expectedValue = entry.getValue();
            assert actualValue != null && actualValue.equals(expectedValue) :
                    String.format("Expected %s to be %s but got %s", key, expectedValue, actualValue);
            logger.info("Verified response field: {} = {}", key, actualValue);
        }
        return this;
    }

    @Step("Store response value in context")
    public ApiSteps storeResponseValue(List<String> keys) {
        for (String key : keys) {
            String value = apiResponse.jsonPath().getString(key);
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