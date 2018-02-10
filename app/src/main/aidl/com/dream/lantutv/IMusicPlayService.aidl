// IMusicPlayService.aidl
package com.dream.lantutv;

// Declare any non-default types here with import statements

interface IMusicPlayService {
            /**
                 * 准备音频
                 */
                void prepareAudio(int index);

                /**
                 * 当前播放索引
                 */
                int getCurPosition();

                /**
                 * 播放
                 */
                void play();

                /**
                 * 暂停
                 */
               void pause();

                /**
                 * 停止
                 */
                void stop();

                /**
                 * 上一首
                 */
                void last();

                /**
                 * 下一首
                 */
                void next();

                /**
                 * 设置播放模式
                 * 0：顺序播放
                 * 1：随机播放
                 */
                void setPlayMode(int playMode);

                /**
                 * 得到播放模式
                 * 0：顺序播放
                 * 1：随机播放
                 */
                int getPlayMode();

                /**
                 * 艺术家
                 */
                String getArtist();

                /**
                 * 歌曲名称
                 */
                String getMusicName();

                /**
                 * 当前播放进度
                 */
                int getPregress();

                /**
                 * 设置播放进度
                 */
                void setPregress(int pregress);

                /**
                 * 总时长
                 */
                int getTotalPregree();

                /**
                 * 是否正在播放
                 */
                boolean isPlaying();

                /**
                 *在通知栏显示歌曲信息
                 */
                void showInfoOnNotification(String musicName,String artist);

                /**
                 * 是否正在显示通知
                 */
                boolean isShowNotification();

                /**
                 * 设置时候正在显示通知
                 */
                void setShowNotification(boolean showNotification);



}
