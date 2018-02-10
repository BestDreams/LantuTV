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
import android.util.Base64;

import com.dream.bean.NetVideo;

import org.xutils.http.RequestParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/1/2.
 */

public class MyUtils {

    public static SimpleDateFormat simpleDateFormatWithHour;
    public static SimpleDateFormat simpleDateFormatWithMinute;

    public static void initSimpleDataWithHourFormatTimeZone(){
        simpleDateFormatWithHour=new SimpleDateFormat("HH:mm:ss");
        simpleDateFormatWithHour.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    public static void initSimpleDataWithMinuteFormatTimeZone(){
        simpleDateFormatWithMinute=new SimpleDateFormat("mm:ss");
        simpleDateFormatWithMinute.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }
    
    /**
     * 时间戳转分钟
     * @param ms
     * @return
     */
    public static int timestampToMinuteOnly(long ms){
        return (int)(ms/1000/60);
    };

    /**
     * 时间戳转小时
     */
    public static String timestampToHour(long ms){
        return simpleDateFormatWithHour.format(ms).toString();
    };

    /**
     * 时间戳转分钟
     */
    public static String timestampToMinute(long ms){
        return simpleDateFormatWithMinute.format(ms).toString();
    };


    /**
     * 秒钟转分钟
     */
    public static String secondToMinute(int ms){
        return ((ms/60)<10?("0"+(ms/60)):(ms/60))+":"+((ms%60)<10?("0"+(ms%60)):(ms%60));
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

    /**
     * 得到xUtils带请求头的RequestParam
     */
    public static RequestParams getRequestParam(String url){
        RequestParams requestParams=new RequestParams(url);
        requestParams.addHeader("X-Channel-Code", "official");
        requestParams.addHeader("X-Client-Agent", "Xiaomi");
        requestParams.addHeader("X-Client-Hash", "2f3d6ffkda95dlz2fhju8d3s6dfges3t");
        requestParams.addHeader("X-Client-ID", "123456789123456");
        requestParams.addHeader("X-Client-Version", "2.3.2");
        requestParams.addHeader("X-Long-Token", "");
        requestParams.addHeader("X-Platform-Type", "0");
        requestParams.addHeader("X-Platform-Version", "5.0");
        requestParams.addHeader("X-Serial-Num", "1492140134");
        requestParams.addHeader("X-User-ID", "");
        return requestParams;
    }

    /**
     * 字符串简化
     */
    public static String strToSimple(String oldStr, String regx){
        if (oldStr.contains(regx)){
            return oldStr.replace(regx, "");
        }
        return oldStr;
    }

    /**
     * 文件名去后缀
     */
    public static String fileNameRemoveSuffix(String fileName){
        if(fileName.contains(".")){
            return fileName.substring(0,fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 得到音乐名
     */
    public static String getMusicName(String fileName){
        if (fileName.contains("-")){
            return fileName.split("-")[1].trim();
        }
        return fileName;
    }

    /**
     * 网络视频数据对象转字符串缓存
     */
    public static String mapToString(Map map){
        String result=null;
        try {
            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(map);
            result=new String(Base64.encode(byteArrayOutputStream.toByteArray(),Base64.DEFAULT));
            byteArrayOutputStream.close();
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 字符串缓存转网络视频数据对象
     * @param objectString
     * @return
     */
    public static Map<String,List<NetVideo>> stringToMap(String objectString) {
        Map<String,List<NetVideo>> netVideoNodeMap=null;
        try {
            byte[] bytes = Base64.decode(objectString.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = null;
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            netVideoNodeMap = (Map<String,List<NetVideo>>) objectInputStream.readObject();
            byteArrayInputStream.close();
            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netVideoNodeMap;
    }
}
