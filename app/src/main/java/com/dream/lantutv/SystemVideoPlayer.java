package com.dream.lantutv;

import android.app.Activity;
import android.app.Service;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.dream.bean.Media;
import com.dream.utils.MyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import es.dmoral.toasty.MyToast;

public class SystemVideoPlayer extends Activity implements View.OnClickListener{

    /**
     * 刷新播放进度
     */
    private static final int MSG_PROGRESS = 0;
    private static final int MSG_AUTO_HIDE_MENU = 1;
    /**
     * 是否显示控制面板
     */
    private boolean isShowMediaController=false;
    /**
     * 是否锁定屏幕
     */
    private boolean isLockScreen=false;
    /**
     * 本地视频列表
     */
    private ArrayList<Media> videoList;
    /**
     * 当前视频索引
     */
    private int currentIndex;

    private VideoView videoView;
    private RelativeLayout playerLayout;
    private RelativeLayout playerMediaController;
    private LinearLayout playerBack;
    private ImageView playerShare;
    private ImageView playerMenu;
    private ImageView playerLock;
    private ImageView playerScreen;
    private ImageView playerPlay;
    private ImageView playerNext;
    private TextView playerTime;
    private TextView playerCurrentPosition;
    private TextView playerLocal;
    private TextView playerSwitch;
    private TextView playerName;
    private SeekBar playerSeekbar;
    private GestureDetector gestureDetector;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_PROGRESS:
                    updateProgress();
                    break;
                case MSG_AUTO_HIDE_MENU:
                    showMediaController(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        MyToast.init(this,true,false);
        initView();
        initData();
        initListener();
    }

