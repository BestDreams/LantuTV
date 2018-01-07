package com.dream.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;

import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2018/1/2.
 */

public class MyUtils {
    public static SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
    /**
     * 时间戳转分钟
     * @param ms
     * @return
     */
    public static int timestampToMinute(long ms){
        return (int)(ms/1000/60);
    };

    /**
     * 时间戳转时间
     */
    public static String timestampToTime(long ms){
        return simpleDateFormat.format(ms).toString();
    };

    /**
     * 秒钟转分钟
     */
    public static String secondToMinute(int ms){
        return ms/60+":"+ms%60;
    }

    /**
     * 请求用户授权权限
     */
    public static int REQUEST_CODE=1001;
    public static boolean isGrantExternalRW(final Activity activity){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int i = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (i!=PackageManager.PERMISSION_GRANTED){
                new AlertDialog.Builder(activity)
                        .setTitle("提示")
                        .setCancelable(false)
                        .setMessage("蓝图TV运行需要获取必要的权限，否则您将无法正常使用蓝图TV")
                        .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    activity.requestPermissions(new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                    },REQUEST_CODE);
                                }
                            }
                        })
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.finish();
                            }
                        })
                        .show();
                return false;
            }
            return true;
        }
        return true;
    };

    /**
     * 是否网络视频
     * @param uri
     * @return
     */
    public static boolean isNetUri(Uri uri){
        if (uri!=null){
            String uriStr = uri.toString().toLowerCase();
            if (uriStr.startsWith("http")||uriStr.startsWith("rtsp")||uriStr.startsWith("mms")){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前网速
     * @param context
     * @return
     */
    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;
    public static String getNetSpeed(Context context) {
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid)==TrafficStats.UNSUPPORTED ? 0 :(TrafficStats.getTotalRxBytes()/1024);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
       return speed+"kb/s";
    }
}
