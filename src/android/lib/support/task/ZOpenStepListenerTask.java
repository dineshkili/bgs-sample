package com.aiotlabs.ifitpro.plugin.bluetooth;

import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderType;
import com.aiotlabs.ifitpro.plugin.bluetooth.LogModule;
import com.aiotlabs.ifitpro.plugin.bluetooth.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 监听记步
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZOpenStepListenerTask
 */
public class ZOpenStepListenerTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 4;

    private byte[] orderData;

    public ZOpenStepListenerTask(MokoOrderTaskCallback callback) {
        super(OrderType.STEP_CHARACTER, OrderEnum.Z_STEPS_CHANGES_LISTENER, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_SETP_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x01;
        orderData[3] = (byte) 0x01;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        if (0x01 != DigitalConver.byte2Int(value[2])) {
            return;
        }
        if (0x00 != DigitalConver.byte2Int(value[3])) {
            return;
        }

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
