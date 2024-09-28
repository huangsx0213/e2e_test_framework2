package api;

import api.model.TestContext;
import api.util.TestDataGenerator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class APIRequestTemplateProcessor {
    private static final Logger logger = LoggerFactory.getLogger(APIRequestTemplateProcessor.class);
    private static final Configuration configuration;

    static {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        updateTemplateDirectory();
    }

    public static void updateTemplateDirectory() {
        String project = APIConfigManager.getInstance().getCurrentProject();
        configuration.setClassForTemplateLoading(APIRequestTemplateProcessor.class, "/templates/" + project);
    }

    private APIRequestTemplateProcessor() {
        // Private constructor to prevent instantiation
    }

    /**
     * Renders a template with the given name and data, using saved fields from TestContext.
     *
     * @param templateName The name of the template to render
     * @param data         A map containing the data to be used in the template
     * @return The rendered template as a string
     * @throws TestException if template rendering fails
     */
    public static String renderTemplate(String templateName, Map<String, String> data) throws TestException {
        if (templateName == null || templateName.trim().isEmpty()) {
            logger.warn("Template name is null or empty, returning empty string");
            return ""; // Return an empty string
        }

        try (StringWriter writer = new StringWriter()) {
            Template template = configuration.getTemplate(templateName);
            Map<String, Object> dynamicData = new HashMap<>();

            // Get saved fields from TestContext
            Map<String, String> savedFields = TestContext.getInstance().getAllDataAsString();

            // Generate dynamic data for each entry in the data map
            for (Map.Entry<String, String> entry : data.entrySet()) {
                dynamicData.put(entry.getKey(), TestDataGenerator.generateDynamicData(entry.getValue(), savedFields));
                logger.debug("Generated dynamic data for '{}': {}", entry.getKey(), dynamicData.get(entry.getKey()));
            }

            template.process(dynamicData, writer);
            String renderedContent = writer.toString();
            logger.debug("Template '{}' rendered successfully", templateName);
            return renderedContent;
        } catch (IOException | TemplateException e) {
            logger.error("Failed to render template: {}", templateName, e);
            throw new TestException("Template rendering failed", e);
        }
    }

    /**
     * Parses a string containing headers into a map.
     *
     * @param headerString A string containing headers, with each header on a new line
     * @return A map of header names to header values
     */
    public static Map<String, String> parseHeaderString(String headerString) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headerString.split("\n");
        for (String line : lines) {
            int separatorIndex = line.indexOf(':');
            if (separatorIndex > 0) {
                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();
                headers.put(key, value);
            } else {
                logger.warn("Invalid header line: {}", line);
            }
        }
        logger.debug("Parsed {} headers", headers.size());
        return headers;
    }

    public static String templateType(String templateName) {
        if (templateName == null || templateName.trim().isEmpty()) {
            return null; // 返回 null 表示没有特定的 Content-Type
        }
        if (templateName.contains("xml")) {
            return "xml";
        } else if (templateName.contains("json")) {
            return "json";
        } else {
            return "text/plain";
        }
    }
}