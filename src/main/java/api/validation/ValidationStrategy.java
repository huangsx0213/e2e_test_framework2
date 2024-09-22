package api.validation;

import api.model.HttpResponse;
import api.exception.TestException;

public interface ValidationStrategy {
    void validate(HttpResponse response) throws TestException.ResponseValidationException;
}