package com.zero.mp3.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zero.mp3.R;
import com.zero.mp3.beans.Music;

import java.util.List;

/**
 * 适配器类，用来适配主界面音乐列表的ListView
 * Created by zero on 15-8-2.
 */
public class MusicListAdapter extends BaseAdapter {
    private List<Music>  mData;
    private Context context;
    private LayoutInflater inflater;


    public MusicListAdapter(Context context,List<Music>  mData){
        this.context = context;
        this.mData = mData;
        inflater = LayoutInflater.from(context);
    }

    public void setmData(List<Music> mData) {
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Music music = mData.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.music_item,null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.music_item_title);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.music_item_artist);
            viewHolder.time = (TextView) convertView.findViewById(R.id.music_time_tv);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(music.getTitle());
        viewHolder.artist.setText(music.getAirtist());
        viewHolder.time.setText(longToDate(music.getDuration()));

        return convertView;
    }

    private static class ViewHolder{
        TextView title;
        TextView artist;
        TextView time;
    }

    /**
     * 毫秒-长整型 转为 分钟显示-字符串
     * @param time
     * @return
     */
    private String longToDate(long time){
        String t = "null";
        long min = (time/1000) / 60;
        long s = (time/1000) % 60;
        t = toFormat(min) +":"+ toFormat(s);
        return t;
    }

    /**
     * 将个位数转为2位，如 6 变为 06
     * @param s
     * @return
     */
    private String toFormat(long s){
        String d = "";
        if (s < 10){
            d = "0"+Long.toString(s);
        }else{
            d = Long.toString(s);
        }
        return d;
    }

}
