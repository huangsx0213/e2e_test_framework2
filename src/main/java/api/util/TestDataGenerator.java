package api.util;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDataGenerator {
    private static final Faker faker = new Faker(new Locale("en-US"));
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public static String generateDynamicData(String template, Map<String, String> savedFields) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = generateReplacement(variable, savedFields);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static String generateReplacement(String variable, Map<String, String> savedFields) {
        // Check if it's a saved field
        if (variable.contains(".")) {
            return getSavedFieldValue(variable, savedFields);
        }

        // Generate dynamic data based on the variable name
        switch (variable) {
            case "env":
                return faker.options().option("dev", "test", "prod");
            case "status":
                return faker.options().option("Active");
            case "randomName":
                return faker.name().fullName();
            case "randomEmail":
                return faker.internet().emailAddress();
            case "randomNumber":
                return String.valueOf(faker.number().numberBetween(1, 1000));
            default:
                return "${" + variable + "}";
        }
    }

    private static String getSavedFieldValue(String variable, Map<String, String> savedFields) {
        return savedFields.getOrDefault(variable, "${" + variable + "}");
    }
}