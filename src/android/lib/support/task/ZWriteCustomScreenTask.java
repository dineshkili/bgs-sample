package com.aiotlabs.ifitpro.plugin.bluetooth;

import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.CustomScreen;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderType;
import com.aiotlabs.ifitpro.plugin.bluetooth.LogModule;
import com.aiotlabs.ifitpro.plugin.bluetooth.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置自定义屏幕
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZWriteCustomScreenTask
 */
public class ZWriteCustomScreenTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 7;

    private byte[] orderData;

    public ZWriteCustomScreenTask(MokoOrderTaskCallback callback, CustomScreen functions) {
        super(OrderType.WRITE_CHARACTER, OrderEnum.Z_WRITE_CUSTOM_SCREEN, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x04;
        orderData[3] = (byte) 0xFF;
        orderData[4] = (byte) 0xFF;
        StringBuilder sb = new StringBuilder("1111111");
        sb.append(functions.sleep ? "1" : "0");
        orderData[5] = (byte) Integer.parseInt(sb.toString(), 2);
        StringBuilder sb1 = new StringBuilder();
        sb1.append(functions.duration ? "1" : "0");
        sb1.append(functions.distance ? "1" : "0");
        sb1.append(functions.calorie ? "1" : "0");
        sb1.append(functions.step ? "1" : "0");
        sb1.append("0"); // 血压
        sb1.append(functions.heartrate ? "1" : "0");
        sb1.append("1"); // 时间
        sb1.append("1"); // 配对
        orderData[6] = (byte) Integer.parseInt(sb1.toString(), 2);
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
