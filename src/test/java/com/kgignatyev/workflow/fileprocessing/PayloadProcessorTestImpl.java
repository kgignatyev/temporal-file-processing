package com.kgignatyev.workflow.fileprocessing;

import io.temporal.client.WorkflowClient;
import io.temporal.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;

public class PayloadProcessorTestImpl implements PayloadProcessor{

    private final WorkflowClient workflowClient;
    public boolean failProcessing = true;

    public PayloadProcessorTestImpl(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    public List threads = new ArrayList();

    @Override
    public void processPayload(String data , String wfID) {

        System.out.println(">>>> processing = " + data);
        if( failProcessing ){
            throw new RuntimeException("intentional failure because failProcessing = true");
        }

        System.out.println(">>>> processed = " + data);
        Thread t = new Thread(() -> {
            try {
                Thread.sleep( 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            workflowClient.newWorkflowStub( PayloadDeliveryWorkflow.class, wfID).payloadProcessed();
        });
        threads.add( t);
        t.start();



    }
}
