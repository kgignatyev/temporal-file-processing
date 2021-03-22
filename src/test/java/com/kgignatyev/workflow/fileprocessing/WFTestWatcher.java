package com.kgignatyev.workflow.fileprocessing;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import static com.kgignatyev.workflow.fileprocessing.WFContext.testEnv;

public class WFTestWatcher implements TestWatcher {

    public void testFailed(ExtensionContext context, Throwable cause) {

        System.out.println("WFTestWatcher.testFailed");

        if (testEnv != null) {
            System.err.println(testEnv.getDiagnostics());
            testEnv.close();
        }
    }
}
