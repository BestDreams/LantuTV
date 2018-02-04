package com.dream.utils;

import android.app.Application;

import org.xutils.x;

/**
 * Created by ZhangPing on 2018/1/5.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false);
        MyUtils.initSimpleDataWithHourFormatTimeZone();
        MyUtils.initSimpleDataWithMinuteFormatTimeZone();
    }
}
