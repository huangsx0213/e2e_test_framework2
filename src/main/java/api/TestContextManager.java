package api;

import api.model.APIResponse;
import api.model.TestContext;
import java.util.List;

public class TestContextManager {
    private final TestContext testContext;

    public TestContextManager() {
        this.testContext = TestContext.getInstance();
    }

    public void storeResponseValues(APIResponse response, List<String> keys) {
        if (keys != null) {
            keys.forEach(key -> {
                String field = key.substring(key.indexOf('.') + 1);
                if (!response.hasJsonPath(field)) {
                    throw new IllegalArgumentException("Invalid JSON path: " + field);
                }
                String value = response.jsonPath().getString(field);
                testContext.setData(key, value);
            });
        }
    }

    public void setPreValidationResponse(String tcid, APIResponse response) {
        testContext.setData("preValidationResponse_" + tcid, response);
    }

    public APIResponse getPreValidationResponse(String tcid) {
        return testContext.getData("preValidationResponse_" + tcid, APIResponse.class)
                .orElseThrow(() -> new IllegalStateException("Pre-validation response not found for TCID: " + tcid));
    }
}