package api;

import api.model.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class DynamicResponseValidator {
    private static final Logger logger = LoggerFactory.getLogger(DynamicResponseValidator.class);

    public static void validate(APIResponse beforeResponse, APIResponse afterResponse, Map<String, String> expectedChanges) {
        logger.info("Starting dynamic validation");
        for (Map.Entry<String, String> entry : expectedChanges.entrySet()) {
            String field = entry.getKey();
            String expectedChange = entry.getValue();

            Object beforeValue = beforeResponse.jsonPath().get(field);
            Object afterValue = afterResponse.jsonPath().get(field);

            logger.debug("Validating field: {}. Before value: {}, After value: {}, Expected change: {}", field, beforeValue, afterValue, expectedChange);

            if (expectedChange.startsWith("+")) {
                validateIncrease(field, beforeValue, afterValue, expectedChange);
            } else if (expectedChange.startsWith("-")) {
                validateDecrease(field, beforeValue, afterValue, expectedChange);
            } else {
                validateExactMatch(field, afterValue, expectedChange);
            }
        }
        logger.info("Dynamic validation completed successfully");
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
}