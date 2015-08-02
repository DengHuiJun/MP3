package com.zero.mp3.service;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.zero.mp3.Utils.L;
import com.zero.mp3.app.AppContext;
import android.app.Service;
/**
 * 播放音乐的服务
 * Created by zero on 15-8-2.
 */
public class PlayService extends Service {

    private final static String TAG = "PlayService";

    private MediaPlayer mediaPlayer;
    private String musicPath;
    private boolean isPause;

    public PlayService(){
//        super("PlayService");
        mediaPlayer = new MediaPlayer();
        isPause = false;
        L.d(TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mediaPlayer.isPlaying()) {
            stop();
        }
        musicPath = intent.getStringExtra("url");
        int msg = intent.getIntExtra("MSG", 0);
        if(msg == AppContext.MUSIC_PLAY) {
            play(0);
        } else if(msg == AppContext.MUSIC_PAUSE) {
            pause();
        } else if(msg == AppContext.MUSIC_STOP) {
            stop();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void play(int position){
        try {
            mediaPlayer.reset();//把各项参数恢复到初始状态
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();  //进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(position));//注册一个监听器
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pause(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    /**
     *
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     *
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int positon;

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();    //开始播放
            if(positon > 0) {    //如果音乐不是从头播放
                mediaPlayer.seekTo(positon);
            }
        }
    }
}
