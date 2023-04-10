package hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import hitasoft.serviceteam.livestreamingaddon.LinkViewActivity;
import hitasoft.serviceteam.livestreamingaddon.PlayerActivity;
import hitasoft.serviceteam.livestreamingaddon.R;
import hitasoft.serviceteam.livestreamingaddon.external.avloading.AVLoadingIndicatorView2;
import hitasoft.serviceteam.livestreamingaddon.external.avloading.BallBeatIndicator;
import hitasoft.serviceteam.livestreamingaddon.external.heartlayout.HeartLayout;
import hitasoft.serviceteam.livestreamingaddon.external.helper.CustomLayoutManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.LocaleManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.NetworkReceiver;
import hitasoft.serviceteam.livestreamingaddon.external.helper.SocketIO;
import hitasoft.serviceteam.livestreamingaddon.external.helper.Utils;
import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import hitasoft.serviceteam.livestreamingaddon.external.utils.AdminData;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiClient;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiInterface;
import hitasoft.serviceteam.livestreamingaddon.external.utils.AppUtils;
import hitasoft.serviceteam.livestreamingaddon.external.utils.Constants;
import hitasoft.serviceteam.livestreamingaddon.external.utils.GetSet;
import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import hitasoft.serviceteam.livestreamingaddon.model.AppDefaultResponse;
import hitasoft.serviceteam.livestreamingaddon.model.FollowRequest;
import hitasoft.serviceteam.livestreamingaddon.model.Gift;
import hitasoft.serviceteam.livestreamingaddon.model.Report;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The audio component of a {@link Player}.
 */
public class SubscribeActivity extends AppCompatActivity implements OnClickListener, SocketIO.SocketEvents,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

/*    ExoPlayer.EventListener,
    PlaybackControlView.VisibilityListener,*/

    String parent="";
    AppCompatTextView txtTime;

    private static final String TAG = "SubscribeActivity";
    GiftAdapter giftAdapter;

    AppCompatTextView publisherName;
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    private static DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    LinearLayout followLay,videoLinkLay;
    MaterialTextView profileFollowIcon;
    private FrameLayout contentLay;
    private PlayerView playerView;
    private ImageView btnClose;
    MaterialTextView btnGift;
    private ImageView btnInfo;
    private RelativeLayout bottomLay;
    private RelativeLayout commentsLay;
    private RecyclerView rvComments;
    private HeartLayout heartLay;
    private LinearLayout viewersLay;
    private RelativeLayout viewLay;
    private ImageView iconView;
    private AppCompatTextView txtViewCount, txtLiveCount;
    private RecyclerView rvViewers;
    private RelativeLayout chatLay;
    private RelativeLayout messageLay, liveCountLay, liveStatusLay;
    private EditText edtMessage;
    private ImageView btnSend;
    private FrameLayout loadingLay, liveTxtLay;
    private ImageView loadingImage;
    private AppCompatTextView txtLoadingTitle;
    private AppCompatTextView loadingViewCount;
    private RoundedImageView loadingUserImage;
    private AppCompatTextView loadingUserName;
    private LinearLayout initializeLay;
    private AVLoadingIndicatorView2 initialIndicator;
    AppCompatTextView bottomTxtLiveCount, txtBottomDuration, lftVoteCount;
    RelativeLayout bottomDurationLay;
    AppCompatTextView bottomStreamTitle;
    RoundedImageView publisherImage;
    RoundedImageView publisherColorImage;
    AppCompatTextView txtPublisherName;
    AVLoadingIndicatorView2 avBallIndicator;
    private CustomLayoutManager commentLayoutManager;


    private RelativeLayout bottomLiveCountLay;
    private FrameLayout bottomLiveTxtLay;
    LinearLayout bottomFirstLay, bottomDeleteLay,bottomShareLay, bottomReportLay, chatHideLay, bottomDetailsLay,
            bottomUserLay, bottomTopLay, bottomViewersLay, bottomGiftsLay,
            bottomInternalShareLay, bottomExternalShareLay;
    AppCompatTextView bottomViewerCount, txtReport, bottomGiftCount;
    RecyclerView bottomRecyclerView, bottomGiftView;
    private BottomSheetDialog bottomDialog, bottomCommentsDialog, bottomGiftsDialog,
            giftDialog, bottomHashTagsDialog, reportDialog;
    private Button btnMoreHashTag;
    private LinearLayout sendLay;
    private TextView txtAttachmentName, txtSend;
    private int giftPosition = 0;

    private RecyclerView giftsView, stickersView, recyclerView, reportsView;


    private ReportAdapter reportAdapter;


    private ApiInterface apiInterface;
    private Animation slideIn;
    private Animation slideOut;
    private boolean isAnimated = false, isUnsubscribed = false, isKeyPadVisible = false, mPermissionsGranted = false;

    private LinearLayoutManager stickerLayoutManager;
    private int finalGiftPosition;
    private int ITEM_LIMIT = 8;
    List<String> tempSmileyList = new ArrayList<>();

    int displayHeight, displayWidth;
    private String from, streamName, streamImage, publisherId, videoId;
    private SocketIO socketIO;
    private Socket mSocket;
    ArrayList<StreamDetails.LiveViewers> viewersList = new ArrayList<>();
    ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();
    ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
    private int publisherColor;

    private String currentDuration = null;
    private StreamDetails streamInfo;
    private StreamDetails streamData;
    private GestureDetector gestureScanner;
    private SharedPreferences pref;

    private String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private ArrayList<String> hashTagList = new ArrayList<>();
    private boolean isUserJoined = false;

    private boolean isStarted = false;


    private static String USER_AGENT;
    private boolean USE_BANDWIDTH_METER = false, needRetrySource;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean shouldAutoPlay = true;
    private int resumeWindow;
    private long resumePosition;
    private RtmpDataSource.RtmpDataSourceFactory rtmpDataSourceFactory;
    private CountDownTimer publishTimer;
    CircleIndicator pagerIndicator;


    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;

    //  private DataSource.Factory mediaDataSourceFactory;
    private CommentsAdapter commentsAdapter;
    private BottomListAdapter bottomListAdapter;

    //   private DebugTextViewHelper debugViewHelper;
    private ViewerListViewAdapter viewerListAdapter;


    protected String userAgent;
    private EditText videoNameEditText;
    private String userLikeColor;

    public List<String> reportList = new ArrayList<String>();

    Handler handler = new Handler();
    Boolean isStopStreamCall = false;
    List<Gift> tempGiftList = new ArrayList<>();
    private ViewPager viewPager;
    AppUtils appUtils;
    Utils appUtil;
    private GiftsAdapter giftsAdapter;
    public static float currentVolume;

    /*private Runnable liveCheckRunnable = new Runnable() {
        @Override
        public void run() {
            getStreamInfo();
        }
    };
*/
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (isStopStreamCall) {
                Log.d(TAG, "run: isstramcall");
                showToastAlert(getString(R.string.stream_end_description));
                unSubscribeStream();
                handler.removeCallbacks(r);
            }
        }
    };


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            Log.d(TAG, "call: onConnect");
            subscribeStream();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // subscribeStream();
                    getStreamInfo(streamName);
                }
            });
        }
    };

    private Emitter.Listener _giftStatus = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_giftStatus: " + data);
            HashMap<String, String> map = new HashMap<>();
            String status = data.optString(Constants.TAG_STATUS);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status.equals(Constants.TAG_TRUE)) {
                        //GetSet.setGems(Long.valueOf(data.optString(Constants.TAG_TOTAL_GEMS)));
                        Gift gift = AdminData.giftList.get(finalGiftPosition);
                        map.put(Constants.TAG_GIFT_ID, gift.getGiftId());
                        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                        map.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                        map.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
                        map.put(Constants.TAG_GIFT_TITLE, gift.getGiftTitle());
                        map.put(Constants.TAG_GIFT_ICON, gift.getGiftIcon());
                        map.put(Constants.TAG_TYPE, Constants.TAG_GIFT);
                        map.put(StreamConstants.TAG_STREAM_NAME, streamName);
                        sendGiftMessage(map);
                        if (giftDialog != null && giftDialog.isShowing()) {
                            giftDialog.dismiss();
                        }
                        addComment(map);
                        getStreamInfo(streamName);
                    } else if (status.equals(Constants.TAG_FALSE)) {
                        Toast.makeText(SubscribeActivity.this, getString(R.string.not_enough_gems), Toast.LENGTH_SHORT).show();
                    } else {
//                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }
            });
        }
    };

    private final Player.EventListener playerListener = new Player.EventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            Log.d(TAG, "onPlayerStateChanged: " + playbackState);
            if (playbackState == Player.STATE_BUFFERING) {
                if (isStopStreamCall) { //handler.postDelayed(r, 2000);
                    handler.post(r);
                }
//                timerCancel();
                setBuffering(VISIBLE);
            } else if (playbackState == Player.STATE_ENDED) {
                Log.d(TAG, "onPlayerStateChanged: STATE_ENDED");
                unSubscribeStream();
                showToastAlert(getString(R.string.stream_end_description));
            } else if (playWhenReady && playbackState == Player.STATE_READY) {
/*                if (isStopStreamCall) {
                    handler.postDelayed(r, 8000);
                    isStarted = true;
                }*/
                if (!isStarted) {
                    //  handler.postDelayed(liveCheckRunnable, 0);
                    isStarted = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBuffering(GONE);
                            if (loadingLay.getVisibility() == VISIBLE) {
                                loadingLay.setVisibility(GONE);
                                initialIndicator.hide();
                                initializeLay.setVisibility(GONE);
                            }
                            //subscribeStream();
                            //sendUserJoined();
                            //progressBar();
                        }
                    });
                }
                setBuffering(GONE);
            } else if (playbackState == Player.STATE_IDLE) {
                if (isStarted) {
                    unSubscribeStream();
                    showToastAlert(getString(R.string.stream_end_description));
                }
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (needRetrySource) {
                // This will only occur if the user has performed a seek whilst in the error state. Update the
                // resume position so that if the user then retries, playback will resume from the position to
                // which they seeked.
                updateResumePosition();
            }
        }



        /*@Override
        public void onPlayerError(ExoPlaybackException error) {

            Log.d(TAG, "onPlayerError: call");

        }*/

