package com.dream.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dream.bean.Lyric;
import com.dream.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/4.
 */

public class LyricView extends View {

    /**
     * 歌词数据集合
     */
    private List<Lyric> lyricList;
    /**
     * 控件宽度
     */
    private int width;
    /**
     * 控件高度
     */
    private float height;
    /**
     * 歌词画笔-普通
     */
    private Paint paintNormal;
    /**
     * 歌词画笔-高亮
     */
    private Paint paintLight;
    /**
     * 当前播放进度
     */
    private long curPosition;
    /**
     * 当前歌词索引
     */
    private int index=0;
    private float lineHeight=0;
    private float linePaddingPX;

    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        lineHeight= DensityUtil.dip2px(context,15);
        linePaddingPX = DensityUtil.dip2px(context,10);
        paintNormal=new Paint();
        paintNormal.setColor(Color.WHITE);
        paintNormal.setAntiAlias(true);
        paintNormal.setTextSize(lineHeight);
        paintNormal.setTextAlign(Paint.Align.CENTER);

        paintLight=new Paint();
        paintLight.setColor(Color.GREEN);
        paintLight.setAntiAlias(true);
        paintLight.setTextSize(lineHeight);
        paintLight.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width=w;
        height=h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lyricList==null||lyricList.size()==0){
            canvas.drawText("暂无歌词",width/2,height/2,paintNormal);

        }else{
            /**
             *  平移距离计算
             *  播放进度：睡眠时间 = 平移距离：行高
             *  平移距离=（播放进度：睡眠时间）* 行高
             */

            Lyric currentLyric=lyricList.get(index);
            float offset=0;
            if (currentLyric.getSleepTime()!=0){
                offset=((curPosition-currentLyric.getTimePoint())/currentLyric.getSleepTime())*(lineHeight+linePaddingPX);
            }
            canvas.translate(0,-offset);

            float tempHeight=height/2;
            for (int i = index-1; i >= 0; i--) {
                tempHeight-=lineHeight+linePaddingPX;
                canvas.drawText(lyricList.get(i).getContent(),width/2,tempHeight,paintNormal);
                if (tempHeight<=0){
                    break;
                }
            }
            canvas.drawText(currentLyric.getContent(),width/2,height/2,paintLight);
            tempHeight=height/2;
            for (int i = index+1; i < lyricList.size(); i++) {
                tempHeight+=lineHeight+linePaddingPX;
                canvas.drawText(lyricList.get(i).getContent(),width/2,tempHeight,paintNormal);
                if (tempHeight>=height+linePaddingPX){
                    break;
                }
            }
        }
    }

    public void setLyricList(List<Lyric> lyricList) {
        this.lyricList = lyricList;
    }

    public String setIndex(int curPosition) {
        this.curPosition=curPosition;
        for (int i = 1; i < lyricList.size(); i++) {
            if (this.curPosition<lyricList.get(i).getTimePoint()&&this.curPosition>=lyricList.get(i-1).getTimePoint()){
                this.index=i-1;
                invalidate();
                return lyricList.get(this.index).getContent();
            }
        }
        return null;
    }
}
