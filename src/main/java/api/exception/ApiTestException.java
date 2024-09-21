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

        public RequestPreparationException(String message, Throwable cause) {
            super("Error preparing request: " + message, cause);
        }
    }

    public static class RequestExecutionException extends ApiTestException {
        public RequestExecutionException(String message) {
            super("Error executing request: " + message);
        }

        public RequestExecutionException(String message, Throwable cause) {
            super("Error executing request: " + message, cause);
        }
    }

    public static class ResponseValidationException extends ApiTestException {
        public ResponseValidationException(String message) {
            super("Error validating response: " + message);
        }

        public ResponseValidationException(String message, Throwable cause) {
            super("Error validating response: " + message, cause);
        }
    }

    public static class ConfigurationException extends ApiTestException {
        public ConfigurationException(String message) {
            super("Configuration error: " + message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super("Configuration error: " + message, cause);
        }
    }
}