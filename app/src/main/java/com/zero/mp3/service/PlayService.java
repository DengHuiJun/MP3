package com.zero.mp3.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.zero.mp3.R;
import com.zero.mp3.Utils.L;
import com.zero.mp3.activites.MainActivity;
import com.zero.mp3.app.AppContext;

/**
 * 播放音乐的服务
 * Created by zero on 15-8-2.
 */
public class PlayService extends Service {

    private final static String TAG = "PlayService";

    private MediaPlayer mMediaPlayer;

    private String mMusicPath;

    private int mPausePosition; //记录停顿的时间

    private int mPlayCode; //从Activity传递来的播放指令

    private int mMusicId; //播放音乐的序号

    private boolean isPause; // 是否暂停

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mPausePosition = 0;
        mMusicId = 0; //默认等于0
        isPause = false;
        showNotification();
    }

    /**
     * 前台服务，防止被后台杀死
     */
    private void showNotification(){
        Notification.Builder localBuilder = new Notification.Builder(this);
        localBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        localBuilder.setAutoCancel(false);
        localBuilder.setSmallIcon(R.drawable.ic_app);
        localBuilder.setTicker("Foreground Service Start");
        localBuilder.setContentTitle("ZERO-极致音乐");
        localBuilder.setContentText("正在播放歌曲");
        startForeground(1, localBuilder.getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mMediaPlayer.isPlaying()) {
            stop();
        }

        mMusicId = intent.getIntExtra("id",0);

        mMusicPath = intent.getStringExtra("url");

        mPlayCode = intent.getIntExtra("MSG", 0);

        L.d(TAG,"code :"+mPlayCode);

        doPlayAction(mPlayCode,intent);

        //用来防止service被后台kill
        return START_STICKY;
    }

    public void play(int position){
        try {

            isPause = false;

            mMediaPlayer.reset();//把各项参数恢复到初始状态

            mMediaPlayer.setDataSource(mMusicPath);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //音频流类型

            mMediaPlayer.prepareAsync(); //异步加载流媒体

            mMediaPlayer.setOnPreparedListener(new PreparedListener(position));

            mMediaPlayer.setOnCompletionListener(new CompletionListener());

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                   // T.showShort(getApplicationContext(),"播放出错！");
                    L.d(TAG,"MediaPlayer  ERROR!");
                    return false;
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        if(mMediaPlayer != null) {

            mMediaPlayer.stop();
            try {
                mMediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
            L.d(TAG,"action stop()");
        }
    }

    public void pause(){
            L.d(TAG,mMediaPlayer.isPlaying()+"boolean");
//        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPausePosition =  mMediaPlayer.getCurrentPosition();
            isPause = true;
            L.d(TAG,"action pause()："+mPausePosition);
//        }

    }

    /**
     *
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     *
     */
    private class PreparedListener implements MediaPlayer.OnPreparedListener {

        private int positon;  //播放的位置

        public PreparedListener(int positon) {
            this.positon = positon;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayer.start();    //开始播放
            if(positon > 0) {    //如果音乐不是从头播放
                mMediaPlayer.seekTo(positon);
            }
        }
    }

    /**
     * 一首歌播放完成后的监听事件
     */
    private class CompletionListener implements MediaPlayer.OnCompletionListener{

        public CompletionListener(){

        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            L.d(TAG,"播放完成调用");
            sendPlayOverMessage();
        }
    }


    private void doPlayAction(int code,Intent intent){
        switch (code){
            case AppContext.MUSIC_PLAY:
                play(0);
                break;
            case AppContext.MUSIC_PAUSE:
                pause();
                break;
            case AppContext.MUSIC_PAUSE_TO_PLAY:
                play(mPausePosition);
                break;
        }
    }

    /**
     * 发送一个广播，当前音乐播放完成,传递歌曲的序号
     */
    private void sendPlayOverMessage(){
        Intent intent = new Intent(AppContext.SEND_BROADCASR_ACTION);
        intent.putExtra("musicId",mMusicId);
        intent.putExtra("isPause",isPause);
        sendBroadcast(intent);
        L.d(TAG,"sendMessage");
    }

    /**
     * 释放及时资源
     * @param mp
     */
    private void releaseMediePlay(MediaPlayer mp){
        if (mp != null && mp.isPlaying())
        {
         mp.stop();
         mp.release();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediePlay(mMediaPlayer);
    }
}
