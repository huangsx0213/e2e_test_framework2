package api.StepDetails;

import api.*;
import api.util.ConfigManager;
import api.model.APITestCase;
import api.model.HttpResponse;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.Map;

public class APISteps {
    private static final Logger logger = LoggerFactory.getLogger(APISteps.class);
    private final ConfigManager configManager;
    private final HttpRequestExecutor httpRequestExecutor;
    private final StandardValidator standardValidator;
    private final ContextManager contextManager;

    private TestCaseManager testCaseManager;
    private HttpResponse httpResponse;
    private String currentTCID;

    public APISteps() {
        this.configManager = ConfigManager.getInstance();
        this.httpRequestExecutor = new HttpRequestExecutor(configManager);
        this.standardValidator = new StandardValidator();
        this.contextManager = new ContextManager();
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
        this.testCaseManager = new TestCaseManager();
    }

    @Step("Load test case for {0}")
    public void loadTestCase(String tcid) {
        this.currentTCID = tcid;
        logger.info("Loaded test case for TCID: {}", tcid);
    }

    private APITestCase getTestCase() {
        return testCaseManager.findTestCaseByTCID(currentTCID);
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
        verifyResponseStatus(testCase);
        verifyResponseContent(testCase);
        executeDynamicValidation(testCase);
    }

    private void verifyResponseStatus(APITestCase testCase) {
        standardValidator.verifyResponseStatusCode(httpResponse, testCase.getExpStatus());
    }

    private void verifyResponseContent(APITestCase testCase) {
        standardValidator.verifyResponseContent(httpResponse, testCase.getExpResultAsMap(), currentTCID);
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
        contextManager.storeResponseValues(httpResponse, testCase.getSaveFields());
    }
}