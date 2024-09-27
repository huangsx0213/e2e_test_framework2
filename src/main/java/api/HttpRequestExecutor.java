package api;

import api.model.APITestCase;
import api.model.HttpResponse;
import api.util.ConfigManager;
import api.util.Utils;

public class HttpRequestExecutor {
    private final ConfigManager configManager;

    public HttpRequestExecutor(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public HttpResponse prepareAndSendRequest(APITestCase testCase) {
        HttpRequestBuilder requestBuilder = createRequestBuilder(testCase);
        HttpResponse response = new HttpResponse(requestBuilder.execute());
        response.logResponse();
        return response;
    }

    private HttpRequestBuilder createRequestBuilder(APITestCase testCase) {
        return new HttpRequestBuilder(configManager)
                .setEndpoint(testCase.getEndpointKey())
                .setHeadersTemplate(testCase.getHeadersTemplateKey())
                .setHeaderOverride(Utils.parseKeyValuePairs(testCase.getHeaderOverride()))
                .setBodyTemplate(testCase.getBodyTemplateKey())
                .setBodyOverride(Utils.parseKeyValuePairs(testCase.getBodyOverride()))
                .setRelaxedHTTPSValidation();
    }
}