package com.aiotlabs.ifitpro.plugin.bluetooth;

import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.NotifyEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderType;
import com.aiotlabs.ifitpro.plugin.bluetooth.LogModule;
import com.aiotlabs.ifitpro.plugin.bluetooth.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置通知
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZWriteNotifyTask
 */
public class ZWriteNotifyTask extends OrderTask {

    private byte[] orderData;

    public ZWriteNotifyTask(MokoOrderTaskCallback callback, NotifyEnum notifyEnum, String showText, boolean isOpen) {
        super(OrderType.WRITE_CHARACTER, OrderEnum.Z_WRITE_NOTIFY, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        int showTextLength = showText.length();
        byte[] showTextBytes = new byte[0];
        if (showTextLength > 0) {
            if (showTextLength > 14) {
                showText = showText.substring(0, 14);
                showTextLength = 14;
            }
            showTextBytes = DigitalConver.hex2bytes(DigitalConver.string2Hex(showText));
        }
        orderData = new byte[6 + showTextLength];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) (3 + showTextLength);
        orderData[3] = (byte) (isOpen ? 0x01 : 0x00);
        orderData[4] = (byte) notifyEnum.getNotifyType();
        orderData[5] = (byte) showTextLength;
        if (showTextLength > 0) {
            System.arraycopy(showTextBytes, 0, orderData, 6, showTextLength);
        }
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
