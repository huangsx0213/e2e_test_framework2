package api.StepDetails;

import api.config.ConfigManager;
import api.context.TestContext;
import api.model.APITestCase;
import api.model.HttpResponse;
import api.request.HttpRequestBuilder;
import api.util.TestCaseManager;
import api.util.Utils;
import api.validation.DynamicValidator;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class APISteps {
    private static final Logger logger = LoggerFactory.getLogger(APISteps.class);
    private final ConfigManager configManager = ConfigManager.getInstance();
    private final TestContext testContext = TestContext.getInstance();
    private HttpRequestBuilder requestBuilder;
    private HttpResponse httpResponse;
    private APITestCase currentAPITestCase;

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

    @Step("Load test case for {0}")
    public void loadTestCase(String tcid) {
        currentAPITestCase = TestCaseManager.findTestCaseByTCID(tcid);
        logger.info("Loaded test case for TCID: {}", tcid);
    }

    @Step("Execute API request")
    public void executeAPIRequest() {
        executeDynamicValidationPreRequests();
        executeMainRequest();
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        verifyApiResponse();
        performDynamicValidation();
    }

    @Step("Store response values")
    public void storeResponseValues() {
        Optional.ofNullable(currentAPITestCase.getSaveFields())
                .filter(fields -> !fields.isEmpty())
                .ifPresent(this::storeResponseValue);
    }

    private void executeDynamicValidationPreRequests() {
        Optional.ofNullable(currentAPITestCase.getDynamicValidationTCID())
                .filter(tcid -> !tcid.isEmpty())
                .ifPresent(this::executeValidationRequest);
    }

    private void executeValidationRequest(String tcid) {
        logger.info("Executing dynamic validation request for TCID: {}", tcid);
        APITestCase validationTestCase = TestCaseManager.findTestCaseByTCID(tcid);
        HttpResponse response = executeTestCase(validationTestCase);
        testContext.setData("preValidationResponse", response);
    }

    private void executeMainRequest() {
        logger.info("Executing main API request for TCID: {}", currentAPITestCase.getTCID());
        httpResponse = executeTestCase(currentAPITestCase);
    }

    private HttpResponse executeTestCase(APITestCase testCase) {
        return prepareAndSendRequest(testCase);
    }

    private HttpResponse prepareAndSendRequest(APITestCase testCase) {
        prepareRequest(testCase);
        return sendRequest();
    }

    private void prepareRequest(APITestCase testCase) {
        requestBuilder = new HttpRequestBuilder(configManager)
                .setEndpoint(testCase.getEndpointKey())
                .setHeadersTemplate(testCase.getHeadersTemplateKey())
                .setHeaderOverride(Utils.parseKeyValuePairs(testCase.getHeaderOverride()))
                .setBodyTemplate(testCase.getBodyTemplateKey())
                .setBodyOverride(Utils.parseKeyValuePairs(testCase.getBodyOverride()));
    }

    private HttpResponse sendRequest() {
        HttpResponse response = new HttpResponse(requestBuilder.setRelaxedHTTPSValidation().execute());
        response.logResponse();
        return response;
    }

    private void verifyApiResponse() {
        verifyResponseStatusCode(currentAPITestCase.getExpStatus());
        verifyResponseContent(Utils.parseKeyValuePairs(currentAPITestCase.getExpResult()));
    }

    private void verifyResponseStatusCode(int expectedStatusCode) {
        int actualStatusCode = httpResponse.getStatusCode();
        assert actualStatusCode == expectedStatusCode :
                String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode);
        logger.info("Verified response status code: {}", actualStatusCode);
    }

    private void verifyResponseContent(Map<String, String> expectedData) {
        expectedData.forEach((key, expectedValue) -> {
            String actualValue = httpResponse.jsonPath().getString(key);
            assert actualValue != null && actualValue.equals(expectedValue) :
                    String.format("Expected %s to be %s but got %s", key, expectedValue, actualValue);
            logger.info("Verified response field: {} = {}", key, actualValue);
        });
    }

    private void performDynamicValidation() {
        Optional.ofNullable(currentAPITestCase.getDynamicValidationTCID())
                .filter(tcid -> !tcid.isEmpty())
                .ifPresent(this::executeDynamicValidation);
    }

    private void executeDynamicValidation(String tcid) {
        APITestCase validationTestCase = TestCaseManager.findTestCaseByTCID(tcid);
        HttpResponse preValidationResponse = testContext.getData("preValidationResponse", HttpResponse.class)
                .orElseThrow(() -> new IllegalStateException("Pre-validation response not found in context"));
        HttpResponse postValidationResponse = executeTestCase(validationTestCase);

        DynamicValidator.validate(preValidationResponse, postValidationResponse,
                currentAPITestCase.getDynamicValidationExpectedChanges());
        logger.info("Dynamic validation finished for TCID: {}", currentAPITestCase.getTCID());
    }

    private void storeResponseValue(List<String> keys) {
        keys.forEach(key -> {
            String value = httpResponse.jsonPath().getString(key);
            testContext.setData(key, value);
            logger.info("Stored response value: {} = {}", key, value);
        });
    }
}