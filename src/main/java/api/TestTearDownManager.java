package api;

import api.model.APITestCase;
import java.util.ArrayList;
import java.util.List;

public class TestTearDownManager {
    private final APITestCaseManager apiTestCaseManager;
    private final APITestExecutionManager apiTestExecutionManager;
    private static final List<String> pendingTearDownCases = new ArrayList<>();

    public TestTearDownManager() {
        this.apiTestCaseManager = new APITestCaseManager();
        this.apiTestExecutionManager = new APITestExecutionManager();
    }

    public void registerTearDownTestCases(APITestCase testCase) {
        List<String> tearDownTCIDs = apiTestCaseManager.getConditionTCIDs(testCase, "[TestTearDown]");
        synchronized (pendingTearDownCases) {
            pendingTearDownCases.addAll(tearDownTCIDs);
        }
    }

    public void executeTearDownTestCases() {
        List<String> casesToExecute;
        synchronized (pendingTearDownCases) {
            casesToExecute = new ArrayList<>(pendingTearDownCases);
            pendingTearDownCases.clear();
        }
        for (String tearDownTCID : casesToExecute) {
            apiTestExecutionManager.executeTestCase(tearDownTCID);
        }
    }
    public static boolean hasPendingTearDownCases() {
        synchronized (pendingTearDownCases) {
            return !pendingTearDownCases.isEmpty();
        }
    }
}