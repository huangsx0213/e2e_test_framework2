package api.template;

import api.config.ConfigManager;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class TemplateProcessor {
    private static final Configuration configuration;

    static {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(TemplateProcessor.class,"/templates");
    }

    public static String processTemplate(String templateName, Map<String, String> data) throws IOException, TemplateException {
        Template template = configuration.getTemplate(templateName);
        StringWriter writer = new StringWriter();

        Map<String, Object> templateData = new HashMap<>(data);
        templateData.put("env", ConfigManager.getCurrentEnvironment());
        templateData.put("project", ConfigManager.getCurrentProject());
        templateData.put("baseUrl", ConfigManager.getBaseUrl());

        template.process(templateData, writer);
        return writer.toString();
    }

    public static Map<String, String> parseHeaders(String headersString) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headersString.split("\n");
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                headers.put(parts[0].trim(), parts[1].trim());
            }
        }
        return headers;
    }
}