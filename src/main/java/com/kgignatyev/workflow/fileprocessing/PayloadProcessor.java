package com.kgignatyev.workflow.fileprocessing;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface PayloadProcessor {

    void processPayload(String data, String wfID);

}
