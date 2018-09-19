package com.aiotlabs.ifitpro.plugin.bluetooth;

import com.aiotlabs.ifitpro.plugin.bluetooth.MokoConstants;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoSupport;
import com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback;
import com.aiotlabs.ifitpro.plugin.bluetooth.NoDisturb;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderEnum;
import com.aiotlabs.ifitpro.plugin.bluetooth.OrderType;
import com.aiotlabs.ifitpro.plugin.bluetooth.LogModule;
import com.aiotlabs.ifitpro.plugin.bluetooth.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 读取勿扰模式
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.ZReadNoDisturbTask
 */
public class ZReadNoDisturbTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    private byte[] orderData;

    public ZReadNoDisturbTask(MokoOrderTaskCallback callback) {
        super(OrderType.READ_CHARACTER, OrderEnum.Z_READ_NODISTURB, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (0x05 != DigitalConver.byte2Int(value[2])) {
            return;
        }
        NoDisturb noDisturb = new NoDisturb();
        noDisturb.noDisturb = DigitalConver.byte2Int(value[3]);
        int startHour = DigitalConver.byte2Int(value[4]);
        int startMin = DigitalConver.byte2Int(value[5]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMin);
        noDisturb.startTime = new SimpleDateFormat("HH:mm").format(calendar.getTime());
        int endHour = DigitalConver.byte2Int(value[6]);
        int endMin = DigitalConver.byte2Int(value[7]);
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        calendar.set(Calendar.MINUTE, endMin);
        noDisturb.endTime = new SimpleDateFormat("HH:mm").format(calendar.getTime());
        MokoSupport.getInstance().setNodisturb(noDisturb);

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