/*        @Override
        public void onPlayerError(@NotNull ExoPlaybackException error) {

            Log.d(TAG, "onPlayerError: call");
            unSubscribeStream();

        }*/

        /*@Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "onPlayerError: call");
            unSubscribeStream();

        }*/


        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            try {
                String errorString = null;
                Log.e(TAG, "onPlayerError: " + e.getMessage());
                Log.e(TAG, "onPlayerError: " + e.type);
                if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                    Exception cause = e.getRendererException();
                    if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                        // Special case for decoder initialization failures.
                        MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                                (MediaCodecRenderer.DecoderInitializationException) cause;
                        if (decoderInitializationException.diagnosticInfo == null) {
                            if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                                errorString = getString(R.string.error_querying_decoders);
                            } else if (decoderInitializationException.secureDecoderRequired) {
                                errorString = getString(R.string.error_no_secure_decoder,
                                        decoderInitializationException.mimeType);
                            } else {
                                errorString = getString(R.string.error_no_decoder,
                                        decoderInitializationException.mimeType);
                            }
                        } else {
                            errorString = getString(R.string.error_instantiating_decoder,
                                    decoderInitializationException.diagnosticInfo);
                        }
                    }
                } else {
                    if (e.type == ExoPlaybackException.TYPE_UNEXPECTED) {
                        if (e.getMessage() != null && e.getMessage().contains("com.google.android.exoplayer2.extractor.SeekMap$SeekPoints")) {
                            if (from.equals(Constants.TAG_SUBSCRIBE) ||
                                    from.equals(Constants.TAG_OTHER_PROFILE)) {
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                        } else {
                            Log.e(TAG, "getUnexpectedException: " + e.getUnexpectedException().getMessage());
                        }
                    } else {
                        Log.e(TAG, "getUnexpectedException: " + e.getUnexpectedException().getMessage());
                    }
                }
                if (errorString != null) {
                    // App.makeToast(errorString);
                    Toast.makeText(SubscribeActivity.this, errorString, Toast.LENGTH_SHORT).show();
                }
                needRetrySource = true;
                if (isBehindLiveWindow(e)) {
                    shouldAutoPlay = false;
                    clearResumePosition();
                    play(null);
                } else {
                    updateResumePosition();
                }
            } catch (Exception exception) {

                unSubscribeStream();
                Toast.makeText(SubscribeActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        }

    };
    private Runnable runnable;
    Handler mHandler = new Handler();

    private void progressBar() {
        runnable = () -> {

            if (player != null) {
                txtTime.setText(String.format(
                        "%02d.%02d",
                        TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()) % TimeUnit.MINUTES.toSeconds(1)
                ));
               }
            mHandler.postDelayed(runnable, 1);
        };
        runnable.run();

    }



    private Emitter.Listener _subscriberLeft = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(TAG, "_subscriberLeft: " + data);
                    getStreamInfo(data.optString(StreamConstants.TAG_STREAM_NAME));
                }
            });
        }
    };

    private Emitter.Listener _streamInfo = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamInfo: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (data.optString("status").equals("false")) {
                        // TODO: stop the stream.
                        return;
                    }
                    Gson gson = new Gson();
                    StreamDetails tempDetails = gson.fromJson("" + data, StreamDetails.class);
                    streamInfo = tempDetails;
                    txtViewCount.setText(tempDetails.getWatchCount() != null ? tempDetails.getWatchCount() : "");
                    lftVoteCount.setText(tempDetails.getLftVoteCount()!=null?
                            tempDetails.getLftVoteCount():"0");
                    publisherId = tempDetails.getPublisherId();
                    videoId = tempDetails.getStreamId();
                    viewersList.clear();
                    viewersList.addAll(tempDetails.getLiveViewers());
                    viewerListAdapter.notifyDataSetChanged();
                    loadingViewCount.setText(tempDetails.getWatchCount() != null ? tempDetails.getWatchCount() : "");

                    giftsList.clear();
                    giftsList.addAll(tempDetails.getGiftList());
                    bottomGiftCount.setText("" + giftsList.size());
                    giftsAdapter.notifyDataSetChanged();
                    setBottomDialogUI(tempDetails);
                }
            });
        }
    };

    private Emitter.Listener _streamBlocked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamBlocked: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SubscribeActivity.this, getString(R.string.broadcast_deactivated_by_admin), Toast.LENGTH_SHORT).show();
                    unSubscribeStream();
                }
            });
        }
    };
    private String type;
    private BottomSheetDialog shareDialog;


    private void sendGift(int giftPosition) {
        final Gift gift = AdminData.giftList.get(giftPosition);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(StreamConstants.TAG_GIFT_FROM, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_GIFT_TO, publisherId);
            jsonObject.put(Constants.TAG_GIFT_ID, gift.getGiftId());
            jsonObject.put(Constants.TAG_GIFT_TITLE, gift.getGiftTitle());
            jsonObject.put(Constants.TAG_GIFT_ICON, gift.getGiftIcon());
            jsonObject.put(Constants.TAG_GEMS_EARNINGS, gift.getGiftGems());
            jsonObject.put(StreamConstants.TAG_TIME, currentDuration);
            jsonObject.put("video_id", videoId);
          /*  if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                jsonObject.put(Constants.TAG_GEMS_COUNT, gift.getGiftGemsPrime());
            } else {*/
            jsonObject.put(Constants.TAG_GEMS_COUNT, gift.getGiftGems());
            // }
            if (mSocket.connected()) {
                Log.d(TAG, "sendGift: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SEND_GIFT, jsonObject);
            }
            if(giftDialog.isShowing())
                giftDialog.dismiss();
        } catch (JSONException e) {
            Log.d(TAG, "sendGift: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendGiftMessage(HashMap<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, map.get(Constants.TAG_TYPE));
            jsonObject.put(Constants.TAG_USER_ID, map.get(Constants.TAG_USER_ID));
            jsonObject.put(Constants.TAG_USER_NAME, map.get(Constants.TAG_USER_NAME));
            jsonObject.put(Constants.TAG_USER_IMAGE, map.get(Constants.TAG_USER_IMAGE));
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, map.get(StreamConstants.TAG_STREAM_NAME));
            jsonObject.put(Constants.TAG_GIFT_ID, map.get(Constants.TAG_GIFT_ID));
            jsonObject.put(Constants.TAG_GIFT_TITLE, map.get(Constants.TAG_GIFT_TITLE));
            jsonObject.put(Constants.TAG_GIFT_ICON, map.get(Constants.TAG_GIFT_ICON));
            jsonObject.put(StreamConstants.TAG_TIME, currentDuration);
            if (mSocket.connected()) {
                Log.d(TAG, "sendGift: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.d(TAG, "sendGift: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getStreamInfo(String streamName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket.connected()) {
                Log.d(TAG, "getStreamInfo: " + new Gson().toJson(jsonObject));
                mSocket.emit(StreamConstants.TAG_GET_STREAM_INFO, jsonObject);
            }
        } catch (JSONException e) {
            Log.d(TAG, "getStreamInfo: error=> " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void sendUserJoined() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, StreamConstants.TAG_STREAM_JOINED);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(StreamConstants.TAG_TIME, currentDuration);
            if (mSocket.connected()) {
                Log.d(TAG, "sendUserJoined: " + new Gson().toJson(jsonObject));
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendUserJoined: " + e.getMessage());
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: enter");
                getStreamInfo(streamName);
            }
        }, 1000);
    }

    private void addComment(HashMap<String, String> map) {
        commentList.add(map);
        commentsAdapter.notifyItemInserted(commentList.size() - 1);
        rvComments.scrollToPosition(commentList.size() - 1);
    }


    private void setBottomDialogUI(StreamDetails details) {
        bottomListAdapter.notifyDataSetChanged();
        bottomStreamTitle.setText(details.getTitle());
        bottomViewerCount.setText(details.getWatchCount());
        bottomTxtLiveCount.setText(details.getWatchCount());
        txtPublisherName.setText(details.getPostedBy());
        Glide.with(getApplicationContext())
                .load(details.getPublisherImage())
                .error(R.drawable.no_video)
                .into(publisherImage);

        Glide.with(getApplicationContext())
                .load(details.getPublisherImage())
                .error(R.drawable.no_video)
                .into(loadingUserImage);
        publisherColorImage.setBackgroundColor(publisherColor);
        txtReport.setText(details.getReported().equals(Constants.TAG_FALSE) ? getString(R.string.report_broadcast) :
                getString(R.string.undo_report_broadcast));
        //setFollowButton(details.getFollow().equals(Constants.TAG_TRUE));
    }

/*    private void setFollowButton(boolean status) {
        if (status) {
            btnUnFollow.setVisibility(VISIBLE);
            btnFollow.setVisibility(GONE);
        } else {
            btnUnFollow.setVisibility(GONE);
            btnFollow.setVisibility(VISIBLE);
        }
    }*/

/*    private Emitter.Listener _streamBlocked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamBlocked: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(SubscribeActivity.this, getString(R.string.broadcast_deactivated_by_admin), Toast.LENGTH_SHORT).show();
                    unSubscribeStream();
                }
            });
        }
    };*/


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_subscribe);


        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(this).build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        shouldAutoPlay = true;
        clearResumePosition();
        pref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        //mediaDataSourceFactory = buildDataSourceFactory(true);
        rtmpDataSourceFactory = new RtmpDataSource.RtmpDataSourceFactory();

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        reportList.add(0, "Abuse");
        reportList.add(1, "In-Appropriate");
        reportList.add(2, "Adult Content");

