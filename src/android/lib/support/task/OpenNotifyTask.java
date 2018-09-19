package com.aiotlabs.ifitpro.plugin.bluetooth;

import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderType;


public class OpenNotifyTask extends OrderTask {
    public byte[] data;

    public OpenNotifyTask(OrderType orderType, OrderEnum orderEnum, MokoOrderTaskCallback callback) {
        super(orderType, orderEnum, callback, OrderTask.RESPONSE_TYPE_NOTIFY);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
