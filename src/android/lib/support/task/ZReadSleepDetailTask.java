package com.aiotlabs.ifitpro.plugin.bluetooth;

import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderType;

import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 读取睡眠详情
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZReadSleepDetailTask
 */
public class ZReadSleepDetailTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 8;

    private byte[] orderData;

    public ZReadSleepDetailTask(MokoOrderTaskCallback callback, Calendar lastSyncTime) {
        super(OrderType.READ_CHARACTER, OrderEnum.Z_READ_SLEEP_DETAIL, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);

        int year = lastSyncTime.get(Calendar.YEAR) - 2000;
        int month = lastSyncTime.get(Calendar.MONTH) + 1;
        int day = lastSyncTime.get(Calendar.DAY_OF_MONTH);

        int hour = lastSyncTime.get(Calendar.HOUR_OF_DAY);
        int minute = lastSyncTime.get(Calendar.MINUTE);

        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x05;
        orderData[3] = (byte) year;
        orderData[4] = (byte) month;
        orderData[5] = (byte) day;
        orderData[6] = (byte) hour;
        orderData[7] = (byte) minute;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }
}
