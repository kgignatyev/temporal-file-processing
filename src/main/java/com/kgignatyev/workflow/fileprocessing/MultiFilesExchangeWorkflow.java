package com.kgignatyev.workflow.fileprocessing;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MultiFilesExchangeWorkflow {

    String TASK_QUEUE = "payloads";

    @WorkflowMethod
    void startExchange(String chainID);

    @SignalMethod
    void sendToVendor( String payload);

    @SignalMethod
    void sendToInitiator( String payload);

    @SignalMethod
    void finishExchange();

    @QueryMethod
    long queueLength();

    @QueryMethod
    boolean isDone();
}
