package api;

import api.model.APITestCase;
import java.util.ArrayList;
import java.util.List;

public class TestTearDownManager {
    private final APITestCaseManager APITestCaseManager;
    private final APITestExecutionManager APITestExecutionManager;
    private static final List<String> pendingTearDownCases = new ArrayList<>();

    public TestTearDownManager() {
        this.APITestCaseManager = new APITestCaseManager();
        this.APITestExecutionManager = new APITestExecutionManager();
    }

    public void registerTearDownTestCases(APITestCase testCase) {
        List<String> tearDownTCIDs = APITestCaseManager.getConditionTCIDs(testCase, "[TestTearDown]");
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
            APITestExecutionManager.executeTestCase(tearDownTCID);
        }
    }
    public static boolean hasPendingTearDownCases() {
        synchronized (pendingTearDownCases) {
            return !pendingTearDownCases.isEmpty();
        }
    }
}