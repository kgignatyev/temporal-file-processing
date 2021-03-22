package com.kgignatyev.workflow.fileprocessing;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Single payload delivery
 */
@WorkflowInterface
public interface PayloadDeliveryWorkflow {



    @WorkflowMethod
    String  deliver(String payload, String to);

    @SignalMethod
    void payloadProcessed();

    @QueryMethod
    PayloadStatus status();
}