/*        View rootView = findViewById(R.id.root);
        rootView.setOnClickListener(this);*/

        slideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_left_in);
        slideOut = AnimationUtils.loadAnimation(this, R.anim.anim_slide_right_out);

        Intent intent = getIntent();
        from = intent.getStringExtra(Constants.TAG_FROM);
        if(from.equals(Constants.NOTIFICATION)){
            streamName = intent.getStringExtra(StreamConstants.TAG_STREAM_NAME);
            getStreamDetails();
        }else{
            Bundle bundle = intent.getExtras();
            streamData = (StreamDetails) bundle.getSerializable(StreamConstants.TAG_STREAM_DATA);
            streamName = streamData.getName();
            streamImage = streamData.getStreamThumbnail();
            videoId = streamData.getStreamId();
        }




       /* if(intent.getExtras()!=null){
            Bundle bundle = intent.getExtras();
            streamData = (StreamDetails) bundle.getSerializable(StreamConstants.TAG_STREAM_DATA);
            if(streamData!=null && streamData.getStreamName()!=null){
                streamName = streamData.getName();
                streamImage = streamData.getStreamThumbnail();
                videoId = streamData.getStreamId();
            }else{
                streamName = intent.getStringExtra(StreamConstants.TAG_STREAM_NAME);
            }
        }else{
            streamName = intent.getStringExtra(StreamConstants.TAG_STREAM_NAME);
        }*/






        contentLay = (FrameLayout) findViewById(R.id.contentLay);
        playerView = (PlayerView) findViewById(R.id.player_view);
        btnClose = (ImageView) findViewById(R.id.btnClose);
        // btnCancel = (ImageView) findViewById(R.id.btnCancel);

        publisherName=findViewById(R.id.loadingTitle);
      //  followLay=findViewById(R.id.followLay);
        videoLinkLay=findViewById(R.id.linkLay);
        txtTime = findViewById(R.id.txtTime);
        btnInfo = (ImageView) findViewById(R.id.btnDetail);
        bottomLay = (RelativeLayout) findViewById(R.id.bottomLay);
        commentsLay = (RelativeLayout) findViewById(R.id.commentsLay);
        rvComments = (RecyclerView) findViewById(R.id.rv_comments);
        heartLay = (HeartLayout) findViewById(R.id.heartLay);
        viewersLay = (LinearLayout) findViewById(R.id.viewersLay);
        viewLay = (RelativeLayout) findViewById(R.id.viewLay);
        iconView = (ImageView) findViewById(R.id.iconView);
        loadingViewCount = (AppCompatTextView) findViewById(R.id.txtViewCount);
        rvViewers = (RecyclerView) findViewById(R.id.rv_viewers);
        chatLay = (RelativeLayout) findViewById(R.id.chatLay);
        messageLay = (RelativeLayout) findViewById(R.id.messageLay);
        edtMessage = (EditText) findViewById(R.id.edtMessage);
        btnSend = (ImageView) findViewById(R.id.btnSend);
        loadingLay = (FrameLayout) findViewById(R.id.loadingLay);
        loadingImage = (ImageView) findViewById(R.id.loadingImage);
        txtLoadingTitle = (AppCompatTextView) findViewById(R.id.txtLoadingTitle);
        loadingUserImage = (RoundedImageView) findViewById(R.id.loadingUserImage);
        loadingUserName = (AppCompatTextView) findViewById(R.id.loadingUserName);
        initializeLay = (LinearLayout) findViewById(R.id.initializeLay);
        initialIndicator = findViewById(R.id.initialIndicator);
        liveStatusLay = findViewById(R.id.liveStatusLay);
        liveTxtLay = findViewById(R.id.liveTxtLay);
        liveCountLay = findViewById(R.id.liveCountLay);
        txtLiveCount = findViewById(R.id.txtLiveCount);
        txtViewCount = findViewById(R.id.txtViewCount);
        avBallIndicator = findViewById(R.id.avBallIndicator);
        btnGift = findViewById(R.id.vote);
        lftVoteCount = findViewById(R.id.tv_lftVoteCount);


/*        playerView.setControllerVisibilityListener(this);
        playerView.requestFocus();*/

        if (LocaleManager.isRTL()) {
            viewLay.setBackground(getResources().getDrawable(R.drawable.rounded_curve_rtl_bg));
            liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg_rtl));
            liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg_rtl));
        } else {
            viewLay.setBackground(getResources().getDrawable(R.drawable.rounded_curve_bg));
            liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg));
            liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg));
        }
        setInitializingLay();

        initView();
        initSocket();
        initializePlayer(streamName);


        btnSend.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnGift.setOnClickListener(this);
        // btnCancel.setOnClickListener(this);

        videoLinkLay.setOnClickListener(this);
        btnInfo.setOnClickListener(this);
        viewLay.setOnClickListener(this);
        getAppDefaultData();

