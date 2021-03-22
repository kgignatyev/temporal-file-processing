package com.kgignatyev.workflow.fileprocessing;

import java.util.ArrayList;
import java.util.List;

public class DeliveryProcessorTestImpl implements DeliveryProcessor{

    public boolean failDelivery = true;
    public List<String> deliveredData = new ArrayList<>();

    @Override
    public void deliver(String data, String address) {
        System.out.println("delivering = " + data);
        if(failDelivery){
            throw new RuntimeException("intentional failure because failProcessing = true");
        }
        deliveredData.add( data);
    }
}
