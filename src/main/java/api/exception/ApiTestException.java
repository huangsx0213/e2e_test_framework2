package api.exception;

public class ApiTestException extends RuntimeException {
    public ApiTestException(String message) {
        super(message);
    }

    public ApiTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class RequestPreparationException extends ApiTestException {
        public RequestPreparationException(String message) {
            super("Error preparing request: " + message);
        }
    }

    public static class RequestExecutionException extends ApiTestException {
        public RequestExecutionException(String message) {
            super("Error executing request: " + message);
        }
    }

    public static class ResponseValidationException extends ApiTestException {
        public ResponseValidationException(String message) {
            super("Error validating response: " + message);
        }
    }
}