package com.aiotlabs.ifitpro.plugin.bluetooth.support.task;

import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.callback.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.OrderType;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.log.LogModule;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置记步目标
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZWriteStepTargetTask
 */
public class ZWriteStepTargetTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 5;

    private byte[] orderData;

    public ZWriteStepTargetTask(MokoOrderTaskCallback callback, int stepTarget) {
        super(OrderType.WRITE_CHARACTER, OrderEnum.Z_WRITE_STEP_TARGET, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        byte[] target = DigitalConver.int2ByteArr(stepTarget, 2);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x02;
        System.arraycopy(target, 0, orderData, 3, 2);
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
