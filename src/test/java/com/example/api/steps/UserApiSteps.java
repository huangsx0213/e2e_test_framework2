package com.example.api.steps;

import com.example.api.config.ConfigManager;
import com.example.api.context.TestContext;
import com.example.api.model.ApiResponse;
import com.example.api.request.HttpRequestBuilder;
import com.example.api.template.TemplateProcessor;
import com.example.api.validation.CompareStateStrategy;
import com.example.api.validation.DynamicValidator;
import com.example.api.validation.FetchReferenceStateStrategy;
import net.serenitybdd.annotations.Step;
import io.restassured.path.json.JsonPath;

import java.util.Map;

public class UserApiSteps {

    private HttpRequestBuilder requestBuilder;
    private ApiResponse apiResponse;
    private DynamicValidator dynamicValidator;
    private TestContext testContext = TestContext.getInstance();

    @Step("Set the current environment to {0}")
    public void setEnvironment(String environment) {
        ConfigManager.setEnvironment(environment);
    }

    @Step("Set the current project to {0}")
    public void setProject(String project) {
        ConfigManager.setProject(project);
    }

    @Step("Prepare a {0} request to {1}")
    public void prepareRequest(String method, String endpoint) {
        requestBuilder = new HttpRequestBuilder()
                .setMethod(method)
                .setEndpoint(endpoint);
    }

    @Step("Set request body using template {0}")
    public void setRequestBody(String templateKey, Map<String, String> data) throws Exception {
        String templateName = ConfigManager.getBodyTemplate(templateKey);
        String body = TemplateProcessor.processTemplate(templateName, data);
        requestBuilder.setBody(body);
    }

    @Step("Set request headers using template {0}")
    public void setRequestHeaders(String templateKey, Map<String, String> data) throws Exception {
        String templateName = ConfigManager.getHeaderTemplate(templateKey);
        String headersString = TemplateProcessor.processTemplate(templateName, data);
        Map<String, String> processedHeaders = TemplateProcessor.parseHeaders(headersString);
        requestBuilder.setHeaders(processedHeaders);
    }

    @Step("Set up dynamic validation with reference API endpoint {0}")
    public void setUpDynamicValidation(String referenceEndpoint) {
        FetchReferenceStateStrategy fetchStrategy = new FetchReferenceStateStrategy(referenceEndpoint);
        CompareStateStrategy compareStrategy = new CompareStateStrategy();
        dynamicValidator = new DynamicValidator(fetchStrategy, compareStrategy);
    }

    @Step("Send the API request with dynamic validation")
    public void sendRequestWithDynamicValidation(Map<String, String> expectedChanges) {
        apiResponse = dynamicValidator.validate(requestBuilder, expectedChanges);
    }

    @Step("Send the API request")
    public void sendRequest() {
        apiResponse = new ApiResponse(requestBuilder.build()
                .request(requestBuilder.getMethod(), requestBuilder.getEndpoint()));
    }

    @Step("Verify response status code is {0}")
    public void verifyResponseStatusCode(int expectedStatusCode) {
        assert apiResponse.getStatusCode() == expectedStatusCode :
                "Expected status code " + expectedStatusCode + " but got " + apiResponse.getStatusCode();
    }

    @Step("Verify response contains expected data")
    public void verifyResponseContent(Map<String, String> expectedData) {
        String responseBody = apiResponse.getBodyAsString();
        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            assert responseBody.contains("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"") :
                    "Response does not contain expected data: " + entry.getKey() + " = " + entry.getValue();
        }
    }

    @Step("Store response value in context")
    public void storeResponseValue(String key, String jsonPath) {
        JsonPath jsonPathEvaluator = new JsonPath(apiResponse.getBodyAsString());
        Object value = jsonPathEvaluator.get(jsonPath);
        testContext.setData(key, value);
    }

    @Step("Use stored value in request")
    public void useStoredValueInRequest(String key, String placeholder) {
        Object value = testContext.getData(key);
        requestBuilder.addQueryParam(placeholder, value.toString());
    }
}