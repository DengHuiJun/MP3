package com.zero.mp3.beans;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * 音乐类，展示如何实现Parcelable
 * Created by zero on 15-8-2.
 */
public class Music implements Parcelable {
    private long id;  // 音乐Id
    private String title; //歌曲名
    private String airtist; //歌手名
    private long duration;     //时长
    private long size;      //文件大小
    private String url;  //文件路径
    private int pausePosition = 0;

    public int getPausePosition() {
        return pausePosition;
    }

    public void setPausePosition(int pausePosition) {
        this.pausePosition = pausePosition;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(airtist);
        dest.writeLong(duration);
        dest.writeLong(size);
        dest.writeString(url);
        dest.writeInt(pausePosition);
    }

    public static final Parcelable.Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public Music(Parcel in){
        id = in.readLong();
        title = in.readString();
        airtist = in.readString();
        duration = in.readLong();
        size = in.readLong();
        url = in.readString();
        pausePosition = in.readInt();
    }

    public Music(){

    }

    public String toJSON() {
        String temp = "";

        return temp;
    }
}
