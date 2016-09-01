package com.zero.mp3.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.zero.mp3.R;
import com.zero.mp3.Utils.FirstLetterUtil;
import com.zero.mp3.Utils.StringMatcher;
import com.zero.mp3.beans.Music;

import java.util.List;

/**
 * 适配器类，用来适配主界面音乐列表的ListView
 * Created by zero on 15-8-2.
 */
public class MusicListAdapter extends BaseAdapter implements SectionIndexer{
    private List<Music>  mData;
    private Context context;
    private LayoutInflater inflater;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public MusicListAdapter(Context context, List<Music> mData){
        this.context = context;
        this.mData = mData;
        inflater = LayoutInflater.from(context);
    }

    public void setData(List<Music> mData) {
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position).getTitle();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Music music = mData.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.music_item, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.music_item_title);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.music_item_artist);
            viewHolder.time = (TextView) convertView.findViewById(R.id.music_time_tv);
            convertView.setTag(viewHolder);
        } else {
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
        if (s < 10) {
            d = "0"+ Long.toString(s);
        } else {
            d = Long.toString(s);
        }
        return d;
    }

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(hanziToPinyin(getItem(j).substring(0,1)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(hanziToPinyin(getItem(j).substring(0,1)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    @SuppressLint("DefaultLocale")
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
        return pinYinCode.toUpperCase();
    }


}
