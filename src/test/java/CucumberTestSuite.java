package com.example.api;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.api.stepdefinitions", "com.example.api.hooks"},
        plugin = {"pretty", "html:target/cucumber-reports"},
        monochrome = true,
        dryRun = false
)
public class CucumberTestSuite {
}