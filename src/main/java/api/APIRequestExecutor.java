package api;

import api.model.APIResponse;
import api.model.APITestCase;
import api.util.Utils;

public class APIRequestExecutor {
    private final APIConfigManager APIConfigManager;

    public APIRequestExecutor(APIConfigManager APIConfigManager) {
        this.APIConfigManager = APIConfigManager;
    }

    public APIResponse prepareAndSendRequest(APITestCase testCase) {
        APIRequestBuilder requestBuilder = createRequestBuilder(testCase);
        APIResponse response = new APIResponse(requestBuilder.execute());
        response.logResponse();
        return response;
    }

    private APIRequestBuilder createRequestBuilder(APITestCase testCase) {
        return new APIRequestBuilder(APIConfigManager)
                .setEndpoint(testCase.getEndpointKey())
                .setHeadersTemplate(testCase.getHeadersTemplateKey())
                .setHeaderOverride(Utils.parseKeyValuePairs(testCase.getHeaderOverride()))
                .setBodyTemplate(testCase.getBodyTemplateKey())
                .setBodyOverride(Utils.parseKeyValuePairs(testCase.getBodyOverride()))
                .setQueryParams(Utils.parseKeyValuePairs(testCase.getQueryParams()))
                .setPathParams(Utils.parseKeyValuePairs(testCase.getPathParams()))
                .setRelaxedHTTPSValidation();
    }
}