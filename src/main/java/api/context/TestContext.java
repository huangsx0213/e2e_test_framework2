package api.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public void clearContext() {
        contextData.clear();
        logger.debug("Cleared test context");
    }

    public void removeData(String key) {
        Object removedValue = contextData.remove(key);
        if (removedValue != null) {
            logger.debug("Removed context data: {} = {}", key, removedValue);
        } else {
            logger.warn("Attempted to remove non-existent context data: {}", key);
        }
    }

    public boolean containsKey(String key) {
        boolean contains = contextData.containsKey(key);
        logger.debug("Context contains key {}: {}", key, contains);
        return contains;
    }

    public Map<String, Object> getAllData() {
        return new ConcurrentHashMap<>(contextData);
    }

    public void mergeContext(Map<String, Object> newData) {
        contextData.putAll(newData);
        logger.debug("Merged {} new items into context", newData.size());
    }

    public void setIfAbsent(String key, Object value) {
        Object oldValue = contextData.putIfAbsent(key, value);
        if (oldValue == null) {
            logger.debug("Set context data (if absent): {} = {}", key, value);
        } else {
            logger.debug("Context data already exists: {} = {}", key, oldValue);
        }
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = contextData.getOrDefault(key, defaultValue);
        logger.debug("Get context data (or default): {} = {}", key, value);
        @SuppressWarnings("unchecked")
        T result = (T) value;
        return result;
    }
}