package com.dream.utils;

import android.content.Context;

/**
 * Created by Administrator on 2017/12/21.
 */

public class DensityUtil {

    /**
     * 根据屏幕密度，将dip转换成px(像素)
     * @param context
     * @param dip
     * @return
     */
    public static int dip2px(Context context,float dip){
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip*density+0.5f);
    };

    /**
     * 根据屏幕密度，将px转换成dip
     * @param context
     * @param px
     * @return
     */
    public static int px2dip(Context context,float px){
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (px/density+0.5f);
    }
}
