package api.validation;

import api.model.APITestCase;
import api.model.HttpResponse;
import api.request.HttpRequestBuilder;
import api.config.ConfigManager;
import api.exception.TestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DynamicValidator {
    private static final Logger logger = LoggerFactory.getLogger(DynamicValidator.class);
    private static final ConfigManager configManager = ConfigManager.getInstance();

    public static void validate(APITestCase validationTestCase, APITestCase currentTestCase, Runnable executeMainRequest) {
        logger.info("Starting dynamic validation using TCID: {}", validationTestCase.getTCID());
        logger.debug("Initial state fetching by {}", validationTestCase.getTCID());
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(configManager);

        // Execute validation request before main request
        HttpResponse beforeResponse = executeValidationRequest(requestBuilder, validationTestCase);
        beforeResponse.logResponse();
        logger.debug("Initial state fetched");

        // Execute main request
        logger.debug("Executing main request for {}",currentTestCase.getTCID());
        executeMainRequest.run();
        logger.debug("Main request executed");

        // Execute validation request after main request
        logger.debug("Final state fetching by {}", validationTestCase.getTCID());
        HttpResponse afterResponse = executeValidationRequest(requestBuilder, validationTestCase);
        afterResponse.logResponse();
        logger.debug("Final state fetched");

        // Validate expected changes
        validateChanges(beforeResponse, afterResponse, currentTestCase.getDynamicValidationExpectedChanges());

        logger.info("Dynamic validation completed successfully");
    }

    private static HttpResponse executeValidationRequest(HttpRequestBuilder requestBuilder, APITestCase testCase) {
        requestBuilder.setEndpoint(testCase.getEndpointKey())
                .setHeadersTemplate(testCase.getHeadersTemplateKey())
                .setHeaderOverride(parseKeyValuePairs(testCase.getHeaderOverride()))
                .setBodyTemplate(testCase.getBodyTemplateKey())
                .setBodyOverride(parseKeyValuePairs(testCase.getBodyOverride()));

        return new HttpResponse(requestBuilder.execute());
    }

    private static void validateChanges(HttpResponse beforeResponse, HttpResponse afterResponse, Map<String, String> expectedChanges) {
        for (Map.Entry<String, String> entry : expectedChanges.entrySet()) {
            String field = entry.getKey();
            String expectedChange = entry.getValue();

            Object beforeValue = beforeResponse.jsonPath().get(field);
            Object afterValue = afterResponse.jsonPath().get(field);

            logger.debug("Validating field: {}. Before value: {}, After value: {}, Expected change: {}",
                    field, beforeValue, afterValue, expectedChange);

            if (expectedChange.startsWith("+")) {
                validateIncrease(field, beforeValue, afterValue, expectedChange);
            } else if (expectedChange.startsWith("-")) {
                validateDecrease(field, beforeValue, afterValue, expectedChange);
            } else {
                validateExactMatch(field, afterValue, expectedChange);
            }
        }
    }

    private static void validateIncrease(String field, Object beforeValue, Object afterValue, String expectedChange) {
        float change = Float.parseFloat(expectedChange.substring(1));
        float after = Float.parseFloat(afterValue.toString());
        float before = Float.parseFloat(beforeValue.toString());
        assert after == before + change : "Expected " + field + " to increase by " + change + ", but it changed from " + beforeValue + " to " + afterValue;
        logger.debug("Field {} increased by {} as expected", field, expectedChange);
    }

    private static void validateDecrease(String field, Object beforeValue, Object afterValue, String expectedChange) {
        float change = Float.parseFloat(expectedChange.substring(1));
        float after = Float.parseFloat(afterValue.toString());
        float before = Float.parseFloat(beforeValue.toString());
        assert after == before - change : "Expected " + field + " to decrease by " + change + ", but it changed from " + beforeValue + " to " + afterValue;
        logger.debug("Field {} decreased by {} as expected", field, expectedChange);
    }

    private static void validateExactMatch(String field, Object afterValue, String expectedChange) {
        if (!afterValue.toString().equals(expectedChange)) {
            throw new TestException.ResponseValidationException(
                    String.format("Expected %s to be %s, but it was %s",
                            field, expectedChange, afterValue));
        }
    }

    private static Map<String, String> parseKeyValuePairs(List<String> pairs) {
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