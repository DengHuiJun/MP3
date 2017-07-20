package com.zero.mp3.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.zero.mp3.R;
import com.zero.mp3.Utils.L;
import com.zero.mp3.activities.MainActivity;
import com.zero.mp3.app.ConstantValue;
import com.zero.mp3.beans.Music;

/**
 * 播放音乐的服务
 * Created by zero on 15-8-2.
 */
public class PlayService extends Service {

    public final static String TAG = "PlayService";

    private MediaPlayer mMediaPlayer;

    private Music mCurrentMusic;

    // 如果有多个Client 可以使用List管理起来
    private Messenger mClientMessenger;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("zero", "get:" + msg.what);
            mCurrentMusic = (Music) msg.obj;
            mClientMessenger = msg.replyTo;
            switch (msg.what) {
                case ConstantValue.MUSIC_PLAY:
                    play();
                    break;

                case ConstantValue.MUSIC_NEXT:
                    play();
                    break;

                case ConstantValue.MUSIC_PREVIOUS:
                    play();
                    break;

                case ConstantValue.MUSIC_PAUSE:
                    pause();
                    break;

                case ConstantValue.MUSIC_STOP:
                    stop();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        //用来防止service被后台kill
        return START_STICKY;
    }

    public void play() {
        try {
            releaseMediaPlay(mMediaPlayer);

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mCurrentMusic.getUrl());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); //音频流类型
//            mMediaPlayer.prepareAsync(); //异步加载流媒体
            mMediaPlayer.prepare();
            if (mCurrentMusic.getPausePosition() > 0) {
                mMediaPlayer.seekTo(mCurrentMusic.getPausePosition());
            }
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new CompletionListener());
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
//                    T.showShort(getApplicationContext(), "播放出错");
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
        mCurrentMusic.setPausePosition(mMediaPlayer.getCurrentPosition());
        mMediaPlayer.pause();
    }

//    /**
//     *
//     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
//     *
//     */
//    private class PreparedListener implements MediaPlayer.OnPreparedListener {
//
//        private int position;  //播放的位置
//
//        public PreparedListener(int positon) {
//            this.position = positon;
//        }
//
//        @Override
//        public void onPrepared(MediaPlayer mp) {
//            mMediaPlayer.start();    //开始播放
//            if(position > 0) {    //如果音乐不是从头播放
//                mMediaPlayer.seekTo(position);
//            }
//        }
//    }

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

    /**
     * 发送消息，当前音乐播放完成,传递歌曲的序号
     */
    private void sendPlayOverMessage(){
        try {
            Message msg = Message.obtain();
            msg.what = ConstantValue.MUSIC_PLAY_FINISH;
            mClientMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放及时资源
     * @param mp
     */
    private void releaseMediaPlay(MediaPlayer mp){
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlay(mMediaPlayer);
    }
}
