package api.util;

import api.config.ConfigManager;
import api.model.APITestCase;
import api.model.HttpResponse;
import api.request.HttpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    private final ConfigManager configManager;

    public RequestExecutor(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public HttpResponse prepareAndSendRequest(APITestCase testCase) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(configManager)
                .setEndpoint(testCase.getEndpointKey())
                .setHeadersTemplate(testCase.getHeadersTemplateKey())
                .setHeaderOverride(Utils.parseKeyValuePairs(testCase.getHeaderOverride()))
                .setBodyTemplate(testCase.getBodyTemplateKey())
                .setBodyOverride(Utils.parseKeyValuePairs(testCase.getBodyOverride()));

        HttpResponse response = new HttpResponse(requestBuilder.setRelaxedHTTPSValidation().execute());
        response.logResponse();
        return response;
    }
}