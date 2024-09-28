package api;

import api.model.APITestCase;
import api.util.ExcelTestCaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class APITestCaseManager {
    private static final Logger logger = LoggerFactory.getLogger(APITestCaseManager.class);
    private List<APITestCase> testCases;

    public APITestCaseManager() {
        this.testCases = new ArrayList<>();
    }

    public List<APITestCase> loadTestCasesFromExcel() {
        testCases = ExcelTestCaseReader.readTestData("API");
        logger.info("Loaded {} test cases from Excel", testCases.size());
        return testCases;
    }

    public APITestCase getTestCaseByTCID(String tcid) {
        if (testCases == null || testCases.isEmpty()) {
            loadTestCasesFromExcel();
        }
        return testCases.stream()
                .filter(tc -> tc.getTCID().equals(tcid))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Test case not found for TCID: {}", tcid);
                    return new IllegalArgumentException("No test case found for TCID: " + tcid);
                });
    }

    public Set<String> getValidationTCIDs(String currentTCID, Map<String, String> expResult) {
        return expResult.keySet().stream()
                .filter(key -> isDynamicField(key, currentTCID))
                .map(key -> key.split("\\.")[0])
                .collect(Collectors.toSet());
    }

    public Map<String, Map<String, String>> getDynamicExpectedResults(Map<String, String> expResult, String currentTCID) {
        return expResult.entrySet().stream()
                .filter(entry -> isDynamicField(entry.getKey(), currentTCID))
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().split("\\.")[0],
                        Collectors.toMap(
                                entry -> entry.getKey().split("\\.")[1],
                                Map.Entry::getValue,
                                (v1, v2) -> v2,
                                HashMap::new
                        )
                ));
    }

    private boolean isDynamicField(String key, String currentTCID) {
        return key.contains(".") && !key.startsWith(currentTCID);
    }
    public List<String> getConditionTCIDs(APITestCase testCase, String prefix) {
        List<String> TCIDs = new ArrayList<>();
        for (String condition : testCase.getConditions()) {
            if (condition.startsWith(prefix)) {
                TCIDs.addAll(Arrays.asList(condition.substring(prefix.length()).split(",")));
            }
        }
        return TCIDs;
    }
}