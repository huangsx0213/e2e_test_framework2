package api.validation;

import api.model.ApiResponse;
import api.request.HttpRequestBuilder;
import api.exception.ApiTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FetchReferenceStateStrategy implements ValidationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(FetchReferenceStateStrategy.class);

    private final HttpRequestBuilder requestBuilder;
    private Map<String, Object> state;

    public FetchReferenceStateStrategy(HttpRequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    @Override
    public void validate(ApiResponse response) throws ApiTestException.ResponseValidationException {
        logger.info("Fetching reference state from: {}", requestBuilder.getEndpoint());
        try {
            ApiResponse referenceResponse = new ApiResponse(requestBuilder.execute());
            state = referenceResponse.jsonPath().getMap("");
            logger.info("Reference state fetched successfully");
        } catch (Exception e) {
            throw new ApiTestException.ResponseValidationException("Failed to fetch reference state", e);
        }
    }

    public Map<String, Object> getState() {
        if (state == null) {
            throw new IllegalStateException("Reference state not fetched yet");
        }
        return state;
    }
}