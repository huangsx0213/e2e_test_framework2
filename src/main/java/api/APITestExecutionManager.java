package api;

import api.model.APIResponse;
import api.model.APITestCase;

import java.util.List;

public class APITestExecutionManager {
    private final APITestCaseManager APITestCaseManager;
    private final APIRequestExecutor APIRequestExecutor;
    private final TestContextManager testContextManager;

    public APITestExecutionManager() {
        this.APITestCaseManager = new APITestCaseManager();
        this.APIRequestExecutor = new APIRequestExecutor(APIConfigManager.getInstance());
        this.testContextManager = new TestContextManager();
    }

    public APITestCase loadTestCase(String tcid) {
        APITestCaseManager.loadTestCasesFromExcel();
        return APITestCaseManager.getTestCaseByTCID(tcid);
    }

    public void executeSetupTestCases(APITestCase testCase) {
        List<String> setupTCIDs = APITestCaseManager.getConditionTCIDs(testCase, "[TestSetup]");
        for (String setupTCID : setupTCIDs) {
            executeTestCase(setupTCID);
        }
    }

    public APIResponse executeMainRequest(APITestCase testCase) {
        return APIRequestExecutor.prepareAndSendRequest(testCase);
    }

    public void executeTestCase(String tcid) {
        APITestCase testCase = APITestCaseManager.getTestCaseByTCID(tcid);
        APIResponse response = APIRequestExecutor.prepareAndSendRequest(testCase);
        storeResponseValues(testCase, response);
    }

    public void storeResponseValues(APITestCase testCase, APIResponse response) {
        testContextManager.storeResponseValues(response, testCase.getSaveFields());
    }
}