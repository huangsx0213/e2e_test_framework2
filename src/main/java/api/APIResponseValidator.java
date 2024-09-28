// APIResponseValidator.java
package api;

import api.model.APIResponse;
import api.model.APITestCase;

import java.util.Map;
import java.util.Set;

public class APIResponseValidator {
    private final APITestCaseManager apiTestCaseManager;
    private final StandardResponseValidator standardResponseValidator;
    private final TestContextManager testContextManager;
    private final APIRequestExecutor apiRequestExecutor;

    public APIResponseValidator() {
        this.apiTestCaseManager = new APITestCaseManager();
        this.standardResponseValidator = new StandardResponseValidator();
        this.testContextManager = new TestContextManager();
        this.apiRequestExecutor = new APIRequestExecutor(APIConfigManager.getInstance());
    }

    public void executePreValidationRequests(APITestCase testCase) {
        Set<String> preValidationTCIDs = apiTestCaseManager.getValidationTCIDs(testCase.getTCID(), testCase.getExpResultAsMap());
        preValidationTCIDs.forEach(this::executeValidationRequest);
    }

    private void executeValidationRequest(String tcid) {
        APITestCase validationTestCase = apiTestCaseManager.getTestCaseByTCID(tcid);
        APIResponse response = apiRequestExecutor.prepareAndSendRequest(validationTestCase);
        testContextManager.setPreValidationResponse(tcid, response);
    }

    public void verifyResponse(APITestCase testCase, APIResponse response) {
        verifyResponseStatus(testCase, response);
        verifyResponseContent(testCase, response);
        executeDynamicValidation(testCase);
    }

    private void verifyResponseStatus(APITestCase testCase, APIResponse response) {
        standardResponseValidator.verifyResponseStatusCode(response, testCase.getExpStatus());
    }

    private void verifyResponseContent(APITestCase testCase, APIResponse response) {
        standardResponseValidator.verifyResponseContent(response, testCase.getExpResultAsMap(), testCase.getTCID());
    }

    private void executeDynamicValidation(APITestCase testCase) {
        Map<String, Map<String, String>> dynamicExpectedResults = apiTestCaseManager.getDynamicExpectedResults(testCase.getExpResultAsMap(), testCase.getTCID());
        dynamicExpectedResults.forEach(this::executeDynamicValidationByTCID);
    }

    private void executeDynamicValidationByTCID(String tcid, Map<String, String> expectedChanges) {
        APIResponse preValidationResponse = testContextManager.getPreValidationResponse(tcid);
        APIResponse postValidationResponse = apiRequestExecutor.prepareAndSendRequest(apiTestCaseManager.getTestCaseByTCID(tcid));
        DynamicResponseValidator.validate(preValidationResponse, postValidationResponse, expectedChanges);
    }
}