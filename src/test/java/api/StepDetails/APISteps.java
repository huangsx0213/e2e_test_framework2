package api.StepDetails;

import api.*;
import api.util.ConfigManager;
import api.model.APITestCase;
import api.model.HttpResponse;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class APISteps {
    private static final Logger logger = LoggerFactory.getLogger(APISteps.class);
    private final ConfigManager configManager;
    private final HttpRequestExecutor httpRequestExecutor;
    private final StandardValidator standardValidator;
    private final ContextManager contextManager;

    private TestCaseManager testCaseManager;
    private HttpResponse httpResponse;
    private String currentTCID;
    private APITestCase currentTestCase;
    private List<String> executedSetupCases;
    private static List<String> pendingTeardownCases = new ArrayList<>();

    public APISteps() {
        this.configManager = ConfigManager.getInstance();
        this.httpRequestExecutor = new HttpRequestExecutor(configManager);
        this.standardValidator = new StandardValidator();
        this.contextManager = new ContextManager();
        this.executedSetupCases = new ArrayList<>();
        this.testCaseManager = new TestCaseManager();
    }

    // Main step methods

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
        currentTCID = tcid;
        testCaseManager.loadTestCasesFromExcel();
        currentTestCase = testCaseManager.getTestCaseByTCID(currentTCID);
        logger.info("Loaded test case for TCID: {}", tcid);

        executeSetupTestCases(currentTestCase);
        registerTeardownTestCases(currentTestCase);
    }

    @Step("Execute API request")
    public void executeAPIRequest() {
        executePreValidationRequests(currentTestCase);
        executeMainRequest(currentTestCase);
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        verifyResponseStatus(currentTestCase, httpResponse);
        verifyResponseContent(currentTestCase, httpResponse);
        executeDynamicValidation(currentTestCase);
    }

    @Step("Store response values")
    public void storeResponseValues() {
        storeResponseValues(currentTestCase, httpResponse);
    }

    // Helper methods

    private void executeSetupTestCases(APITestCase testCase) {
        List<String> setupTCIDs = this.testCaseManager.getConditionTCIDs(testCase, "[TestSetup]");
        for (String setupTCID : setupTCIDs) {
            if (!executedSetupCases.contains(setupTCID)) {
                logger.info("Executing setup test case: {}", setupTCID);
                executeTestCase(setupTCID);
                executedSetupCases.add(setupTCID);
            }
        }
    }

    private void registerTeardownTestCases(APITestCase testCase) {
        List<String> teardownTCIDs = this.testCaseManager.getConditionTCIDs(testCase, "[TestTeardown]");
        synchronized (pendingTeardownCases) {
            pendingTeardownCases.addAll(teardownTCIDs);
        }
    }

    private void executeTestCase(String tcid) {
        APITestCase testCase = testCaseManager.getTestCaseByTCID(tcid);
        HttpResponse response = httpRequestExecutor.prepareAndSendRequest(testCase);
        verifyResponseStatus(testCase, response);
        verifyResponseContent(testCase, response);
        storeResponseValues(testCase, response);
    }

    private void executePreValidationRequests(APITestCase testCase) {
        Set<String> preValidationTCIDs = testCaseManager.getValidationTCIDs(currentTCID, testCase.getExpResultAsMap());
        preValidationTCIDs.forEach(this::executeValidationRequest);
    }

    private void executeValidationRequest(String tcid) {
        logger.info("Executing dynamic validation request by TCID: {}", tcid);
        APITestCase validationTestCase = testCaseManager.getTestCaseByTCID(tcid);
        HttpResponse response = httpRequestExecutor.prepareAndSendRequest(validationTestCase);
        contextManager.setPreValidationResponse(tcid, response);
    }

    private void executeMainRequest(APITestCase testCase) {
        logger.info("Executing main API request for TCID: {}", this.currentTCID);
        httpResponse = httpRequestExecutor.prepareAndSendRequest(testCase);
    }

    private void verifyResponseStatus(APITestCase testCase, HttpResponse response) {
        standardValidator.verifyResponseStatusCode(response, testCase.getExpStatus());
    }

    private void verifyResponseContent(APITestCase testCase, HttpResponse response) {
        standardValidator.verifyResponseContent(response, testCase.getExpResultAsMap(), testCase.getTCID());
    }

    private void executeDynamicValidation(APITestCase testCase) {
        Map<String, Map<String, String>> dynamicExpectedResults = testCaseManager.getDynamicExpectedResults(testCase.getExpResultAsMap(), currentTCID);
        dynamicExpectedResults.forEach(this::executeDynamicValidationByTCID);
    }

    private void executeDynamicValidationByTCID(String tcid, Map<String, String> expectedChanges) {
        logger.info("Executing dynamic validation by TCID: {}", tcid);
        HttpResponse preValidationResponse = contextManager.getPreValidationResponse(tcid);
        HttpResponse postValidationResponse = httpRequestExecutor.prepareAndSendRequest(testCaseManager.getTestCaseByTCID(tcid));
        DynamicValidator.validate(preValidationResponse, postValidationResponse, expectedChanges);
        logger.info("Dynamic validation finished by TCID: {}", tcid);
    }

    private void storeResponseValues(APITestCase testCase, HttpResponse response) {
        contextManager.storeResponseValues(response, testCase.getSaveFields());
    }

    public void executeTeardownTestCases() {
        List<String> casesToExecute;
        synchronized (pendingTeardownCases) {
            casesToExecute = new ArrayList<>(pendingTeardownCases);
            pendingTeardownCases.clear();
        }
        for (String teardownTcid : casesToExecute) {
            loadTestCase(teardownTcid);
            logger.info("Executing teardown test case: {}", teardownTcid);
            executeTestCase(teardownTcid);
        }
    }

    public static boolean hasPendingTeardownCases() {
        synchronized (pendingTeardownCases) {
            return !pendingTeardownCases.isEmpty();
        }
    }
}