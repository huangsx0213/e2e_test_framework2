package api;

import api.model.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class StandardResponseValidator {
    private static final Logger logger = LoggerFactory.getLogger(StandardResponseValidator.class);

    public void verifyResponseStatusCode(APIResponse response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        if (actualStatusCode != expectedStatusCode) {
            throw new AssertionError(String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode));
        }
        logger.info("Verified response status code: {}", actualStatusCode);
    }

    public void verifyResponseContent(APIResponse response, Map<String, String> expectedData, String currentTCID) {
        expectedData.forEach((key, expectedValue) -> {
            if (!isDynamicField(key, currentTCID)) {
                verifyField(response, key, expectedValue);
            }
        });
    }

    private void verifyField(APIResponse response, String key, String expectedValue) {
        String field = key.substring(key.indexOf('.') + 1);
        String actualValue = response.jsonPath().getString(field);
        if (actualValue == null || !actualValue.equals(expectedValue)) {
            throw new AssertionError(String.format("Expected %s to be %s but got %s", key, expectedValue, actualValue));
        }
        logger.info("Verified response field: {} = {}", key, actualValue);
    }

    private boolean isDynamicField(String key, String currentTCID) {
        return key.contains(".") && !key.startsWith(currentTCID);
    }
}