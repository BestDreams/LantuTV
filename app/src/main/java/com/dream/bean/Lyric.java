package com.dream.bean;

/**
 * Created by Administrator on 2018/2/4.
 */

public class Lyric {

    private String content;
    private long timePoint;
    private long sleepTime;

    public Lyric(String content, long timePoint, long sleepTime) {
        this.content = content;
        this.timePoint = timePoint;
        this.sleepTime = sleepTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }
}
