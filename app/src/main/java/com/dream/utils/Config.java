package com.dream.utils;

/**
 * Created by ZhangPing on 2018/1/9.
 */

public class Config {
    /**
     * API: 单个视频 By Id
     */
    public static final String CONFIG_URL_VIDEO_SINGLE_ID="http://app.pearvideo.com/clt/jsp/v2/content.jsp?contId=";
    /**
     * API: 主页数据
     */
    public static final String CONFIG_URL_VIDEO_HOME="http://app.pearvideo.com/clt/jsp/v2/home.jsp?lastLikeIds=1063871%2C1063985%2C1064069%2C1064123%2C1064078%2C1064186%2C1062372%2C1064164%2C1064081%2C1064176%2C1064070%2C1064019";
    /**
     * API: 音乐搜索
     */
    public static final String CONFIG_URL_MUSIC_SEARCH="http://music.163.com/api/search/get/";
    /**
     * API:音乐歌词
     */
    public static final String CONFIG_URL_MUSIC_LYRIC="http://music.163.com/api/song/lyric?os=pc&lv=-1&kv=-1&tv=-1&id=";

    /**
     * 欢迎页跳转主页
     */
    public static final int CONFIG_TIME_TO_MAIN=2000;
    /**
     * 刷新网速时间
     */
    public static final int CONFIG_TIME_UPDATE_NETSPEED=1000;
    /**
     * 隐藏控制面板
     */
    public static final int CONFIG_TIME_HIDE_MENU=5000;
    /**
     * 刷新播放进度
     */
    public static final int CONFIG_TIME_UPDATE_PROGRESS=1000;


    /**
     * 主题颜色
     */
    public static final String CONFIG_COLOR_THEME="#4b405c";

    /**
     * 音乐播放通知ID
     */
    public static final int NOTIFICATION_MUSIC_ID=1001;

    /**
     * 本地存储 KEY
     */
    public static final String SP_LASTPLAYING="lastPlaying";


}
