package api.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;


public class ConfigManager {
    private static final String CONFIG_FILE = "/config/config.yaml";
    private static Map<String, Object> config;
    private static String currentEnvironment;
    private static String currentProject;

    static {
        loadConfig();
//        setEnvironment("dev");
//        setProject("default");
    }

    private static void loadConfig() {
        try (InputStream inputStream = ConfigManager.class.getResourceAsStream(CONFIG_FILE)) {
            Yaml yaml = new Yaml();
            config = yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static void setEnvironment(String environment) {
        if (!getEnvironments().containsKey(environment)) {
            throw new IllegalArgumentException("Invalid environment: " + environment);
        }
        currentEnvironment = environment;
    }

    public static void setProject(String project) {
        if (!getProjects().containsKey(project)) {
            throw new IllegalArgumentException("Invalid project: " + project);
        }
        currentProject = project;
    }

    public static String getCurrentEnvironment() {
        return currentEnvironment;
    }

    public static String getCurrentProject() {
        return currentProject;
    }

    public static Map<String, Object> getEnvironments() {
        return (Map<String, Object>) config.get("environments");
    }

    public static Map<String, Object> getProjects() {
        return (Map<String, Object>) config.get("projects");
    }

    public static String getEndpoint(String key) {
        Map<String, Object> env = (Map<String, Object>) getEnvironments().get(currentEnvironment);
        Map<String, Object> api = (Map<String, Object>) env.get("api");
        Map<String, Object> endpoints = (Map<String, Object>) api.get("endpoints");
        return (String) endpoints.get(key);
    }
    public static String getBaseUrl() {
        Map<String, Object> env = (Map<String, Object>) getEnvironments().get(currentEnvironment);
        Map<String, Object> api = (Map<String, Object>) env.get("api");
        return (String) api.get("base_url");
    }

    public static String getBodyTemplate(String key) {
        Map<String, Object> project = (Map<String, Object>) getProjects().get(currentProject);
        Map<String, Object> templates = (Map<String, Object>) project.get("body_templates");
        return (String) templates.get(key);
    }

    public static String getHeaderTemplate(String key) {
        Map<String, Object> project = (Map<String, Object>) getProjects().get(currentProject);
        Map<String, Object> templates = (Map<String, Object>) project.get("header_templates");
        return (String) templates.get(key);
    }

    public static Object getConfigValue(String key) {
        Map<String, Object> env = (Map<String, Object>) getEnvironments().get(currentEnvironment);
        return env.get(key);
    }
}