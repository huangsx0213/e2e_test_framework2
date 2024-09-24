package api.util;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class ConfigManager {

    private static volatile ConfigManager instance;

    private Map<String, Object> apiEndpointConfig;
    private String currentEnvironment;
    private String currentProject;

    // 私有构造函数，调用loadConfigs()加载配置
    private ConfigManager() {

    }

    // 单例模式的获取实例方法
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    // 加载配置文件
    private void loadConfigs() {
        String projectConfigPath = String.format("/config/%s/api-endpoint-config.yaml", currentProject);
        apiEndpointConfig = loadConfig(projectConfigPath);
    }

    // 抽取加载配置逻辑，重用代码
    private Map<String, Object> loadConfig(String filePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from " + filePath, e);
        }
    }

    // 设置环境变量
    public void setEnvironment(String environment) {
        if (!apiEndpointConfig.containsKey(environment)) {
            throw new IllegalArgumentException("Invalid environment: " + environment);
        }
        this.currentEnvironment = environment;
    }

    // 设置项目名称
    public void setProject(String project) {
        this.currentProject = project;
        loadConfigs();
    }

    // 获取API端点URL
    public String getEndpointUrl(String key) {
        return getEndpointProperty(key, "url");
    }

    // 获取API端点方法
    public String getEndpointMethod(String key) {
        return getEndpointProperty(key, "method");
    }

    // 从配置中获取具体的属性（如url, method）
    private String getEndpointProperty(String key, String property) {
        checkEnvironmentSet();
        Map<String, Object> endpoints = getEndpointsForEnvironment();
        Map<String, Object> endpoint = (Map<String, Object>) endpoints.get(key);

        if (endpoint == null || !endpoint.containsKey(property)) {
            throw new IllegalArgumentException("Endpoint or property not found for key: " + key);
        }

        return (String) endpoint.get(property);
    }

    // 获取当前环境的所有端点
    private Map<String, Object> getEndpointsForEnvironment() {
        Map<String, Object> environmentConfig = (Map<String, Object>) apiEndpointConfig.get(currentEnvironment);
        return (Map<String, Object>) environmentConfig.get("endpoints");
    }

    // 获取当前环境
    public String getCurrentEnvironment() {
        checkEnvironmentSet();
        return currentEnvironment;
    }

    // 获取当前项目
    public String getCurrentProject() {
        checkProjectSet();
        return currentProject;
    }

    // 检查环境是否设置
    private void checkEnvironmentSet() {
        if (currentEnvironment == null) {
            throw new IllegalStateException("Environment not set. Call setEnvironment() before accessing configurations.");
        }
    }

    // 检查项目是否设置
    private void checkProjectSet() {
        if (currentProject == null) {
            throw new IllegalStateException("Project not set. Call setProject() before accessing configurations.");
        }
    }
}
