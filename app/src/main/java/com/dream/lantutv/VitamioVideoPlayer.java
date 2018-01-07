package com.dream.lantutv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dream.bean.LocalVideo;
import com.dream.utils.MyUtils;

import java.util.ArrayList;

import es.dmoral.toasty.MyToast;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

public class VitamioVideoPlayer extends Activity implements View.OnClickListener{

    /**
     * 刷新播放进度
     */
    private static final int MSG_PROGRESS = 0;
    private static final int MSG_AUTO_HIDE_MENU = 1;
    private static final int MSG_HIDE_VLOUMEN = 2;
    private static final int MSG_UPDATE_NET_SPEED = 3;
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
    private ArrayList<LocalVideo> videoList;
    /**
     * 当前视频索引
     */
    private int currentIndex;
    /**
     * 手势识别器
     */
    private GestureDetector gestureDetector;
    /**
     * 是否静音
     */
    private boolean isMute=false;
    /**
     * 声音管理器
     */
    private AudioManager audioManager;
    /**
     * 最大音量
     */
    private int maxVloume;
    /**
     * 当前音量
     */
    private int currentVolume;
    /**
     *是否是网络视频
     */
    private boolean isNetUri=false;
    /**
     * 是否使用系统播放卡顿监听
     */
    private boolean isUseSystemLoading=false;
    /**
     * 上一次播放进度
     */
    private int preVideoProgress=0;
    /**
     * VideoLoing弹窗
     */
    private RelativeLayout videoLoading;
    /**
     * VideoLoing文本控件
     */
    private TextView videoLoadingText;
    /**
     * 当前网速
     */
    private String currentNetSpeed;

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
    private TextView playerSwitch;
    private ImageView playerVolume;
    private TextView playerName;
    private SeekBar playerSeekbar;
    private TextView playerLightValue;
    private TextView playerVolumeValue;

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
                case MSG_HIDE_VLOUMEN:
                    playerVolumeValue.setVisibility(View.INVISIBLE);
                    break;
                case MSG_UPDATE_NET_SPEED:
                    updateNetSpeed();
                    handler.removeMessages(MSG_UPDATE_NET_SPEED);
                    handler.sendEmptyMessageDelayed(MSG_UPDATE_NET_SPEED,1000);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_vitamio_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
        playerSwitch = (TextView) findViewById(R.id.player_switch);
        playerVolume = (ImageView) findViewById(R.id.player_volume);
        playerName = (TextView) findViewById(R.id.player_name);
        playerSeekbar = (SeekBar) findViewById(R.id.player_seekbar);
        playerLightValue = (TextView) findViewById(R.id.player_light_value);
        playerVolumeValue = (TextView) findViewById(R.id.player_volume_value);
        videoLoading = (RelativeLayout) findViewById(R.id.video_loading);
        videoLoadingText = (TextView) findViewById(R.id.video_loading_text);
        if (isNetUri){
            playerLayout.setClickable(false);
            playerLayout.setSystemUiVisibility(View.INVISIBLE);
            playerMediaController.setVisibility(View.GONE);
            handler.sendEmptyMessage(MSG_UPDATE_NET_SPEED);
            videoLoading.setVisibility(View.VISIBLE);
        }
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
        audioManager= (AudioManager) getSystemService(this.AUDIO_SERVICE);
        maxVloume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        videoList= (ArrayList<LocalVideo>) getIntent().getSerializableExtra("videoList");
        currentIndex=getIntent().getIntExtra("currentIndex",0);
        if (videoList==null||videoList.size()==0){
            //播放网络视频
            Uri uri = getIntent().getData();
            if (uri!=null){
                isNetUri=MyUtils.isNetUri(uri);
                videoView.setVideoURI(uri);
                if (getIntent().getStringExtra("videoName")==null||getIntent().getStringExtra("videoName").equals("")){
                    playerName.setText(uri.toString());
                }else{
                    playerName.setText(getIntent().getStringExtra("videoName"));
                }
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
        if (isUseSystemLoading){
            videoView.setOnInfoListener(new MyOnInfoListener());
        }
    }

    /**
     * 视频准备完成监听
     */
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{

        @Override
        public void onPrepared(MediaPlayer mp) {
            int duration = (int) videoView.getDuration();
            playerSeekbar.setMax(duration);
            playerTime.setText("/"+MyUtils.timestampToTime(duration));
            playerLayout.setClickable(true);
            playerMediaController.setVisibility(View.VISIBLE);
            videoLoading.setVisibility(View.GONE);
            handler.removeMessages(MSG_UPDATE_NET_SPEED);
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
            new AlertDialog.Builder(VitamioVideoPlayer.this)
                    .setTitle("提示")
                    .setMessage("无法播放该视频")
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            return true;
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
     * 视频播放卡顿监听
     */
    class MyOnInfoListener implements MediaPlayer.OnInfoListener{

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    videoLoading.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessage(MSG_UPDATE_NET_SPEED);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    videoLoading.setVisibility(View.GONE);
                    handler.removeMessages(MSG_UPDATE_NET_SPEED);
                    break;
            }
            return true;
        }
    }


    /**
     * 触摸事件监听
     * @startX X轴起始坐标
     * @startY Y轴起始坐标
     * @scrollState 滑动状态 0：未滑动 1横向滑动 2上下滑动
     */
    private float startX;
    private float startY;
    private int scrollState=0;
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
                    if (Math.abs(endX-startX)>10){
                        //左右滑动
                        if (scrollState!=2){
                            scrollState=1;
                            float distanceX=endX-startX;
                            float distanceProgress = playerSeekbar.getMax() * distanceX / v.getWidth();
                            videoView.seekTo((int) (videoView.getCurrentPosition()+distanceProgress));
                            playerCurrentPosition.setText(MyUtils.timestampToTime(videoView.getCurrentPosition()));
                            playerSeekbar.setProgress((int) (videoView.getCurrentPosition()+distanceProgress));
                            startX=endX;
                        }
                    } else if (Math.abs(endY-startY)>10){
                        //上下滑动
                        if (scrollState!=1){
                            scrollState=2;
                            float distanceY=startY-endY;
                            if (startX<v.getWidth()/2){
                                //左边滑动，调节亮度

                            }else{
                                //右边滑动，调节声音

                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    scrollState=0;
                    handler.removeMessages(MSG_PROGRESS);
                    handler.removeMessages(MSG_AUTO_HIDE_MENU);
                    handler.sendEmptyMessage(MSG_PROGRESS);
                    handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
                    handler.sendEmptyMessageDelayed(MSG_HIDE_VLOUMEN,1000);
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
            case R.id.player_volume:
                isMute=!isMute;
                updateVolumeProgress(isMute,currentVolume);
                break;
            case R.id.player_decode:
                handler.removeMessages(MSG_AUTO_HIDE_MENU);
                new AlertDialog.Builder(VitamioVideoPlayer.this)
                        .setTitle("提示")
                        .setCancelable(false)
                        .setMessage("确定使用系统解码器？")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
                                useSystemPlayer();
                                finish();
                            }
                        })
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
                            }
                        })
                        .show();
                break;

        }
    }

    /**
     * 显示控制面板
     * @param isShow true显示 false不显示
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
     * @param index 要播放视频的下标
     */
    public void playLocalVideo(int index){
        if (index==videoList.size()) {
            index=0;
        }
        LocalVideo localVideo = videoList.get(index);
        isNetUri=MyUtils.isNetUri(Uri.parse(localVideo.getData()));
        videoView.setVideoPath(localVideo.getData());
        playerName.setText(localVideo.getDisplay_name());
        currentIndex=index;
    }

    /**
     * 更新播放进度
     */
    public void updateProgress(){
        int currentPosition = (int) videoView.getCurrentPosition();
        playerSeekbar.setProgress(currentPosition);
        playerCurrentPosition.setText(MyUtils.timestampToTime(currentPosition));
        if (isNetUri){
            int bufferPercentage = videoView.getBufferPercentage();
            int progress=bufferPercentage*playerSeekbar.getMax()/100;
            playerSeekbar.setSecondaryProgress(progress);
        }else{
            playerSeekbar.setSecondaryProgress(0);
        }

        System.out.println(currentPosition+" "+preVideoProgress+" "+(currentPosition-preVideoProgress));
        if (!isUseSystemLoading&&videoView.isPlaying()&&isNetUri){
            if (currentPosition-preVideoProgress<100){
                //视频卡了
                videoLoading.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(MSG_UPDATE_NET_SPEED);
            }else{
                //视频不卡
                videoLoading.setVisibility(View.GONE);
                handler.removeMessages(MSG_UPDATE_NET_SPEED);
            }
        }

        preVideoProgress=currentPosition;

        handler.removeMessages(MSG_PROGRESS);
        handler.sendEmptyMessageDelayed(MSG_PROGRESS,1000);
    }


    /**
     * 更新音量进度
     * @param isMute true静音 false非静音
     */
    public void updateVolumeProgress(boolean isMute,int progress){
        if (isMute||progress==0){
            playerVolume.setImageResource(R.mipmap.player_volume_off);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
        }else{
            playerVolume.setImageResource(R.mipmap.player_volume_on);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            currentVolume=progress;
        }
        handler.removeMessages(MSG_AUTO_HIDE_MENU);
        handler.sendEmptyMessageDelayed(MSG_AUTO_HIDE_MENU,5000);
    }

    /**
     * 刷新当前网速
     */
    public void updateNetSpeed(){
        currentNetSpeed=MyUtils.getNetSpeed(VitamioVideoPlayer.this);
        videoLoadingText.setText(currentNetSpeed);
    }

    /**
     * 使用系统播放器
     */
    public void useSystemPlayer(){
        Intent intent=new Intent(VitamioVideoPlayer.this,SystemVideoPlayer.class);
        if (videoList==null||videoList.size()==0){
            intent.setData( getIntent().getData());
        }else{
            intent.putExtra("videoList",videoList);
            intent.putExtra("currentIndex",currentIndex);
        }
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                updateVolumeProgress(false,currentVolume);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
