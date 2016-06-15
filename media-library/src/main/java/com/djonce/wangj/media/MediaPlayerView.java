package com.djonce.wangj.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * 媒体播放view
 * 由MediaViewView 与 MediaPlayerController组成
 * <p/>
 * Created by wangj on 2016/5/23.
 */
public class MediaPlayerView extends RelativeLayout {
    private static final String TAG = MediaPlayerView.class.getSimpleName();
    private Context mContext;
    private ImageView mImageView;  // 现在视频首帧图片，暂停时显示当前暂停的图片
    private MediaVideoView mediaVideoView;
    private MediaPlayerController mediaPlayerController;

    private boolean isPaused = false; // 是否是点击暂停状态
    private String videoPath;
    private OnLoadImageListener loadImageListener;

    public MediaPlayerView(Context context) {
        super(context);
        initView(context);
    }

    public MediaPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MediaPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;

        View mContainer = LayoutInflater.from(context).inflate(R.layout.media_player_view, null);
        mediaVideoView = (MediaVideoView) mContainer.findViewById(R.id.media_video_view);
        mediaPlayerController = (MediaPlayerController) mContainer.findViewById(R.id.media_player_controller);
        mImageView = (ImageView) mContainer.findViewById(R.id.media_video_img);
        mediaPlayerController.setMediaPlayer(mediaVideoView);
        mediaVideoView.setMediaController(mediaPlayerController);

        addView(mContainer);
        setListener();
    }

    private void setListener() {
        mediaVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {

            }
        });

        mediaVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                if (mediaPlayerController != null) {
                    mediaPlayerController.show(360000);
                }
            }
        });

        mediaVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {

                switch (what) {
                    case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        Toast.makeText(mContext, "亲，请检查下您的网络状况~", Toast.LENGTH_SHORT).show();
                        break;
                }

                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                        // 开始播放

                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                        // 在数据缓冲
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                        Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                        Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                        Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                        Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:

                        Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + extra);

                        break;
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                        break;
                }
                return true;
            }

        });

        mediaVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                // 显示播放按钮

                Log.e(TAG, " on error ");

                return false;
            }
        });
    }


    public void setOnPlayerControllerListener(MediaPlayerController.OnPlayerControllerListener playerControllerListener) {
        if (playerControllerListener != null) {
            mediaPlayerController.setOnPlayerControllerListener(playerControllerListener);
        }
    }

    public void setVideoShowMode(MediaVideoView.VideoMode mode) {
        switch (mode) {
            case NORMAL:
                LayoutParams normal = (LayoutParams) getLayoutParams();
                normal.width = DisplayUtils.SCREEN_WIDTH_PIXELS;
                normal.height = DisplayUtils.SCREEN_WIDTH_PIXELS * 9 / 16;

                if (mediaVideoView != null) {
                    mediaVideoView.setVideoMode(MediaVideoView.VideoMode.NORMAL);
                }

                if (mediaPlayerController != null) {
                    mediaPlayerController.show();
                }

                break;
            case SMALL:
                LayoutParams small = (LayoutParams) getLayoutParams();
                small.width = DisplayUtils.SCREEN_WIDTH_PIXELS / 2;
                small.height = (DisplayUtils.SCREEN_WIDTH_PIXELS / 2) * 9 / 16;
                small.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                small.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                if (mediaVideoView != null) {
                    mediaVideoView.setVideoMode(MediaVideoView.VideoMode.SMALL);
                }

                // 不用显示control panel,只显示progress mini bar
                if (mediaPlayerController != null) {
                    mediaPlayerController.show();
                }

                break;
            case FULL:
                LayoutParams full = (LayoutParams) getLayoutParams();
                full.width = LayoutParams.MATCH_PARENT;
                full.height = LayoutParams.MATCH_PARENT;

                if (mediaVideoView != null) {
                    mediaVideoView.setVideoMode(MediaVideoView.VideoMode.FULL);
                }

                if (mediaPlayerController != null) {
                    mediaPlayerController.hide();
                }
                break;

        }
    }

    public int getCurrentPlayPosition() {
        return mediaVideoView.getCurrentPosition();
    }

    public void stopPlayback() {
        mediaVideoView.stopPlayback();
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        mediaVideoView.setVideoPath(videoPath);
    }

    /**
     *  加载视频首页图
     * @param videoPath
     * @param imagePath
     */
    public void setVideoAndImagePath(String videoPath, String imagePath) {
        mediaVideoView.setVideoPath(videoPath);
        handleImagePath(imagePath);
    }

    public void setOnLoadImageListener(OnLoadImageListener loadImageListener) {
        this.loadImageListener = loadImageListener;
    }

    private void handleImagePath(String imagePath) {
        if (mImageView != null && !TextUtils.isEmpty(imagePath)) {
            if (loadImageListener != null) {
                loadImageListener.onLoadImage(mImageView, imagePath);
            }
        }
    }

    public void autoStartPlay() {
        mediaPlayerController.autoStartPlay();
    }

    public void pause() {
        mediaPlayerController.onPause();
        // 截取一张视频图片
        if (loadImageListener != null) {
            loadImageListener.onCutImage(mImageView, videoPath);
        }
        Log.d(TAG, "pause: 暂停");
    }

    public void resume() {
        mediaPlayerController.onResume();
        Log.d(TAG, "resume: 活动");
    }

    public interface OnLoadImageListener {
        void onLoadImage(ImageView imageView, String imagePath);

        Bitmap onCutImage(ImageView imageView, String imagePath); // 截屏图片
    }

}
