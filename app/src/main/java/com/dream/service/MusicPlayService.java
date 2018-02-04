package com.dream.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dream.bean.Media;
import com.dream.lantutv.IMusicPlayService;
import com.dream.lantutv.R;
import com.dream.lantutv.SystemMusicPlayer;
import com.dream.utils.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/1/18.
 */

public class MusicPlayService extends Service {

    /**
     * 播放模式
     * MUSIC_MODE_ORDER 顺序播放
     * MUSIC_MODE_RANDOM 随机播放
     * MUSIC_MODE_SINGLE 单曲循环
     */
    public static final int MUSIC_MODE_ORDER=0;
    public static final int MUSIC_MODE_RANDOM=1;
    public static final int MUSIC_MODE_SINGLE=2;


    /**
     * 音乐媒体集合
     */
    private List<Media> mediaList;
    /**
     * 当前播放索引
     */
    private int position;
    /**
     * 当前媒体对象
     */
    private Media media;
    /**
     * 媒体播放对象
     */
    private MediaPlayer mediaPlayer;
    /**
     * 通知管理器
     */
    private NotificationManager notificationManager;
    /**
     * 播放模式
     */
    private int playMode=0;
    /**
     * 随机数
     */
    private Random random;

    @Override
    public void onCreate() {
        super.onCreate();
        loadingMediaData();
        initData();
    }

    private void initData() {
        random=new Random();
    }

    /**
     * 准备音频
     */
    private void prepareAudio(int index){
        if (mediaList!=null&&mediaList.size()>0){
            position=index;
            media=mediaList.get(position);
            if (mediaPlayer!=null){
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer=null;
            }
            try {
                mediaPlayer=new MediaPlayer();
                mediaPlayer.setDataSource(media.getData());
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 准备完成监听
     */
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            sendBroadcast(new Intent(SystemMusicPlayer.BRODCAST_MUSIC_PERPARED));
        }
    }

    /**
     * 播放完成监听
     */
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            next();
        }
    }

    /**
     * 播放出错监听
     */
    class MyOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }
    }

    /**
     * 当前播放索引
     */
    private int getCurPosition(){
        return position;
    }

    /**
     * 播放
     */
    private void play(){
        if (mediaPlayer!=null){
            mediaPlayer.start();
        }
    }

    /**
     *在通知栏显示歌曲播放通知
     */
    private void showInfoOnNotification(){
        notificationManager= (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent=new Intent(this,SystemMusicPlayer.class);
        intent.putExtra("notification",true);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,2001,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification=new Notification.Builder(this)
                .setSmallIcon(R.mipmap.music_item_icon)
                .setContentTitle(getMusicName())
                .setContentText(getArtist())
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(Config.NOTIFICATION_MUSIC_ID, notification);
    }

    /**
     * 暂停
     */
    private void pause(){
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    /**
     * 停止
     */
    private void stop(){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
        }
    }

    /**
     * 上一首
     */
    private void last(){
        if (mediaList!=null&&mediaList.size()>0){
            if (position==0){
                position=mediaList.size()-1;
            }else{
                position--;
            }
            prepareAudio(position);
        }

    }

    /**
     * 下一首
     */
    private void next(){
        switch (playMode){
            /**
             * 顺序播放
             */
            case MUSIC_MODE_ORDER:
                if (mediaList!=null&&mediaList.size()>0){
                    if (position==mediaList.size()-1){
                        position=0;
                    }else{
                        position++;
                    }
                }
                break;
            /**
             * 随机播放
             */
            case MUSIC_MODE_RANDOM:
                position=random.nextInt(mediaList.size());
                break;
            case MUSIC_MODE_SINGLE:
                break;
        }
        Log.i("my","当前播放："+position);
        prepareAudio(position);
    }

    /**
     * 设置播放模式
     * 0：顺序播放
     * 1：随机播放
     */
    private void setPlayMode(int playMode){
        this.playMode=playMode;
    }

    /**
     * 得到播放模式
     * 0：顺序播放
     * 1：随机播放
     */
    public int getPlayMode(){
        return playMode;
    }

    /**
     * 艺术家
     */
    private String getArtist(){
        return media.getArtist();
    }

    /**
     * 歌曲名称
     */
    private String getMusicName(){
        return media.getDisplay_name();
    }

    /**
     * 当前播放进度
     */
    private int getPregress(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 设置播放进度
     */
    private void setPregress(int pregress){
        mediaPlayer.seekTo(pregress);
    }
    /**
     * 总时长
     */
    private int getTotalPregree(){
        return mediaPlayer.getDuration();
    }

    /**
     * 是否正在播放
     */
    private boolean isPlaying(){
        if (mediaPlayer!=null){
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private IMusicPlayService.Stub stub=new IMusicPlayService.Stub() {
        MusicPlayService service = MusicPlayService.this;

        @Override
        public void prepareAudio(int index) throws RemoteException {
            service.prepareAudio(index);
        }

        @Override
        public int getCurPosition() throws RemoteException {
            return service.getCurPosition();
        }

        @Override
        public void play() throws RemoteException {
            service.play();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public void last() throws RemoteException {
            service.last();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getMusicName() throws RemoteException {
            return service.getMusicName();
        }

        @Override
        public int getPregress() throws RemoteException {
            return service.getPregress();
        }

        @Override
        public void setPregress(int pregress) throws RemoteException {
            service.setPregress(pregress);
        }

        @Override
        public int getTotalPregree() throws RemoteException {
            return service.getTotalPregree();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void showInfoOnNotification() throws RemoteException {
            service.showInfoOnNotification();
        }
    };

    /**
     * 扫描加载本地音乐
     */
    public void loadingMediaData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaList=new ArrayList<>();
                ContentResolver contentResolver = getContentResolver();
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
                        mediaList.add(new Media(id,display_name,duration,size,data,artist));
                    }
                    cursor.close();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer!=null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        super.onDestroy();
    }
}
