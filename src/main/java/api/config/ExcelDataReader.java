package api.config;

import api.model.TestCase;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelDataReader {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataReader.class);
    private static final String EXCEL_FILE_PATH = "src/test/resources/cases/api_test_cases.xlsx";
    private static final Map<String, List<TestCase>> cache = new HashMap<>();

    public static List<TestCase> readTestData(String sheetName) {
        if (cache.containsKey(sheetName)) {
            logger.info("Returning cached test cases for sheet: {}", sheetName);
            return cache.get(sheetName);
        }

        List<TestCase> testCases = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(EXCEL_FILE_PATH);
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
                    TestCase testCase = createTestCase(row, headerMap);
                    if (testCase.isValid()) {
                        testCases.add(testCase);
                    } else {
                        logger.warn("Invalid test case at row {}: {}", i + 1, testCase);
                    }
                }
            }

            cache.put(sheetName, testCases);
            logger.info("Loaded {} valid test cases from sheet: {}", testCases.size(), sheetName);

        } catch (IOException e) {
            logger.error("Failed to read Excel file: {}", EXCEL_FILE_PATH, e);
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return testCases;
    }

    private static Map<String, Integer> createHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String header = headerRow.getCell(i).getStringCellValue();
            headerMap.put(header, i);
        }
        return headerMap;
    }

    private static TestCase createTestCase(Row row, Map<String, Integer> headerMap) {
        TestCase testCase = new TestCase();

        setTestCaseField(testCase, "TCID", getCellValueAsString(row, headerMap.get("TCID")));
        setTestCaseField(testCase, "Name", getCellValueAsString(row, headerMap.get("Name")));
        setTestCaseField(testCase, "Descriptions", getCellValueAsString(row, headerMap.get("Descriptions")));
        setTestCaseField(testCase, "Conditions", parseList(getCellValueAsString(row, headerMap.get("Conditions"))));
        setTestCaseField(testCase, "EndpointKey", getCellValueAsString(row, headerMap.get("Endpoint Key")));
        setTestCaseField(testCase, "HeadersTemplateKey", getCellValueAsString(row, headerMap.get("Headers Template Key")));
        setTestCaseField(testCase, "HeaderOverride", parseList(getCellValueAsString(row, headerMap.get("Header Override"))));
        setTestCaseField(testCase, "BodyTemplateKey", getCellValueAsString(row, headerMap.get("Body Template Key")));
        setTestCaseField(testCase, "BodyOverride", parseList(getCellValueAsString(row, headerMap.get("Body Override"))));
        setTestCaseField(testCase, "Run", "Y".equalsIgnoreCase(getCellValueAsString(row, headerMap.get("Run"))));
        setTestCaseField(testCase, "Tags", parseList(getCellValueAsString(row, headerMap.get("Tags"))));
        setTestCaseField(testCase, "ExpStatus", parseInteger(getCellValueAsString(row, headerMap.get("Exp Status"))));
        setTestCaseField(testCase, "ExpResult", parseList(getCellValueAsString(row, headerMap.get("Exp Result"))));
        setTestCaseField(testCase, "SaveFields", parseList(getCellValueAsString(row, headerMap.get("Save Fields"))));
        setTestCaseField(testCase, "DynamicValidationEndpoint", getCellValueAsString(row, headerMap.get("Dynamic Validation Endpoint")));
        setTestCaseField(testCase, "DynamicValidationExpectedChanges", parseMap(getCellValueAsString(row, headerMap.get("Dynamic Validation Expected Changes"))));

        return testCase;
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

    private static void setTestCaseField(TestCase testCase, String fieldName, Object value) {
        try {
            if (value instanceof List) {
                testCase.getClass().getMethod("set" + fieldName, List.class).invoke(testCase, value);
            } else if (value instanceof Map) {
                testCase.getClass().getMethod("set" + fieldName, Map.class).invoke(testCase, value);
            } else if (value instanceof Boolean) {
                testCase.getClass().getMethod("set" + fieldName, boolean.class).invoke(testCase, value);
            } else if (value instanceof Integer) {
                testCase.getClass().getMethod("set" + fieldName, int.class).invoke(testCase, value);
            } else {
                testCase.getClass().getMethod("set" + fieldName, String.class).invoke(testCase, value);
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