package com.dream.utils;

import android.app.Application;

import org.xutils.BuildConfig;
import org.xutils.x;

import es.dmoral.toasty.MyToast;
import es.dmoral.toasty.Toasty;

/**
 * Created by ZhangPing on 2018/1/5.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false);
        MyUtils.initSimpleDataFormatTimeZone();
        MyToast.init(this,true,false);
    }
}
