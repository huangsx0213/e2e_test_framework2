package com.example.api.context;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    private static final ThreadLocal<TestContext> instance = new ThreadLocal<>();
    private final Map<String, Object> contextData;

    private TestContext() {
        contextData = new HashMap<>();
    }

    public static TestContext getInstance() {
        if (instance.get() == null) {
            instance.set(new TestContext());
        }
        return instance.get();
    }

    public void setData(String key, Object value) {
        contextData.put(key, value);
    }

    public Object getData(String key) {
        return contextData.get(key);
    }

    public void clearContext() {
        contextData.clear();
    }
}