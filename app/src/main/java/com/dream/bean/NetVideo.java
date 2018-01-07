package com.dream.bean;

import java.io.Serializable;

/**
 * Created by ZhangPing on 2018/1/5.
 */

public class NetVideo implements Serializable{
    private String contId;
    private String name;
    private String pic;
    private String praiseTimes;
    private String duration;
    private String video;

    public NetVideo() {
    }

    public NetVideo(String contId, String name, String pic, String praiseTimes, String duration, String video) {
        this.contId = contId;
        this.name = name;
        this.pic = pic;
        this.praiseTimes=praiseTimes;
        this.duration=duration;
        this.video = video;
    }

    public String getContId() {
        return contId;
    }

    public void setContId(String contId) {
        this.contId = contId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPraiseTimes() {
        return praiseTimes;
    }

    public void setPraiseTimes(String praiseTimes) {
        this.praiseTimes = praiseTimes;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    @Override
    public String toString() {
        return "NetVideo{" +
                "contId='" + contId + '\'' +
                ", name='" + name + '\'' +
                ", pic='" + pic + '\'' +
                ", duration='" + duration + '\'' +
                ", praiseTimes='" + praiseTimes + '\'' +
                ", video='" + video + '\'' +
                '}';
    }
}
