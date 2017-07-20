package com.zero.mp3.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.zero.mp3.R;
import com.zero.mp3.Utils.FirstLetterUtil;
import com.zero.mp3.Utils.L;
import com.zero.mp3.Utils.PlayUtils;
import com.zero.mp3.Utils.T;
import com.zero.mp3.adapter.MusicListAdapter;
import com.zero.mp3.app.ConstantValue;
import com.zero.mp3.beans.Music;
import com.zero.mp3.service.PlayService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private final static String TAG = "MainActivity";

    private List<Music> mMusics;
    private MusicListAdapter mAdapter;

    private boolean mIsPlaying;

    private Map<String, String> multPronounceMap = new HashMap<>();

    private int mPlayCode; //播放模式

    private Music mCurrentMusic;

    private Messenger mPlayService;
    private boolean mBound = false;

    private Messenger mGetMsgFromService = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == ConstantValue.MUSIC_PLAY_FINISH) {
                nextMusic();
            }
            super.handleMessage(msg);

        }
    });

    @Bind(R.id.toolbar) Toolbar mToolbar;

    //加载音乐的进度条
    @Bind(R.id.update_music_pb) ProgressBar mUpdatePBar;

    //主列表
    @Bind(R.id.music_list_lv) ListView mMusicListView;

    //下一曲
    @Bind(R.id.music_function_next_iv) ImageView mNextIv;

    //播放或暂停
    @Bind(R.id.music_function_play_iv) ImageView mPlayIv;

    //上一曲
    @Bind(R.id.music_function_previous_iv) ImageView mPreviousIv;

    //底部显示歌曲名
    @Bind(R.id.music_title_tv) TextView mBottomTitle;

    //底部显示歌手名
    @Bind(R.id.singer_name_tv) TextView mBottomName;

    @Bind(R.id.add_fab) FloatingActionButton mAddFAB;

    @Bind(R.id.main_bottom_timer_ll) View mBottomTimerLl;

    @Bind(R.id.no_tip_ly) View mNoTipLy;

    @Bind(R.id.main_bottom_ly) View mMainBottomLy;

    @Bind(R.id.list_content) View mListContentFl;

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, PlayService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initLastTimeMusic();
        initToolBar();
        initListener();
        initData();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mPlayService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mPlayService = null;
            mBound = false;
        }
    };


    /**
     * 首次打开获取最后一次播放的音乐
     */
    private void initLastTimeMusic() {
        mCurrentMusic = PlayUtils.getMusicByPf(this);
    }

    private void initListener() {
        mMusicListView.setOnItemClickListener(this);
    }

    private void initToolBar() {
        mToolbar.setLogo(R.drawable.ic_toolbar);
        mToolbar.setTitle("ZERO系列");
        mToolbar.setSubtitle("极致音乐");
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    public void initData() {

        generateMultPronounceMap();

        mIsPlaying = false;

        mPlayCode = ConstantValue.MUSIC_REPEAT;

        mMusics = new ArrayList<>();
        mAdapter = new MusicListAdapter(this, mMusics);
        mAddFAB.attachToListView(mMusicListView);

        mMusicListView.setAdapter(mAdapter);
        mMusicListView.setFastScrollEnabled(true);

        new LoadMusicDataTask().execute();
    }

    /**
     * 从内存卡中读取音乐列表
     * @return
     */
    public void getDataFromSD() {
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

            if (cursor == null) {
                return;
            }
            Music music;
            while (cursor.moveToNext()) {
                music = new Music();
                long id = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media._ID));   // 音乐id
                String title = cursor.getString((cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
                String artist = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
                long duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
                long size = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media.SIZE));  // 文件大小
                String url = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));   // 文件路径
                int isMusic = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否为音乐

                if (isMusic != 0) {
                    music.setId(id);
                    music.setTitle(title);
                    music.setAirtist(artist);
                    music.setDuration(duration);
                    music.setSize(size);
                    music.setUrl(url);
                    mMusics.add(music);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music music = mMusics.get(position);
        mCurrentMusic = music;
        sendMsgToService(ConstantValue.MUSIC_PLAY, music);

        refreshBottomDisplay();
        mPlayIv.setImageResource(R.drawable.ic_action_playback_play);
        mBottomName.setFocusable(true);
        mBottomName.setFocusableInTouchMode(true);
    }

    /**
     * 从数据库读取音乐的任务
     */
    private class  LoadMusicDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mMainBottomLy.setVisibility(View.GONE);
            mListContentFl.setVisibility(View.GONE);
            mUpdatePBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            getDataFromSD();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            L.d(TAG, "onPostExecute");
            mUpdatePBar.setVisibility(View.GONE);

            if (mMusics.size() > 1) {
                Collections.sort(mMusics, new Comparator<Music>() {
                    @Override
                    public int compare(Music lhs, Music rhs) {
                        String l = hanziToPinyin(String.valueOf(lhs.getTitle().charAt(0)));
                        String r = hanziToPinyin(String.valueOf(rhs.getTitle().charAt(0)));
                        return l.compareTo(r);
                    }
                });
            }

            if (mMusics.isEmpty()) {
                mNoTipLy.setVisibility(View.VISIBLE);
                mListContentFl.setVisibility(View.GONE);
                mMainBottomLy.setVisibility(View.GONE);
            } else {
                mNoTipLy.setVisibility(View.GONE);
                mListContentFl.setVisibility(View.VISIBLE);
                mMainBottomLy.setVisibility(View.VISIBLE);

                // 之前没有播发记录
                if (mCurrentMusic == null) {
                    mCurrentMusic = mMusics.get(0);
                } else {
                    if (!mMusics.contains(mCurrentMusic)) {
                        mCurrentMusic = mMusics.get(0);
                    }
                }
                refreshBottomDisplay();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private String hanziToPinyin(String input) {
        if (input.length() > 1) {
            input = input.substring(0,1);
        }

        String pinYinCode = FirstLetterUtil.getFirstLetter(input);

        if (pinYinCode != null) {
            if (pinYinCode.length() > 1) {
                pinYinCode = pinYinCode.substring(0, 1);
            }
        } else {
            pinYinCode = "";
        }

        String firstLetter = multPronounceMap.get(input);
        // 对于某些多音字，映射到指定首字母
        if (!TextUtils.isEmpty(firstLetter)) {
            return firstLetter;
        }
        return pinYinCode.toUpperCase();
    }

    // 多音字转化
    private void generateMultPronounceMap(){
        multPronounceMap.put("阿", "A");
        multPronounceMap.put("朝", "C");
        multPronounceMap.put("哈", "H");
        multPronounceMap.put("红", "H");
        multPronounceMap.put("会", "H");
        multPronounceMap.put("乐", "L");
        multPronounceMap.put("齐", "Q");
        multPronounceMap.put("信", "X");
        multPronounceMap.put("长", "C");
        multPronounceMap.put("广", "G");
        multPronounceMap.put("单", "D");
    }

    /**
     * 在底部设置正在播放的曲名与作者
     */
    public void refreshBottomDisplay() {
        mBottomTitle.setText(mCurrentMusic.getTitle());
        mBottomName.setText(mCurrentMusic.getAirtist());
    }

    /**
     * 绑定播放键事件（暂停）
     */
    @OnClick(R.id.music_function_play_iv)
    public void playMusic() {
        if (mIsPlaying) {

            sendMsgToService(ConstantValue.MUSIC_PAUSE, mCurrentMusic);

            mPlayIv.setImageResource(R.drawable.ic_action_playback_pause);
            mBottomName.setFocusable(false);
            mBottomName.setFocusableInTouchMode(false);
        } else {

            sendMsgToService(ConstantValue.MUSIC_PLAY, mCurrentMusic);

            mPlayIv.setImageResource(R.drawable.ic_action_playback_play);
            mBottomName.setFocusable(true);
            mBottomName.setFocusableInTouchMode(true);
            mIsPlaying = true;
        }
    }

    /**
     * 绑定添加事件【最后考虑为歌曲播放模式，循环或者随机】
     */
    @OnClick(R.id.add_fab)
    public void setPlayMode() {
        mPlayCode = (mPlayCode +1) % 3;
        musicPlayMode(mPlayCode);

    }

    //下一曲
    @OnClick(R.id.music_function_next_iv)
    public void nextMusic() {
        T.showShort(this, "下一曲");
        int nextPosition = (mMusics.indexOf(mCurrentMusic) + 1) % mMusics.size();
        mCurrentMusic = mMusics.get(nextPosition);
        mCurrentMusic.setPausePosition(0);

        sendMsgToService(ConstantValue.MUSIC_NEXT, mCurrentMusic);

        refreshBottomDisplay();
    }

    //上一曲
    @OnClick(R.id.music_function_previous_iv)
    public void previousMusic() {
        T.showShort(this, "上一曲");
        int previousPosition = (mMusics.indexOf(mCurrentMusic) - 1) % mMusics.size();
        if (previousPosition < 0) {
            previousPosition = 0;
        }
        mCurrentMusic = mMusics.get(previousPosition);

        sendMsgToService(ConstantValue.MUSIC_PREVIOUS, mCurrentMusic);
        refreshBottomDisplay();
    }

    @OnClick(R.id.music_show_ll)
    public void showTimer() {
        if (mBottomTimerLl.getVisibility() == View.VISIBLE) {
            mBottomTimerLl.setVisibility(View.GONE);
        } else {
            mBottomTimerLl.setVisibility(View.VISIBLE);
        }
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            String msg = "";
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    msg += "Click set";
                    break;
            }
            if(!msg.equals("")) {
                T.showShort(getApplicationContext(),msg);
            }
            return true;
        }
    };

    /**
     * 给后台服务发送指令
     */
    public void sendMsgToService(int code, Music music) {
        if (!mBound || mCurrentMusic == null)
            return;
        try {

            if (code == ConstantValue.MUSIC_PLAY) {
                mIsPlaying = true;
            } else if (code == ConstantValue.MUSIC_PAUSE) {
                mIsPlaying = false;
            }

            Message msg = Message.obtain();
            msg.what = code;
            msg.obj = music;
            msg.replyTo = mGetMsgFromService;
            mPlayService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置播放模式
     */
    private void musicPlayMode(int code) {
        switch (code) {
            case ConstantValue.MUSIC_REPEAT:
                T.showShort(getApplicationContext(), "切换到列表循环");
                mPlayCode = ConstantValue.MUSIC_REPEAT;
                mAddFAB.setImageResource(R.drawable.ic_action_playback_repeat);
                break;
            case ConstantValue.MUSIC_REPEAT_ONE:
                T.showShort(getApplicationContext(), "切换到单曲循环");
                mPlayCode = ConstantValue.MUSIC_REPEAT_ONE;
                mAddFAB.setImageResource(R.drawable.ic_action_playback_repeat_1);
                break;
            case ConstantValue.MUSIC_RANDOM:
                T.showShort(getApplicationContext(), "切换到随机播放");
                mPlayCode = ConstantValue.MUSIC_RANDOM;
                mAddFAB.setImageResource(R.drawable.ic_action_playback_schuffle);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);

        if (mCurrentMusic != null) {
            PlayUtils.saveMusicByPf(this, mCurrentMusic);
        }
        super.onDestroy();
    }

    /**
     * 获取随机数
     * @param length
     * @return
     */
    public int getRandomId(int length) {
        return (int)(Math.random()*1000)%length;
    }
}
