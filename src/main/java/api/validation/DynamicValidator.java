package api.validation;

import api.model.ApiResponse;
import api.request.HttpRequestBuilder;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DynamicValidator {
    private static final Logger logger = LoggerFactory.getLogger(DynamicValidator.class);

    public static void validate(String endpoint, Map<String, String> expectedChanges, HttpRequestBuilder requestBuilder) {
        logger.info("Starting dynamic validation for endpoint: {}", endpoint);

        // 执行请求前获取初始状态
        ApiResponse initialResponse = new ApiResponse(requestBuilder.setEndpoint(endpoint).execute());
        JsonPath initialJson = new JsonPath(initialResponse.getBodyAsString());
        logger.debug("Initial state fetched");

        // 执行主请求
        requestBuilder.execute();
        logger.debug("Main request executed");

        // 执行请求后再次获取状态
        ApiResponse finalResponse = new ApiResponse(requestBuilder.setEndpoint(endpoint).execute());
        JsonPath finalJson = new JsonPath(finalResponse.getBodyAsString());
        logger.debug("Final state fetched");

        // 验证预期的变化
        for (Map.Entry<String, String> entry : expectedChanges.entrySet()) {
            String field = entry.getKey();
            String expectedChange = entry.getValue();

            Object initialValue = initialJson.get(field);
            Object finalValue = finalJson.get(field);

            logger.debug("Validating field: {}. Initial value: {}, Final value: {}, Expected change: {}",
                    field, initialValue, finalValue, expectedChange);

            if (expectedChange.startsWith("+")) {
                int change = Integer.parseInt(expectedChange.substring(1));
                assert (Integer) finalValue == (Integer) initialValue + change :
                        String.format("Expected %s to increase by %d, but it changed from %s to %s",
                                field, change, initialValue, finalValue);
            } else if (expectedChange.startsWith("-")) {
                int change = Integer.parseInt(expectedChange.substring(1));
                assert (Integer) finalValue == (Integer) initialValue - change :
                        String.format("Expected %s to decrease by %d, but it changed from %s to %s",
                                field, change, initialValue, finalValue);
            } else {
                assert finalValue.toString().equals(expectedChange) :
                        String.format("Expected %s to be %s, but it was %s",
                                field, expectedChange, finalValue);
            }

            logger.info("Validation passed for field: {}", field);
        }

        logger.info("Dynamic validation completed successfully");
    }
}