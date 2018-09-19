package com.aiotlabs.ifitpro.plugin.bluetooth;

import java.io.Serializable;

/**
 * @Date 2018/2/8
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.OrderTaskResponse
 */
public class OrderTaskResponse implements Serializable {
    public OrderEnum order;
    public int responseType;
    public byte[] responseValue;
}
