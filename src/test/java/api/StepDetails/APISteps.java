package api.StepDetails;

import api.config.ConfigManager;
import api.model.APITestCase;
import api.model.HttpResponse;
import api.util.*;
import api.validation.DynamicValidator;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

public class APISteps {
    private static final Logger logger = LoggerFactory.getLogger(APISteps.class);
    private final ConfigManager configManager;
    private  TestCaseManager testCaseManager;
    private final RequestExecutor requestExecutor;
    private final ResponseValidator responseValidator;
    private final DynamicValidator dynamicValidator;
    private final ContextManager contextManager;

    private HttpResponse httpResponse;
    private String currentTCID;

    public APISteps() {
        this.configManager = ConfigManager.getInstance();

        this.requestExecutor = new RequestExecutor(configManager);
        this.responseValidator = new ResponseValidator();
        this.dynamicValidator = new DynamicValidator();
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

    @Step("Execute API request")
    public void executeAPIRequest() {
        APITestCase testCase = testCaseManager.findTestCaseByTCID(currentTCID);
        executePreValidationRequests(currentTCID,testCase);
        executeMainRequest(testCase);
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        APITestCase testCase = testCaseManager.findTestCaseByTCID(currentTCID);
        responseValidator.verifyResponseStatusCode(httpResponse, testCase.getExpStatus());
        responseValidator.verifyResponseContent(httpResponse, testCase.getExpResultAsMap(),currentTCID);
        executeDynamicValidation(currentTCID,testCase);
    }

    @Step("Store response values")
    public void storeResponseValues() {
        APITestCase testCase = testCaseManager.findTestCaseByTCID(currentTCID);
        contextManager.storeResponseValues(httpResponse, testCase.getSaveFields());
    }

    private void executePreValidationRequests(String currentTCID, APITestCase testCase) {

        Set<String> preValidationTcids = testCaseManager.extractPreValidationTcids(currentTCID, testCase.getExpResultAsMap());
        preValidationTcids.forEach(this::executeValidationRequest);
    }

    private void executeValidationRequest(String tcid) {
        logger.info("Executing dynamic validation request by TCID: {}", tcid);
        APITestCase validationTestCase = testCaseManager.findTestCaseByTCID(tcid);
        HttpResponse response = requestExecutor.prepareAndSendRequest(validationTestCase);
        contextManager.setPreValidationResponse(tcid, response);
    }

    private void executeMainRequest(APITestCase testCase) {
        logger.info("Executing main API request for TCID: {}", testCase.getTCID());
        httpResponse = requestExecutor.prepareAndSendRequest(testCase);
    }

    private void executeDynamicValidation(String currentTCID,APITestCase testCase) {
        testCaseManager.extractDynamicValidations(currentTCID,testCase.getExpResultAsMap())
                .forEach(this::executeValidationByTcid);
    }

    private void executeValidationByTcid(String tcid, java.util.Map<String, String> expectedChanges) {
        HttpResponse preValidationResponse = contextManager.getPreValidationResponse(tcid);
        HttpResponse postValidationResponse = requestExecutor.prepareAndSendRequest(testCaseManager.findTestCaseByTCID(tcid));
        dynamicValidator.validate(preValidationResponse, postValidationResponse, expectedChanges);
        logger.info("Dynamic validation finished by TCID: {}", tcid);
    }
}