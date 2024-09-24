package api;

import api.model.TestContext;
import api.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class ContextManager {
    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);
    private final TestContext testContext;

    public ContextManager() {
        this.testContext = TestContext.getInstance();
    }

    public void storeResponseValues(HttpResponse response, List<String> keys) {
        if (keys != null) {
            keys.forEach(key -> {
                String field = key.substring(key.indexOf('.') + 1);
                String value = response.jsonPath().getString(field);
                testContext.setData(key, value);
                logger.info("Stored response value: {} = {}", key, value);
            });
        }
    }

    public void setPreValidationResponse(String tcid, HttpResponse response) {
        testContext.setData("preValidationResponse_" + tcid, response);
    }

    public HttpResponse getPreValidationResponse(String tcid) {
        return testContext.getData("preValidationResponse_" + tcid, HttpResponse.class)
                .orElseThrow(() -> new IllegalStateException("Pre-validation response not found for TCID: " + tcid));
    }
}