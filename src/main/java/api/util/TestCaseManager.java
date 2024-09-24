package api.util;

import api.config.ExcelDataReader;
import api.model.APITestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TestCaseManager {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseManager.class);
    private List<APITestCase> apiTestCases;

    public TestCaseManager() {
        loadTestCases();
    }

    private void loadTestCases() {
        if (apiTestCases == null) {
            apiTestCases = ExcelDataReader.readTestData("API");
            logger.info("Loaded {} test cases from Excel", apiTestCases.size());
        }
    }

    public APITestCase findTestCaseByTCID(String tcid) {
        return apiTestCases.stream()
                .filter(tc -> tc.getTCID().equals(tcid))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Test case not found for TCID: {}", tcid);
                    return new IllegalArgumentException("No test cases found for TCID: " + tcid);
                });
    }

    public Set<String> extractPreValidationTcids(String currentTCID, Map<String, String> expResult) {
        return expResult.keySet().stream()
                .filter(key -> key.contains(".") && !key.startsWith(currentTCID))
                .map(key -> key.substring(0, key.indexOf('.')))
                .collect(Collectors.toSet());
    }

    public Map<String, Map<String, String>> extractDynamicValidations(String currentTCID,Map<String, String> expResult) {
        Map<String, Map<String, String>> dynamicValidations = new HashMap<>();
        expResult.forEach((key, value) -> {
            if (isDynamicField(key,currentTCID)) {
                String tcid = key.substring(0, key.indexOf('.'));
                String field = key.substring(key.indexOf('.') + 1);
                dynamicValidations.computeIfAbsent(tcid, k -> new HashMap<>()).put(field, value);
            }
        });
        return dynamicValidations;
    }

    private boolean isDynamicField(String key,String currentTCID) {
        return key.contains(".") && !key.startsWith(currentTCID);
    }
}