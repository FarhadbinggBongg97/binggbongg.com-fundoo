package com.app.binggbongg.fundoo.Video;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.Utility;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.utils.Constants;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class VideoPreviewActivity extends AppCompatActivity {

    private File outputVideoPath;
    private VideoView videoView;
    private ProgressBar progress_bar;
    private ImageView volumeControl;

    public PlayState videoState; // video pause and play
    ScheduledExecutorService mExecutorService; // ProgressBar
    Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mExecutorService = Executors.newSingleThreadScheduledExecutor();

        videoView = findViewById(R.id.videoView);
        volumeControl = findViewById(R.id.volumeControl);
        progress_bar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btnBack);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setColorFilter(getResources().getColor(R.color.colorWhite));

        outputVideoPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_MIX_VIDEO + Utility.VIDEO_FORMAT);

        initializePlayer();

        videoView.setOnClickListener(v -> animatePlayControl());
        btnBack.setOnClickListener(v -> closeActivity());

    }

    private void initializePlayer() {

        videoView.setVideoPath(String.valueOf(outputVideoPath));

        videoView.setOnPreparedListener(mp -> {

            mp.start();
            videoState = PlayState.ON;
            progressBar();

        });
        videoView.setOnCompletionListener(MediaPlayer::start);
    }

    private void progressBar() {
        runnable = () -> {
            if (videoView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress_bar.setProgress((int) ((videoView.getCurrentPosition() * 100) / videoView.getDuration()), true);
                } else
                    progress_bar.setProgress((int) ((videoView.getCurrentPosition() * 100) / videoView.getDuration()));
            }
            mExecutorService.schedule(runnable, 100, TimeUnit.MILLISECONDS);
        };

        mExecutorService.execute(runnable);
    }


    private void animatePlayControl() {

        if (volumeControl != null) volumeControl.setVisibility(View.VISIBLE);
        try {
            if (videoView != null) {
                if (videoState == PlayState.OFF) {

                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);
                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                        volumeControl.animate().alpha(0f)
                                .setDuration(500).setStartDelay(100);
                        videoView.start();
                        videoState = PlayState.ON;
                    }


                } else if (videoState == PlayState.ON) {
                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);
                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                        videoView.pause();
                        videoState = PlayState.OFF;
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }

    public void closeActivity() {
        mExecutorService.shutdown();
        finish();
        overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
    }

    public enum PlayState {ON, OFF}
}