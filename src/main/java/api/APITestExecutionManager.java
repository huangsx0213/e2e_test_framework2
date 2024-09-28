package api;

import api.model.APIResponse;
import api.model.APITestCase;

import java.util.List;

public class APITestExecutionManager {
    private final APITestCaseManager apiTestCaseManager;
    private final APIRequestExecutor apiRequestExecutor;
    private final TestContextManager testContextManager;

    public APITestExecutionManager() {
        this.apiTestCaseManager = new APITestCaseManager();
        this.apiRequestExecutor = new APIRequestExecutor(APIConfigManager.getInstance());
        this.testContextManager = new TestContextManager();
    }

    public APITestCase loadTestCase(String tcid) {
        apiTestCaseManager.loadTestCasesFromExcel();
        return apiTestCaseManager.getTestCaseByTCID(tcid);
    }

    public void executeSetupTestCases(APITestCase testCase) {
        List<String> setupTCIDs = apiTestCaseManager.getConditionTCIDs(testCase, "[TestSetup]");
        for (String setupTCID : setupTCIDs) {
            executeTestCase(setupTCID);
        }
    }

    public APIResponse executeMainRequest(APITestCase testCase) {
        return apiRequestExecutor.prepareAndSendRequest(testCase);
    }

    public void executeTestCase(String tcid) {
        APITestCase testCase = apiTestCaseManager.getTestCaseByTCID(tcid);
        APIResponse response = apiRequestExecutor.prepareAndSendRequest(testCase);
        storeResponseValues(testCase, response);
    }

    public void storeResponseValues(APITestCase testCase, APIResponse response) {
        testContextManager.storeResponseValues(response, testCase.getSaveFields());
    }
}