package com.aiotlabs.ifitpro.plugin.bluetooth;

import android.app.Application;

import com.aiotlabs.ifitpro.plugin.bluetooth.support.MokoSupport;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        MokoSupport.getInstance().init(getApplicationContext());
    }
}
