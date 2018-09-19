package com.aiotlabs.ifitpro.plugin.bluetooth.support.callback;

import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.BleDevice;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 扫描设备回调
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.support.callback.MokoScanDeviceCallback
 */
public interface MokoScanDeviceCallback {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 开始扫描
     */
    void onStartScan();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 扫描的设备
     */
    void onScanDevice(BleDevice device);

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 结束扫描
     */
    void onStopScan();
}
