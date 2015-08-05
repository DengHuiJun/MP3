package com.zero.mp3.activites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.zero.mp3.R;
import com.zero.mp3.Utils.L;
import com.zero.mp3.Utils.PlayUtils;
import com.zero.mp3.Utils.T;
import com.zero.mp3.adapter.MusicListAdapter;
import com.zero.mp3.app.AppContext;
import com.zero.mp3.beans.Music;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{
    private final static String TAG = "MainActivity";

    private List<Music> mMusics;
    private MusicListAdapter mAdapter;
    private boolean isPlaying;

    private int mMusicCode; //播放模式

    private int currentMusicId; //当前播放歌曲的序号

    private String url="";//暂时存储音乐路径

    private MusicBroadcastReceiver mBroadcastReceiver;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.update_music_pb)
    ProgressBar mUpdatePBar;

    @Bind(R.id.music_list_lv)
    ListView mMusicListView;

    //下一曲
    @Bind(R.id.music_function_next_iv)
    ImageView mNextIv;

    //播放或暂停
    @Bind(R.id.music_function_play_iv)
    ImageView mPlayIv;

    //上一曲
    @Bind(R.id.music_function_previous_iv)
    ImageView mPreviousIv;

    //底部显示歌曲名
    @Bind(R.id.music_title_tv)
    TextView mBottomTitle;

    //底部显示歌手名
    @Bind(R.id.singer_name_tv)
    TextView mBottomName;

    @Bind(R.id.add_fab)
    FloatingActionButton mAddFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        L.d(TAG, TAG);
        initReceiver();
        initData();
        initToolBar();
        mMusicListView.setAdapter(mAdapter);
        mMusicListView.setOnItemClickListener(this);
        new getMusicTask().execute();

    }

    private void initReceiver() {
        mBroadcastReceiver = new MusicBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppContext.SEND_BROADCASR_ACTION);
        registerReceiver(mBroadcastReceiver,filter);
    }

    private void initToolBar() {
        mToolbar.setLogo(R.drawable.ic_toolbar);
        mToolbar.setTitle("ZERO系列");
        mToolbar.setSubtitle("极致音乐");
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    public void initData(){
        isPlaying = false;
        mMusicCode = AppContext.MUSIC_REPEAT;
//        add_fab.attachToListView(mMusicListView);  //贴上ListView，使得滑动隐藏按钮
        mMusics = new ArrayList<>();
        mAdapter = new MusicListAdapter(this,mMusics);
    }

//    /**
//     * 更新音乐列表
//     */
//    public void refreshData(){
//        mUpdatePBar.setVisibility(View.VISIBLE);
//        new getMusicTask().execute();
//    }

    /**
     * 从内存卡中读取音乐列表
     * @return
     */
    public List<Music> getData(){
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Music> musics = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++)
        {
            Music music = new Music();
            cursor.moveToNext();
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));   //文件路径
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            if (isMusic != 0 )
            {
                music.setId(id);
                music.setTitle(title);
                music.setAirtist(artist);
                music.setDuration(duration);
                music.setSize(size);
                music.setUrl(url);
                musics.add(music);
            }
        }
        return musics;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music music = mMusics.get(position);
        url = music.getUrl();

        currentMusicId = position;

        L.d(TAG,"id="+currentMusicId);

        PlayUtils.playMusicIntent(this,currentMusicId,url,AppContext.MUSIC_PLAY);

        setBottomDisplay(music.getTitle(), music.getAirtist());

        mPlayIv.setImageResource(R.drawable.ic_action_playback_play);
        mBottomName.setFocusable(true);
        mBottomName.setFocusableInTouchMode(true);
        isPlaying = true;
    }

    /**
     * 从数据库读取音乐的任务
     */
    private class  getMusicTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            L.d(TAG,"doInBackground");
            mMusics = getData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            L.d(TAG, "onPostExecute");
            mAdapter.setmData(mMusics);
            mAdapter.notifyDataSetChanged();
            mUpdatePBar.setVisibility(View.GONE);
            setBottomDisplay(mMusics.get(0).getTitle(),mMusics.get(0).getAirtist());
        }
    }

    /**
     * 在底部设置正在播放的曲名与作者
     * @param title
     * @param artist
     */
    public void setBottomDisplay(String title,String artist){
        mBottomTitle.setText(title);
        mBottomName.setText(artist);
    }

    /**
     * 绑定播放键事件（暂停）
     */
    @OnClick(R.id.music_function_play_iv)
    public void playMusic(){
        if (isPlaying)
        {
            PlayUtils.playMusicIntent(this,currentMusicId,url,AppContext.MUSIC_PAUSE);
            L.d(TAG,"Send to service:pause");
            mPlayIv.setImageResource(R.drawable.ic_action_playback_pause);
            mBottomName.setFocusable(false);
            mBottomName.setFocusableInTouchMode(false);
            isPlaying= false;
        }else{
            PlayUtils.playMusicIntent(this,currentMusicId,url,AppContext.MUSIC_PAUSE_TO_PLAY);
            L.d(TAG, "Send to service:pauseToPlay");
            mPlayIv.setImageResource(R.drawable.ic_action_playback_play);
            mBottomName.setFocusable(true);
            mBottomName.setFocusableInTouchMode(true);
            isPlaying = true;
        }
    }

    /**
     * 绑定添加事件【最后考虑为歌曲播放模式，循环或者随机】
     */
    @OnClick(R.id.add_fab)
    public void setPlayMode(){
        mMusicCode = (mMusicCode+1) % 3;
        musicPlayMode(mMusicCode);

    }

    //下一曲
    @OnClick(R.id.music_function_next_iv)
    public void nextMusic(){
       // T.showShort(this,"next music");
        currentMusicId = (currentMusicId + 1) % mMusics.size();
        url = mMusics.get(currentMusicId).getUrl();
        PlayUtils.playMusicIntent(this, currentMusicId,url, AppContext.MUSIC_PLAY);
        setBottomDisplay(mMusics.get(currentMusicId).getTitle(), mMusics.get(currentMusicId).getAirtist());
        L.d(TAG,"nextId:" + currentMusicId);
    }

    //上一曲
    @OnClick(R.id.music_function_previous_iv)
    public void previousMusic(){
        T.showShort(this, "prev music");
        currentMusicId = (currentMusicId - 1) % mMusics.size();
        if (currentMusicId == -1){
            currentMusicId = mMusics.size()-1;
        }
        url = mMusics.get(currentMusicId).getUrl();
        PlayUtils.playMusicIntent(this, currentMusicId,url, AppContext.MUSIC_PLAY);
        setBottomDisplay(mMusics.get(currentMusicId).getTitle(), mMusics.get(currentMusicId).getAirtist());
        L.d(TAG, "previousId:" + currentMusicId );
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
     * 音乐播放模式
     */
    private void musicPlayMode(int code){
        switch (code){
            case AppContext.MUSIC_REPEAT:
                T.showShort(getApplicationContext(),"切换到列表循环");
                mMusicCode = AppContext.MUSIC_REPEAT;
                mAddFAB.setImageResource(R.drawable.ic_action_playback_repeat);
                break;
            case AppContext.MUSIC_REPEAT_ONE:
                T.showShort(getApplicationContext(),"切换到单曲循环");
                mMusicCode = AppContext.MUSIC_REPEAT_ONE;
                mAddFAB.setImageResource(R.drawable.ic_action_playback_repeat_1);
                break;
            case AppContext.MUSIC_RANDOM:
                T.showShort(getApplicationContext(),"切换到随机播放");
                mMusicCode = AppContext.MUSIC_RANDOM;
                mAddFAB.setImageResource(R.drawable.ic_action_playback_schuffle);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * 广播接收器，用来监听音乐的播放情况
     */
    public class  MusicBroadcastReceiver extends BroadcastReceiver{

        public MusicBroadcastReceiver(){
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            int id = intent.getIntExtra("musicId",0);
            boolean isPause = intent.getBooleanExtra("isPause", true);

            L.d(TAG,"Receive id :"+id);

            if (isPause){
            }
            else
            {
                switch (mMusicCode) {
                    case AppContext.MUSIC_REPEAT:
                        currentMusicId = id;
                        nextMusic();
                        break;
                    case AppContext.MUSIC_REPEAT_ONE:
                        currentMusicId = id;
                        url = mMusics.get(currentMusicId).getUrl();
                        PlayUtils.playMusicIntent(MainActivity.this,id,url, AppContext.MUSIC_PLAY);
                        setBottomDisplay(mMusics.get(currentMusicId).getTitle(), mMusics.get(currentMusicId).getAirtist());
                        break;
                    case AppContext.MUSIC_RANDOM:
                        currentMusicId = getRandomId(mMusics.size());
                        url = mMusics.get(currentMusicId).getUrl();
                        PlayUtils.playMusicIntent(MainActivity.this,id,url, AppContext.MUSIC_PLAY);
                        setBottomDisplay(mMusics.get(currentMusicId).getTitle(), mMusics.get(currentMusicId).getAirtist());
                        break;
                }
            }

        }
    }

    /**
     * 获取随机数
     * @param length
     * @return
     */
    public int getRandomId(int length){
        return (int)(Math.random()*1000)%length;
    }
}
