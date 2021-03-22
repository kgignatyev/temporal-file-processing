package com.kgignatyev.workflow.fileprocessing;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import static com.kgignatyev.workflow.fileprocessing.MultiFilesExchangeWorkflow.TASK_QUEUE;
import static com.kgignatyev.workflow.fileprocessing.WFContext.testEnv;
import static org.junit.jupiter.api.Assertions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(WFTestWatcher.class)
public class MultiFilesExchangeWorkflowTest {


    static private Worker worker;
    static private WorkflowClient workflowClient;
    static PayloadProcessorTestImpl payloadProcessor ;
    static DeliveryProcessorTestImpl deliveryProcessor = new DeliveryProcessorTestImpl();

    String testChainID = "1-2-3";
    String wfId = "123-12";





    @BeforeAll
    static public void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker(TASK_QUEUE);
        workflowClient = testEnv.getWorkflowClient();
        payloadProcessor = new PayloadProcessorTestImpl(workflowClient);
        worker.registerWorkflowImplementationTypes(MultiFilesExchangeWorkflowImpl.class, PayloadDeliveryWorkflowImpl.class);
        worker.registerActivitiesImplementations( payloadProcessor ,deliveryProcessor );
        testEnv.start();
    }

    @AfterAll
    static public void tearDown() {
        testEnv.close();
    }

    @Test
    public void testHappyPath() throws InterruptedException {
        WorkflowOptions options =
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId(wfId).build();
        MultiFilesExchangeWorkflow transferWorkflow =
                workflowClient.newWorkflowStub(MultiFilesExchangeWorkflow.class, options);
        WorkflowExecution wf =  WorkflowClient.start(transferWorkflow::startExchange, testChainID);
        System.out.println("wf = " + wf);
        String payload1 = "payload-vendor-1";
        String payload2 = "payload-vendor-2";
        String payload3 = "payload-vendor-3";
        String payload4 = "payload-vendor-4";
        transferWorkflow.sendToVendor(payload1);
        transferWorkflow.sendToVendor(payload2);
        transferWorkflow.sendToVendor(payload3);
        transferWorkflow.sendToVendor(payload4);
        assertEquals( 4, transferWorkflow.queueLength());

        Thread.sleep(2000);
        payloadProcessor.failProcessing = false;
        deliveryProcessor.failDelivery = false;
        Thread.sleep(6000);
        //now we should have all payloads sent for processing
        assertEquals( 0, transferWorkflow.queueLength());

        //check sequence
        assertEquals( payload1, deliveryProcessor.deliveredData.get(0));
        assertEquals( payload2, deliveryProcessor.deliveredData.get(1));
        assertEquals( payload3, deliveryProcessor.deliveredData.get(2));
        assertEquals( payload4, deliveryProcessor.deliveredData.get(3));
        assertFalse( transferWorkflow.isDone());

        transferWorkflow.finishExchange();

        Thread.sleep(2000);
        assertTrue( transferWorkflow.isDone());
    }

}
