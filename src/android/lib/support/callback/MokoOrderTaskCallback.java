package com.aiotlabs.ifitpro.plugin.bluetooth.support.callback;

import com.aiotlabs.ifitpro.plugin.bluetooth.support.entity.OrderTaskResponse;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 返回数据回调类
 * @ClassPath com.aiotlabs.ifitpro.plugin.bluetooth.MokoOrderTaskCallback
 */
public interface MokoOrderTaskCallback {

    void onOrderResult(OrderTaskResponse response);

    void onOrderTimeout(OrderTaskResponse response);

    void onOrderFinish();
}
