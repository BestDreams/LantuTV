package com.dream.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/1/2.
 */

public class Media implements Serializable{
    private int id;
    private String display_name;
    private long duration;
    private long size;
    private String data;
    private String artist;

    public Media() {
    }

    public Media(int id,String display_name, long duration, long size, String data, String artist) {
        this.id=id;
        this.display_name = display_name;
        this.duration = duration;
        this.size = size;
        this.data = data;
        this.artist = artist;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
