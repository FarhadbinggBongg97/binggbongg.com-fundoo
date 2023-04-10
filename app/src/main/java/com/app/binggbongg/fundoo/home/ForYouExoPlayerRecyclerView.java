package com.app.binggbongg.fundoo.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.utils.SharedPref;
import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.home.eventbus.ForYouProfileUpdate;
import com.app.binggbongg.fundoo.home.eventbus.homeForYouSwipePrevent;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.HomeResponse;
import com.app.binggbongg.utils.Constants;
import com.google.android.material.textview.MaterialTextView;
import com.vincan.medialoader.DownloadManager;
import com.vincan.medialoader.MediaLoader;
import com.vincan.medialoader.download.DownloadListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class ForYouExoPlayerRecyclerView extends RecyclerView implements DownloadListener {

    private static final String TAG = "ExoPlayerRecyclerView";
    private static final String AppName = "Fundoo";

    /**
     * PlayerViewHolder UI component
     * Watch PlayerViewHolder class
     */
    private ImageView mediaCoverImage, volumeControl;
    private ProgressBar progressBar, progress_duration;
    private View viewHolderParent;
    private FrameLayout mediaContainer;
    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;
    private MaterialTextView reverseTime;

    boolean isAutoScroll;

    RecyclerView recyclerView1;

    private MediaLoader mMediaLoader; // used for cache

    /**
     * variable declaration
     */
    // Media List
    private ArrayList<HomeResponse.Result> homepageResponseResult = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    public int targetPosition;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private RequestManager requestManager;
    // controlling volume state
    private VolumeState volumeState;

    public PlayState videoState; // video pause and play
    Handler mHandler = new Handler();
    Runnable runnable;
    Runnable updateView;

    int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;


    private final View.OnClickListener videoViewClickListener = v -> {
        /*togglePlay();*/
    };

    public ForYouExoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ForYouExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public void togglePlay() {


        if ((homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO))) {
            if (videoPlayer != null) {
                if (videoState == PlayState.OFF) {
                    videoPlayer.setPlayWhenReady(true);
                    videoState = PlayState.ON;

                } else if (videoState == PlayState.ON) {
                    videoPlayer.setPlayWhenReady(false);
                    videoState = PlayState.OFF;
                }

                animatePlayControl();
            }
        } else {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(homepageResponseResult.get(targetPosition).getAndroidUrl()));
                getContext().startActivity(browserIntent);
            } catch (Exception e) {
                Toasty.error(getContext(), R.string.not_valid_url, Toasty.LENGTH_SHORT).show();
            }

        }


    }

    private void init(Context context) {

        this.context = context.getApplicationContext();

        Display display = ((WindowManager) Objects.requireNonNull(
                getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        mMediaLoader = MediaLoader.getInstance(getContext());

        videoSurfaceView = new PlayerView(this.context);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);

        // Added new
        //DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024).createDefaultLoadControl();
        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());
        videoPlayer = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                //      .setLoadControl(loadControl)
                .build();

        //Create the player using ExoPlayerFactory
        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        setVolumeControl(VolumeState.ON);


        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                recyclerView1 = recyclerView;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {


                    Timber.i("SCROLL_STATE_IDLE");

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic

                    if (volumeControl != null) volumeControl.setVisibility(GONE);

                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true);
                    } else {
                        playVideo(false);
                        Timber.i("end of the list");
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Log.e(TAG, "onScrollStateChanged: ::::::::::::::::::SCROLL_STATE_DRAGGING");
                    Timber.i("SCROLL_STATE_DRAGGING");

                    if (homepageResponseResult.size() > 0 && playPosition != -1)
                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, "Ad")); // swipe disabled

                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Log.e(TAG, "onScrollStateChanged: ::::::::::::::::::SCROLL_STATE_SETTLING");
                    Timber.i("SCROLL_STATE_SETTLING");

                    if (homepageResponseResult.size() > 0 && playPosition != -1 && homepageResponseResult.get(playPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {
                        EventBus.getDefault().post(new homeForYouSwipePrevent(false, "")); // swipe enabled
                    } else
                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, "Ad")); // swipe disabled
                }
            }


            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                recyclerView1 = recyclerView;
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = ((LinearLayoutManager) getLayoutManager()).getItemCount();
                firstVisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();


            }
        });


        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }
            }
        });

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups,
                                        TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

                Timber.d("onLoadingChanged: %s", isLoading);

            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
                Timber.d("onTimelineChanged: %s", timeline);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        Log.e(TAG, "onPlayerStateChanged: :::::STATE_BUFFERING");
                        //videoPlayer.seekTo(0);
                        //mediaCoverImage.setAlpha(1f);
                        break;
                    case Player.STATE_ENDED:

                        isAutoScroll = SharedPref.getBoolean(SharedPref.isAUTO_SCROLL, false);

                        if (isAutoScroll) {
                            recyclerView1.smoothScrollToPosition(targetPosition + 1);
                            videoPlayer.seekTo(0);
                            if (ForYouVideoFragment.giftDialog != null &&
                                    ForYouVideoFragment.giftDialog.isShowing()) {
                                ForYouVideoFragment.giftDialog.dismiss();
                            }
                            if (ForYouVideoFragment.hideBottomBarBS != null &&
                                    ForYouVideoFragment.hideBottomBarBS.isShowing()) {
                                ForYouVideoFragment.hideBottomBarBS.dismiss();
                            }
                        } else {
                            videoPlayer.seekTo(0);
                        }

                        break;
                    case Player.STATE_IDLE:
                        Log.e(TAG, "onPlayerStateChanged: :::::STATE_IDLE");
                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: :::::STATE_READY");
                        mediaCoverImage.animate().alpha(0f)
                                .setDuration(300).setStartDelay(250);

                        videoState = PlayState.ON;
                        volumeState = VolumeState.ON;

                        if (!isVideoViewAdded) {
                            addVideoView();
                            progressBar();
                        }

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.e(TAG, "onRepeatModeChanged: ::::::::::::" + repeatMode);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.e(TAG, "onShuffleModeEnabledChanged: ::::::::::::::" + shuffleModeEnabled);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError: :::::::::::::::::::::" + error);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.e(TAG, "onPositionDiscontinuity: :::::::::::::" + reason);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.e(TAG, "onPlaybackParametersChanged: :::::::::::::::" + playbackParameters.speed);
            }

            @Override
            public void onSeekProcessed() {

            }
        });
    }

    private void progressBar() {

        runnable = () -> {

            if (videoPlayer != null) {
//                reverseTime.setText((videoPlayer.getCurrentPosition()/videoPlayer.getDuration())*100);
                long timeLeft = (videoPlayer.getDuration() - videoPlayer.getCurrentPosition());
                if (timeLeft > 0) {
                    reverseTime.setText(calculateTimeLeft(timeLeft) + "");
                }

                progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration()));
            }
            mHandler.postDelayed(runnable, 1);
        };
        runnable.run();

    }

    public String calculateTimeLeft(long timeLeft) {
        return String.format(
                "%d.%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeLeft) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(timeLeft) % TimeUnit.MINUTES.toSeconds(1)
        );
    }


    public void setPlayControl(Boolean state) {

        videoPlayer.setPlayWhenReady(state);
        if (volumeControl != null && volumeControl.getVisibility() == VISIBLE)
            volumeControl.setVisibility(GONE);
    }


    public void playVideo(boolean isEndOfList) {


        if (!isEndOfList) {
            int startPosition = ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition =
                        startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = homepageResponseResult.size() - 1;
        }


        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition;

        Timber.d("playVideo: %s", playPosition);

        if (playPosition >= 0)
            if (homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {

                String userType = "";
                if (GetSet.getUserId() != null) {
                    userType = (homepageResponseResult.get(targetPosition).getPublisherId()).equals(GetSet.getUserId()) ? "" : homepageResponseResult.get(targetPosition).getPublisherId();
                    Timber.i("userType %s", userType);
                }

                EventBus.getDefault().post(new ForYouProfileUpdate(homepageResponseResult.get(playPosition).getPublisherId(), homepageResponseResult.get(playPosition).getPublisherImage(), userType));
                EventBus.getDefault().post(new homeForYouSwipePrevent(false, "")); // swipe enabled
            } else
                EventBus.getDefault().post(new homeForYouSwipePrevent(true, "Ad")); // swipe disabled

        if (videoSurfaceView == null) {
            return;
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();

        View child = getChildAt(currentPosition);

        if (child == null) {
            return;
        }

        ForYouVideoFragment.VideoAdapter.VideoViewHolder holder = (ForYouVideoFragment.VideoAdapter.VideoViewHolder) child.getTag();


        if (holder == null) {
            playPosition = -1;
            return;
        }
        viewHolderParent = holder.itemView;
        mediaContainer = holder.mediaContainer;
        mediaCoverImage = holder.video_thumbnail;
        progressBar = holder.progressBar;
        volumeControl = holder.video_play_pause;
        progress_duration = holder.progress_duration;
        reverseTime = holder.time_reverse_tv;


        videoSurfaceView.setPlayer(videoPlayer);
        //viewHolderParent.setOnClickListener(videoViewClickListener);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                context, Util.getUserAgent(context, AppName));

        String mediaUrl;

        MediaSource videoSource = null;

        HomeResponse.Result data = homepageResponseResult.get(targetPosition);

        if (Objects.equals(data.getVideoType(), "gallery") || (Objects.equals(data.getPlaytype(), "ad"))) {
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else {
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        }

        if (homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {

            boolean isCached = mMediaLoader.isCached(homepageResponseResult.get(targetPosition).getPlaybackUrl());

            if (!isCached && getContext() != null)
                DownloadManager.getInstance(getContext()).enqueue(new DownloadManager.Request(homepageResponseResult.get(targetPosition).getPlaybackUrl()), this);

            Timber.d("playVideo: isCached=> %s", isCached);

            if (isCached) {
                mediaUrl = String.valueOf(mMediaLoader.getCacheFile(homepageResponseResult.get(targetPosition).getPlaybackUrl()));

            } else {
                mediaUrl = homepageResponseResult.get(targetPosition).getPlaybackUrl();
            }

            if (mediaUrl != null) {
                videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(mediaUrl));
            }

        } else {
            videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(homepageResponseResult.get(targetPosition).getAndroidVideo()));
        }

        Timber.i("For you playing Url %s", homepageResponseResult.get(targetPosition).getPlaybackUrl());

        videoPlayer.prepare(videoSource);
        videoPlayer.setPlayWhenReady(true);

    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {

        int at = playPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }

    // Remove the old player
    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }
    }

    private void addVideoView() {
        mediaContainer.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
    }

    private void resetVideoView() {
        if (isVideoViewAdded) {
            mediaCoverImage.setAlpha(1f);
            playPosition = -1;
            removeVideoView(videoSurfaceView);
            videoSurfaceView.setVisibility(INVISIBLE);
        }
    }

    /*public void releasePlayer() {

        if (videoPlayer != null) {
            videoSurfaceView = null;
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }*/

    public void onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer.stop(true);
        }
    }


    private void animatePlayControl() {

        if (volumeControl != null) volumeControl.setVisibility(VISIBLE);

        try {
            if (videoPlayer != null) {
                if (videoState == PlayState.OFF) {

                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);
                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                    }


                } else if (videoState == PlayState.ON) {
                    if (
                            volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);

                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                        volumeControl.animate().alpha(0f)
                                .setDuration(300).setStartDelay(100);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //public void onRestartPlayer() {
    //  if (videoPlayer != null) {
    //   playVideo(true);
    //  }
    //}

    private void setVolumeControl(VolumeState state) {
        volumeState = state;
        if (state == VolumeState.OFF) {
            videoPlayer.setVolume(0f);
        } else if (state == VolumeState.ON) {
            videoPlayer.setVolume(1f);
        }
    }


    public void setMediaObjects(ArrayList<HomeResponse.Result> mHomepageResponseResult) {
        this.homepageResponseResult = mHomepageResponseResult;
    }

    @Override
    public void onProgress(String url, File file, int progress) {

        Timber.d("url %s progerss:::::::: %s", url, progress);
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError: :::::::::::::::", e);
    }

    /**
     * Volume ENUM
     */
    private enum VolumeState {
        ON, OFF
    }

    public enum PlayState {ON, OFF}

}
