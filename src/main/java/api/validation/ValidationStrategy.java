package api.validation;

import api.model.ApiResponse;
import api.exception.ApiTestException;

public interface ValidationStrategy {
    void validate(ApiResponse response) throws ApiTestException.ResponseValidationException;
}