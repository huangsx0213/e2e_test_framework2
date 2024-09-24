package api.util;

import api.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ResponseValidator {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidator.class);

    public void verifyResponseStatusCode(HttpResponse response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        if (actualStatusCode != expectedStatusCode) {
            throw new AssertionError(String.format("Expected status code %d but got %d", expectedStatusCode, actualStatusCode));
        }
        logger.info("Verified response status code: {}", actualStatusCode);
    }

    public void verifyResponseContent(HttpResponse response, Map<String, String> expectedData,String currentACID) {
        expectedData.forEach((key, expectedValue) -> {
            if (!isDynamicField(key,currentACID)) {
                String field = key.substring(key.indexOf('.') + 1);
                String actualValue = response.jsonPath().getString(field);
                if (actualValue == null || !actualValue.equals(expectedValue)) {
                    throw new AssertionError(String.format("Expected %s to be %s but got %s", key, expectedValue, actualValue));
                }
                logger.info("Verified response field: {} = {}", key, actualValue);
            }
        });
    }

    private boolean isDynamicField(String key,String currentACID) {
        return key.contains(".") && !key.startsWith(currentACID);
    }
}