//        Log.e(TAG, "onCreate: :::::::::::::::::::::::::"+intent.getStringExtra("parent"));

        if (intent.getStringExtra("parent") != null){
            parent=intent.getStringExtra("parent");
            if(parent.equals("userVideo")){
                followLay.setVisibility(View.GONE);
                btnGift.setVisibility(View.GONE);
            }

        }

    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    AppDefaultResponse defaultData = response.body();
                    if (defaultData.getStatus().equals(Constants.TAG_TRUE)) {
                        AdminData.resetData();
                        AdminData.freeGems = defaultData.getFreeGems();
                        AdminData.giftList = defaultData.getGifts();
                        AdminData.giftsDetails = defaultData.getGiftsDetails();
                        AdminData.reportList = defaultData.getReports();
                        /*Add first item as Select all location filter*/
                        /*AdminData.locationList = new ArrayList<>();
                        AdminData.locationList.add(getString(R.string.select_all));
                        AdminData.locationList.addAll(defaultData.getLocations());*/
                        AdminData.membershipList = defaultData.getMembershipPackages();
                        AdminData.filterGems = defaultData.getFilterGems();
                        AdminData.filterOptions = defaultData.getFilterOptions();
                        AdminData.inviteCredits = defaultData.getInviteCredits();
                        AdminData.showAds = defaultData.getShowAds();
                        AdminData.showVideoAd = defaultData.getVideoAds();
//                        AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                        //AdminData.googleAdsId = getString(R.string.banner_ad_id);
                        AdminData.contactEmail = defaultData.getContactEmail();
                        AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                        AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                        AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                        AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                        AdminData.videoCallsGems = defaultData.getVideoCalls();
                        /*AdminData.streamDetails = defaultData.getStreamConnectionInfo();*/


                        AdminData.max_sound_duration = defaultData.getMaxSoundDuration();

                        /*SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                        SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                        SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                        SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());*/
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    private void setInitializingLay() {
        loadingLay.setVisibility(VISIBLE);
        if(streamData!=null){
            Glide.with(this)
                    .load(streamData.getStreamThumbnail())
                    .placeholder(R.drawable.profile_square)
                    .error(R.drawable.profile_square)
                    .into(loadingImage);

            Glide.with(this)
                    .load(streamData.getPublisherImage())
                    .error(R.drawable.profile_square)
                    .placeholder(R.drawable.profile_square)
                    .into(loadingUserImage);
            publisherName.setText(streamData.getPostedBy());
            txtLoadingTitle.setText(streamData.getTitle());
            loadingUserName.setText(streamData.getPostedBy());
        }
        setBuffering(GONE);
        initialIndicator.setIndicator(new BallBeatIndicator());
        initialIndicator.show();
        initializeLay.setVisibility(VISIBLE);
    }


    private void initSocket() {
        socketIO = new SocketIO(SubscribeActivity.this);
        mSocket = socketIO.getInstance();
        socketIO.setSocketEvents(SubscribeActivity.this);

        mSocket.on(Socket.EVENT_CONNECT, onConnect);

        mSocket.on(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
        mSocket.on(StreamConstants.TAG_STREAM_INFO, _streamInfo);
        mSocket.on(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
        mSocket.on(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        mSocket.on(Constants.TAG_GIFT_STATUS, _giftStatus);

        Log.e(TAG, "initSocket: :::::"+mSocket.connected() );
        mSocket.connect();

    }

    private void initView() {
        gestureScanner = new GestureDetector(this, this);
        Random random = new Random();
        int i1 = random.nextInt(StreamConstants.LIKE_COLOR.length);
        publisherColor = Color.argb(60, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        userLikeColor = StreamConstants.LIKE_COLOR[i1].trim();

/*        new HeightProvider(this).init().setHeightListener(new HeightProvider.HeightListener() {
            @Override
            public void onHeightChanged(int height) {
                if (height > 0) {
                    findViewById(R.id.bottomLay).setTranslationY(-height);
                } else {
                    findViewById(R.id.bottomLay).setTranslationY(0);
                }
            }
        });*/

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.viewersLay);
        params.height = (int) (displayHeight * 0.4);
        commentsLay.setLayoutParams(params);

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isAnimated) {
                    isAnimated = true;
                    slideOutAnim();
                } else if (charSequence.length() == 0) {
                    slideInAnim();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        View view = findViewById(R.id.parentLay);
        findViewById(R.id.parentLay).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = view.getRootView().getHeight() - view.getHeight();
                isKeyPadVisible = heightDiff > dpToPx(getApplicationContext(), 200);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvViewers.setLayoutManager(linearLayoutManager);
        viewerListAdapter = new ViewerListViewAdapter(this, viewersList);
        rvViewers.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvViewers.setAdapter(viewerListAdapter);
        rvViewers.setHasFixedSize(true);
        viewerListAdapter.notifyDataSetChanged();

        commentsAdapter = new CommentsAdapter(this, commentList);
        commentLayoutManager = new CustomLayoutManager(this);
        commentLayoutManager.setScrollEnabled(false);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setAdapter(commentsAdapter);
        commentsAdapter.notifyDataSetChanged();
        setContentLayTouchListener();

        getStreamDetails();
        initBottomDetailsDialog();
        initBottomViewersDialog();
        initBottomGiftDialog();
       // initBottomHashTagDialog();*/
    }

    private void initBottomViewersDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_viewers, null);
        bottomCommentsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomCommentsDialog.setContentView(bottomSheet);

        bottomViewersLay = bottomSheet.findViewById(R.id.bottomViewersLay);
        bottomViewerCount = bottomSheet.findViewById(R.id.bottomViewerCount);
        bottomRecyclerView = bottomSheet.findViewById(R.id.bottomRecyclerView);
        bottomTopLay = bottomSheet.findViewById(R.id.bottomTopLay);
        txtBottomDuration = bottomSheet.findViewById(R.id.txtBottomDuration);
        bottomDurationLay = bottomSheet.findViewById(R.id.bottomDurationLay);

        bottomDurationLay.setVisibility(GONE);

        bottomListAdapter = new BottomListAdapter(this, viewersList);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomRecyclerView.setAdapter(bottomListAdapter);
        bottomRecyclerView.setHasFixedSize(true);
        bottomListAdapter.notifyDataSetChanged();

        bottomCommentsDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet1 = dialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        bottomCommentsDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet12 = dialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet12).setHideable(true);
        });
    }

    private void initBottomDetailsDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_details, null);
        bottomDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomDialog.setContentView(bottomSheet);

        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomFirstLay = bottomSheet.findViewById(R.id.bottomFirstLay);
        bottomDeleteLay = bottomSheet.findViewById(R.id.bottomDeleteLay);
        bottomTxtLiveCount = bottomSheet.findViewById(R.id.txtLiveCount);
        bottomLiveTxtLay = bottomSheet.findViewById(R.id.liveTxtLay);
        bottomLiveCountLay = bottomSheet.findViewById(R.id.liveCountLay);
        bottomStreamTitle = bottomSheet.findViewById(R.id.bottomStreamTitle);
        bottomShareLay=bottomSheet.findViewById(R.id.shareLay);
        followLay = bottomSheet.findViewById(R.id.followLay);
        profileFollowIcon = bottomSheet.findViewById(R.id.profileFollowIcon);

        bottomUserLay = bottomSheet.findViewById(R.id.bottomUserLay);
        publisherImage = bottomSheet.findViewById(R.id.publisherImage);
        publisherColorImage = bottomSheet.findViewById(R.id.publisherColorImage);
        txtPublisherName = bottomSheet.findViewById(R.id.txtPublisherName);

        bottomDetailsLay = bottomSheet.findViewById(R.id.bottomDetailsLay);
        chatHideLay = bottomSheet.findViewById(R.id.chatHideLay);
        bottomReportLay = bottomSheet.findViewById(R.id.bottomReportLay);
        txtReport = bottomSheet.findViewById(R.id.txtReport);


        if (LocaleManager.isRTL()) {
            bottomLiveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg_rtl));
            bottomLiveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg_rtl));
        } else {
            bottomLiveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg));
            bottomLiveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg));
        }

        if(streamData!=null){
            bottomStreamTitle.setText(streamData.getTitle());
        }
        bottomReportLay.setVisibility(VISIBLE);
        bottomUserLay.setVisibility(VISIBLE);
        bottomDeleteLay.setVisibility(GONE);
        chatHideLay.setVisibility(VISIBLE);

