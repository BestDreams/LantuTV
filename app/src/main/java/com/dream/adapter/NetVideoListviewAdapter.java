package com.dream.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dream.bean.NetVideo;
import com.dream.lantutv.R;
import com.dream.utils.MyUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/1/2.
 */

public class NetVideoListviewAdapter extends BaseAdapter {

    private Context context;
    private List<NetVideo> list;

    public NetVideoListviewAdapter(Context context, List<NetVideo> list) {
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
            convertView=View.inflate(context, R.layout.netvideo_listview_item,null);
            viewHodler=new ViewHodler();
            viewHodler.pic= (ImageView) convertView.findViewById(R.id.netvideo_pic);
            viewHodler.name= (TextView) convertView.findViewById(R.id.netvideo_name);
            viewHodler.times= (TextView) convertView.findViewById(R.id.netvideo_times);
            viewHodler.duration= (TextView) convertView.findViewById(R.id.netvideo_duration);
            convertView.setTag(viewHodler);
        }else{
            viewHodler= (ViewHodler) convertView.getTag();
        }
        NetVideo netVideo =list.get(position);
        if (netVideo!=null){
            Glide.with(context)
                    .load(netVideo.getPic())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(viewHodler.pic);
            viewHodler.name.setText(netVideo.getName());
            viewHodler.times.setText(netVideo.getPraiseTimes()+"次点赞");
            viewHodler.duration.setText(MyUtils.secondToMinute(Integer.parseInt(netVideo.getDuration())));
        }
        return convertView;
    }

    class ViewHodler{
        ImageView pic;
        TextView name;
        TextView times;
        TextView duration;
    }
}
