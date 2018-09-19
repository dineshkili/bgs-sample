package com.aiotlabs.ifitpro.plugin.bluetooth;


public interface MokoConnStateCallback {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接成功
     */
    void onConnectSuccess();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 断开连接
     */
    void onDisConnected();

    /**
     * @Date 2017/8/29
     * @Author wenzheng.liu
     * @Description 重连超时
     */
    void onConnTimeout(int reConnCount);
}
