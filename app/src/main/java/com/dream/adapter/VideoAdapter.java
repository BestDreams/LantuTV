package com.dream.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dream.bean.Media;
import com.dream.lantutv.R;
import com.dream.utils.MyUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/1/2.
 */

public class VideoAdapter extends BaseAdapter {

    private Context context;
    private List<Media> list;

    public VideoAdapter(Context context, List<Media> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler viewHodler;
        if (convertView==null){
            convertView=View.inflate(context, R.layout.video_listview_item,null);
            viewHodler=new ViewHodler();
            viewHodler.name= (TextView) convertView.findViewById(R.id.video_name);
            viewHodler.time= (TextView) convertView.findViewById(R.id.video_time);
            viewHodler.size= (TextView) convertView.findViewById(R.id.video_size);
            convertView.setTag(viewHodler);
        }else{
            viewHodler= (ViewHodler) convertView.getTag();
        }
        Media media=list.get(position);
        viewHodler.name.setText(media.getDisplay_name());
        viewHodler.time.setText("时长："+MyUtils.timestampToMinute(media.getDuration())+"分钟");
        viewHodler.size.setText("大小："+ Formatter.formatFileSize(context,media.getSize()));
        return convertView;
    }

    class ViewHodler{
        TextView name;
        TextView time;
        TextView size;
    }
}
