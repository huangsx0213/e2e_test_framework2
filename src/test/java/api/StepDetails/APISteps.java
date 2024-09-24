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

import java.util.*;
import java.util.stream.Collectors;

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
        executePreValidationRequests();
        executeMainRequest();
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        verifyResponseStatusCode();
        verifyResponseContent();
        executeDynamicValidation();
    }

    @Step("Store response values")
    public void storeResponseValues() {
        Optional.ofNullable(currentAPITestCase.getSaveFields())
                .filter(fields -> !fields.isEmpty())
                .ifPresent(this::storeResponseValue);
    }

    private void executePreValidationRequests() {
        Map<String, String> expResult = Utils.parseKeyValuePairs(currentAPITestCase.getExpResult());
        Set<String> preValidationTcids = extractPreValidationTcids(expResult);

        for (String tcid : preValidationTcids) {
            executeValidationRequest(tcid);
        }
    }

    private Set<String> extractPreValidationTcids(Map<String, String> expResult) {
        return expResult.keySet().stream()
                .filter(key -> key.contains(".") && !key.startsWith(currentAPITestCase.getTCID()))
                .map(key -> key.substring(0, key.indexOf('.')))
                .collect(Collectors.toSet());
    }

    private void executeValidationRequest(String tcid) {
        logger.info("Executing dynamic validation request by TCID: {}", tcid);
        APITestCase validationTestCase = TestCaseManager.findTestCaseByTCID(tcid);
        HttpResponse response = prepareAndSendRequest(validationTestCase);
        testContext.setData("preValidationResponse_" + tcid, response);
    }

    private void executeMainRequest() {
        logger.info("Executing main API request for TCID: {}", currentAPITestCase.getTCID());
        httpResponse = prepareAndSendRequest(currentAPITestCase);
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

    private void verifyResponseStatusCode() {
        int expectedStatusCode = currentAPITestCase.getExpStatus();
        int actualStatusCode = httpResponse.getStatusCode();
        if (actualStatusCode != expectedStatusCode) {
            throw new AssertionError(String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode));
        }
        logger.info("Verified response status code: {}", actualStatusCode);
    }

    private void verifyResponseContent() {
        Map<String, String> expectedData = Utils.parseKeyValuePairs(currentAPITestCase.getExpResult());
        expectedData.forEach(this::verifyResponseField);
    }

    private void verifyResponseField(String key, String expectedValue) {
        String actualValue;
        if (isDynamicField(key)) {
            return;
        }
        String field = key.substring(key.indexOf('.') + 1);
        actualValue = httpResponse.jsonPath().getString(field);
        if (actualValue == null || !actualValue.equals(expectedValue)) {
            throw new AssertionError(String.format("Expected %s to be %s but got %s", key, expectedValue, actualValue));
        }
        logger.info("Verified response field: {} = {}", key, actualValue);
    }

    private boolean isDynamicField(String key) {
        return key.contains(".") && !key.startsWith(currentAPITestCase.getTCID());
    }

    private void executeDynamicValidation() {
        Map<String, String> expResult = Utils.parseKeyValuePairs(currentAPITestCase.getExpResult());
        Map<String, Map<String, String>> dynamicValidations = extractDynamicValidations(expResult);

        dynamicValidations.forEach(this::executeValidationForTcid);
    }

    private Map<String, Map<String, String>> extractDynamicValidations(Map<String, String> expResult) {
        Map<String, Map<String, String>> dynamicValidations = new HashMap<>();
        expResult.forEach((key, value) -> {
            if (isDynamicField(key)) {
                String tcid = key.substring(0, key.indexOf('.'));
                String field = key.substring(key.indexOf('.') + 1);
                dynamicValidations.computeIfAbsent(tcid, k -> new HashMap<>()).put(field, value);
            }
        });
        return dynamicValidations;
    }

    private void executeValidationForTcid(String tcid, Map<String, String> expectedChanges) {
        HttpResponse preValidationResponse = getPreValidationResponse(tcid);
        HttpResponse postValidationResponse = prepareAndSendRequest(TestCaseManager.findTestCaseByTCID(tcid));
        DynamicValidator.validate(preValidationResponse, postValidationResponse, expectedChanges);
        logger.info("Dynamic validation finished by TCID: {}", tcid);
    }

    private HttpResponse getPreValidationResponse(String tcid) {
        return testContext.getData("preValidationResponse_" + tcid, HttpResponse.class)
                .orElseThrow(() -> new IllegalStateException("Pre-validation response not found for TCID: " + tcid));
    }

    private void storeResponseValue(List<String> keys) {
        keys.forEach(key -> {
            String field = key.substring(key.indexOf('.') + 1);
            String value = httpResponse.jsonPath().getString(field);
            testContext.setData(key, value);
            logger.info("Stored response value: {} = {}", key, value);
        });
    }
}