    /**
     * 初始化视图
     */
    public void initView(){
        videoView = (VideoView) findViewById(R.id.video_view);
        playerLayout = (RelativeLayout) findViewById(R.id.player_layout);
        playerMediaController = (RelativeLayout) findViewById(R.id.player_media_controller);
        playerBack = (LinearLayout) findViewById(R.id.player_back);
        playerShare = (ImageView) findViewById(R.id.player_share);
        playerMenu = (ImageView) findViewById(R.id.player_menu);
        playerLock = (ImageView) findViewById(R.id.player_lock);
        playerScreen = (ImageView) findViewById(R.id.player_screen);
        playerPlay = (ImageView) findViewById(R.id.player_play);
        playerNext = (ImageView) findViewById(R.id.player_next);
        playerTime = (TextView) findViewById(R.id.player_time);
        playerCurrentPosition = (TextView) findViewById(R.id.player_currentPosition);
        playerLocal = (TextView) findViewById(R.id.player_local);
        playerSwitch = (TextView) findViewById(R.id.player_switch);
        playerName = (TextView) findViewById(R.id.player_name);
        playerSeekbar = (SeekBar) findViewById(R.id.player_seekbar);
        gestureDetector=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                playAndPause();
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                showMediaController(isShowMediaController);
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                isLockScreen=false;
                showMediaController(true);
                return super.onDoubleTap(e);
            }

        });
    }

    /**
     * 初始化数据
     */
    public void initData(){
        videoList= (ArrayList<Media>) getIntent().getSerializableExtra("videoList");
        currentIndex=getIntent().getIntExtra("currentIndex",0);
        if (videoList==null||videoList.size()==0){
            //播放网络视频
            Uri uri = getIntent().getData();
            if (uri!=null){
                videoView.setVideoURI(uri);
            }
        }else{
            //播放本地视频
            playLocalVideo(currentIndex);
        }
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        videoView.setOnPreparedListener(new MyOnPreparedListener());
        videoView.setOnErrorListener(new MyOnErrorListener());
        videoView.setOnCompletionListener(new MyOnCompletionListener());
        playerSeekbar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
        playerLayout.setOnTouchListener(new MyOnTouchListener());
    }

    /**
     * 视频准备完成监听
     */
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{

        @Override
        public void onPrepared(MediaPlayer mp) {
            int duration = videoView.getDuration();
            playerSeekbar.setMax(duration);
            playerTime.setText("/"+MyUtils.timestampToTime(duration));
            handler.sendEmptyMessage(MSG_PROGRESS);
            handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
            videoView.start();
        }
    }

    /**
     * 视频播放错误监听
     */
    class MyOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            MyToast.warn("播放异常，请重试...");
            finish();
            return false;
        }
    }

    /**
     * 视频播放完成监听
     */
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            playLocalVideo(currentIndex+1);
        }
    }

    /**
     * 进度条拖动监听
     */
    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser){
                videoView.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(MSG_AUTO_HIDE_MENU);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
        }
    }

    /**
     * 触摸事件监听
     */
    private float startX;
    private float startY;
    class MyOnTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    handler.removeMessages(MSG_PROGRESS);
                    handler.removeMessages(MSG_AUTO_HIDE_MENU);
                    startX=event.getX();
                    startY=event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float endX=event.getX();
                    float endY=event.getY();
                    //左右滑动
                    if (Math.abs(endX-startX)>50){
                        float distanceX=endX-startX;
                        float distanceProgress = playerSeekbar.getMax() * distanceX / v.getWidth();
                        videoView.seekTo((int) (videoView.getCurrentPosition()+distanceProgress));
                        playerCurrentPosition.setText(MyUtils.timestampToTime(videoView.getCurrentPosition()));
                        playerSeekbar.setProgress((int) (videoView.getCurrentPosition()+distanceProgress));
                        startX=endX;
                    }
                    //上下滑动
                    if (Math.abs(endY-startY)>50){
                        float distanceY=endY-startY;
                        if (startX<v.getWidth()/2){
                            //左边滑动，调节亮度
                            System.out.println("左边滑动："+distanceY);
                        }else{
                            //右边滑动，调节声音
                            System.out.println("右边滑动："+distanceY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    handler.removeMessages(MSG_PROGRESS);
                    handler.removeMessages(MSG_AUTO_HIDE_MENU);
                    handler.sendEmptyMessage(MSG_PROGRESS);
                    handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.player_play:
                playAndPause();
                break;
            case R.id.player_back:
                finish();
                break;
            case R.id.player_lock:
                showMediaController(isShowMediaController);
                isLockScreen=!isLockScreen;
                MyToast.info("屏幕已锁定，双击解除锁定");
                break;
            case R.id.player_next:
                playLocalVideo(currentIndex+1);
                break;

        }
    }

    /**
     * 显示控制面板
     */
    public void showMediaController(boolean isShow){
        if (!isLockScreen){
            if (isShow){
                playerMediaController.setVisibility(View.VISIBLE);
                playerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                handler.removeMessages(MSG_AUTO_HIDE_MENU);
                handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
            }else{
                playerMediaController.setVisibility(View.GONE);
                playerLayout.setSystemUiVisibility(View.INVISIBLE);
            }
            isShowMediaController=!isShow;
        }
    }

    /**
     *播放和暂停
     */
    public void playAndPause(){
        if (videoView.isPlaying()){
            videoView.pause();
            playerPlay.setImageResource(R.mipmap.player_nav_play);
        }else{
            videoView.start();
            playerPlay.setImageResource(R.mipmap.player_nav_pasue);
        }
        handler.removeMessages(MSG_AUTO_HIDE_MENU);
        handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
    }

    /**
     * 播放视频
     */
    public void playLocalVideo(int index){
        if (index==videoList.size()) {
            index=0;
        }
        Media media = videoList.get(index);
        videoView.setVideoPath(media.getData());
        playerName.setText(media.getDisplay_name());
        currentIndex=index;
    }

    /**
     * 更新播放进度
     */
    public void updateProgress(){
        int currentPosition = videoView.getCurrentPosition();
        playerSeekbar.setProgress(currentPosition);
        playerCurrentPosition.setText(MyUtils.timestampToTime(currentPosition));
        handler.removeMessages(MSG_PROGRESS);
        handler.sendEmptyMessageDelayed(MSG_PROGRESS,1000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
