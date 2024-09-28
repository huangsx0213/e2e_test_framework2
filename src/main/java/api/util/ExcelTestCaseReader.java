package api.util;

import api.APIConfigManager;
import api.model.APITestCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelTestCaseReader {
    private static final Logger logger = LoggerFactory.getLogger(ExcelTestCaseReader.class);
    private static final Map<String, List<APITestCase>> cache = new HashMap<>();

    public static List<APITestCase> readTestData(String sheetName) {
        String project = APIConfigManager.getInstance().getCurrentProject();
        String excelFilePath = String.format("src/test/resources/cases/%s/api_test_cases.xlsx", project);
        if (cache.containsKey(sheetName)) {
            logger.info("Returning cached test cases for sheet: {}", sheetName);
            return cache.get(sheetName);
        }

        List<APITestCase> APITestCases = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = createHeaderMap(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    APITestCase APITestCase = createTestCase(row, headerMap);
                    if (APITestCase.isValid()) {
                        APITestCases.add(APITestCase);
                    } else {
                        logger.warn("Invalid test case at row {}: {}", i + 1, APITestCase);
                    }
                }
            }

            cache.put(sheetName, APITestCases);
            logger.info("Loaded {} valid test cases from sheet: {}", APITestCases.size(), sheetName);

        } catch (IOException e) {
            logger.error("Failed to read Excel file: {}", excelFilePath, e);
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return APITestCases;
    }

    private static Map<String, Integer> createHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = headerRow.getCell(i).getStringCellValue();
            headerMap.put(header, i);
        }
        return headerMap;
    }

    private static APITestCase createTestCase(Row row, Map<String, Integer> headerMap) {
        APITestCase APITestCase = new APITestCase();

        setTestCaseField(APITestCase, "TCID", getCellValueAsString(row, headerMap.get("TCID")));
        setTestCaseField(APITestCase, "Name", getCellValueAsString(row, headerMap.get("Name")));
        setTestCaseField(APITestCase, "Descriptions", getCellValueAsString(row, headerMap.get("Descriptions")));
        setTestCaseField(APITestCase, "Conditions", parseList(getCellValueAsString(row, headerMap.get("Conditions"))));
        setTestCaseField(APITestCase, "EndpointKey", getCellValueAsString(row, headerMap.get("Endpoint Key")));
        setTestCaseField(APITestCase, "HeadersTemplateKey", getCellValueAsString(row, headerMap.get("Headers Template Key")));
        setTestCaseField(APITestCase, "HeaderOverride", parseList(getCellValueAsString(row, headerMap.get("Header Override"))));
        setTestCaseField(APITestCase, "BodyTemplateKey", getCellValueAsString(row, headerMap.get("Body Template Key")));
        setTestCaseField(APITestCase, "BodyOverride", parseList(getCellValueAsString(row, headerMap.get("Body Override"))));
        setTestCaseField(APITestCase, "Run", "Y".equalsIgnoreCase(getCellValueAsString(row, headerMap.get("Run"))));
        setTestCaseField(APITestCase, "Tags", parseList(getCellValueAsString(row, headerMap.get("Tags"))));
        setTestCaseField(APITestCase, "ExpStatus", parseInteger(getCellValueAsString(row, headerMap.get("Exp Status"))));
        setTestCaseField(APITestCase, "ExpResult", parseList(getCellValueAsString(row, headerMap.get("Exp Result"))));
        setTestCaseField(APITestCase, "SaveFields", parseList(getCellValueAsString(row, headerMap.get("Save Fields"))));
        setTestCaseField(APITestCase, "DynamicValidationTCID", getCellValueAsString(row, headerMap.get("Dynamic Validation TCID")));
        setTestCaseField(APITestCase, "DynamicValidationExpectedChanges", parseMap(getCellValueAsString(row, headerMap.get("Dynamic Validation Expected Changes"))));

        return APITestCase;
    }

    private static String getCellValueAsString(Row row, Integer cellIndex) {
        if (cellIndex == null) {
            return "";
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private static void setTestCaseField(APITestCase APITestCase, String fieldName, Object value) {
        try {
            if (value instanceof List) {
                APITestCase.getClass().getMethod("set" + fieldName, List.class).invoke(APITestCase, value);
            } else if (value instanceof Map) {
                APITestCase.getClass().getMethod("set" + fieldName, Map.class).invoke(APITestCase, value);
            } else if (value instanceof Boolean) {
                APITestCase.getClass().getMethod("set" + fieldName, boolean.class).invoke(APITestCase, value);
            } else if (value instanceof Integer) {
                APITestCase.getClass().getMethod("set" + fieldName, int.class).invoke(APITestCase, value);
            } else {
                APITestCase.getClass().getMethod("set" + fieldName, String.class).invoke(APITestCase, value);
            }
        } catch (Exception e) {
            logger.error("Failed to set field: {} with value: {}", fieldName, value, e);
        }
    }

    private static List<String> parseList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse integer: {}", value);
            return 0;
        }
    }

    private static Map<String, String> parseMap(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(value.split("\n"))
                .map(pair -> pair.split(":"))
                .filter(keyValue -> keyValue.length == 2)
                .collect(Collectors.toMap(
                        keyValue -> keyValue[0].trim(),
                        keyValue -> keyValue[1].trim(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }
}