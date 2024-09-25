package api.StepDefinitions;

import api.StepDetails.APISteps;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import net.serenitybdd.annotations.Steps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Steps
    private APISteps apiSteps;

    @Before
    public void beforeScenario(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());
    }

    @After
    public void afterScenario(Scenario scenario) {
        logger.info("Executing teardown for scenario: {}", scenario.getName());
        try {
            apiSteps.executeTeardownTestCases();
            logger.info("Teardown completed successfully");
        } catch (Exception e) {
            logger.error("Error during teardown execution", e);
        }
    }
}