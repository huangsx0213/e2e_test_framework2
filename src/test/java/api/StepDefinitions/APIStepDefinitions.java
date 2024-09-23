package api.StepDefinitions;

import api.context.TestContext;
import api.model.APITestCase;
import api.StepDetails.APISteps;
import api.config.ExcelDataReader;
import api.model.HttpResponse;
import api.validation.DynamicValidator;
import io.cucumber.java.en.*;
import net.serenitybdd.annotations.Steps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class APIStepDefinitions {
    private static final Logger logger = LoggerFactory.getLogger(APIStepDefinitions.class);

    @Steps
    APISteps apiSteps;

    private APITestCase currentAPITestCase;
    private static List<APITestCase> apiTestCases;


    @Given("I am working on the {string} project")
    public void iAmWorkingOnTheProject(String project) {
        apiSteps.setProject(project);
        logger.info("Project set: {}", project);
    }

    @And("I am using the {string} environment")
    public void iAmUsingTheEnvironment(String environment) {
        apiSteps.setEnvironment(environment);
        logger.info("Environment set: {}", environment);
    }

    @Given("I have test cases for {string}")
    public void iHaveTestDataFor(String tcid) {
        loadTestCases();
        currentAPITestCase = findTestCaseByTCID(tcid);
    }

    @When("I execute the API request")
    public void iExecuteTheAPIRequest() {
        executeDynamicValidationPreRequests();
        executeMainRequest();
    }

    private void executeDynamicValidationPreRequests() {
        Optional<String> dynamicValidationTCID = Optional.ofNullable(currentAPITestCase.getDynamicValidationTCID());
        if (dynamicValidationTCID.isPresent() && !dynamicValidationTCID.get().isEmpty()) {
            logger.info("Dynamic validation pre-request setup using TCID: {}", dynamicValidationTCID.get());
            APITestCase validationTestCase = findTestCaseByTCID(dynamicValidationTCID.get());
            HttpResponse preValidationResponse = apiSteps.executeTestCase(validationTestCase);
            TestContext.getInstance().setData("preValidationResponse", preValidationResponse);
            logger.info("Dynamic validation pre-request executed using TCID: {}", dynamicValidationTCID.get());
        }
    }

    private void executeMainRequest() {
        logger.info("Executing main API request for TCID: {}", currentAPITestCase.getTCID());
        setupApiRequest(currentAPITestCase);
        apiSteps.sendRequest();
        logger.info("Main API request executed for TCID: {}", currentAPITestCase.getTCID());
    }

    @Then("I verify the API response")
    public void iVerifyTheAPIResponse() {
        verifyApiResponse();
        performDynamicValidation();
    }

    private void performDynamicValidation() {
        Optional<String> dynamicValidationTCID = Optional.ofNullable(currentAPITestCase.getDynamicValidationTCID());
        if (dynamicValidationTCID.isPresent() && !dynamicValidationTCID.get().isEmpty()) {
            APITestCase validationTestCase = findTestCaseByTCID(dynamicValidationTCID.get());
            HttpResponse preValidationResponse = TestContext.getInstance().getData("preValidationResponse", HttpResponse.class)
                    .orElseThrow(() -> new IllegalStateException("Pre-validation response not found in context"));
            logger.info("Dynamic validation post-request setup using TCID: {}", dynamicValidationTCID.get());
            HttpResponse postValidationResponse = apiSteps.executeTestCase(validationTestCase);
            logger.info("Dynamic validation post-request executed using TCID: {}", dynamicValidationTCID.get());

            DynamicValidator.validate(preValidationResponse, postValidationResponse, currentAPITestCase.getDynamicValidationExpectedChanges());
            logger.info("Dynamic validation finished for TCID: {}", currentAPITestCase.getTCID());
        }
    }

    @And("I store the response value")
    public void iStoreTheResponseValue() {
        storeResponseValues();
    }


    private void loadTestCases() {
        if (apiTestCases == null) {
            apiTestCases = ExcelDataReader.readTestData("API");
            logger.info("Loaded {} test cases from Excel", apiTestCases.size());
        }
    }

    private APITestCase findTestCaseByTCID(String tcid) {
        return apiTestCases.stream()
                .filter(tc -> tc.getTCID().equals(tcid))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Test case not found for TCID: {}", tcid);
                    return new IllegalArgumentException("No test cases found for TCID: " + tcid);
                });
    }


    private void setupApiRequest(APITestCase testCase) {
        apiSteps.prepareRequest(testCase.getEndpointKey())
                .setRequestHeaders(testCase.getHeadersTemplateKey(), parseKeyValuePairs(testCase.getHeaderOverride()))
                .setRequestBody(testCase.getBodyTemplateKey(), parseKeyValuePairs(testCase.getBodyOverride()));
    }

    private void verifyApiResponse() {
        apiSteps.verifyResponseStatusCode(currentAPITestCase.getExpStatus())
                .verifyResponseContent(parseKeyValuePairs(currentAPITestCase.getExpResult()));
        logger.info("API response verified successfully for TCID: {}", currentAPITestCase.getTCID());
    }

    private void storeResponseValues() {
        List<String> saveFields = currentAPITestCase.getSaveFields();
        if (saveFields != null && !saveFields.isEmpty()) {
            apiSteps.storeResponseValue(saveFields);
            logger.info("Stored response values for fields: {}", saveFields);
        }
    }

    private Map<String, String> parseKeyValuePairs(List<String> pairs) {
        Map<String, String> result = new HashMap<>();
        if (pairs != null) {
            pairs.forEach(pair -> {
                String[] keyValue = pair.split("[:=]", 2);
                if (keyValue.length == 2) {
                    result.put(keyValue[0].trim(), keyValue[1].trim());
                } else {
                    logger.warn("Invalid key-value pair: {}", pair);
                }
            });
        }
        return result;
    }
}
