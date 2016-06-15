package com.djonce.wangj.stickyvideo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.djonce.wangj.media.DisplayUtils;
import com.djonce.wangj.media.MediaPlayerController;
import com.djonce.wangj.media.MediaPlayerView;
import com.djonce.wangj.media.MediaVideoView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.sticky_layout)
    RelativeLayout stickyLayout;

    @Bind(R.id.full_layout)
    RelativeLayout fullLayout;

    View secondHeader;
    RelativeLayout secondRootLayout;
    MediaPlayerView videoView;

    ArrayAdapter<String> adapter;
    boolean isSticky = false;  // 是否是悬浮状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        DisplayUtils.init(this);

        initViews();
        setAdapter();

        startVideo();
    }


    private void initViews() {

        View firstHeader = View.inflate(this, R.layout.view_first_header, null);

        listView.addHeaderView(firstHeader);
        secondHeader = View.inflate(this, R.layout.view_second_header, null);

        secondRootLayout = (RelativeLayout) secondHeader.findViewById(R.id.secondRootLayout);
        videoView = (MediaPlayerView) secondHeader.findViewById(R.id.videoView);
        listView.addHeaderView(secondHeader);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isSticky && firstVisibleItem == 0) {
                    removeStickyView();
                    isSticky = false;
                }

                if (!isSticky && firstVisibleItem > 1) {
                    // 将headView添加到sticky layout中
                    addStickyView();
                    isSticky = true;
                }
            }
        });

        videoView.setOnPlayerControllerListener(new MediaPlayerController.OnPlayerControllerListener() {
            @Override
            public void onPlay() {
                Log.e(TAG, " --- onPlay ---");
            }

            @Override
            public void onPause() {
                Log.e(TAG, " --- onPause ---");
            }

            @Override
            public void onResume() {

            }

            @Override
            public void onZoomBig() {
                Log.e(TAG, " --- onZoomBig ---");
                zoomBig();
            }

            @Override
            public void onZoomSmall() {
                zoomSmall();
            }

            @Override
            public void onComplete() {
                Log.e(TAG, " --- onComplete ---");
            }
        });

    }

    private void zoomSmall() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        quitFullScreen();
        removeFullView();
    }

    private void zoomBig() {
        // 旋转屏幕
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setFullScreen();
        addFullView();
    }

    private void startVideo() {
        String videoPath = "http://video2.peiyinxiu.com/2016050313476874fdeb76898f27.mp4";
        videoView.setVideoPath(videoPath);
        videoView.requestFocus();
        videoView.autoStartPlay();
    }


    private void addStickyView() {
        // 将head添加到sticky layout中
        secondRootLayout.removeAllViews();
        if (stickyLayout != null) {
            stickyLayout.removeAllViews();

            videoView.setVideoShowMode(MediaVideoView.VideoMode.SMALL);
            stickyLayout.addView(videoView);
            stickyLayout.startAnimation(showStickyAnimation());
        }

    }

    private void removeStickyView() {
        // 移除 sticky layout 中view
        if (stickyLayout != null) {
            stickyLayout.removeAllViews();
            stickyLayout.startAnimation(hideStickyAnimation());
        }
        // 恢复 videoView 状态
        videoView.setVideoShowMode(MediaVideoView.VideoMode.NORMAL);
        secondRootLayout.addView(videoView);
    }

    private void addFullView() {
        secondRootLayout.removeAllViews();
        if (fullLayout != null) {
            fullLayout.removeAllViews();

            videoView.setVideoShowMode(MediaVideoView.VideoMode.FULL);
            fullLayout.addView(videoView);
            fullLayout.setVisibility(View.VISIBLE);
            fullLayout.startAnimation(showStickyAnimation());
        }
    }

    private void removeFullView() {
        if (fullLayout != null) {
            fullLayout.removeAllViews();
            fullLayout.setVisibility(View.GONE);
            fullLayout.startAnimation(hideStickyAnimation());
        }
        // 恢复 videoView 状态
        videoView.setVideoShowMode(MediaVideoView.VideoMode.NORMAL);
        secondRootLayout.removeAllViews();
        secondRootLayout.addView(videoView);
    }


    private void setAdapter() {
        if (adapter == null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getData());
        }

        listView.setAdapter(adapter);
    }

    private List<String> getData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("item " + i);
        }

        return list;
    }

    private Animation showStickyAnimation() {

        Animation mAlphaAction = new AlphaAnimation(0.0f, 1.0f);
        mAlphaAction.setDuration(300);

        return mAlphaAction;
    }

    private Animation hideStickyAnimation() {

        Animation mHideAlphaAction = new AlphaAnimation(1.0f, 0.0f);
        mHideAlphaAction.setDuration(100);
        return mHideAlphaAction;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    private void setFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void quitFullScreen() {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (videoView.getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT) {
            zoomSmall();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.pause();
        }
    }
}
