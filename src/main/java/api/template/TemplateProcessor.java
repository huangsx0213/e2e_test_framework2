package api.template;

import api.config.ConfigManager;
import api.exception.ApiTestException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class TemplateProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TemplateProcessor.class);
    private static final Configuration configuration;

    static {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(TemplateProcessor.class, "/templates/" + ConfigManager.getInstance().getCurrentProject());
        logger.info("Template processor initialized for project: {}", ConfigManager.getInstance().getCurrentProject());
    }

    private TemplateProcessor() {
        // Private constructor to prevent instantiation
    }

    public static String renderTemplate(String templateName, Map<String, String> data) throws ApiTestException {
        try (StringWriter writer = new StringWriter()) {
            Template template = configuration.getTemplate(templateName);
            template.process(data, writer);
            String renderedContent = writer.toString();
            logger.debug("Template '{}' rendered successfully", templateName);
            return renderedContent;
        } catch (IOException | TemplateException e) {
            logger.error("Failed to render template: {}", templateName, e);
            throw new ApiTestException("Template rendering failed", e);
        }
    }

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

    public static void setTemplateDirectory(String directory) {
        try {
            configuration.setDirectoryForTemplateLoading(new java.io.File(directory));
            logger.info("Template directory set to: {}", directory);
        } catch (IOException e) {
            logger.error("Failed to set template directory: {}", directory, e);
            throw new ApiTestException("Failed to set template directory", e);
        }
    }

    public static String combineTemplates(String... templateNames) throws ApiTestException {
        StringBuilder combined = new StringBuilder();
        for (String templateName : templateNames) {
            combined.append(renderTemplate(templateName, new HashMap<>())).append("\n");
        }
        logger.debug("Combined {} templates", templateNames.length);
        return combined.toString().trim();
    }
}