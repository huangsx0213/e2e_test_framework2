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
    private final TestCaseManager testCaseManager;

    private HttpResponse httpResponse;
    private String currentTCID;
    private APITestCase currentTestCase;
    private final List<String> executedSetupCases;
    private static final List<String> pendingTeardownCases = new ArrayList<>();

    public APISteps() {
        this.configManager = ConfigManager.getInstance();
        this.httpRequestExecutor = new HttpRequestExecutor(configManager);
        this.standardValidator = new StandardValidator();
        this.contextManager = new ContextManager();
        this.testCaseManager = new TestCaseManager();
        this.executedSetupCases = new ArrayList<>();
    }

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

        executeSetupTestCases();
        registerTeardownTestCases();
    }

    @Step("Execute API request")
    public void executeAPIRequest() {
        executePreValidationRequests();
        executeMainRequest();
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        verifyResponseStatus();
        verifyResponseContent();
        executeDynamicValidation();
    }

    @Step("Store response values")
    public void storeResponseValues() {
        contextManager.storeResponseValues(httpResponse, currentTestCase.getSaveFields());
    }

    private void executeSetupTestCases() {
        List<String> setupTCIDs = testCaseManager.getConditionTCIDs(currentTestCase, "[TestSetup]");
        for (String setupTCID : setupTCIDs) {
            if (!executedSetupCases.contains(setupTCID)) {
                logger.info("Executing setup test case: {}", setupTCID);
                executeTestCase(setupTCID);
                executedSetupCases.add(setupTCID);
            }
        }
    }

    private void registerTeardownTestCases() {
        List<String> teardownTCIDs = testCaseManager.getConditionTCIDs(currentTestCase, "[TestTeardown]");
        synchronized (pendingTeardownCases) {
            pendingTeardownCases.addAll(teardownTCIDs);
        }
    }

    private void executeTestCase(String tcid) {
        APITestCase testCase = testCaseManager.getTestCaseByTCID(tcid);
        HttpResponse response = httpRequestExecutor.prepareAndSendRequest(testCase);
        verifyResponseStatus(testCase, response);
        verifyResponseContent(testCase, response);
        contextManager.storeResponseValues(response, testCase.getSaveFields());
    }

    private void executePreValidationRequests() {
        Set<String> preValidationTCIDs = testCaseManager.getValidationTCIDs(currentTCID, currentTestCase.getExpResultAsMap());
        preValidationTCIDs.forEach(this::executeValidationRequest);
    }

    private void executeValidationRequest(String tcid) {
        logger.info("Executing dynamic validation request by TCID: {}", tcid);
        APITestCase validationTestCase = testCaseManager.getTestCaseByTCID(tcid);
        HttpResponse response = httpRequestExecutor.prepareAndSendRequest(validationTestCase);
        contextManager.setPreValidationResponse(tcid, response);
    }

    private void executeMainRequest() {
        logger.info("Executing main API request for TCID: {}", currentTCID);
        httpResponse = httpRequestExecutor.prepareAndSendRequest(currentTestCase);
    }

    private void verifyResponseStatus() {
        standardValidator.verifyResponseStatusCode(httpResponse, currentTestCase.getExpStatus());
    }

    private void verifyResponseContent() {
        standardValidator.verifyResponseContent(httpResponse, currentTestCase.getExpResultAsMap(), currentTCID);
    }

    private void executeDynamicValidation() {
        Map<String, Map<String, String>> dynamicExpectedResults = testCaseManager.getDynamicExpectedResults(currentTestCase.getExpResultAsMap(), currentTCID);
        dynamicExpectedResults.forEach(this::executeDynamicValidationByTCID);
    }

    private void executeDynamicValidationByTCID(String tcid, Map<String, String> expectedChanges) {
        logger.info("Executing dynamic validation by TCID: {}", tcid);
        HttpResponse preValidationResponse = contextManager.getPreValidationResponse(tcid);
        HttpResponse postValidationResponse = httpRequestExecutor.prepareAndSendRequest(testCaseManager.getTestCaseByTCID(tcid));
        DynamicValidator.validate(preValidationResponse, postValidationResponse, expectedChanges);
        logger.info("Dynamic validation finished by TCID: {}", tcid);
    }

    public void executeTeardownTestCases() {
        List<String> casesToExecute;
        synchronized (pendingTeardownCases) {
            casesToExecute = new ArrayList<>(pendingTeardownCases);
            pendingTeardownCases.clear();
        }
        for (String teardownTcid : casesToExecute) {
            logger.info("Executing teardown test case: {}", teardownTcid);
            executeTestCase(teardownTcid);
        }
    }


    private void verifyResponseStatus(APITestCase testCase, HttpResponse response) {
        standardValidator.verifyResponseStatusCode(response, testCase.getExpStatus());
    }

    private void verifyResponseContent(APITestCase testCase, HttpResponse response) {
        standardValidator.verifyResponseContent(response, testCase.getExpResultAsMap(), testCase.getTCID());
    }
}