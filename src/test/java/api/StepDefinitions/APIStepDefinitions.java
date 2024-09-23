package api.StepDefinitions;

import api.StepDetails.APISteps;
import io.cucumber.java.en.*;
import net.serenitybdd.annotations.Steps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIStepDefinitions {
    private static final Logger logger = LoggerFactory.getLogger(APIStepDefinitions.class);

    @Steps
    APISteps apiSteps;

    @Given("I am working on the {string} project")
    public void iAmWorkingOnTheProject(String project) {
        apiSteps.setProject(project);
    }

    @And("I am using the {string} environment")
    public void iAmUsingTheEnvironment(String environment) {
        apiSteps.setEnvironment(environment);
    }

    @Given("I have test cases for {string}")
    public void iHaveTestDataFor(String tcid) {
        apiSteps.loadTestCase(tcid);
    }

    @When("I execute the API request")
    public void iExecuteTheAPIRequest() {
        apiSteps.executeAPIRequest();
    }

    @Then("I verify the API response")
    public void iVerifyTheAPIResponse() {
        apiSteps.verifyAPIResponse();
    }

    @And("I store the response value")
    public void iStoreTheResponseValue() {
        apiSteps.storeResponseValues();
    }
}