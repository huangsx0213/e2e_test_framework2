package api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;

public class APIResponseConverter {
    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static String convertXmlToJson(String xml) {
        try {
            JsonNode jsonNode = xmlMapper.readTree(xml);
            return jsonMapper.writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert XML to JSON", e);
        }
    }
}