package com.dream.fragment;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dream.adapter.VideoAdapter;
import com.dream.base.BaseFragment;
import com.dream.bean.LocalVideo;
import com.dream.lantutv.R;
import com.dream.lantutv.SystemVideoPlayer;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class VideoFragment extends BaseFragment implements AdapterView.OnItemClickListener {


    public VideoFragment() {
    }

    private ListView videoListview;
    private RelativeLayout videoLoading;
    private LinearLayout videoEmpty;

    @Override
    public View initView() {
        View view = View.inflate(activity,R.layout.fragment_video, null);
        videoListview = (ListView) view.findViewById(R.id.video_listview);
        videoListview.setOnItemClickListener(this);
        videoLoading = (RelativeLayout) view.findViewById(R.id.video_loading);
        videoEmpty = (LinearLayout) view.findViewById(R.id.video_empty);
        initData();
        return view;
    }

    @Override
    public void initData() {
        getMediaData();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            videoLoading.setVisibility(View.GONE);
            if (list==null||list.size()==0){
                videoListview.setVisibility(View.GONE);
                videoEmpty.setVisibility(View.VISIBLE);
            }else{
                videoEmpty.setVisibility(View.GONE);
                videoListview.setVisibility(View.VISIBLE);
                videoListview.setAdapter(new VideoAdapter(activity,list));
            }
        }
    };

    /**
     * 扫描加载本地视频
     */
    private ArrayList<LocalVideo> list;
    public void getMediaData(){
        videoLoading.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        list=new ArrayList<>();
                        ContentResolver contentResolver = activity.getContentResolver();
                        String[] scannerArray={
                                MediaStore.Video.Media._ID, //视频ID
                                MediaStore.Video.Media.DISPLAY_NAME, //视频名称
                                MediaStore.Video.Media.DURATION, //视频时长
                                MediaStore.Video.Media.SIZE, //视频大小
                                MediaStore.Video.Media.DATA, //视频地址
                                MediaStore.Video.Media.ARTIST //艺术家
                        };
                        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, scannerArray, null, null, null);
                        if (cursor!=null){
                            while (cursor.moveToNext()){
                                int id = cursor.getInt(0);
                                String display_name = cursor.getString(1);
                                long duration = cursor.getLong(2);
                                long size=cursor.getLong(3);
                                String data=cursor.getString(4);
                                String artist=cursor.getString(5);
                                list.add(new LocalVideo(id,display_name,duration,size,data,artist));
                            }
                            cursor.close();
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        },1000);
    }

    //视频列表点击事件，播放视频
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //播放网络视频
        //Uri uri=Uri.parse("http://192.168.0.91:8080/Test.mp4");
        Uri uri=Uri.parse("http://video.pearvideo.com/head/20170414/cont-1064396-10371532.mp4");
        Intent intent=new Intent(getActivity(),SystemVideoPlayer.class);
        intent.setData(uri);
        startActivity(intent);

        //播放本地视频
//        Intent intent=new Intent(getActivity(),SystemVideoPlayer.class);
//        intent.putExtra("videoList",list);
//        intent.putExtra("currentIndex",position);
//        startActivity(intent);
    }
}
