package api;

public class TestException extends RuntimeException {
    public TestException(String message) {
        super(message);
    }

    public TestException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class RequestPreparationException extends TestException {
        public RequestPreparationException(String message) {
            super("Error preparing request: " + message);
        }

        public RequestPreparationException(String message, Throwable cause) {
            super("Error preparing request: " + message, cause);
        }
    }

    public static class RequestExecutionException extends TestException {
        public RequestExecutionException(String message) {
            super("Error executing request: " + message);
        }

        public RequestExecutionException(String message, Throwable cause) {
            super("Error executing request: " + message, cause);
        }
    }

    public static class ResponseValidationException extends TestException {
        public ResponseValidationException(String message) {
            super("Error validating response: " + message);
        }

        public ResponseValidationException(String message, Throwable cause) {
            super("Error validating response: " + message, cause);
        }
    }

    public static class ConfigurationException extends TestException {
        public ConfigurationException(String message) {
            super("Configuration error: " + message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super("Configuration error: " + message, cause);
        }
    }
}