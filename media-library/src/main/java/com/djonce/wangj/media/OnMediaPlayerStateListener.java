package com.djonce.wangj.media;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 *
 * Created by wangj on 2016/6/16 0016.
 */
public abstract class OnMediaPlayerStateListener {

    public void  onFirstPlay(){}  // 首次播放

    public void onClickPlay(){}  // 点击播放

    public abstract void onSeekPlay();   // 滑动进度播放

    public void onBufferStart() {}  // 视频缓冲

    public void onBufferEnd() {}  // 视频缓冲完成

    public abstract void onPause();      // 暂停了播放

    public abstract void onResume();     // 恢复播放状态

    public abstract void onError(IMediaPlayer mp, int what, int extra);      // 播放错误

    public abstract void onCompleted(IMediaPlayer mp);  // 播放完毕
}
