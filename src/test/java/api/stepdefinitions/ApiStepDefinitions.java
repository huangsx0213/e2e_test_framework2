package api.stepdefinitions;

import api.model.TestCase;
import api.steps.ApiSteps;
import api.config.ExcelDataReader;
import api.validation.DynamicValidator;
import io.cucumber.java.en.*;
import net.serenitybdd.annotations.Steps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class ApiStepDefinitions {
    private static final Logger logger = LoggerFactory.getLogger(ApiStepDefinitions.class);

    @Steps
    ApiSteps apiSteps;

    private TestCase currentTestCase;
    private static List<TestCase> testCases;

    static {
        testCases = ExcelDataReader.readTestData("API");
        logger.info("Loaded {} test cases from Excel", testCases.size());
    }

    @Given("I have test cases for {string}")
    public void iHaveTestDataFor(String tcid) {
        currentTestCase = testCases.stream()
                .filter(tc -> tc.getTCID().equals(tcid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No test cases found for TCID: " + tcid));
        logger.info("Test case loaded for TCID: {}", tcid);
    }

    @When("I execute the API request")
    public void iExecuteTheAPIRequest() {
        apiSteps.prepareRequest(currentTestCase.getEndpointKey())
                .setRequestHeaders(currentTestCase.getHeadersTemplateKey(), parseOverride(currentTestCase.getHeaderOverride()))
                .setRequestBody(currentTestCase.getBodyTemplateKey(), parseOverride(currentTestCase.getBodyOverride()));

        if (currentTestCase.getDynamicValidationEndpoint() != null && !currentTestCase.getDynamicValidationEndpoint().isEmpty()) {
            DynamicValidator.validate(
                    currentTestCase.getDynamicValidationEndpoint(),
                    currentTestCase.getDynamicValidationExpectedChanges(),
                    apiSteps.getRequestBuilder()
            );
            logger.info("Performed dynamic validation for endpoint: {}", currentTestCase.getDynamicValidationEndpoint());
        } else {
            apiSteps.sendRequest();
        }

        logger.info("API request executed for endpoint: {}", currentTestCase.getEndpointKey());
    }

    @Then("I verify the API response")
    public void iVerifyTheAPIResponse() {
        apiSteps.verifyResponseStatusCode(currentTestCase.getExpStatus())
                .verifyResponseContent(parseExpectedResult(currentTestCase.getExpResult()));
        logger.info("API response verified successfully");
    }

    @And("I store the response value")
    public void iStoreTheResponseValue() {
        if (currentTestCase.getSaveFields() != null && !currentTestCase.getSaveFields().isEmpty()) {
            apiSteps.storeResponseValue(currentTestCase.getSaveFields());
            logger.info("Stored response values: {}", currentTestCase.getSaveFields());
        }
    }

    @Given("I am using the {string} environment")
    public void iAmUsingTheEnvironment(String environment) {
        apiSteps.setEnvironment(environment);
        logger.info("Using environment: {}", environment);
    }

    @And("I am working on the {string} project")
    public void iAmWorkingOnTheProject(String project) {
        apiSteps.setProject(project);
        logger.info("Working on project: {}", project);
    }

    private Map<String, String> parseOverride(List<String> override) {
        Map<String, String> result = new HashMap<>();
        if (override != null) {
            for (String pair : override) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    result.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return result;
    }

    private Map<String, String> parseExpectedResult(List<String> expResult) {
        Map<String, String> expected = new HashMap<>();
        if (expResult != null) {
            for (String pair : expResult) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    expected.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return expected;
    }
}