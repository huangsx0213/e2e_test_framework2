package api.StepDetails;

import api.*;
import api.APIConfigManager;
import api.model.APITestCase;
import api.model.APIResponse;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APISteps {
    private static final Logger logger = LoggerFactory.getLogger(APISteps.class);
    private final APIConfigManager apiConfigManager;
    private final APITestExecutionManager APITestExecutionManager;
    private final APIResponseValidator APIResponseValidator;
    private final TestTearDownManager testTeardownManager;

    private APIResponse APIResponse;
    private APITestCase currentTestCase;
    private String currentTCID;

    public APISteps() {
        this.apiConfigManager = APIConfigManager.getInstance();
        this.APITestExecutionManager = new APITestExecutionManager();
        this.APIResponseValidator = new APIResponseValidator();
        this.testTeardownManager = new TestTearDownManager();
    }

    @Step("Set the environment to {0}")
    public void setEnvironment(String environment) {
        apiConfigManager.setEnvironment(environment);
        logger.info("Environment set to: {}", environment);
    }

    @Step("Set the project to {0}")
    public void setProject(String project) {
        apiConfigManager.setProject(project);
        logger.info("Project set to: {}", project);
    }

    @Step("Load test case for {0}")
    public void loadTestCase(String tcid) {
        currentTestCase = APITestExecutionManager.loadTestCase(tcid);
        logger.info("Loaded test case for TCID: {}", tcid);
        currentTCID = currentTestCase.getTCID();

    }

    @Step("Execute API request")
    public void executeAPIRequest() {
        logger.info("******************************** Executing Setup Request **********************************");
        logger.info("Executing setup test cases for TCID: {}", currentTCID);
        APITestExecutionManager.executeSetupTestCases(currentTestCase);
        logger.info("Setup test cases executed for TCID: {}", currentTCID);

        testTeardownManager.registerTearDownTestCases(currentTestCase);

        logger.info("******************************** Executing Pre-validation Request *************************");
        logger.info("Executing pre-validation requests for TCID: {}",currentTCID);
        APIResponseValidator.executePreValidationRequests(currentTestCase);
        logger.info("Pre-validation requests executed for TCID: {}", currentTCID);

        logger.info("******************************** Executing Main Request ***********************************");
        logger.info("Executing main request for TCID: {}",currentTCID);
        APIResponse = APITestExecutionManager.executeMainRequest(currentTestCase);
        logger.info("Main request executed for TCID: {}",currentTCID);
    }

    @Step("Verify API response")
    public void verifyAPIResponse() {
        logger.info("******************************** Executing Verifying Response*******************************");
        logger.info("Verifying response for TCID: {}", currentTCID);
        APIResponseValidator.verifyResponse(currentTestCase, APIResponse);
    }

    @Step("Store response values")
    public void storeResponseValues() {
        logger.info("Storing response values for TCID: {}", currentTCID);
        APITestExecutionManager.storeResponseValues(currentTestCase, APIResponse);
    }

    public void executeTearDownTestCases() {
        logger.info("Executing tear down test cases");
        testTeardownManager.executeTearDownTestCases();
    }
}