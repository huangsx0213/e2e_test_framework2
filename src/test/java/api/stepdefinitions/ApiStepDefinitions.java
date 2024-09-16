package api.stepdefinitions;

import api.steps.UserApiSteps;
import io.cucumber.java.en.*;
import net.serenitybdd.annotations.Steps;

import java.util.Map;

public class ApiStepDefinitions {

    @Steps
    UserApiSteps apiSteps;

    @Given("I am using the {string} environment")
    public void iAmUsingTheEnvironment(String environment) {
        apiSteps.setEnvironment(environment);
    }

    @Given("I am working on the {string} project")
    public void iAmWorkingOnTheProject(String project) {
        apiSteps.setProject(project);
    }

    @Given("I have a {string} request to {string}")
    public void iHaveARequestTo(String method, String endpoint) {
        apiSteps.prepareRequest(method, endpoint);
    }

    @And("I set the request body using template {string} with:")
    public void iSetTheRequestBodyUsingTemplateWith(String templateKey, Map<String, String> data) throws Exception {
        apiSteps.setRequestBody(templateKey, data);
    }

    @And("I set the headers using template {string} with:")
    public void iSetTheHeadersUsingTemplateWith(String templateKey, Map<String, String> data) throws Exception {
        apiSteps.setRequestHeaders(templateKey, data);
    }

    @And("I set up dynamic validation with reference API endpoint {string}")
    public void iSetUpDynamicValidationWithReferenceAPIEndpoint(String referenceEndpoint) {
        apiSteps.setUpDynamicValidation(referenceEndpoint);
    }

    @When("I send the request with dynamic validation expecting:")
    public void iSendTheRequestWithDynamicValidationExpecting(Map<String, String> expectedChanges) {
        apiSteps.sendRequestWithDynamicValidation(expectedChanges);
    }

    @When("I send the request")
    public void iSendTheRequest() {
        apiSteps.sendRequest();
    }
    @Then("The response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        apiSteps.verifyResponseStatusCode(expectedStatusCode);
    }


    @And("the response should contain:")
    public void theResponseShouldContain(Map<String, String> expectedData) {
        apiSteps.verifyResponseContent(expectedData);
    }


}