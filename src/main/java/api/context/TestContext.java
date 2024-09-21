package api.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        logger.debug("Set context cases: {} = {}", key, value);
    }

    public Object getData(String key) {
        Object value = contextData.get(key);
        logger.debug("Get context cases: {} = {}", key, value);
        return value;
    }

    public void clearContext() {
        contextData.clear();
        logger.debug("Cleared test context");
    }

    public void removeData(String key) {
        contextData.remove(key);
        logger.debug("Removed context cases: {}", key);
    }

    public boolean containsKey(String key) {
        return contextData.containsKey(key);
    }

    public Map<String, Object> getAllData() {
        return new ConcurrentHashMap<>(contextData);
    }
}