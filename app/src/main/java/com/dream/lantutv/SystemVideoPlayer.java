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

import com.dream.utils.MyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import es.dmoral.toasty.MyToast;

public class SystemVideoPlayer extends Activity implements View.OnClickListener{

    /**
     * 刷新播放进度
     */
    private static final int MSG_PROGRESS = 0;
    /**
     * 是否显示控制面板
     */
    private boolean isShowMediaController=false;
    /**
     * 是否锁定屏幕
     */
    private boolean isLockScreen=false;

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
    private SeekBar playerSeekbar;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_PROGRESS:
                    int currentPosition = videoView.getCurrentPosition();
                    playerSeekbar.setProgress(currentPosition);
                    playerCurrentPosition.setText(MyUtils.timestampToTime(currentPosition));
                    removeMessages(MSG_PROGRESS);
                    sendEmptyMessageDelayed(MSG_PROGRESS,1000);
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
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        playerSeekbar = (SeekBar) findViewById(R.id.player_seekbar);
    }

    /**
     * 初始化数据
     */
    public void initData(){
        Uri uri = getIntent().getData();
        if (uri!=null){
            videoView.setVideoURI(uri);
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
        playerLayout.setOnLongClickListener(new MyOnLongClickListener());
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
            videoView.start();
        }
    }

    /**
     * 视频播放错误监听
     */
    class MyOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(SystemVideoPlayer.this,"播放异常，请重试",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 视频播放完成监听
     */
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            finish();
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

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class MyOnLongClickListener implements View.OnLongClickListener{
        @Override
        public boolean onLongClick(View v) {
            isLockScreen=false;
            showMediaController();
            return true;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.player_layout:
                showMediaController();
                break;
            case R.id.player_play:
                playAndPause();
                break;
            case R.id.player_back:
                finish();
                break;
            case R.id.player_lock:
                showMediaController();
                isLockScreen=!isLockScreen;
                MyToast.info("屏幕已锁定，长按屏幕解除锁定");
                break;
            case R.id.player_screen:
                break;

        }
    }

    /**
     * 显示控制面板
     */
    public void showMediaController(){
        if (!isLockScreen){
            isShowMediaController=!isShowMediaController;
            if (isShowMediaController){
                playerMediaController.setVisibility(View.GONE);
                playerLayout.setSystemUiVisibility(View.INVISIBLE);
            }else{
                playerMediaController.setVisibility(View.VISIBLE);
                playerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
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
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
