package com.kgignatyev.workflow.fileprocessing;

import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultiFilesExchangeWorkflowImpl  implements MultiFilesExchangeWorkflow{

    LinkedList<String> payloadsForVendor = new LinkedList<>();
    List<PayloadAndInfo> payloadsForInitiator = new ArrayList<>();

    boolean active = true;
    boolean done = false;
    private ChildWorkflowOptions subWFOption = ChildWorkflowOptions.newBuilder()
            .setWorkflowRunTimeout( Duration.ofSeconds(20))
            .build();


    @Override
    public void startExchange(String chainID) {
        while (active){
            Workflow.await( ()-> queueLength() > 0 || !active);
            sendQueuedPayloads();
        }
        done = true;
    }

    private void sendQueuedPayloads() {
        System.out.println("\t\tMultiFilesExchangeWorkflowImpl.sendQueuedPayloads");

        while ( payloadsForVendor.size()>0){
            String payloadToSend = payloadsForVendor.peekFirst();
            System.out.println("payload:" + payloadToSend);
            PayloadDeliveryWorkflow child = Workflow.newChildWorkflowStub(PayloadDeliveryWorkflow.class, subWFOption);
            // This is a non blocking call that returns immediately.
            Promise<String>  payloadProcessing = Async.function(child::deliver, payloadToSend, "v1" );

            payloadProcessing.get(); // blocked wait for the child to complete.
            System.out.println("removing:" + payloadToSend);
            payloadsForVendor.remove( payloadToSend );
        }

    }



    @Override
    public void sendToVendor(String payload) {
        System.out.println("MultiFilesExchangeWorkflowImpl.sendToVendor");
        payloadsForVendor.add( payload );


    }

    @Override
    public void sendToInitiator(String payload) {
        System.out.println("MultiFilesExchangeWorkflowImpl.sendToInitiator");
        payloadsForInitiator.add(new PayloadAndInfo( payload ));
    }

    @Override
    public void finishExchange() {
        active = false;
    }

    @Override
    public long queueLength() {
        System.out.println("payloadsForVendor = " + payloadsForVendor);
        return payloadsForVendor.size();
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
