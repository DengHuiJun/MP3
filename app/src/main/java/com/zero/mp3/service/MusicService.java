package com.zero.mp3.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * 重新写音乐服务，继承IntentService
 * Created by zero on 15-10-12.
 */
public class MusicService extends IntentService{
    private static final String TAG = "MusicService";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * Used to name the worker thread, important only for debugging.
     */
    public MusicService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
