package com.example.api.hooks;

import com.example.api.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void setUp() {
        TestContext.getInstance().clearContext();
    }

    @After
    public void tearDown() {
        TestContext.getInstance().clearContext();
    }
}