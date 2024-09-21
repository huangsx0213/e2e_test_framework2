package api.hooks;

import api.context.TestContext;
import api.config.ConfigManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());
        TestContext.getInstance().clearContext();

        // 设置默认环境和项目
//        ConfigManager.getInstance().setEnvironment("dev");
//        ConfigManager.getInstance().setProject("default");

        logger.info("Test context cleared and default configurations set");
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            logger.error("Scenario failed: {}", scenario.getName());
            // 可以在这里添加额外的失败处理逻辑，比如截图或者保存日志
        } else {
            logger.info("Scenario passed: {}", scenario.getName());
        }

        TestContext.getInstance().clearContext();
        logger.info("Test context cleared after scenario completion");
    }

    @Before("@api")
    public void setUpApiTest() {
        logger.info("Setting up for API test");
        // 可以在这里添加特定于API测试的设置
    }

    @After("@api")
    public void tearDownApiTest() {
        logger.info("Tearing down API test");
        // 可以在这里添加特定于API测试的清理工作
    }
}