//        bottomShareLay.setOnClickListener(v-> {
//            shareVideoDialog(streamData);
//        });

        bottomDetailsLay.setOnClickListener(view -> {
            bottomDialog.dismiss();
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                bottomCommentsDialog.show();
            }
        });


        bottomReportLay.setOnClickListener(view -> {
            if (bottomDialog != null && bottomDialog.isShowing()) {
                bottomDialog.dismiss();
            }
            if (streamInfo == null) {
                openReportDialog();
            } else if (streamInfo.getReported().equals(Constants.TAG_FALSE)) {
                openReportDialog();
            } else {
                sendReport("", false);
            }
        });


        chatHideLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomGiftsDialog != null && !bottomGiftsDialog.isShowing()) {
                    bottomGiftsDialog.show();
                }
            }
        });

        bottomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        followLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(streamData!=null){
                    if (streamData.getFollow().equals("true")){
                        type="Unfollow";
                    }else if (streamData.getFollow().equals("false")){
                        type="Follow";
                    }
                    followLay.startAnimation(AnimationUtils.loadAnimation(SubscribeActivity.this, R.anim.fade_out_fast));
                    followAPI(streamData.getPublisherId(), streamData.getPublisherImage(),type);
                    Log.e(TAG, "onClick: :::::::::::::::::"+new Gson().toJson(streamData) );
                }
            }
        });
    }


    private void initBottomGiftDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_gifts, null);
        bottomGiftsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomGiftsDialog.setContentView(bottomSheet);

        bottomGiftsLay = bottomSheet.findViewById(R.id.bottomGiftsLay);
        bottomGiftCount = bottomSheet.findViewById(R.id.bottomGiftCount);
        bottomGiftView = bottomSheet.findViewById(R.id.bottomGiftView);

        giftsAdapter = new GiftsAdapter(this, giftsList);
        bottomGiftView.setLayoutManager(new LinearLayoutManager(this));
        bottomGiftView.setAdapter(giftsAdapter);
        bottomGiftView.setHasFixedSize(true);
        giftsAdapter.notifyDataSetChanged();

        bottomGiftsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomGiftsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }


    private boolean appInstalledOrNot(String uri) {

        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

//    private void shareVideoDialog(StreamDetails streamData) {
//
//        String link=streamData.getInviteLink();
//
//        View shareView = getLayoutInflater().inflate(R.layout.share_video_dialog, null);
//        shareDialog = new BottomSheetDialog(SubscribeActivity.this, R.style.BottomSheetDialog); // Style here
//        shareDialog.setContentView(shareView);
//
//        shareDialog.setOnDismissListener(dialogInterface -> {
//
//        });
//
//        RelativeLayout copyLinkLay = shareView.findViewById(R.id.copyLinkLay);
//        RelativeLayout copyWhatsAppLay = shareView.findViewById(R.id.whatsappLay);
//        RelativeLayout copyFacebookLay = shareView.findViewById(R.id.facebookLay);
//        RelativeLayout copyMessagerLay = shareView.findViewById(R.id.messagerLay);
//        RelativeLayout copyInstaLay = shareView.findViewById(R.id.instaLay);
//        RelativeLayout copyTwitterLay = shareView.findViewById(R.id.twitterLay);
//        RelativeLayout copyMailLay = shareView.findViewById(R.id.gmailLay);
//        RelativeLayout copySMSLay = shareView.findViewById(R.id.smsLay);
//        RelativeLayout copySnapchatLay = shareView.findViewById(R.id.snapchatLay);
//        RelativeLayout copyInAppLay = shareView.findViewById(R.id.inAppLay);
//        shareDialog.show();
//
//
//        copyLinkLay.setOnClickListener(v -> {
//
//            ContentResolver contentResolver = getContentResolver();
//
//            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//            if (clipboardManager != null) {
//                ClipData data = ClipData.newUri(contentResolver, "label", Uri.parse(link));
//                clipboardManager.setPrimaryClip(data);
//                Toast.makeText(this, "link copied!", Toast.LENGTH_SHORT).show();
//                shareDialog.dismiss();
//            }
//
//        });
//
//
//        copyWhatsAppLay.setOnClickListener(view -> {
//            boolean installed = appInstalledOrNot("com.whatsapp");
//            if (installed) {
//
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.whatsapp");
//                startActivity(sendIntent);
//
//            } else {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
//                startActivity(intent);
//            }
//            shareDialog.dismiss();
//        });
//
//
//        copyFacebookLay.setOnClickListener(v -> {
//
//            boolean installed = appInstalledOrNot("com.facebook.katana");
//            if (installed) {
//
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.facebook.katana");
//                startActivity(sendIntent);
//
//            } else {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
//                startActivity(intent);
//            }
//            shareDialog.dismiss();
//        });
//
//        copyMessagerLay.setOnClickListener(v -> {
//            boolean installed = appInstalledOrNot("com.facebook.orca");
//            if (installed) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.facebook.orca");
//                startActivity(sendIntent);
//            } else {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.orca"));
//                startActivity(intent);
//            }
//            shareDialog.dismiss();
//        });
//        copyInstaLay.setOnClickListener(v -> {
//            boolean installed = appInstalledOrNot("com.instagram.android");
//            if (installed) {
//
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.instagram.android");
//                startActivity(sendIntent);
//            } else {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.instagram.android"));
//                startActivity(intent);
//            }
//            shareDialog.dismiss();
//        });
//
//        copyTwitterLay.setOnClickListener(v -> {
//            boolean installed = appInstalledOrNot("com.twitter.android");
//            if (installed) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.twitter.android");
//                startActivity(sendIntent);
//            } else {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.twitter.android"));
//                startActivity(intent);
//            }
//            shareDialog.dismiss();
//        });
//
//        copySnapchatLay.setOnClickListener(v -> {
//            boolean installed = appInstalledOrNot("com.snapchat.android");
//            if (installed) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
//                sendIntent.setType("text/plain");
//                sendIntent.setPackage("com.snapchat.android");
//                startActivity(sendIntent);
//            } else {
//                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.snapchat.android"));
//                startActivity(intent);
//            }
//            shareDialog.dismiss();
//        });
//
//        copyMailLay.setOnClickListener(v -> {
//            Intent email = new Intent(Intent.ACTION_SEND);
////                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{ });
////                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
//            email.putExtra(Intent.EXTRA_TEXT, link);
//
//            //need this to prompts email client only
//            email.setType("text/plain");
//
//            startActivity(Intent.createChooser(email, "Choose an Email client :"));
//            shareDialog.dismiss();
//        });
//    }

    private void slideDownAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                bottomLay.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomLay.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomLay.startAnimation(animate);
    }

    private void sendReport(String title, boolean report) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_NAME, streamName);
        map.put(Constants.TAG_TYPE, Constants.TAG_LIVE_STREAMING);
        if (report) {
            map.put(Constants.TAG_REPORT, title);
        }

        Call<Map<String, String>> call = apiInterface.reportStream(GetSet.getToken(), map);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> reportResponse = response.body();
                    if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        if (report) {
                            Toast.makeText(SubscribeActivity.this, getString(R.string.reported_successfully), Toast.LENGTH_SHORT).show();
                            if (streamInfo != null) {
                                streamInfo.setReported(Constants.TAG_TRUE);
                            }
                            txtReport.setText(getString(R.string.undo_report_broadcast));
                        } else {
                            Toast.makeText(SubscribeActivity.this, getString(R.string.undo_report_successfully), Toast.LENGTH_SHORT).show();
                            if (streamInfo != null) {
                                streamInfo.setReported(Constants.TAG_FALSE);
                            }
                            txtReport.setText(getString(R.string.report_broadcast));
                        }
                        if (bottomDialog != null && bottomDialog.isShowing()) {
                            bottomDialog.dismiss();
                        }
                    } else
                        Toast.makeText(SubscribeActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubscribeActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Toast.makeText(SubscribeActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openReportDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_report_addon, null);
        reportDialog = new BottomSheetDialog(SubscribeActivity.this, R.style.Bottom_StreamDialog); // Style here
        reportDialog.setContentView(bottomSheet);
        ((View) bottomSheet.getParent()).setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        reportsView = bottomSheet.findViewById(R.id.reportView);

        reportDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
        loadReports();
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(this, AdminData.reportList);
        reportsView.setLayoutManager(new LinearLayoutManager(this));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    public void getStreamDetails() {

        Call<StreamDetails> call3 = apiInterface.getStreamDetails(GetSet.getToken(), GetSet.getUserId(), streamName);
        call3.enqueue(new Callback<StreamDetails>() {
            @Override
            public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        streamData = response.body();
                        streamName = streamData.getName();
                        streamImage = streamData.getStreamThumbnail();
                        videoId = streamData.getStreamId();

                        Log.e("streamrl","-"+new Gson().toJson(streamData));

                        if(!TextUtils.isEmpty(streamData.getLink_url())){
                            videoLinkLay.setVisibility(VISIBLE);
                        }else{
                            videoLinkLay.setVisibility(GONE);
                        }

                        if (streamData.follow.equals("true")){
                            profileFollowIcon.setText("Following");
                            followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorDarkGrey));
                            followLay.clearAnimation();
                            streamData.setFollow("true");
                        }else{
                            profileFollowIcon.setText("Follow");
                            followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                            followLay.clearAnimation();
                            streamData.setFollow("false");
                        }
                        if (!streamData.getType().equals(Constants.TAG_LIVE)) {
                            /*handler.removeCallbacks(liveCheckRunnable);
                            unSubscribeStream();*/
                          //  finish();
                        } else {
                            publishTimer = new CountDownTimer(20000, 1000) {
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    if (!isStarted) {
                                        Toast.makeText(SubscribeActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }.start();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StreamDetails> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

  /*  public void getStreamInfo() {

        Call<StreamDetails> call3 = apiInterface.getStreamDetails(GetSet.getToken(), GetSet.getUserId(), streamName);
        call3.enqueue(new Callback<StreamDetails>() {
            @Override
            public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        if (!response.body().getType().equals(Constants.TAG_LIVE)) {
                            handler.removeCallbacks(liveCheckRunnable);
                            unSubscribeStream();
                            showToastAlert(getString(R.string.stream_end_description));
                        } else {
                            handler.postDelayed(liveCheckRunnable, 3000);                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StreamDetails> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });

    }*/

    public int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void slideInAnim() {
        isAnimated = false;
        TranslateAnimation animate = new TranslateAnimation(
                0,
                -50,
                0,
                0);
        animate.setDuration(200);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnSend.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnSend.startAnimation(animate);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        releasePlayer();
        shouldAutoPlay = true;
        clearResumePosition();
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (socketIO != null)
            socketIO.setSocketEvents(SubscribeActivity.this);
        if(player!=null && player.isPlaying()){
            player.setVolume(currentVolume);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        unSubscribeStream();
        if (socketIO != null)
            socketIO.setSocketEvents(null);
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unSubscribeStream();
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
            mSocket.off(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
            mSocket.off(StreamConstants.TAG_STREAM_INFO, _streamInfo);
            mSocket.off(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
            mSocket.off(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        }
        Constants.isInStream = false;
    }

    private void slideOutAnim() {
        btnSend.setVisibility(VISIBLE);
        if (LocaleManager.isRTL()) {
            btnSend.setRotation(180);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        } else {
            btnSend.setRotation(0);
            btnSend.setVisibility(VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initView();
            initSocket();
            initializePlayer(streamName);
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        playerView.showController();
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || playerView.dispatchMediaKeyEvent(event);
    }


    private void initializePlayer(String rtmpUrl) {

//        String URL = "rtmp://mediaserver.hitasoft.in/LiveApp/" + rtmpUrl;
        // String URL = "rtmp://137.184.76.48/LiveApp/" + rtmpUrl;
        String URL = "rtmp://mediaserver.hitasoft.in/LiveApp/" + rtmpUrl;
        // String URL =  pref.getString(StreamConstants.TAG_STREAM_BASE_URL, StreamConstants.RTMP_URL) + rtmpUrl;
       // String URL = "rtmp://mediaserver.hitasoft.in/LiveApp/" + rtmpUrl;
//        String URL = "rtmp://137.184.76.48/LiveApp/" + rtmpUrl;

        Log.d(TAG, "initializePlayer: rtmp=> " + URL);

        Intent intent = getIntent();

        boolean needNewPlayer = player == null;
        if (needNewPlayer) {

            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);


            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenderers()
                            ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(SubscribeActivity.this);
            renderersFactory.setExtensionRendererMode(extensionRendererMode);
            playerView.setControllerVisibilityListener(null);
            playerView.setControllerAutoShow(false);
            playerView.requestFocus();
            playerView.setKeepContentOnPlayerReset(true);
            final int loadControlStartBufferMs = 1500;


            /* Instantiate a DefaultLoadControl.Builder. */
            DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
            builder.setBufferDurationsMs(
                    DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    loadControlStartBufferMs,
                    DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);

            DefaultLoadControl loadControl = new DefaultLoadControl();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
            trackSelector = new DefaultTrackSelector(SubscribeActivity.this, videoTrackSelectionFactory);
            player = new SimpleExoPlayer.Builder(SubscribeActivity.this, renderersFactory)
                    .setBandwidthMeter(BANDWIDTH_METER)
                    .setLoadControl(loadControl)
                    .setTrackSelector(trackSelector)
                    .build();


            player.addListener(playerListener);
            playerView.setPlayer(player);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            player.setPlayWhenReady(shouldAutoPlay);
            /*player.setRepeatMode(Player.REPEAT_MODE_OFF);*/


/*            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    useExtensionRenderers()
                            ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                    null, extensionRendererMode);*/
            //   player = ExoPlayerFactory.newSimpleInstance(this, trackSelector,
            //           new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  500, 1500, 500, 1500),
            //           null, extensionRendererMode);
/*            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setMetadataOutput(eventLogger);

            playerView.setPlayer(player);
            player.setPlayWhenReady(shouldAutoPlay);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();*/
        }
        if (needNewPlayer || needRetrySource) {
            //  String action = intent.getAction();
            Uri[] uris;
            String[] extensions;

            uris = new Uri[1];
            uris[0] = Uri.parse(URL);
            extensions = new String[1];
            extensions[0] = "";
            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(USE_BANDWIDTH_METER);
        switch (type) {
            case C.TYPE_SS:
                SsMediaSource.Factory ssMediaSourceFactory = new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return ssMediaSourceFactory.createMediaSource(uri);
            case C.TYPE_DASH:
                DashMediaSource.Factory dashMediaFactory = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return dashMediaFactory.createMediaSource(uri);
            case C.TYPE_HLS:
                HlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(buildDataSourceFactory(USE_BANDWIDTH_METER));
                return new HlsMediaSource.Factory(hlsDataSourceFactory)
                        .setAllowChunklessPreparation(true).createMediaSource(uri);
            case C.TYPE_OTHER:
                if (uri.getScheme().equals("rtmp")) {

                    return new ProgressiveMediaSource.Factory(rtmpDataSourceFactory, new DefaultExtractorsFactoryForFLV())
                            .createMediaSource(uri);
                } else {
                    return new ProgressiveMediaSource.Factory(buildDataSourceFactory(USE_BANDWIDTH_METER))
                            .createMediaSource(uri);
                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private Emitter.Listener _msgReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "call: msgReceived: " + data);
                    String type = data.optString(Constants.TAG_TYPE);

                    switch (type) {
                        case StreamConstants.TAG_STREAM_JOINED:
                           // currentDuration = data.optString(StreamConstants.TAG_TIME);
                            if (data.optString(StreamConstants.TAG_STREAM_NAME).equals(streamName)) {
                                getStreamInfo(streamName);
                                HashMap<String, String> map = new HashMap<>();
                                map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                                map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                                map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                                map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                                map.put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE));
                                addComment(map);

                            }
                            break;
                        case Constants.TAG_TEXT: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
                            map.put(Constants.TAG_VISIBILITY, Constants.TAG_TRUE);
                            addComment(map);
                            break;
                        }
                        case StreamConstants.TAG_LIKED: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_TYPE, Constants.TAG_MESSAGE);
                            try {
                                String userId = data.optString(Constants.TAG_USER_ID);
                                String likeColor = data.optString(StreamConstants.TAG_LIKE_COLOR);
                                heartLay.addHeart(Color.parseColor(likeColor));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }

                        case StreamConstants.TAG_STREAM_END: {
                            Log.d(TAG, "TAG_STREAM_END: ");
                            isStopStreamCall = true;
                            try {
                                //handler.postDelayed(r, 2000);
                                handler.post(r);
                            } catch (Exception e) {

                            }
                        }

                        case Constants.TAG_GIFT: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(StreamConstants.TAG_STREAM_NAME, streamName);
                            map.put(Constants.TAG_GIFT_TITLE, data.optString(Constants.TAG_GIFT_TITLE));
                            map.put(Constants.TAG_GIFT_ICON, data.optString(Constants.TAG_GIFT_ICON));
                            map.put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE));
                            addComment(map);
                            lftVoteCount.setText(data.optString("lifetime_vote_count")!=null?
                                    data.optString("lifetime_vote_count"):"0");
                            getStreamInfo(streamName);
                            break;
                        }
                        case StreamConstants.TAG_DURATION:
                            currentDuration = data.optString(StreamConstants.TAG_TIME);
                            if (!isUserJoined) {
                                isUserJoined = true;
                                sendUserJoined();
                            }
                            txtTime.setText(currentDuration);
                            break;
                    }
                }
            });
        }
    };


    private void subscribeStream() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            if (mSocket.connected()) {
                Log.e(TAG, "subscribeStream params: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SUBSCRIBE_STREAM, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "subscribeStream: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showToastAlert(String message) {
        runOnUiThread(() -> Toast.makeText(SubscribeActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void unSubscribeStream() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket != null && mSocket.connected() && !isUnsubscribed) {
                isUnsubscribed = true;
                mSocket.emit(StreamConstants.TAG_UNSUBSCRIBE_STREAM, jsonObject);
                Log.d(TAG, "unSubscribeStream: " + jsonObject);
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
        finish();
    }

    private void setBuffering(int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visibility == VISIBLE) {
                    if (loadingLay.getVisibility() != VISIBLE) {
                        avBallIndicator.setVisibility(visibility);
                        contentLay.setOnTouchListener(null);
                        btnGift.setEnabled(false);
                        edtMessage.setEnabled(false);
                    }
                } else {
                    avBallIndicator.setVisibility(visibility);
                    setContentLayTouchListener();
                }
            }
        });
    }

    private void setContentLayTouchListener() {
        contentLay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureScanner.onTouchEvent(event);
            }
        });
        btnGift.setEnabled(true);
        edtMessage.setEnabled(true);
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            /*debugViewHelper.stop();
            debugViewHelper = null;*/
            shouldAutoPlay = player.getPlayWhenReady();
            updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            //trackSelectionHelper = null;

        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation

    /*@Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }*/

