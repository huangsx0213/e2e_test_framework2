package api.validation;

public interface ValidationStrategy {
    void execute(Object... args);
}