package com.kgignatyev.workflow.fileprocessing;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.kgignatyev.workflow.fileprocessing.MultiFilesExchangeWorkflow.TASK_QUEUE;
import static com.kgignatyev.workflow.fileprocessing.WFContext.testEnv;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PayloadDeliveryWorkflowTest {

    static private Worker worker;
    static private WorkflowClient workflowClient;
    static PayloadProcessorTestImpl payloadProcessor;
    static DeliveryProcessorTestImpl deliveryProcessor = new DeliveryProcessorTestImpl();

    String testChainID = "1-2-3";
    String wfId = "123-12";





    @BeforeAll
    static public void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        worker = testEnv.newWorker(TASK_QUEUE);
        workflowClient = testEnv.getWorkflowClient();
        payloadProcessor = new PayloadProcessorTestImpl(workflowClient);
        worker.registerWorkflowImplementationTypes(PayloadDeliveryWorkflowImpl.class);
        worker.registerActivitiesImplementations( payloadProcessor,deliveryProcessor );
        testEnv.start();
    }

    @AfterAll
    static public void tearDown() {
        testEnv.close();
    }

    @Test
    public void testDelivery() throws InterruptedException {
        WorkflowOptions options =
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId(wfId).build();
        PayloadDeliveryWorkflow transferWorkflow =  workflowClient.newWorkflowStub(PayloadDeliveryWorkflow.class, options);
        WorkflowExecution wf =  WorkflowClient.start(transferWorkflow::deliver, "payload-sample","v1");
        System.out.println("wf = " + wf);
        Thread.sleep(2000);
        assertEquals( PayloadStatus.NEW,  transferWorkflow.status() );
        payloadProcessor.failProcessing = false;
        //wait for completion
        Thread.sleep(4000);

        assertEquals( PayloadStatus.SENDING,  transferWorkflow.status() );
        deliveryProcessor.failDelivery = false;
        Thread.sleep(2000);
        assertEquals( PayloadStatus.DELIVERED,  transferWorkflow.status() );



    }

}
