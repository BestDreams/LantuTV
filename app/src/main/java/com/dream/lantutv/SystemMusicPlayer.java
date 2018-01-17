package com.dream.lantutv;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.dream.view.CircleImageView;

public class SystemMusicPlayer extends AppCompatActivity implements View.OnClickListener {

    /**
     * 是否正在播放
     */
    private boolean isPlaying=false;

    /**
     * 封面旋转角度
     */
    private int progress=0;

    /**
     * 播放模式
     * true 随机播放
     * false 顺序播放
     */
    private boolean isRandomPlay=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initView();
    }

    private Handler hander=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(musicCover, "rotation", progress, progress += 1);
                    objectAnimator.setDuration(50);
                    objectAnimator.start();
                    hander.sendEmptyMessageDelayed(0,50);
                    if (progress==360){
                        progress=0;
                    }
                    break;
            }
        }
    };
    public void initCover(){
        hander.sendEmptyMessage(0);
    }

    private CircleImageView musicCover;
    private ImageView musicPlay;
    private ImageView musicType;
    public void initView(){
        musicCover = (CircleImageView) findViewById(R.id.music_cover);
        musicPlay = (ImageView) findViewById(R.id.music_play);
        musicType = (ImageView) findViewById(R.id.music_type);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.music_play:
                playOrPause();
                break;
            case R.id.music_type:
                orderOrRandom();
                break;
        }
    }

    /**
     * 播放/暂停
     */
    public void playOrPause(){
        isPlaying=!isPlaying;
        if (isPlaying){
            initCover();
            musicPlay.setImageResource(R.mipmap.music_pause);
        }else{
            musicPlay.setImageResource(R.mipmap.music_play);
            hander.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 随机/顺序播放
     */
    public void orderOrRandom(){
        isRandomPlay=!isRandomPlay;
        if (isRandomPlay){
            musicType.setImageResource(R.mipmap.music_random);
        }else{
            musicType.setImageResource(R.mipmap.music_order);
        }
    }

    @Override
    protected void onDestroy() {
        hander.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
