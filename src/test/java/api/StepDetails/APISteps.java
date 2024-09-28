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
        testCaseManager.loadTestCases();
        this.currentTCID = tcid;
        logger.info("Loaded test case for TCID: {}", tcid);

        APITestCase testCase = getTestCase();
        executeSetupTestCases(testCase);
        registerTeardownTestCases(testCase);
    }

    private APITestCase getTestCase() {
        return testCaseManager.findTestCaseByTCID(currentTCID);
    }

    private void executeSetupTestCases(APITestCase testCase) {
        List<String> setupCases = extractCases(testCase.getConditions(), "[TestSetup]");
        for (String setupTcid : setupCases) {
            if (!executedSetupCases.contains(setupTcid)) {
                logger.info("Executing setup test case: {}", setupTcid);
                executeTestCase(setupTcid);
                executedSetupCases.add(setupTcid);
            }
        }
    }

    private void registerTeardownTestCases(APITestCase testCase) {
        List<String> teardownCases = extractCases(testCase.getConditions(), "[TestTeardown]");
        synchronized (pendingTeardownCases) {
            pendingTeardownCases.addAll(teardownCases);
        }
    }

    private List<String> extractCases(List<String> conditions, String prefix) {
        List<String> cases = new ArrayList<>();
        for (String condition : conditions) {
            if (condition.startsWith(prefix)) {
                cases.addAll(Arrays.asList(condition.substring(prefix.length()).split(",")));
            }
        }
        return cases;
    }

    private void executeTestCase(String tcid) {
        APITestCase testCase = testCaseManager.findTestCaseByTCID(tcid);
        HttpResponse response = httpRequestExecutor.prepareAndSendRequest(testCase);
        verifyResponseStatus(testCase, response);
        verifyResponseContent(testCase, response);
        storeResponseValues(testCase, response);
    }

    @Step("Execute API request")
    public void executeAPIRequest() {
        APITestCase testCase = getTestCase();
        executePreValidationRequests(testCase);
        executeMainRequest(testCase);
    }

    private void executePreValidationRequests(APITestCase testCase) {
        Set<String> preValidationTcids = testCaseManager.extractPreValidationTCIDs(currentTCID, testCase.getExpResultAsMap());
        preValidationTcids.forEach(this::executeValidationRequest);
    }

    private void executeValidationRequest(String tcid) {
        logger.info("Executing dynamic validation request by TCID: {}", tcid);
        APITestCase validationTestCase = testCaseManager.findTestCaseByTCID(tcid);
        HttpResponse response = httpRequestExecutor.prepareAndSendRequest(validationTestCase);
        contextManager.setPreValidationResponse(tcid, response);
    }

    private void executeMainRequest(APITestCase testCase) {
        logger.info("Executing main API request for TCID: {}", testCase.getTCID());
        httpResponse = httpRequestExecutor.prepareAndSendRequest(testCase);
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        APITestCase testCase = getTestCase();
        verifyResponseStatus(testCase, httpResponse);
        verifyResponseContent(testCase, httpResponse);
        executeDynamicValidation(testCase);
    }

    private void verifyResponseStatus(APITestCase testCase, HttpResponse response) {
        standardValidator.verifyResponseStatusCode(response, testCase.getExpStatus());
    }

    private void verifyResponseContent(APITestCase testCase, HttpResponse response) {
        standardValidator.verifyResponseContent(response, testCase.getExpResultAsMap(), testCase.getTCID());
    }

    private void executeDynamicValidation(APITestCase testCase) {
        Map<String, Map<String, String>> dynamicValidations = testCaseManager.extractDynamicValidations(testCase.getExpResultAsMap(), currentTCID);
        dynamicValidations.forEach(this::executeValidationByTCID);
    }

    private void executeValidationByTCID(String tcid, Map<String, String> expectedChanges) {
        logger.info("Executing dynamic validation by TCID: {}", tcid);
        HttpResponse preValidationResponse = contextManager.getPreValidationResponse(tcid);
        HttpResponse postValidationResponse = httpRequestExecutor.prepareAndSendRequest(testCaseManager.findTestCaseByTCID(tcid));
        DynamicValidator.validate(preValidationResponse, postValidationResponse, expectedChanges);
        logger.info("Dynamic validation finished by TCID: {}", tcid);
    }

    @Step("Store response values")
    public void storeResponseValues() {
        APITestCase testCase = getTestCase();
        storeResponseValues(testCase, httpResponse);
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
