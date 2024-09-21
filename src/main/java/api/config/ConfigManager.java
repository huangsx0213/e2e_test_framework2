package api.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class ConfigManager {
    private static final String API_ENDPOINT_CONFIG_FILE = "/config/api-endpoint-config.yaml";
    private static final String PROJECT_TEMPLATE_CONFIG_FILE = "/config/project-template-config.yaml";
    private static volatile ConfigManager instance;
    private Map<String, Object> apiEndpointConfig;
    private Map<String, Object> projectTemplateConfig;
    private String currentEnvironment;
    private String currentProject;

    private ConfigManager() {
        loadConfigs();
    }

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

    private void loadConfigs() {
        try (InputStream apiInputStream = getClass().getResourceAsStream(API_ENDPOINT_CONFIG_FILE);
             InputStream projInputStream = getClass().getResourceAsStream(PROJECT_TEMPLATE_CONFIG_FILE)) {
            Yaml yaml = new Yaml();
            apiEndpointConfig = yaml.load(apiInputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configurations", e);
        }
    }

    public void setEnvironment(String environment) {
        if (!apiEndpointConfig.containsKey(environment)) {
            throw new IllegalArgumentException("Invalid environment: " + environment);
        }
        currentEnvironment = environment;
    }

    public void setProject(String project) {
        currentProject = project;
    }

    public String getEndpointUrl(String key) {
        checkEnvironmentSet();
        Map<String, Object> env = (Map<String, Object>) apiEndpointConfig.get(currentEnvironment);
        Map<String, Object> endpoints = (Map<String, Object>) env.get("endpoints");
        Map<String, Object> endpoint = (Map<String, Object>) endpoints.get(key);
        return (String) endpoint.get("url");
    }

    public String getEndpointMethod(String key) {
        checkEnvironmentSet();
        Map<String, Object> env = (Map<String, Object>) apiEndpointConfig.get(currentEnvironment);
        Map<String, Object> endpoints = (Map<String, Object>) env.get("endpoints");
        Map<String, Object> endpoint = (Map<String, Object>) endpoints.get(key);
        return (String) endpoint.get("method");
    }


    public String getCurrentEnvironment() {
        return currentEnvironment;
    }

    public String getCurrentProject() {
        return currentProject;
    }

    private void checkEnvironmentSet() {
        if (currentEnvironment == null) {
            throw new IllegalStateException("Environment not set. Call setEnvironment() before accessing endpoint configurations.");
        }
    }

    private void checkProjectSet() {
        if (currentProject == null) {
            throw new IllegalStateException("Project not set. Call setProject() before accessing template configurations.");
        }
    }
}