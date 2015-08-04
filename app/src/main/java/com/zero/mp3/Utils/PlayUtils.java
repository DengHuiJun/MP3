package com.zero.mp3.Utils;

import android.content.Context;
import android.content.Intent;

import com.zero.mp3.app.AppContext;
import com.zero.mp3.service.PlayService;

/**
 * Created by zero on 15-8-4.
 */
public class PlayUtils {
    public static final int MUSIC_REPEAT = 0 ;
    public static final int MUSIC_REPEAT_ONE = 1;
    public static final int MUSIC_RANDOM = 2;


    public static final int MUSIC_NEXT = 3;
    public static final int MUSIC_PREVIOUS = 4;


    /**
     * 播放音乐的功能
     * @param c
     * @param url
     * @param code
     */
    public static void playMusicIntent(Context c,String url,int code){
        Intent intent = new Intent(c,PlayService.class);
        intent.putExtra("url",url);
        intent.putExtra("MSG", code);
        c.startService(intent);
    }
}
