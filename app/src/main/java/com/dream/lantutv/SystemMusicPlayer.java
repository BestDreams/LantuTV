package com.dream.lantutv;

import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dream.adapter.ViewPagerAdapater;
import com.dream.service.MusicPlayService;
import com.dream.utils.Config;
import com.dream.utils.MyUtils;
import com.dream.view.CircleImageView;
import com.dream.view.LyricView;

import java.util.ArrayList;
import java.util.List;


public class SystemMusicPlayer extends AppCompatActivity implements View.OnClickListener {
    private static final int MSG_ROTATE_COVER=0;
    private static final int MSG_MUSIC_PROGRESS = 1;
    private static final int MSG_MUSIC_LYRIC = 2;

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
     * 0 顺序播放
     * 1 随机播放
     * 2 单曲循环
     */
    private int musicPlayMode=0;

    /**
     * 音乐播放服务AIDL
     */
    private IMusicPlayService service;

    /**
     * 广播接受者
     */
    private BroadcastReceiver recevier;

    /**
     * 当前播放位置
     */
    private int position=0;

    /**
     * 是否从通知进来
     */
    private boolean isFormNotification;
    /**
     * 本地存储对象
     */
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ViewPager musicViewpager;

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
                    if (isFormNotification){
                        setViewData(true);
                    }else{
                        service.prepareAudio(position);
                    }
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
        initMusicPage();
        initData();
        bindAndSatrtService();
    }

    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_ROTATE_COVER:
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(musicCover, "rotation", progress, progress += 1);
                    objectAnimator.setDuration(50);
                    objectAnimator.start();
                    handler.removeMessages(MSG_ROTATE_COVER);
                    handler.sendEmptyMessageDelayed(MSG_ROTATE_COVER,50);
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
                        handler.removeMessages(MSG_MUSIC_PROGRESS);
                        handler.sendEmptyMessageDelayed(MSG_MUSIC_PROGRESS,1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_MUSIC_LYRIC:
                    /**
                     * 当前播放进度
                     */
                    try {
                        musicCurlayric.setText(musicLyric.setIndex(service.getPregress()));
                        handler.removeMessages(MSG_MUSIC_LYRIC);
                        handler.sendEmptyMessageDelayed(MSG_MUSIC_LYRIC,100);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


    private ImageView musicPlay;
    private ImageView musicType;
    private TextView musicName;
    private TextView musicArtist;
    private TextView musicProgress;
    private SeekBar musicSeekbar;
    private TextView musicTotal;


    public void initView(){
        musicPlay = (ImageView) findViewById(R.id.music_play);
        musicType = (ImageView) findViewById(R.id.music_type);
        musicName = (TextView) findViewById(R.id.music_name);
        musicArtist = (TextView) findViewById(R.id.music_artist);
        musicProgress = (TextView) findViewById(R.id.music_progress);
        musicSeekbar = (SeekBar) findViewById(R.id.music_seekbar);
        musicTotal = (TextView) findViewById(R.id.music_total);
        musicViewpager = (ViewPager) findViewById(R.id.music_viewpager);
        musicSeekbar.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }

    private List<View> musicPages;
    private CircleImageView musicCover;
    private LyricView musicLyric;
    private TextView musicCurlayric;
    private void initMusicPage() {
        /**
         * 封面页
         */
        View coverView=View.inflate(this,R.layout.music_view_cover,null);
        musicCover = (CircleImageView) coverView.findViewById(R.id.music_cover);
        musicCurlayric = (TextView) coverView.findViewById(R.id.music_curlayric);
        /**
         * 歌词页
         */
        View lyricView=View.inflate(this,R.layout.music_view_lyric,null);
        musicLyric = (LyricView) lyricView.findViewById(R.id.music_lyric);

        musicPages=new ArrayList<>();
        musicPages.add(coverView);
        musicPages.add(lyricView);
        musicViewpager.setAdapter(new ViewPagerAdapater(musicPages));
    }

    private void initData() {
        recevier=new MyBroadcastReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(BRODCAST_MUSIC_PERPARED);
        registerReceiver(recevier,intentFilter);
        sharedPreferences=getSharedPreferences("app",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        isFormNotification=getIntent().getBooleanExtra("notification",false);
        position=getIntent().getIntExtra("position",0);
    }

    /**
     * 媒体准备完成时
     */
    class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                service.play();
                setViewData(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新播放器数据
     * @param isRotate 是否旋转封面
     */
    private void setViewData(boolean isRotate){
        try {
            if (service!=null){
                isPlaying=service.isPlaying();
                musicPlay.setImageResource(isPlaying?R.mipmap.music_pause:R.mipmap.music_play);
                musicName.setText(MyUtils.fileNameRemoveSuffix(service.getMusicName()).split("-")[1].trim());
                musicArtist.setText(" —  "+service.getArtist()+"  — ");
                musicPlayMode=service.getPlayMode();
                switch (musicPlayMode){
                    case MusicPlayService.MUSIC_MODE_ORDER:
                        musicType.setImageResource(R.mipmap.music_order);
                        break;
                    case MusicPlayService.MUSIC_MODE_RANDOM:
                        musicType.setImageResource(R.mipmap.music_random);
                        break;
                    case MusicPlayService.MUSIC_MODE_SINGLE:
                        musicType.setImageResource(R.mipmap.music_single);
                        break;
                }
                handler.removeMessages(MSG_MUSIC_LYRIC);
                handler.sendEmptyMessage(MSG_MUSIC_LYRIC);
                handler.removeMessages(MSG_MUSIC_PROGRESS);
                handler.sendEmptyMessage(MSG_MUSIC_PROGRESS);
                if (isRotate){
                    handler.removeMessages(MSG_ROTATE_COVER);
                    handler.sendEmptyMessage(MSG_ROTATE_COVER);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 滑动进度条时
     */
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
    private Intent musicServiceIntent;
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
                setMusicPlayMode();
                break;
            case R.id.music_last:
                try {
                    service.last();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.music_next:
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.music_exit:
                finish();
                break;
        }
    }

    /**
     * 播放/暂停
     */
    public void playOrPause(){
        try {
            isPlaying=service.isPlaying();
            handler.removeMessages(MSG_ROTATE_COVER);
            if (isPlaying){
                /**
                 * 暂停
                 */
                musicPlay.setImageResource(R.mipmap.music_play);
                service.pause();
            }else{
                /**
                 * 播放
                 */
                handler.sendEmptyMessage(MSG_ROTATE_COVER);
                musicPlay.setImageResource(R.mipmap.music_pause);
                service.play();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置音乐播放模式
     */
    public void setMusicPlayMode(){
        try {
            switch (musicPlayMode){
                case MusicPlayService.MUSIC_MODE_ORDER:
                    musicPlayMode=MusicPlayService.MUSIC_MODE_RANDOM;
                    musicType.setImageResource(R.mipmap.music_random);
                    Toast.makeText(this,"随机播放",Toast.LENGTH_SHORT).show();
                    break;
                case MusicPlayService.MUSIC_MODE_RANDOM:
                    musicPlayMode=MusicPlayService.MUSIC_MODE_SINGLE;
                    musicType.setImageResource(R.mipmap.music_single);
                    Toast.makeText(this,"单曲循环",Toast.LENGTH_SHORT).show();
                    break;
                case MusicPlayService.MUSIC_MODE_SINGLE:
                    musicPlayMode=MusicPlayService.MUSIC_MODE_ORDER;
                    musicType.setImageResource(R.mipmap.music_order);
                    Toast.makeText(this,"顺序播放",Toast.LENGTH_SHORT).show();
                    break;
            }
            service.setPlayMode(musicPlayMode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从通知栏移除通知
     */
    @Override
    protected void onStart() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Config.NOTIFICATION_MUSIC_ID);
        super.onStart();
    }

    /**
     * 在通知栏显示通知
     */
    @Override
    protected void onStop() {
        try {
            if (service.isPlaying()){
                service.showInfoOnNotification();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if (service.isPlaying()){
                editor.putBoolean(Config.SP_LASTPLAYING,true);
            }else{
                editor.putBoolean(Config.SP_LASTPLAYING,false);
                stopService(musicServiceIntent);
            }
            editor.commit();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(serviceConnection);
        unregisterReceiver(recevier);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
