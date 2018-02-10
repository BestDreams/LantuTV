package com.dream.utils;

import com.dream.bean.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/2/10.
 */

public class LyricUtils {

    /**
     * 得到歌词数据
     * @param file
     * @return
     */
    public List<Lyric> getLyricData(File file){
        List<Lyric> lyricList=null;
        if (file!=null&&file.exists()){
            lyricList=new ArrayList<>();
            try {
                FileInputStream fileInputStream=new FileInputStream(file);
                InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line="";
                while ((line=bufferedReader.readLine())!=null){
                    getLyricBeanFormLine(lyricList,line);
                }
                lyricSort(lyricList);
                setSleepTime(lyricList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return lyricList;
    }

    /**
     * 歌词排序
     */
    public void lyricSort(List<Lyric> lyricList){
        Collections.sort(lyricList, new Comparator<Lyric>() {
            @Override
            public int compare(Lyric o1, Lyric o2) {
                if (o1.getTimePoint()<o2.getTimePoint()){
                    return -1;
                }else if (o1.getTimePoint()>o2.getTimePoint()){
                    return 1;
                }
                return 0;
            }
        });
    }

    public void setSleepTime(List<Lyric> lyricList){
        for (int i = 1; i < lyricList.size(); i++) {
            Lyric preLyric=lyricList.get(i-1);
            Lyric nextLyric=lyricList.get(i);
            preLyric.setSleepTime(nextLyric.getTimePoint()-preLyric.getTimePoint());
        }
    }

    /**
     * 得到每一行的歌词对象
     * @param line
     * @return
     */
    private void getLyricBeanFormLine(List<Lyric> lyricList,String line){
        if (line==null||line.equals("")||line.length()<=10){
            return;
        }else{
            String timeStr="";
            int startIndex=line.indexOf("[");
            int endIndex=line.indexOf("]");
            int lastIndex=line.lastIndexOf("]");
            String content=line.substring(lastIndex+1,line.length());
            while (endIndex!=-1){
                timeStr=line.substring(startIndex+1,endIndex);
                long timePoint=getTimePointByTimeStr(timeStr);
                lyricList.add(new Lyric(content,timePoint,0));
                line=line.substring(endIndex+1,line.length());
                startIndex=line.indexOf("[");
                endIndex=line.indexOf("]");
            }
        }
    }

    /**
     * 时间转换成时间戳
     * @param timeStr
     * @return
     */
    private long getTimePointByTimeStr(String timeStr) {
        String[] temp_pre = timeStr.split(":");
        long timePoint=0;
        timePoint+=Integer.parseInt(temp_pre[0])*60*1000;
        String[] temp_back = temp_pre[1].split("\\.");
        timePoint+=Integer.parseInt(temp_back[0])*1000;
        timePoint+=Integer.parseInt(temp_back[1]+"0");
        return timePoint;
    }

}
