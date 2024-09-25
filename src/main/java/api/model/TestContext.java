package api.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

public class TestContext {
    private static final Logger logger = LoggerFactory.getLogger(TestContext.class);
    private static final ThreadLocal<TestContext> instance = ThreadLocal.withInitial(TestContext::new);
    private final Map<String, Object> contextData;

    private TestContext() {
        this.contextData = new ConcurrentHashMap<>();
    }

    public static TestContext getInstance() {
        return instance.get();
    }

    public void setData(String key, Object value) {
        contextData.put(key, value);
        logger.debug("Set context data: {} = {}", key, value);
    }

    public <T> Optional<T> getData(String key, Class<T> type) {
        Object value = contextData.get(key);
        if (value != null && type.isInstance(value)) {
            logger.debug("Get context data: {} = {}", key, value);
            return Optional.of(type.cast(value));
        }
        logger.warn("Context data not found or type mismatch for key: {}", key);
        return Optional.empty();
    }

    public Map<String, Object> getAllData() {
        return new HashMap<>(contextData);
    }

    public Map<String, String> getAllDataAsString() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : contextData.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }
}