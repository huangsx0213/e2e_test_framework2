package api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static Map<String, String> parseKeyValuePairs(List<String> pairs) {
        Map<String, String> result = new HashMap<>();
        if (pairs != null) {
            pairs.forEach(pair -> {
                String[] keyValue = pair.split("[:=]", 2);
                if (keyValue.length == 2) {
                    result.put(keyValue[0].trim(), keyValue[1].trim());
                } else {
                    logger.warn("Invalid key-value pair: {}", pair);
                }
            });
        }
        return result;
    }

    public static String formatXml(String xml) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            logger.error("Failed to format XML", e);
            return xml;
        }
    }
}