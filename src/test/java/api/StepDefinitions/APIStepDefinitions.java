package api.StepDefinitions;

import api.model.APITestCase;
import api.StepDetails.APISteps;
import api.config.ExcelDataReader;
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

    @Given("I have test cases for {string}")
    public void iHaveTestDataFor(String tcid) {
        loadTestCases();
        currentAPITestCase = findTestCaseByTCID(tcid);
    }

    @When("I execute the API request")
    public void iExecuteTheAPIRequest() {
        setupApiRequest();
        executeRequestOrValidate();
    }

    @Then("I verify the API response")
    public void iVerifyTheAPIResponse() {
        verifyApiResponse();
    }

    @And("I store the response value")
    public void iStoreTheResponseValue() {
        storeResponseValues();
    }

    @And("I am using the {string} environment")
    public void iAmUsingTheEnvironment(String environment) {
        apiSteps.setEnvironment(environment);
        logger.info("Environment set: {}", environment);
    }

    @Given("I am working on the {string} project")
    public void iAmWorkingOnTheProject(String project) {
        apiSteps.setProject(project);
        logger.info("Project set: {}", project);
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

    private void setupApiRequest() {
        apiSteps.prepareRequest(currentAPITestCase.getEndpointKey())
                .setRequestHeaders(currentAPITestCase.getHeadersTemplateKey(), parseKeyValuePairs(currentAPITestCase.getHeaderOverride()))
                .setRequestBody(currentAPITestCase.getBodyTemplateKey(), parseKeyValuePairs(currentAPITestCase.getBodyOverride()));
    }

    private void executeRequestOrValidate() {
        Optional<String> dynamicValidationEndpoint = Optional.ofNullable(currentAPITestCase.getDynamicValidationEndpoint());
        if (dynamicValidationEndpoint.isPresent() && !dynamicValidationEndpoint.get().isEmpty()) {
            DynamicValidator.validate(
                    dynamicValidationEndpoint.get(),
                    currentAPITestCase.getDynamicValidationExpectedChanges(),
                    apiSteps.getRequestBuilder()
            );
            logger.info("Dynamic validation performed for endpoint: {}", dynamicValidationEndpoint.get());
        } else {
            apiSteps.sendRequest();
            logger.info("API request executed for endpoint: {}", currentAPITestCase.getEndpointKey());
        }
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
