package api.validation;

import api.model.ApiResponse;
import api.exception.ApiTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CompareStateStrategy implements ValidationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CompareStateStrategy.class);

    private final Map<String, Object> initialState;
    private final Map<String, String> expectedChanges;

    public CompareStateStrategy(Map<String, Object> initialState, Map<String, String> expectedChanges) {
        this.initialState = initialState;
        this.expectedChanges = expectedChanges;
    }

    @Override
    public void validate(ApiResponse response) throws ApiTestException.ResponseValidationException {
        logger.info("Starting state comparison validation");
        for (Map.Entry<String, String> entry : expectedChanges.entrySet()) {
            String field = entry.getKey();
            String expectedChange = entry.getValue();

            Object initialValue = initialState.get(field);
            Object finalValue = response.jsonPath().get(field);

            logger.debug("Validating field: {}. Initial value: {}, Final value: {}, Expected change: {}",
                    field, initialValue, finalValue, expectedChange);

            if (expectedChange.startsWith("+")) {
                validateIncrease(field, initialValue, finalValue, expectedChange);
            } else if (expectedChange.startsWith("-")) {
                validateDecrease(field, initialValue, finalValue, expectedChange);
            } else {
                validateExactMatch(field, finalValue, expectedChange);
            }
        }
        logger.info("State comparison validation completed successfully");
    }

    private void validateIncrease(String field, Object initialValue, Object finalValue, String expectedChange) {
        int change = Integer.parseInt(expectedChange.substring(1));
        if (!((Integer) finalValue).equals(((Integer) initialValue) + change)) {
            throw new ApiTestException.ResponseValidationException(
                    String.format("Expected %s to increase by %d, but it changed from %s to %s",
                            field, change, initialValue, finalValue));
        }
    }

    private void validateDecrease(String field, Object initialValue, Object finalValue, String expectedChange) {
        int change = Integer.parseInt(expectedChange.substring(1));
        if (!((Integer) finalValue).equals(((Integer) initialValue) - change)) {
            throw new ApiTestException.ResponseValidationException(
                    String.format("Expected %s to decrease by %d, but it changed from %s to %s",
                            field, change, initialValue, finalValue));
        }
    }

    private void validateExactMatch(String field, Object finalValue, String expectedChange) {
        if (!finalValue.toString().equals(expectedChange)) {
            throw new ApiTestException.ResponseValidationException(
                    String.format("Expected %s to be %s, but it was %s",
                            field, expectedChange, finalValue));
        }
    }
}