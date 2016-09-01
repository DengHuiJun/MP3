package com.zero.mp3.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.zero.mp3.R;
import com.zero.mp3.service.PlayService;

/**
 * Created by zero on 15-8-4.
 */
public class PlayUtils {
    private static SharedPreferences mMusicSP;
    public static final String MUSIC_SP_KEY = "musicUrl";
    public static final String MUSIC_SP_DEFAULT = "noUrl";

    /**
     * 播放音乐的功能
     * @param c
     * @param url
     * @param code
     */
    public static void playMusicIntent(Context c, int id, String url, int code){
        Intent intent = new Intent(c, PlayService.class);
        intent.putExtra("id",id);
        intent.putExtra("url",url);
        intent.putExtra("MSG", code);
        c.startService(intent);
    }

    public static void sendPlayCode() {

    }

    public static boolean saveMusicUrlByPf(Context context, String url) {
        mMusicSP = context.getSharedPreferences(context.getResources().getString(R.string.share_preferences_key)
                ,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mMusicSP.edit();
        editor.putString(MUSIC_SP_KEY,url);
        editor.commit();
        return true;
    }

    public static String getMusicUrlByPf(Context context) {
        mMusicSP = context.getSharedPreferences(context.getResources().getString(R.string.share_preferences_key)
                ,Context.MODE_PRIVATE);
        String tempUrl = mMusicSP.getString(MUSIC_SP_KEY,MUSIC_SP_DEFAULT);
        return tempUrl;
    }
}
