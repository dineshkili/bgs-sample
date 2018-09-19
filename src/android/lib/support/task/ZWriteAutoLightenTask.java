package com.aiotlabs.ifitpro.plugin.bluetooth.support.task;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.callback.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.AutoLighten;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.OrderType;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.log.LogModule;
import com.aiotlabs.ifitpro.plugin.bluetooth.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置翻腕亮屏
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZWriteAutoLightenTask
 */
public class ZWriteAutoLightenTask extends OrderTask {
    public static final String PATTERN_HH_MM = "HH:mm";

    private static final int ORDERDATA_LENGTH = 8;

    private byte[] orderData;

    public ZWriteAutoLightenTask(MokoOrderTaskCallback callback, AutoLighten autoLighten) {
        super(OrderType.WRITE_CHARACTER, OrderEnum.Z_WRITE_AUTO_LIGHTEN, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x05;
        orderData[3] = (byte) autoLighten.autoLighten;
        Calendar startCalendar = strDate2Calendar(autoLighten.startTime, PATTERN_HH_MM);
        orderData[4] = (byte) startCalendar.get(Calendar.HOUR_OF_DAY);
        orderData[5] = (byte) startCalendar.get(Calendar.MINUTE);
        Calendar endCalendar = strDate2Calendar(autoLighten.endTime, PATTERN_HH_MM);
        orderData[6] = (byte) endCalendar.get(Calendar.HOUR_OF_DAY);
        orderData[7] = (byte) endCalendar.get(Calendar.MINUTE);
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

    public static Calendar strDate2Calendar(String strDate, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        try {
            Date date = sdf.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