/*    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
    }*/

    /*@Override
    public void onPositionDiscontinuity() {
        if (needRetrySource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }*/

/*    @Override
    public void onPlayerError(ExoPlaybackException e) {
        //videoStartControlLayout.setVisibility(View.VISIBLE);
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof DecoderInitializationException) {
                // Special case for decoder initialization failures.
                DecoderInitializationException decoderInitializationException =
                        (DecoderInitializationException) cause;
                if (decoderInitializationException.diagnosticInfo == null) {
                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.diagnosticInfo);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString);
        }
        needRetrySource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            play(null);
        } else {
            updateResumePosition();
            showControls();
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_video);
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_audio);
            }
        }
    }*/

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }


    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

    public void play(View view) {
        /*    String URL = RTMP_BASE_URL + videoNameEditText.getText().toString();*/
        //String URL = "http://192.168.1.34:5080/vod/streams/test_adaptive.m3u8";

        String URL = "rtmp://media.hitasoft.in/LiveApp/";
        initializePlayer(streamName);
        //videoStartControlLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (isKeyPadVisible) {
            hideSoftKeyboard(SubscribeActivity.this);
        } else {
            sendLike();
        }
        return false;
    }

    private void sendLike() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, StreamConstants.TAG_LIKED);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(StreamConstants.TAG_LIKE_COLOR, userLikeColor);
            jsonObject.put(StreamConstants.TAG_TIME, currentDuration);
            heartLay.addHeart(Color.parseColor(userLikeColor));
            if (mSocket.connected()) {
                Log.i(TAG, "sendLike: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendChat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN && bottomLay.getVisibility() == VISIBLE) {
            sendLike();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        float sensitivity = 50;
        // Get swipe delta value in x axis.
        float deltaX = e1.getX() - e2.getX();

        // Get swipe delta value in y axis.
        float deltaY = e1.getY() - e2.getY();
        // Get absolute value.
        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);
        // Minimal x and y axis swipe distance.
        int MIN_SWIPE_DISTANCE_X = 100;
        int MIN_SWIPE_DISTANCE_Y = 100;

        // Maximal x and y axis swipe distance.
        int MAX_SWIPE_DISTANCE_X = 1000;
        int MAX_SWIPE_DISTANCE_Y = 1000;

        Log.i(TAG, "onFling: ");
        if ((deltaYAbs >= MIN_SWIPE_DISTANCE_Y) && (deltaYAbs <= MAX_SWIPE_DISTANCE_Y)) {
            if (deltaY > 0) {
                if (bottomLay.getVisibility() != VISIBLE)
                    slideUpAnim();
            } else {
                if (bottomLay.getVisibility() == VISIBLE)
                    slideDownAnim();
            }
        }

        return true;
    }

    private void slideUpAnim() {
        bottomLay.setVisibility(VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                bottomLay.getHeight(),
                0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        bottomLay.startAnimation(animate);
    }


/*
    @Override
    public void onClick(View v) {


    }
*/

    @Override
    public void onClick(View view) {
        /*if (view == retryButton) {
            play(null);
        }*/

        int id = view.getId();
        if (id == R.id.btnClose) {
            //closeConfirmDialog(getString(R.string.really_want_exit));
            unSubscribeStream();
            showToastAlert(getString(R.string.stream_end_description));
        }


        else if (id == R.id.btnSend) {

            if (TextUtils.isEmpty(edtMessage.getText())) {
                //  makeToast(getString(R.string.enter_comments));
            } else {
                sendChat();
            }
        } else if (id == R.id.btnDetail) {
            if (bottomDialog != null && !bottomDialog.isShowing())
                bottomDialog.show();
        } else if (id == R.id.viewLay) {
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing())
                bottomCommentsDialog.show();
        }/*else if (id==R.id.followLay){


            if (streamData.getFollow().equals("true")){
                type="Unfollow";
            }else if (streamData.getFollow().equals("false")){
                type="Follow";
            }

            followLay.startAnimation(AnimationUtils.loadAnimation(SubscribeActivity.this, R.anim.fade_out_fast));
            followAPI(streamData.getPublisherId(), streamData.getPublisherImage(),type);

            Log.e(TAG, "onClick: :::::::::::::::::"+new Gson().toJson(streamData) );
        }*/else if(id==R.id.linkLay){
            if (!TextUtils.isEmpty(streamData.getLink_url())){
                currentVolume = player.getVolume();
                player.setVolume(0F);
                Intent intent = new Intent(SubscribeActivity.this, LinkViewActivity.class);
                intent.putExtra("from","video");
                intent.putExtra("link_url",streamData.getLink_url().toString());
                startActivity(intent);
            }else{
                Toast.makeText(SubscribeActivity.this, "No video link found!", Toast.LENGTH_SHORT).show();
            }
        }else if(id == R.id.vote){
            openGiftDialog();
        }


    }

    private void followAPI(String publisherId, String publisherImage,String type) {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            followRequest.setType(type);

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Map<String, String> followResponse = response.body();

                    if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                        String userType = "";
                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                        }
                        streamData.setFollow("true");
