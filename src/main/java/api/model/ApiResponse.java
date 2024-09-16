package api.model;

import io.restassured.response.Response;

public class ApiResponse {
    private final Response response;

    public ApiResponse(Response response) {
        this.response = response;
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public String getBodyAsString() {
        return response.getBody().asString();
    }

    public <T> T getBody(Class<T> clazz) {
        return response.getBody().as(clazz);
    }
}
