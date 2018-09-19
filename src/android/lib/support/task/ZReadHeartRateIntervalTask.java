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
 * @Description 读取心率间隔
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZReadHeartRateIntervalTask
 */
public class ZReadHeartRateIntervalTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    private byte[] orderData;

    public ZReadHeartRateIntervalTask(MokoOrderTaskCallback callback) {
        super(OrderType.READ_CHARACTER, OrderEnum.Z_READ_HEART_RATE_INTERVAL, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x00;
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
        MokoSupport.getInstance().setHeartRateInterval(DigitalConver.byte2Int(value[3]) / 10);

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