//                        EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publisherImage, userType));

//                        Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));

                        Log.e(TAG, "onResponse: ::::::::::::::::Follow:::"+  new Gson().toJson(followResponse));
                        if (type.equals("Follow")){
                            Log.e(TAG, "onResponse: ::::::::::::::::Follow:::"+  new Gson().toJson(followResponse));
                            profileFollowIcon.setText("Following");
                            followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorDarkGrey));
                            followLay.clearAnimation();
                            streamData.setFollow("true");
                        }else{
                            profileFollowIcon.setText("Follow");
                            followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                            followLay.clearAnimation();
                            streamData.setFollow("false");
                        }

//                        Bundle payload = new Bundle();
//                        for (int i = 0; i < homeApiResponse.size(); i++) {
//                            if (homeApiResponse.get(i).getPublisherId().equals(publisherId)) {
//                                homeApiResponse.get(i).setFollowedUser(true);
//                                payload.putString("follow", "true");
//                                videoAdapter.notifyItemChanged(i, payload);
//                            }
//                        }
                    }
                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            Toast.makeText(this, "Oops! No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeConfirmDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SubscribeActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                unSubscribeStream();
                showToastAlert(getString(R.string.stream_end_description));
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    private void sendChat() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            jsonObject.put(Constants.TAG_MESSAGE, "" + edtMessage.getText());
            jsonObject.put(StreamConstants.TAG_TIME, currentDuration);
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            map.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
            map.put(StreamConstants.TAG_STREAM_NAME, streamName);
            map.put(Constants.TAG_MESSAGE, "" + edtMessage.getText());
            map.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
            addComment(map);
            edtMessage.getText().clear();
            if (mSocket.connected()) {
                Log.i(TAG, "sendChat: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "sendChat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public class ViewerListViewAdapter extends RecyclerView.Adapter<ViewerListViewAdapter.MyViewHolder> {

        ArrayList<StreamDetails.LiveViewers> viewersList;
        Context context;
        Random rnd;

        public ViewerListViewAdapter(Context context, ArrayList<StreamDetails.LiveViewers> viewersList) {
            this.viewersList = viewersList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_viewers, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final StreamDetails.LiveViewers viewer = viewersList.get(position);
            int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            Glide.with(context).load(viewer.getUserImage())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .error(R.drawable.ic_account)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.userImage);
            holder.colorImage.setBackgroundColor(color);

            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return viewersList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            ImageView colorImage;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                colorImage = view.findViewById(R.id.colorImage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

    }

    public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;
        ArrayList<HashMap<String, String>> commentList;
        Context context;

        public CommentsAdapter(Context context, ArrayList<HashMap<String, String>> commentList) {
            this.commentList = commentList;
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (commentList.get(position) != null) {
                String type = "" + commentList.get(position).get(Constants.TAG_TYPE);
                switch (type) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_TEXT;
                    case StreamConstants.TAG_STREAM_JOINED:
                        return VIEW_TYPE_JOIN;
                    case Constants.TAG_GIFT:
                        return VIEW_TYPE_GIFT;
                }
            }
            return VIEW_TYPE_TEXT;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_JOIN) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
                return new MyViewHolder(view);
            }else if (viewType == VIEW_TYPE_GIFT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_gifts, parent, false);
                return new GiftViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof MyViewHolder) {
                final MyViewHolder holder = (MyViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                holder.txtMessage.setText(map.get(Constants.TAG_MESSAGE));
                holder.txtUserName.setText("@" + map.get(Constants.TAG_USER_NAME));
                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        holder.itemLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(holder.itemLay, holder.getAdapterPosition());
                        holder.itemLay.setVisibility(GONE);

                    }
                }.start();

            } else if (viewHolder instanceof JoiningViewHolder) {
                final JoiningViewHolder joinViewHolder = (JoiningViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);

                joinViewHolder.txtJoined.setText("@" + map.get(Constants.TAG_USER_NAME) + " Joined");
                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        joinViewHolder.itemLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(joinViewHolder.itemLay, joinViewHolder.getAdapterPosition());
                        joinViewHolder.itemLay.setVisibility(GONE);

                    }
                }.start();
            }else if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                Glide.with(context)
                        .load(/*Constants.GIFT_IMAGE_URL +*/ map.get(Constants.TAG_GIFT_ICON))
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }

                String userName = "@" + map.get(Constants.TAG_USER_NAME) + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + map.get(Constants.TAG_GIFT_TITLE) + "\"";
                String gift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + gift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);

                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        holder.giftLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                               setFadeAnimation(holder.giftLay, holder.getAdapterPosition());
                                holder.giftLay.setVisibility(GONE);

                    }
                }.start();
            }

        }

        public class JoiningViewHolder extends RecyclerView.ViewHolder {
            TextView txtJoined;
            RelativeLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);
                txtJoined = view.findViewById(R.id.txtJoined);
                itemLay = view.findViewById(R.id.itemLay);

            }
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {
            TextView txtGiftName;
            ImageView giftImage;
            RelativeLayout giftLay;

            public GiftViewHolder(View view) {
                super(view);
                txtGiftName = view.findViewById(R.id.txtGiftName);
                giftImage = view.findViewById(R.id.giftImage);
                giftLay = view.findViewById(R.id.giftLay);

            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView txtUserName;
            TextView txtMessage;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);

                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);

            }
        }

    }

    private void setFadeAnimation(final View view, final int position) {
        /*sets animation from complete opaque state(1.0f) to complete transparent state(0.0f)*/
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(StreamConstants.FADE_DURATION);
        view.startAnimation(anim);
    }

    public class BottomListAdapter extends RecyclerView.Adapter<BottomListAdapter.MyViewHolder> {

        ArrayList<StreamDetails.LiveViewers> viewerList;
        Context context;
        Random rnd;

        public BottomListAdapter(Context context, ArrayList<StreamDetails.LiveViewers> viewerList) {
            this.viewerList = viewerList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_viewer, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final StreamDetails.LiveViewers viewer = viewerList.get(position);

            int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            Glide.with(context).load("" + viewer.getUserImage())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .error(R.drawable.no_video)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.userImage);
            holder.colorImage.setBackgroundColor(color);
            holder.txtUserName.setText(viewer.getUserName());
            holder.mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return viewerList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            RoundedImageView colorImage;
            TextView txtUserName;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                userImage = view.findViewById(R.id.userImage);
                colorImage = view.findViewById(R.id.colorImage);
                txtUserName = view.findViewById(R.id.txtUserName);
                mainLay = view.findViewById(R.id.mainLay);

            }
        }
    }

    public class ReportAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<Report> reportList;
        private RecyclerView.ViewHolder viewHolder;

        public ReportAdapter(Context context, List<Report> reportList) {
            this.context = context;
            this.reportList = reportList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.live_streaming_item_report, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {

            Log.d(TAG, "onBind: reportList" + reportList.get(position));

            /* ((MyViewHolder) holder).txtReport.setText(reportList.get(position));*/

            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtReport;
            LinearLayout layItemReport;

            public MyViewHolder(View view) {
                super(view);

                txtReport = view.findViewById(R.id.txtReport);
                layItemReport = view.findViewById(R.id.layItemReport);

                layItemReport.setOnClickListener(v -> {
                    reportDialog.dismiss();
                    if (bottomDialog != null && bottomDialog.isShowing()) {
                        bottomDialog.dismiss();
                    }
                    sendReport(reportList.get(getAdapterPosition()).getTitle(), true);
                });

            }




/*            @OnClick(R.id.itemReportLay)
            public void onViewClicked() {

                reportDialog.dismiss();
                if (bottomDialog != null && bottomDialog.isShowing()) {
                    bottomDialog.dismiss();
                }
                sendReport(reportList.get(getAdapterPosition()).getTitle(), true);
            }*/

        }


    }

    private void openGiftDialog() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_gift_home, null);
        giftDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Style here
        giftDialog.setContentView(sheetView);

        giftDialog.setOnDismissListener(dialogInterface -> {

        });

        viewPager = sheetView.findViewById(R.id.view);
        sendLay = sheetView.findViewById(R.id.sendLay);
        txtAttachmentName = sheetView.findViewById(R.id.txtAttachmentName);
        txtSend = sheetView.findViewById(R.id.txtSend);
        pagerIndicator = sheetView.findViewById(R.id.pagerIndicator);

        setViewPager();

        sendLay.setVisibility(View.GONE);
        giftDialog.show();

        txtSend.setOnClickListener(view -> {
            int position = (viewPager.getCurrentItem() * ITEM_LIMIT) + giftPosition;
            sendLay.setVisibility(GONE);
            txtAttachmentName.setText("");
            finalGiftPosition = position;
            sendGift(finalGiftPosition);

        });

        giftDialog.setOnShowListener(dialog -> {

        });


    }

    private void setViewPager() {

        int count;
        if (AdminData.giftList.size() <= ITEM_LIMIT) {
            count = 1;
        } else {
            count = AdminData.giftList.size() % ITEM_LIMIT == 0 ? AdminData.giftList.size() / ITEM_LIMIT : (AdminData.giftList.size() / ITEM_LIMIT) + 1;
        }

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this, count, Constants.TYPE_GIFTS);
        viewPager.setAdapter(pagerAdapter);
        pagerIndicator.setViewPager(viewPager);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadItems();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public class GiftsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
        Context context;
        Random rnd;

        public GiftsAdapter(Context context, ArrayList<StreamDetails.Gifts> giftsList) {
            this.giftsList = giftsList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gifts_received, parent, false);
            return new GiftViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                StreamDetails.Gifts gift = giftsList.get(position);
                Glide.with(context)
                        .load(/*Constants.IMAGE_URL + */gift.getGiftIcon())
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);

                Glide.with(context)
                        .load(/*Constants.IMAGE_URL +*/ gift.getUserImage())
                        .error(R.drawable.fundo_logo)
                        .placeholder(R.drawable.fundo_logo)
                        .into(holder.userImage);
                int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
                holder.colorImage.setBackgroundColor(color);
                String userName = "@" + gift.getUserName() + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + gift.getGiftTitle() + "\"";
                String strGift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + strGift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        }

        @Override
        public int getItemCount() {
            return giftsList.size();
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            RoundedImageView colorImage;
            FrameLayout profileLay;
            AppCompatTextView txtGiftName;
            ImageView giftImage;
            ConstraintLayout itemLay;

            public GiftViewHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                colorImage = view.findViewById(R.id.colorImage);
                profileLay = view.findViewById(R.id.profileLay);
                itemLay = view.findViewById(R.id.itemLay);
                giftImage = view.findViewById(R.id.giftImage);
                txtGiftName = view.findViewById(R.id.txtGiftName);

            }
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private final Context context;
        private int count = 0;
        private String type = "";

        public ViewPagerAdapter(Context context, int count, String type) {
            this.context = context;
            this.type = type;
            this.count = count;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_stickers, container, false);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            loadItems();
            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup view, int position, @NonNull Object object) {
            view.removeView((View) object);
        }
    }


    private void loadItems() {

        /*Set Array list by 8*/

        int start = viewPager.getCurrentItem() * ITEM_LIMIT;
        int end = start + (ITEM_LIMIT - 1);

        if (end > AdminData.giftList.size()) {
            end = AdminData.giftList.size() - 1;
        } else if (AdminData.giftList.size() <= end) {
            end = AdminData.giftList.size() - 1;
        }
        loadGifts(start, end);
    }

    private void loadGifts(int start, int end) {

        tempGiftList = new ArrayList<>();

        /*tempGiftList used only for display, For send AdminData.giftList used*/

        for (int i = start; i <= end; i++) {
            tempGiftList.add(AdminData.giftList.get(i));
        }
        giftAdapter = new GiftAdapter(this, tempGiftList);
        stickerLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();


    }

    public class GiftAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<Gift> giftList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public GiftAdapter(Context context, List<Gift> giftList) {
            this.context = context;
            this.giftList = giftList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_gifts, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final Gift gift = giftList.get(position);
                ((MyViewHolder) holder).progressBar.setVisibility(VISIBLE);
                ((MyViewHolder) holder).giftImage.setVisibility(View.INVISIBLE);
                Log.e("giftUrl","-"+Constants.GIFT_IMAGE_URL + gift.getGiftIcon());
                Glide.with(context)
                        .load(/*Constants.GIFT_IMAGE_URL +*/ gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((MyViewHolder) holder).progressBar.setVisibility(GONE);
                                ((MyViewHolder) holder).giftImage.setImageDrawable(context.getDrawable(R.drawable.gift));
                                ((MyViewHolder) holder).giftImage.setVisibility(VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                ((MyViewHolder) holder).progressBar.setVisibility(GONE);
                                ((MyViewHolder) holder).giftImage.setImageDrawable(resource);
                                ((MyViewHolder) holder).giftImage.setVisibility(VISIBLE);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.gift))
                        .into(((MyViewHolder) holder).giftImage);
                // ((MyViewHolder) holder).txtGiftPrice.setText(/*(GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" +*/ gift.getGiftGems());
                ((MyViewHolder) holder).txtGiftPrice.setText(String.valueOf(gift.getGiftGems()));

                ((MyViewHolder) holder).itemLay.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        giftPosition = position;
                        sendLay.setVisibility(View.VISIBLE);
                        txtSend.setVisibility(View.VISIBLE);
                        txtAttachmentName.setText(giftList.get(position).getGiftTitle());
                    }
                });

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = giftList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView giftImage;
            TextView txtGiftPrice;
            RelativeLayout itemLay;
            ProgressBar progressBar;

            public MyViewHolder(View view) {
                super(view);

                giftImage = view.findViewById(R.id.giftImage);
                txtGiftPrice = view.findViewById(R.id.txtGiftPrice);
                itemLay = view.findViewById(R.id.itemLay);
                progressBar = view.findViewById(R.id.progressBar);

                ViewGroup.LayoutParams params = itemLay.getLayoutParams();
                // Changes the height and width to the specified *pixels*
                params.width = displayWidth / 4;
                itemLay.setLayoutParams(params);
            }

        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                progressBar = parent.findViewById(R.id.progress_bar);
            }
        }
    }
}
