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

/**
 * Created by Administrator on 2018/2/4.
 */

public class LyricView extends View {

    /**
     * 歌词数据集合
     */
    private ArrayList<Lyric> lyricList;
    /**
     * 控件宽度
     */
    private int width;
    /**
     * 控件高度
     */
    private int height;
    /**
     * 歌词画笔-普通
     */
    private Paint paintNormal;
    /**
     * 歌词画笔-高亮
     */
    private Paint paintLight;
    /**
     * 当前歌词索引
     */
    private int index=0;
    private int lineHeight=0;

    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        lineHeight= DensityUtil.dip2px(context,15);

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

        /**
         * 模拟数据
         */
        lyricList=new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            lyricList.add(new Lyric("Hello World"+i,1000*i,1000));
        }
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
            int tempHeight=height/2;
            for (int i = index-1; i >= 0; i--) {
                tempHeight-=lineHeight;
                canvas.drawText(lyricList.get(i).getContent(),width/2,tempHeight,paintNormal);
                if (tempHeight<=0){
                    break;
                }
            }

            canvas.drawText(lyricList.get(index).getContent(),width/2,height/2,paintLight);

            tempHeight=height/2;
            for (int i = index+1; i < lyricList.size(); i++) {
                tempHeight+=lineHeight;
                canvas.drawText(lyricList.get(i).getContent(),width/2,tempHeight,paintNormal);
                if (tempHeight>=height){
                    break;
                }
            }
        }
    }

    public void setLyricList(ArrayList<Lyric> lyricList) {
        this.lyricList = lyricList;
    }

    public String setIndex(int curPosition) {
        for (int i = 1; i < lyricList.size(); i++) {
            if (curPosition<lyricList.get(i).getTimePoint()&&curPosition>=lyricList.get(i-1).getTimePoint()){
                this.index=i-1;
                invalidate();
                return lyricList.get(this.index).getContent();
            }
        }
        return null;
    }
}
