package com.dream.lantutv;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dream.service.MusicPlayService;
import com.dream.utils.MyUtils;
import com.dream.view.CircleImageView;

public class SystemMusicPlayer extends AppCompatActivity implements View.OnClickListener {

    private static final int MSG_ROTATE_COVER=0;
    private static final int MSG_MUSIC_PROGRESS = 1;
    public static final String BRODCAST_MUSIC_PERPARED="BRODCAST_MUSIC_PERPARED";

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

    /**
     * 音乐播放服务AIDL
     */
    private IMusicPlayService service;

    /**
     * 广播接受者
     */
    private BroadcastReceiver recevier;

    private Intent musicServiceIntent;

    private int position=0;
    /**
     * 服务连接
     */
    private ServiceConnection serviceConnection=new ServiceConnection() {
        /**
         * 当前连接成功时
         * @param componentName
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                service=IMusicPlayService.Stub.asInterface(iBinder);
                if (service!=null){
                    service.prepareAudio(position);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * 当断开连接时
         * @param componentName
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                if (service!=null){
                    service.stop();
                    service=null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initView();
        initData();
        bindAndSatrtService();
    }

    private Handler hander=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_ROTATE_COVER:
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(musicCover, "rotation", progress, progress += 1);
                    objectAnimator.setDuration(50);
                    objectAnimator.start();
                    hander.sendEmptyMessageDelayed(0,50);
                    if (progress==360){
                        progress=0;
                    }
                    break;
                case MSG_MUSIC_PROGRESS:
                    try {
                        musicSeekbar.setMax(service.getTotalPregree());
                        musicSeekbar.setProgress(service.getPregress());
                        musicProgress.setText(MyUtils.timestampToMinute(service.getPregress()));
                        musicTotal.setText(MyUtils.timestampToMinute(service.getTotalPregree()));
                        hander.sendEmptyMessageDelayed(MSG_MUSIC_PROGRESS,1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private CircleImageView musicCover;
    private ImageView musicPlay;
    private ImageView musicType;
    private TextView musicName;
    private TextView musicArtist;
    private TextView musicProgress;
    private SeekBar musicSeekbar;
    private TextView musicTotal;

    public void initView(){
        musicCover = (CircleImageView) findViewById(R.id.music_cover);
        musicPlay = (ImageView) findViewById(R.id.music_play);
        musicType = (ImageView) findViewById(R.id.music_type);
        musicName = (TextView) findViewById(R.id.music_name);
        musicArtist = (TextView) findViewById(R.id.music_artist);
        musicProgress = (TextView) findViewById(R.id.music_progress);
        musicSeekbar = (SeekBar) findViewById(R.id.music_seekbar);
        musicTotal = (TextView) findViewById(R.id.music_total);
        musicSeekbar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    private void initData() {
        recevier=new MyBroadcastReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BRODCAST_MUSIC_PERPARED);
        registerReceiver(recevier,intentFilter);
    }

    /**
     * 广播监听者
     */
    class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                musicName.setText(service.getMusicName());
                musicArtist.setText(" —  "+service.getArtist()+"  — ");
                hander.sendEmptyMessage(MSG_MUSIC_PROGRESS);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (b){
                try {
                    service.setPregress(i);
                    musicProgress.setText(MyUtils.timestampToMinute(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * 绑定并开始音乐播放服务
     */
    public void bindAndSatrtService(){
        musicServiceIntent=new Intent(this, MusicPlayService.class);
        musicServiceIntent.setAction("com.dream.service.MusicPlayService");
        bindService(musicServiceIntent,serviceConnection,this.BIND_AUTO_CREATE);
        startService(musicServiceIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.music_play:
                //播放或暂停
                playOrPause();
                break;
            case R.id.music_type:
                //设置播放模式
                orderOrRandom();
                break;
        }
    }

    /**
     * 播放/暂停
     */
    public void playOrPause(){
        try {
            isPlaying=service.isPlaying();
            if (isPlaying){
                /**
                 * 暂停
                 */
                musicPlay.setImageResource(R.mipmap.music_play);
                hander.removeMessages(MSG_ROTATE_COVER);
                service.pause();
            }else{
                /**
                 * 播放
                 */
                hander.sendEmptyMessage(MSG_ROTATE_COVER);
                musicPlay.setImageResource(R.mipmap.music_pause);
                service.play();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
        /*hander.removeCallbacksAndMessages(null);
        unbindService(serviceConnection);
        stopService(musicServiceIntent);*/
        super.onDestroy();
    }
}
