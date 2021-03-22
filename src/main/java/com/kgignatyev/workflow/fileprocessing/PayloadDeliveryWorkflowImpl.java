package com.kgignatyev.workflow.fileprocessing;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class PayloadDeliveryWorkflowImpl implements PayloadDeliveryWorkflow{

    private PayloadAndInfo p;


    private final ActivityOptions processingActivityOptions =
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setScheduleToStartTimeout(Duration.ofMinutes(1))
                    .setRetryOptions(
                            RetryOptions.newBuilder()
                                    .setInitialInterval(Duration.ofSeconds(1))
                                    .setMaximumInterval(Duration.ofSeconds(100))
                                    .build())
                    .build();

    private final ActivityOptions deliveryActivityOptions =
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(5))
                    .setScheduleToStartTimeout(Duration.ofMinutes(1))
                    .setRetryOptions(
                            RetryOptions.newBuilder()
                                    .setInitialInterval(Duration.ofSeconds(1))
                                    .setMaximumInterval(Duration.ofSeconds(10))
                                    .build())
                    .build();

    private final PayloadProcessor payloadProcessor = Workflow.newActivityStub(PayloadProcessor.class, processingActivityOptions);
    private final DeliveryProcessor deliveryProcessor = Workflow.newActivityStub(DeliveryProcessor.class, deliveryActivityOptions);

    @Override
    public String deliver(String payload, String to) {
        this.p = new PayloadAndInfo( payload );
        payloadProcessor.processPayload( payload , Workflow.getInfo().getWorkflowId());
        p.status = PayloadStatus.PROCESSING;
        System.out.println(">>>> PROCESSING");
        Workflow.await( ()-> PayloadStatus.PROCESSED.equals(  p.status ));
        System.out.println(">>>> PROCESSED");
        p.status = PayloadStatus.SENDING;
        deliveryProcessor.deliver( payload, to);
        System.out.println(">>>> DELIVERED");
        p.status = PayloadStatus.DELIVERED;
        return "OK";
    }

    @Override
    public void payloadProcessed() {
        System.out.println(">>>> PayloadDeliveryWorkflowImpl.payloadProcessed");
        p.status = PayloadStatus.PROCESSED;
    }

    @Override
    public PayloadStatus status() {
        return p.status;
    }
}
