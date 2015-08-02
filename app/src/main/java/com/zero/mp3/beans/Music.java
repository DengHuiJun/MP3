package com.zero.mp3.beans;

/**
 * 音乐类
 * Created by zero on 15-8-2.
 */
public class Music {
    private long id;  // 音乐Id
    private String title; //歌曲名
    private String airtist; //歌手名
    private long duration;     //时长
    private long size;      //文件大小
    private String url;  //文件路径

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAirtist() {
        return airtist;
    }

    public void setAirtist(String airtist) {
        this.airtist = airtist;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
