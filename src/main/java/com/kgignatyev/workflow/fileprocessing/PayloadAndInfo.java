package com.kgignatyev.workflow.fileprocessing;

public class PayloadAndInfo {
    public final String payload;
    public PayloadStatus status = PayloadStatus.NEW;

    public PayloadAndInfo(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "PaylodAndInfo{" +
                "payload='" + payload + '\'' +
                ", status=" + status +
                '}';
    }
}
