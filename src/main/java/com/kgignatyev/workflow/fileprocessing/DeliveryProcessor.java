package com.kgignatyev.workflow.fileprocessing;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface DeliveryProcessor {

    void deliver(String data, String address);
}
