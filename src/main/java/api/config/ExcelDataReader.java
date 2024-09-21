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
                    testCases.add(testCase);
                }
            }

            cache.put(sheetName, testCases);
            logger.info("Loaded {} test cases from sheet: {}", testCases.size(), sheetName);

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
        setTestCaseField(testCase, "Conditions", Arrays.asList(getCellValueAsString(row, headerMap.get("Conditions")).split("\n")));
        setTestCaseField(testCase, "EndpointKey", getCellValueAsString(row, headerMap.get("Endpoint Key")));
        setTestCaseField(testCase, "HeadersTemplateKey", getCellValueAsString(row, headerMap.get("Headers Template Key")));
        setTestCaseField(testCase, "HeaderOverride", Arrays.asList(getCellValueAsString(row, headerMap.get("Header Override")).split("\n")));
        setTestCaseField(testCase, "BodyTemplateKey", getCellValueAsString(row, headerMap.get("Body Template Key")));
        setTestCaseField(testCase, "BodyOverride", Arrays.asList(getCellValueAsString(row, headerMap.get("Body Override")).split("\n")));
        setTestCaseField(testCase, "Run", "Y".equalsIgnoreCase(getCellValueAsString(row, headerMap.get("Run"))));
        setTestCaseField(testCase, "Tags", Arrays.asList(getCellValueAsString(row, headerMap.get("Tags")).split("\n")));
        setTestCaseField(testCase, "ExpStatus", Integer.parseInt(getCellValueAsString(row, headerMap.get("Exp Status"))));
        setTestCaseField(testCase, "ExpResult", Arrays.asList(getCellValueAsString(row, headerMap.get("Exp Result")).split("\n")));
        setTestCaseField(testCase, "SaveFields", Arrays.asList(getCellValueAsString(row, headerMap.get("Save Fields")).split("\n")));
        setTestCaseField(testCase, "DynamicValidationEndpoint", getCellValueAsString(row, headerMap.get("Dynamic Validation Endpoint")));
        setTestCaseField(testCase, "DynamicValidationExpectedChanges", parseExpectedChanges(getCellValueAsString(row, headerMap.get("Dynamic Validation Expected Changes"))));

        return testCase;
    }

    private static String getCellValueAsString(Row row, int cellIndex) {
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
                // 处理 List 类型
                testCase.getClass().getMethod("set" + fieldName, List.class).invoke(testCase, value);
            } else if (value instanceof Map) {
                // 处理 Map 类型
                testCase.getClass().getMethod("set" + fieldName, Map.class).invoke(testCase, value);
            } else if (value instanceof Boolean) {
                // 处理 boolean 类型，使用 Boolean.TYPE 来指定基本类型
                testCase.getClass().getMethod("set" + fieldName, Boolean.TYPE).invoke(testCase, value);
            } else if (value instanceof Integer) {
                // 处理 boolean 类型，使用 Boolean.TYPE 来指定基本类型
                testCase.getClass().getMethod("set" + fieldName, Integer.TYPE).invoke(testCase, value);
            }else {
                // 处理其他类型
                testCase.getClass().getMethod("set" + fieldName, value.getClass()).invoke(testCase, value);
            }
        } catch (Exception e) {
            logger.error("Failed to set field: {} with value: {}", fieldName, value, e);
        }
    }



    private static Map<String, String> parseExpectedChanges(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(value.split("\n"))
                .map(pair -> pair.split(":"))
                .filter(keyValue -> keyValue.length == 2)
                .collect(Collectors.toMap(
                        keyValue -> keyValue[0].trim(),
                        keyValue -> keyValue[1].trim()
                ));
    }
}