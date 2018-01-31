package com.dream.fragment;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.dream.base.BaseFragment;
import com.dream.bean.Media;
import com.dream.lantutv.R;
import com.dream.lantutv.SystemVideoPlayer;
import com.dream.utils.MyUtils;
import com.mcxtzhang.commonadapter.lvgv.CommonAdapter;
import com.mcxtzhang.commonadapter.lvgv.ViewHolder;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.yalantis.phoenix.PullToRefreshView;

import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.MyToast;


/**
 * A simple {@link Fragment} subclass.
 */

public class MusicFragment extends BaseFragment{


    public MusicFragment() {
    }

    private ListView videoListview;
    private RelativeLayout videoLoading;
    private LinearLayout videoEmpty;
    private PullToRefreshView videoPullToRefresh;

    @Override
    public View initView() {
        System.out.println("初始化音乐");
        View view = View.inflate(activity,R.layout.fragment_media, null);
        videoListview = (ListView) view.findViewById(R.id.video_listview);
        videoLoading = (RelativeLayout) view.findViewById(R.id.video_loading);
        videoEmpty = (LinearLayout) view.findViewById(R.id.video_empty);
        videoPullToRefresh = (PullToRefreshView) view.findViewById(R.id.video_pullToRefresh);
        videoPullToRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                videoPullToRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getMediaData(false);
                        videoPullToRefresh.setRefreshing(false);
                    }
                },2000);
            }
        });
        initData();
        return view;
    }

    @Override
    public void initData() {
        getMediaData(true);
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
                setupSwipMenuAdapter();
            }
        }
    };

    /**
     * 侧滑删除适配器
     */
    private CommonAdapter commonAdapter;
    public void setupSwipMenuAdapter(){
        commonAdapter=new CommonAdapter(getActivity(),list,R.layout.video_listview_item){

            @Override
            public void convert(final ViewHolder viewHolder, Object o, final int position) {
                Media media = (Media) o;
                viewHolder.setImageResource(R.id.video_icon,R.mipmap.music_item_icon);
                viewHolder.setText(R.id.video_name,MyUtils.fileNameRemoveSuffix(media.getDisplay_name()));
                viewHolder.setText(R.id.video_time,"时长："+ MyUtils.timestampToMinute(media.getDuration())+"分钟");
                viewHolder.setText(R.id.video_size,"大小："+ Formatter.formatFileSize(getActivity(), media.getSize()));
                viewHolder.setOnClickListener(R.id.video_content, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayer(position);
                    }
                });
                viewHolder.setOnClickListener(R.id.video_del, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteItem(position,(SwipeMenuLayout) viewHolder.getConvertView());
                    }
                });
                viewHolder.setOnClickListener(R.id.video_info, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showVideoInfo(list.get(position));
                    }
                });
            }
        };
        videoListview.setAdapter(commonAdapter);
    }

    /**
     * 扫描加载本地视频
     */
    private ArrayList<Media> list;
    public void getMediaData(boolean isShowLoading){
        int delayed=0;
        if (isShowLoading){
            delayed=1000;
            videoLoading.setVisibility(View.VISIBLE);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        list=new ArrayList<>();
                        ContentResolver contentResolver = activity.getContentResolver();
                        String[] scannerArray={
                                MediaStore.Audio.Media._ID, //视频ID
                                MediaStore.Audio.Media.DISPLAY_NAME, //视频名称
                                MediaStore.Audio.Media.DURATION, //视频时长
                                MediaStore.Audio.Media.SIZE, //视频大小
                                MediaStore.Audio.Media.DATA, //视频地址
                                MediaStore.Audio.Media.ARTIST //艺术家
                        };
                        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, scannerArray, null, null, null);
                        if (cursor!=null){
                            while (cursor.moveToNext()){
                                int id = cursor.getInt(0);
                                String display_name = cursor.getString(1);
                                long duration = cursor.getLong(2);
                                long size=cursor.getLong(3);
                                String data=cursor.getString(4);
                                String artist=cursor.getString(5);
                                list.add(new Media(id,display_name,duration,size,data,artist));
                            }
                            cursor.close();
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        },delayed);
    }


    //视频列表点击事件，播放视频
    public void startPlayer(int position){
        Intent intent=new Intent(getActivity(),SystemVideoPlayer.class);
        //播放本地视频
        intent.putExtra("videoList",list);
        intent.putExtra("currentIndex",position);
        //播放网络视频
        /*intent.setData(Uri.parse(""));*/
        startActivity(intent);
    }

    public void deleteItem(final int position, final SwipeMenuLayout menu){
        new AlertDialog.Builder(getActivity())
                .setTitle("提示")
                .setMessage("确定要删除吗？"+"\n"+list.get(position).getDisplay_name())
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String delResult = deleteFile(list.get(position).getData());
                        if (delResult.equals("successful")){
                            MyToast.info("删除成功");
                            menu.quickClose();
                            list.remove(position);
                            commonAdapter.notifyDataSetChanged();
                        }else{
                            MyToast.warn(delResult);
                        }
                    }
                })
                .setPositiveButton("取消",null)
                .show();
    }

    /**
     * 删除文件
     */
    public String deleteFile(String path){
        File file=new File(path);
        if (file.isFile()){
            if (file.exists()){
                if (file.delete()){
                    getActivity().getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "= \"" + file.getPath() + "\"", null);
                    return "successful";
                }else{
                    return "删除失败，未知错误";
                }
            }else{
                return "文件不存在";
            }
        }
        return "删除失败，不是文件";
    }

    /**
     * 显示视频属性
     */
    public void showVideoInfo(Media video){
        new AlertDialog.Builder(getActivity())
                .setTitle("属性")
                .setMessage(
                        "名称："+MyUtils.fileNameRemoveSuffix(video.getDisplay_name())+"\n\n"+
                        "时长："+MyUtils.timestampToHour(video.getDuration())+"分钟\n\n"+
                        "大小："+Formatter.formatFileSize(getActivity(),video.getSize())+"\n\n"+
                        "格式："+video.getDisplay_name().substring(video.getDisplay_name().lastIndexOf(".")+1)+"\n\n"+
                        "路径："+video.getData()
                )
                .setPositiveButton("确定",null)
                .show();
    }


    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
