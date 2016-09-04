package com.zero.mp3.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.zero.mp3.R;
import com.zero.mp3.beans.Music;

/**
 * Created by zero on 15-8-4.
 */
public class PlayUtils {
    private static SharedPreferences mMusicSP;
    public static final String MUSIC_SP_KEY = "key_save_music";
    public static final String MUSIC_SP_DEFAULT = "no_music";

    public static boolean saveMusicByPf(Context context, Music music) {
        mMusicSP = context.getSharedPreferences(context.getResources().getString(R.string.share_preferences_key)
                ,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mMusicSP.edit();
        String str = new Gson().toJson(music);
        editor.putString(MUSIC_SP_KEY, str);
        editor.apply();
        return true;
    }

    public static Music getMusicByPf(Context context) {
        mMusicSP = context.getSharedPreferences(context.getResources().getString(R.string.share_preferences_key)
                ,Context.MODE_PRIVATE);
        String str = mMusicSP.getString(MUSIC_SP_KEY, MUSIC_SP_DEFAULT);
        if (str.equals(MUSIC_SP_DEFAULT)) {
            return null;
        }
        return new Gson().fromJson(str, Music.class);
    }
}
