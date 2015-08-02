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

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(music.getTitle());
        viewHolder.artist.setText(music.getAirtist());

        return convertView;
    }

    private static class ViewHolder{
        TextView title;
        TextView artist;
    }
}
