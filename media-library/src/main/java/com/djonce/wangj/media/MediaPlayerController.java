package com.djonce.wangj.media;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

/**
 * 播放器控制面板
 *
 * Created by wangj on 2016/5/23.
 */
public class MediaPlayerController extends FrameLayout implements IMediaController {

    private static final String TAG = MediaPlayerController.class.getSimpleName();
    private MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private ImageView mVideoPlayBtn;
    private SeekBar mVideoSeekBar;
    private TextView mVideoTotalTime;
    private ImageView mIvFullscreenOpen;
    private TextView mVideoPlayTimeLeft;
    private ProgressBar mVideoMiniProgressbar;
    private ProgressBar mBufferProgressBar; // 缓冲进度
    private LinearLayout mVideoControlPanel;

    private boolean mDragging;
    private boolean mShowing;
    private static final int sDefaultTimeout = 3000;
    private static final int sMaxTimeout = 3600000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private OnPlayerControllerListener listener;

    public MediaPlayerController(Context context) {
        super(context);
        this.mContext = context;
        initControllerView();
    }

    public MediaPlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initControllerView();
    }

    public MediaPlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initControllerView();
    }

    private void initControllerView() {
        View mContainer = LayoutInflater.from(mContext).inflate(R.layout.media_player_controller_view, null);
        mVideoPlayBtn = (ImageView) mContainer.findViewById(R.id.video_play_btn);
        mVideoSeekBar = (SeekBar) mContainer.findViewById(R.id.video_seek_bar);
        mVideoPlayTimeLeft = (TextView) mContainer.findViewById(R.id.video_play_time_left);
        mVideoTotalTime = (TextView) mContainer.findViewById(R.id.video_total_time);
        mIvFullscreenOpen = (ImageView) mContainer.findViewById(R.id.iv_fullscreen_open);
        mVideoMiniProgressbar = (ProgressBar) mContainer.findViewById(R.id.video_mini_progressbar);
        mVideoControlPanel = (LinearLayout) mContainer.findViewById(R.id.video_control_panel);

        mBufferProgressBar = (ProgressBar) mContainer.findViewById(R.id.video_buffer_progress_bar);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mVideoSeekBar.setOnSeekBarChangeListener(mSeekListener);
        mVideoPlayBtn.setOnClickListener(mPauseListener);
        mIvFullscreenOpen.setOnClickListener(mFullScreenOpen);

        addView(mContainer);
    }

    public void setOnPlayerControllerListener(OnPlayerControllerListener listener) {
        this.listener = listener;
    }


    @Override
    public void hide() {
        hideOprateViews();
    }

    public void hideOprateViews() {
        mShowing = false;
        mVideoMiniProgressbar.setVisibility(VISIBLE);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        mVideoPlayBtn.setVisibility(GONE);
        // 隐藏开始按钮 显示seek bar， 总时间， 已播时长， 放大按钮
        mVideoControlPanel.setVisibility(GONE);
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Override
    public void setEnabled(boolean enabled) {
        // 当前控件是否可用
        if (enabled) {

        }else {

        }

    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void show(int timeout) {
        mShowing = true;

        if (mPlayer != null && mPlayer.isPlaying()) {
            mVideoPlayBtn.setVisibility(GONE);
        }

        if(timeout > 30000) {
            mVideoPlayBtn.setVisibility(VISIBLE);
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
        }
        if (mPlayer != null && mPlayer instanceof MediaVideoView) {
            switch (((MediaVideoView)mPlayer).getVideoMode()) {
                case SMALL:
                    Log.e(TAG, "-- SMALL --");

                    if (!mPlayer.isPlaying()) {
                        mVideoPlayBtn.setVisibility(VISIBLE);
                    }

                    // 隐藏 mini bar , 停止更新mini bar
                    mVideoMiniProgressbar.setVisibility(VISIBLE);
                    mVideoControlPanel.setVisibility(GONE);

                    break;
                case NORMAL:
                    Log.e(TAG, "-- NORMAL --");
                    // 当前播放的状态
                    mIvFullscreenOpen.setImageResource(R.drawable.ds_detail_fullscreen_open);
                    // 隐藏 mini bar , 停止更新mini bar
                    mVideoMiniProgressbar.setVisibility(GONE);
                    mVideoControlPanel.setVisibility(VISIBLE);
                    break;
                case FULL:
                    Log.e(TAG, "-- FULL --");

                    // 隐藏 mini bar , 停止更新mini bar
                    mVideoMiniProgressbar.setVisibility(GONE);
                    mVideoControlPanel.setVisibility(VISIBLE);
                    mIvFullscreenOpen.setImageResource(R.drawable.ds_detail_fullscreen_close);
                    break;
            }
        }

        // 显示开始按钮 显示seek bar ,总时间， 已播时长， 放大按钮
        setSeekBarProgress();

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            Message msg = mHandler.obtainMessage(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }


    @Override
    public void showProgress(boolean isShow) {
        if(isShow) {
            // 显示加载进度
            mBufferProgressBar.setVisibility(VISIBLE);
        }else {
            // 关闭进度框
            mBufferProgressBar.setVisibility(GONE);
        }
    }

    @Override
    public void showOnce(View view) {

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    setSeekBarProgress();
                    if (!mDragging && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    /**
     * 格式化时长
     * @param timeMs
     * @return
     */
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mVideoMiniProgressbar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mVideoMiniProgressbar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mVideoMiniProgressbar.setSecondaryProgress(percent * 10);
        }

        return position;
    }

    /**
     * 暂停 设置seek bar
     *
     * @return position
     */
    private int setSeekBarProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mVideoSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mVideoSeekBar.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mVideoSeekBar.setSecondaryProgress(percent * 10);
        }

        if (mVideoTotalTime != null)
            mVideoTotalTime.setText(stringForTime(duration));
        if (mVideoPlayTimeLeft != null)
            mVideoPlayTimeLeft.setText(stringForTime(position));

        return position;
    }

    private final OnClickListener mPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            autoStartPlay();
        }
    };

    private final OnClickListener mFullScreenOpen = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // 执行 放大 缩小的功能即可， 显示的功能放在 controller show的时候处理

            if (mPlayer != null && mPlayer instanceof MediaVideoView) {
                switch (((MediaVideoView)mPlayer).getVideoMode()) {
                    case SMALL:

                        break;
                    case NORMAL:
                        if(listener != null) {
                            listener.onZoomBig();
                        }
                        break;
                    case FULL:
                        if(listener != null) {
                            listener.onZoomSmall();
                        }
                        break;
                }
            }

        }
    };

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            Log.e(TAG, "progress :" + progress + "fromuser:" + fromuser);
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / mVideoSeekBar.getMax();
            mPlayer.seekTo((int) newposition);
            if (mVideoPlayTimeLeft != null){
                mVideoPlayTimeLeft.setText(stringForTime((int) newposition));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setSeekBarProgress();

            show(mPlayer.isPlaying() ? sDefaultTimeout: sMaxTimeout);

            autoStartPlay();
        }
    };


    private void doVideoResume() {

        if(mPlayer != null) {
            mPlayer.start();
            // 隐藏播放按钮

            show();

            mVideoPlayBtn.setVisibility(GONE);
            mVideoSeekBar.invalidate();
            setSeekBarProgress();
        }
    }


    public void autoStartPlay() {
        doVideoResume();
        if (listener != null) {
            listener.onPlay();
        }
    }

    public void onPause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            // 显示播放按钮
            show(sMaxTimeout);
        }
    }

    public void onResume() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            autoStartPlay();
        }
    }

    public interface OnPlayerControllerListener {
        void onPlay();  // 播放

        void onPause(); // 暂停

        void onResume(); // 开始

        void onZoomBig();

        void onZoomSmall();

        void onComplete();
    }

}
