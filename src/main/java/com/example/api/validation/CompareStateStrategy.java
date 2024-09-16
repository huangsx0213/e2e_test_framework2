package com.example.api.validation;

import java.util.Map;

public class CompareStateStrategy implements ValidationStrategy {
    @Override
    public void execute(Object... args) {
        Map<String, Object> initialState = (Map<String, Object>) args[0];
        Map<String, Object> finalState = (Map<String, Object>) args[1];
        Map<String, String> expectedChanges = (Map<String, String>) args[2];

        for (Map.Entry<String, String> entry : expectedChanges.entrySet()) {
            String field = entry.getKey();
            String expectedChange = entry.getValue();

            Object initialValue = initialState.get(field);
            Object finalValue = finalState.get(field);

            if (expectedChange.startsWith("+")) {
                int change = Integer.parseInt(expectedChange.substring(1));
                assert (Integer) finalValue == (Integer) initialValue + change :
                        "Expected " + field + " to increase by " + change;
            } else if (expectedChange.startsWith("-")) {
                int change = Integer.parseInt(expectedChange.substring(1));
                assert (Integer) finalValue == (Integer) initialValue - change :
                        "Expected " + field + " to decrease by " + change;
            } else {
                assert finalValue.equals(expectedChange) :
                        "Expected " + field + " to be " + expectedChange;
            }
        }
    }
}