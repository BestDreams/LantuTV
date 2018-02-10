package com.dream.bean;

/**
 * Created by Administrator on 2018/2/4.
 */

public class Lyric {

    private String content;
    private float timePoint;
    private float sleepTime;

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

    public float getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(float timePoint) {
        this.timePoint = timePoint;
    }

    public float getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(float sleepTime) {
        this.sleepTime = sleepTime;
    }
}
