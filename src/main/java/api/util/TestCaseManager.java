package api.util;

import api.config.ExcelDataReader;
import api.model.APITestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestCaseManager {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseManager.class);
    private static List<APITestCase> apiTestCases;

    public static void loadTestCases() {
        if (apiTestCases == null) {
            apiTestCases = ExcelDataReader.readTestData("API");
            logger.info("Loaded {} test cases from Excel", apiTestCases.size());
        }
    }

    public static APITestCase findTestCaseByTCID(String tcid) {
        loadTestCases();
        return apiTestCases.stream()
                .filter(tc -> tc.getTCID().equals(tcid))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Test case not found for TCID: {}", tcid);
                    return new IllegalArgumentException("No test cases found for TCID: " + tcid);
                });
    }
}