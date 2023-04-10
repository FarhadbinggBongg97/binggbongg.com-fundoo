package hitasoft.serviceteam.livestreamingaddon;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
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
import com.google.android.exoplayer2.ui.PlayerControlView;
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

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import hitasoft.serviceteam.livestreamingaddon.external.avloading.AVLoadingIndicatorView2;
import hitasoft.serviceteam.livestreamingaddon.external.heartlayout.HeartLayout;
import hitasoft.serviceteam.livestreamingaddon.external.helper.CustomLayoutManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.LocaleManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.NetworkReceiver;
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
import jp.wasabeef.glide.transformations.BlurTransformation;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayerActivity extends AppCompatActivity implements PlayerControlView.VisibilityListener, PlaybackPreparer {
    final private static String TAG = PlayerActivity.class.getSimpleName();
    LinearLayout followLay, videoLinkLay;

    String type = "Follow", parent = "";
    PlayerView playerView;
    AppCompatTextView txtTime;
    AppCompatTextView loadingTitle, videoid, lftVoteCount;
    AppCompatTextView loadingPublisherName;
    ImageView loadingImage;
    ImageView loadingPublisherColor;
    ImageView loadingPublisherImage, animGiftImage;
    RelativeLayout loadingLay;
    ImageView btnComments;
    ImageView btnDetail;
    MaterialTextView btnGift;
    ImageView btnClose;
    ImageView btnRefresh;
    CheckBox btnPlay;
    RelativeLayout playLay;
    AVLoadingIndicatorView2 avBallIndicator;
    RecyclerView rvComments;
    HeartLayout heartLay;
    AppCompatTextView txtLiveCount, txtBottomDuration;
    FrameLayout parentLay;
    RelativeLayout timeLay;
    RelativeLayout scrollToBottom;


    private RelativeLayout liveCountLay;
    private RelativeLayout liveStatusLay;
    private FrameLayout liveTxtLay;
    AppCompatTextView bottomStreamTitle;
    RoundedImageView publisherImage;
    RoundedImageView publisherColorImage;
    AppCompatTextView txtPublisherName;
    /*    LinearLayout btnFollow;
        RoundedImageView btnUnFollow;*/
    LinearLayout bottomFirstLay, bottomDeleteLay, bottomReportLay, chatHideLay, bottomDetailsLay,
            bottomUserLay, bottomViewersLay, bottomGiftDetailsLay, bottomInternalShareLay,
            bottomExternalShareLay, bottomGiftsLay, animGiftLay;
    AppCompatTextView bottomViewerCount, txtTotalLikes, txtReport, bottomGiftCount;
    RecyclerView bottomRecyclerView, bottomCommentsView, recyclerView, bottomGiftView;
    RelativeLayout bottomDurationLay, bottomLikesLay;
    TextView txtNoCommands;
    //    HorizontalScrollView hashTagsLay;
    private BottomSheetDialog bottomDialog, bottomViewersDialog, bottomCommentsDialog,
            giftDialog, bottomHashTagsDialog, bottomGiftsDialog;
    private Button btnMoreHashTag;
    //private HashtagView hashtagView, bottomHashTagView;
    private String streamName, streamUrl, publisherId, totalTime = "00:00", from = null;
    Handler handler = new Handler();
    ApiInterface apiInterface;
    private SimpleExoPlayer player;
    private boolean shouldAutoPlay = true, durationSet = false;
    public static boolean isStreamDeleted = false;
    private CommentsAdapter commentsAdapter;
    private GiftsAdapter giftsAdapter;
    ArrayList<StreamDetails.Comments> commentList = new ArrayList<>();
    List<StreamDetails.Comments> tempCommentList = new ArrayList<>();
    ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
    private LiveCommentsAdapter liveCommentsAdapter;
    ArrayList<HashMap<String, String>> liveCommentList = new ArrayList<HashMap<String, String>>();
    ArrayList<String> timeList = new ArrayList<>();
    private BottomSheetDialog reportDialog;
    private RecyclerView reportsView;
    private ReportAdapter reportAdapter;
    // AppUtils appUtils;
    private static DefaultBandwidthMeter BANDWIDTH_METER;
    private static String USER_AGENT;
    private boolean USE_BANDWIDTH_METER = false, needRetrySource;
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private PlayerActivity context;
    private ViewPager viewPager;
    private LinearLayout sendLay;
    private TextView txtAttachmentName, txtSend;
    private int giftPosition = 0;
    CircleIndicator pagerIndicator;
    private int finalGiftPosition;
    private int ITEM_LIMIT = 8;
    int displayHeight, displayWidth;

    Utils appUtils;

    MaterialTextView profileFollowIcon;

    public List<String> reportList = new ArrayList<String>();
    List<Gift> tempGiftList = new ArrayList<>();
    GiftAdapter giftAdapter;
    private LinearLayoutManager stickerLayoutManager;
    Handler hideViewhandler = new Handler();

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private Runnable hideViewUpdate = new Runnable() {
        @Override
        public void run() {
            Log.e("checkWorking", "-gpne");
//            rvComments.setVisibility(INVISIBLE);
            // scrollToBottom.setVisibility(GONE);
        }
    };

    private Runnable timerUpdate = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                long currentMilliSeconds = player.getCurrentPosition();
                String duration, commentDuration;
                duration = appUtils.getFormattedDuration(currentMilliSeconds);
                commentDuration = appUtils.getCommentsDuration(currentMilliSeconds);
                if (tempCommentList.size() > 0) {
                    if (timeList.contains(commentDuration.trim())) {
                        int firstIndex = timeList.indexOf(commentDuration.trim());
                        int lastIndex = timeList.lastIndexOf(commentDuration.trim());
                        List<StreamDetails.Comments> subList;
                        if (firstIndex == lastIndex) {
                            subList = Collections.singletonList(tempCommentList.get(firstIndex));
                        } else {
                            subList = tempCommentList.subList(firstIndex, lastIndex);
                        }
                        for (StreamDetails.Comments comments : subList) {
                            if (comments.getTime() != null && comments.getTime().equals(commentDuration)) {
                                setCommentUI(comments);
                            }
                        }
                    }
                }
                txtTime.setText(duration + " / " + totalTime);
                handler.postDelayed(timerUpdate, 1000);
            }
        }
    };

    private StreamDetails streamDetails;
    private StreamDetails apiStreamData;
    private DefaultTrackSelector trackSelector;
    DefaultLoadControl loadControl;
    private long resumePosition;
    private boolean isPlayerInitialized = false;
    private int resumeWindow;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 10;
    private RelativeLayout commentsLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        appUtils = new Utils(this);
        videoLinkLay = findViewById(R.id.linkLay);


        playerView = findViewById(R.id.playerView);
        txtTime = findViewById(R.id.txtTime);
        loadingTitle = findViewById(R.id.loadingTitle);
        loadingPublisherName = findViewById(R.id.loadingPublisherName);
        loadingImage = findViewById(R.id.loadingImage);
        loadingPublisherColor = findViewById(R.id.loadingPublisherColor);
        loadingPublisherImage = findViewById(R.id.loadingPublisherImage);
        loadingLay = findViewById(R.id.loadingLay);
        btnComments = findViewById(R.id.btnComments);
        btnDetail = findViewById(R.id.btnDetail);
        btnClose = findViewById(R.id.btnClose);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnPlay = findViewById(R.id.btnPlay);
        playLay = findViewById(R.id.playLay);
        avBallIndicator = findViewById(R.id.avBallIndicator);
        rvComments = findViewById(R.id.rv_comments);
        ;
        heartLay = findViewById(R.id.heartLay);
        txtLiveCount = findViewById(R.id.txtLiveCount);
        txtBottomDuration = findViewById(R.id.txtBottomDuration);
        parentLay = findViewById(R.id.parentLay);
        timeLay = findViewById(R.id.timeLay);
        btnGift = findViewById(R.id.btnVote);
        commentsLay = (RelativeLayout) findViewById(R.id.commentsLay);
        animGiftImage = findViewById(R.id.img_animGift);
        animGiftLay = findViewById(R.id.lay_animGift);
        videoid = findViewById(R.id.tv_videoId);
        lftVoteCount = findViewById(R.id.tv_lftCount);

        reportList.add(0, "Abuse");
        reportList.add("In-Appropriate");
        reportList.add("Adult Content");


        context = PlayerActivity.this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        //appUtils = new AppUtils(this);
        USER_AGENT = Util.getUserAgent(this, getString(R.string.app_name));
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(this).build();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.viewersLay);
        params.height = (int) (displayHeight * 0.4);
        commentsLay.setLayoutParams(params);


        getFromIntent();
        //checkUserIsActive();
        initView();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGiftDialog();
            }
        });

        videoLinkLay.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(apiStreamData.getLink_url())) {
                pausePlayer();
                Intent intent = new Intent(context, LinkViewActivity.class);
                intent.putExtra("from", "video");
                intent.putExtra("link_url", apiStreamData.getLink_url().toString());
                startActivity(intent);
            } else {
                Toast.makeText(context, "No video link found!", Toast.LENGTH_SHORT).show();
            }
        });

//        rvComments.setVisibility(INVISIBLE);
        //scrollToBottom.setVisibility(GONE);

       /* parentLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvComments.setVisibility(VISIBLE);
                if(player!=null){
                    if(player.getPlaybackState()==Player.STATE_ENDED)
                        rvComments.setVisibility(INVISIBLE);
                }
                LinearLayoutManager lManager = (LinearLayoutManager) rvComments.getLayoutManager();
                if(lManager==null){}
                else if(lManager.findLastVisibleItemPosition()==lManager.getItemCount()-1){
                    scrollToBottom.setVisibility(GONE);
                }else scrollToBottom.setVisibility(VISIBLE);

                hideViewhandler.removeCallbacks(hideViewUpdate);
                hideViewUpdate = new Runnable() {
                    @Override
                    public void run() {
                        rvComments.setVisibility(INVISIBLE);
                      //  scrollToBottom.setVisibility(GONE);
                    }
                };
                hideViewhandler.postDelayed(hideViewUpdate,20000);
            }
        });*/

    }

    private void followAPI(String publisherId, String publisherImage, String type) {

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

//                        EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publisherImage, userType));

//                        Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));

                        if (type.equals("Follow")) {
                            Log.e(TAG, "onResponse: ::::::::::::::::Follow:::" + new Gson().toJson(followResponse));
                            profileFollowIcon.setText("Following");
                            followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorDarkGrey));
                            followLay.clearAnimation();
                            apiStreamData.setFollow("true");
                        } else {
                            profileFollowIcon.setText("Follow");
                            followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                            followLay.clearAnimation();
                            apiStreamData.setFollow("false");
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
            Toast.makeText(this, " Oops! No internet connection", Toast.LENGTH_SHORT).show();
        }
    }


    private void getFromIntent() {
        Intent intent = getIntent();
        from = intent.getStringExtra(Constants.TAG_FROM);
        streamDetails = (StreamDetails) intent.getSerializableExtra(StreamConstants.TAG_STREAM_DATA);
        streamName = streamDetails.getName();
        streamUrl = streamDetails.getPlayBackUrl();
        //streamUrl = "http://media.hitasoft.in/LiveApp/streams/" + streamName + ".mp4";
        publisherId = streamDetails.getPublisherId();
//        Log.e(TAG, "onCreate: :::::::::::::::::::::::::"+intent.getStringExtra("parent"));
        if (intent.getStringExtra("parent") != null) {
            parent = intent.getStringExtra("parent");
            if (parent.equals("userVideo")) {
                btnGift.setVisibility(View.GONE);
            }
        }
/*        if (!NetworkReceiver.isConnected()) {
            App.makeToast(getString(R.string.no_internet_connection));
        }*/
    }

    private void initView() {

        Log.d(TAG, "streamDetails=> " + new Gson().toJson(streamDetails));

        initBottomDetailsDialog();
        initBottomViewersDialog();
        initBottomCommentsDialog();
        initBottomGiftDialog();
//        initBottomHashTagDialog();
        avBallIndicator.setVisibility(VISIBLE);
        loadingTitle.setVisibility(INVISIBLE);
        loadingTitle.setText(streamDetails.getPostedBy());
        loadingPublisherName.setText(streamDetails.postedBy);

        liveCommentsAdapter = new LiveCommentsAdapter(this, liveCommentList);
        CustomLayoutManager commentLayoutManager = new CustomLayoutManager(this);
        LinearLayoutManager linearManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        commentLayoutManager.setScrollEnabled(false);
        //rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setAdapter(liveCommentsAdapter);
        liveCommentsAdapter.notifyDataSetChanged();

        Log.i(TAG, "initView: " + streamDetails.getPostedBy());
        if (streamDetails.getPublisherName() != null) {
            txtPublisherName.setText(streamDetails.getPublisherName());
        } else if (streamDetails.getPostedBy() != null) {
            txtPublisherName.setText(streamDetails.getPostedBy());
        } else {
            txtPublisherName.setText("");
        }


        /*rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = rvComments.getChildCount();
                totalItemCount = linearManager.getItemCount();
                firstVisibleItem = linearManager.findFirstVisibleItemPosition();
                int lastVisibleItemPos =  linearManager.findLastVisibleItemPosition();
                Log.e("checkStroll","-"+lastVisibleItemPos+" ");
                if(linearManager.findLastVisibleItemPosition()==linearManager.getItemCount()-1){
                    scrollToBottom.setVisibility(GONE);
                }else scrollToBottom.setVisibility(VISIBLE);

            }
        });
*/

        Glide.with(this)
                .load(streamDetails.getStreamThumbnail())
                .apply(new RequestOptions().error(R.drawable.rounded_square_semi_transparent)
                        .dontAnimate()
                        .transform(new BlurTransformation(25, 3)))
                .placeholder(R.drawable.profile_square)
                .into(loadingImage);

        Glide.with(this)
                .load(streamDetails.getPublisherImage())
                .placeholder(R.drawable.profile_square)
                .into(loadingPublisherImage);

        Random rnd = new Random();
        int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        loadingPublisherColor.setBackgroundColor(color);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        btnPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (needRetrySource) {
                        setUpExoPlayer(streamUrl);
                    } else {
                        startPlayer();
                    }
                } else {
                    pausePlayer();
                }
            }
        });
        if (streamUrl != null) {
            setUpExoPlayer(streamUrl);
        }
        getAppDefaultData();
        getStreamDetails(streamName);
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


    private void setUpExoPlayer(String streamUrl) {
        /*HttpProxyCacheServer proxy = App.getProxy(this);
        proxy.registerCacheListener(this, streamUrl);
        String proxyUrl = proxy.getProxyUrl(streamUrl);*/
        Uri uri = Uri.parse("" + streamUrl);
        boolean needNewPlayer = player == null;
        if (needNewPlayer) {
            isPlayerInitialized = true;
            playerView.setControllerVisibilityListener(null);
            playerView.setControllerAutoShow(false);
            playerView.requestFocus();
            playerView.setKeepContentOnPlayerReset(true);
            if (uri.toString().startsWith("file://")) {
                player = new SimpleExoPlayer.Builder(context).build();
            } else {
                final int loadControlStartBufferMs = 1500;
                /* Instantiate a DefaultLoadControl.Builder. */
                DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
                builder.setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        loadControlStartBufferMs,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
                /* Build the actual DefaultLoadControl instance */
                loadControl = builder.createDefaultLoadControl();
                TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
                trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);
                player = new SimpleExoPlayer.Builder(context)
                        .setBandwidthMeter(BANDWIDTH_METER)
                        .setLoadControl(loadControl)
                        .setTrackSelector(trackSelector)
                        .build();
            }
            /* Milliseconds of media data buffered before playback starts or resumes. */
            player.addListener(playerListener);
            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(this);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);


        }
        if (needNewPlayer || needRetrySource) {
            // Prepare the player with the source.
            MediaSource mediaSource = buildMediaSource(uri, getMimeType("" + uri));
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }
            player.setPlayWhenReady(shouldAutoPlay);
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
        }
    }

    /*private boolean checkCachedState(String streamUrl) {
        HttpProxyCacheServer proxy = App.getProxy(this);
        return proxy.isCached(streamUrl);
    }*/

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(USE_BANDWIDTH_METER);
        Log.i(TAG, "buildMediaSource: " + type);
        switch (type) {
            case C.TYPE_SS: {
                Log.d(TAG, "buildMediaSource: smoth streeming");
                SsMediaSource.Factory ssMediaSourceFactory = new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return ssMediaSourceFactory.createMediaSource(uri);
            }
            case C.TYPE_DASH: {
                Log.d(TAG, "buildMediaSource: dash");
                DashMediaSource.Factory dashMediaFactory = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false));
                return dashMediaFactory.createMediaSource(uri);
            }
            case C.TYPE_HLS: {
                Log.d(TAG, "buildMediaSource: hls");
                HlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(buildDataSourceFactory(USE_BANDWIDTH_METER));
                return new HlsMediaSource.Factory(hlsDataSourceFactory)
                        .setAllowChunklessPreparation(true).createMediaSource(uri);
            }
            case C.TYPE_OTHER: {
                Log.d(TAG, "buildMediaSource: others");
                if (!uri.getScheme().equals("rtmp")) {
                    if (uri.toString().startsWith("file://")) {
                        DefaultExtractorsFactory extractorsFactory =
                                new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true);
                        DataSource.Factory dataSourceFactory =
                                new DefaultDataSourceFactory(this, USER_AGENT);
                        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory);
                        return factory.createMediaSource(uri);
                    } else {
                        return new ProgressiveMediaSource.Factory(buildDataSourceFactory(USE_BANDWIDTH_METER))
                                .createMediaSource(uri);
                    }
                }
            }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /*@Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }*/

    private Player.EventListener playerListener = new Player.EventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, "onPlayerStateChanged: " + playbackState);
            Log.i(TAG, "onPlayerStateChanged: " + playWhenReady);
            if (playbackState == Player.STATE_ENDED) {
                btnRefresh.setVisibility(VISIBLE);
                btnPlay.setVisibility(GONE);
                hideViewhandler.removeCallbacks(hideViewUpdate);
                handler.removeCallbacks(timerUpdate);
                durationSet = false;
                liveCommentList.clear();
                liveCommentsAdapter.notifyDataSetChanged();
//                rvComments.setVisibility(INVISIBLE);
            } else if (playWhenReady && playbackState == Player.STATE_READY) {
                timeLay.setVisibility(VISIBLE);
//                btnGift.setVisibility(VISIBLE);
                btnPlay.setChecked(true);
                handler.post(timerUpdate);
                if (!durationSet) {
                    if (from != null && !from.equals(Constants.TAG_HISTORY)) {
                        updateWatchCount();
                    }
                    durationSet = true;
                    long duration = player.getDuration();
                    if (TimeUnit.MILLISECONDS.toHours(player.getDuration()) != 0)
                        totalTime = String.format(Locale.ENGLISH, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
                    else
                        totalTime = String.format(Locale.ENGLISH, "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
                    txtBottomDuration.setText(totalTime);
                }
                loadingLay.setVisibility(GONE);
                avBallIndicator.setVisibility(GONE);
            } else if (playbackState == Player.STATE_BUFFERING) {
                avBallIndicator.setVisibility(VISIBLE);
                handler.removeCallbacks(timerUpdate);
                hideViewhandler.removeCallbacks(hideViewUpdate);
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Log.i(TAG, "onIsPlayingChanged: " + isPlaying);
            if (!isPlaying) {
                handler.removeCallbacks(timerUpdate);
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.i(TAG, "onPositionDiscontinuity: " + reason);
            if (needRetrySource) {
                // This will only occur if the user has performed a seek whilst in the error state. Update the
                // resume position so that if the user then retries, playback will resume from the position to
                // which they seeked.
                updateResumePosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            try {
                String errorString = null;
                Log.i(TAG, "onPlayerError: " + e.type);
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
                } else if (e.type == ExoPlaybackException.TYPE_SOURCE) {
                    if (!NetworkReceiver.isConnected()) {
                        //App.makeToast(getString(R.string.no_internet_connection));
                    } else {
                        //App.makeToast(getString(R.string.something_went_wrong));
                    }
                    finish();
                } else {
                    Log.e(TAG, "onPlayerError: " + e.getUnexpectedException().getMessage());
                }
                if (errorString != null) {
                    //App.makeToast(errorString);
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
                Log.e(TAG, "onPlayerError: " + exception.getMessage());
            }
        }
    };

    private void updateWatchCount() {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
        requestMap.put("stream_id", streamDetails.getStreamId());
        Log.e("checkUpdate", "-" + requestMap);
        Call<HashMap<String, String>> call = apiInterface.updateWatchCount(requestMap);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();
                Log.e("responseUpdate", "-" + response.body());
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }


    public void play(View view) {
        setUpExoPlayer(streamUrl);
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

    private void setCommentUI(StreamDetails.Comments comments) {
//        Logging.d(TAG, "setCommentUI: " + new Gson().toJson(comments));
        String type = comments.getType();
        switch (type) {
            case StreamConstants.TAG_STREAM_JOINED: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(Constants.TAG_MESSAGE, comments.getMessage());
                map.put(Constants.TAG_TYPE, comments.getType());
                addComment(map);
            }
            break;
            case Constants.TAG_TEXT: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(Constants.TAG_MESSAGE, comments.getMessage());
                map.put(Constants.TAG_TYPE, comments.getType());
                map.put(Constants.TAG_VISIBILITY, Constants.TAG_TRUE);
                addComment(map);
            }
            break;
            case StreamConstants.TAG_LIKED: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(Constants.TAG_MESSAGE, comments.getMessage());
                map.put(Constants.TAG_TYPE, Constants.TAG_MESSAGE);
                try {
                    String userId = comments.getUserId();
                    String likeColor = comments.getLikeColor();
                    Log.i(TAG, "setCommentUI: " + likeColor);
                    heartLay.addHeart(Color.parseColor(likeColor));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case Constants.TAG_GIFT: {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, comments.getUserId());
                map.put(Constants.TAG_USER_NAME, comments.getUserName());
                map.put(Constants.TAG_USER_IMAGE, comments.getUserImage());
                map.put(StreamConstants.TAG_STREAM_NAME, streamName);
                map.put(Constants.TAG_GIFT_TITLE, comments.getGiftTitle());
                map.put(Constants.TAG_GIFT_ICON, comments.getGiftIcon());
                map.put(Constants.TAG_TYPE, comments.getType());
                addComment(map);
            }
            break;
            default:
                break;
        }
    }

    private void addComment(HashMap<String, String> map) {
        liveCommentList.add(map);
        liveCommentsAdapter.notifyItemInserted(liveCommentList.size() - 1);
        Log.e(TAG, "addComment: :::::::::::::::::" + new Gson().toJson(map));
        rvComments.scrollToPosition(liveCommentList.size() - 1);
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

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, null, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(USER_AGENT, bandwidthMeter);
    }


    /*private void checkUserIsActive() {
        Call<HashMap<String, String>> call = apiInterface.isActive(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();
                if (response.isSuccessful()) {
                    if (Response.get(Constants.TAG_STATUS).equals(Constants.TAG_FALSE)) {
                        App.makeToast(getString(R.string.account_deactivated_by_admin));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(PlayerActivity.this).clearDB();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }*/

/*    private void updateWatchCount() {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
        requestMap.put(StreamConstants.TAG_STREAM_ID, streamDetails.getStreamId());

        Call<HashMap<String, String>> call = apiInterface.updateWatchCount(requestMap);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }*/

    private void initBottomDetailsDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_details, null);
        bottomDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomDialog.setContentView(bottomSheet);

        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomFirstLay = bottomSheet.findViewById(R.id.bottomFirstLay);
        bottomDeleteLay = bottomSheet.findViewById(R.id.bottomDeleteLay);
        liveTxtLay = bottomSheet.findViewById(R.id.liveTxtLay);
        txtLiveCount = bottomSheet.findViewById(R.id.txtLiveCount);
        bottomStreamTitle = bottomSheet.findViewById(R.id.bottomStreamTitle);
        /*hashTagsLay = bottomSheet.findViewById(R.id.hashTagsLay);
        hashtagView = bottomSheet.findViewById(R.id.hashtagView);
        btnMoreHashTag = bottomSheet.findViewById(R.id.btnMoreHashTag);*/
        bottomUserLay = bottomSheet.findViewById(R.id.bottomUserLay);
        publisherImage = bottomSheet.findViewById(R.id.publisherImage);
        txtPublisherName = bottomSheet.findViewById(R.id.txtPublisherName);
/*        btnFollow = bottomSheet.findViewById(R.id.btnFollow);
        btnUnFollow = bottomSheet.findViewById(R.id.btnUnFollow);*/
        bottomDetailsLay = bottomSheet.findViewById(R.id.bottomDetailsLay);
        chatHideLay = bottomSheet.findViewById(R.id.chatHideLay);
        bottomReportLay = bottomSheet.findViewById(R.id.bottomReportLay);
        txtReport = bottomSheet.findViewById(R.id.txtReport);
        liveStatusLay = bottomSheet.findViewById(R.id.liveStatusLay);
        liveCountLay = bottomSheet.findViewById(R.id.liveCountLay);
        followLay = bottomSheet.findViewById(R.id.followLay);
        profileFollowIcon = bottomSheet.findViewById(R.id.profileFollowIcon);
/*
        bottomGiftDetailsLay = bottomSheet.findViewById(R.id.bottomGiftDetailsLay);
        bottomInternalShareLay = bottomSheet.findViewById(R.id.bottomInternalShareLay);
        bottomExternalShareLay = bottomSheet.findViewById(R.id.bottomExternalShareLay);
*/
        bottomStreamTitle.setText(streamDetails.getTitle() != null ? streamDetails.getTitle() : "");
        bottomUserLay.setVisibility(VISIBLE);
        liveStatusLay.setVisibility(GONE);
        chatHideLay.setVisibility(VISIBLE);
        //btnFollow.setVisibility(GONE);

        if (LocaleManager.isRTL()) {
            liveTxtLay.setBackground(context.getResources().getDrawable(R.drawable.live_status_bg_rtl));
            liveCountLay.setBackground(context.getResources().getDrawable(R.drawable.live_count_bg_rtl));
        } else {
            liveTxtLay.setBackground(context.getResources().getDrawable(R.drawable.live_status_bg));
            liveCountLay.setBackground(context.getResources().getDrawable(R.drawable.live_count_bg));
        }

        if (("" + streamDetails.getPublisherId()).equals(GetSet.getUserId())) {
            bottomDeleteLay.setVisibility(VISIBLE);
            //  chatHideLay.setVisibility(VISIBLE);
            bottomReportLay.setVisibility(GONE);
        } else {
            bottomReportLay.setVisibility(VISIBLE);
            bottomDeleteLay.setVisibility(GONE);
            //   chatHideLay.setVisibility(GONE);
        }

        if (parent.equals("userVideo")) {
            followLay.setVisibility(View.GONE);
        } else {
            followLay.setVisibility(VISIBLE);
        }

        /*appUtils.setHashTagItemPadding(hashtagView);
        hashtagView.setEllipsize(HashtagView.ELLIPSIZE_END);
        hashtagView.setRowCount(1);*/
        /*btnMoreHashTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomHashTagsDialog != null && !bottomHashTagsDialog.isShowing()) {
                    bottomHashTagsDialog.show();
                }
            }
        });*/

        bottomDetailsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomViewersDialog != null && !bottomViewersDialog.isShowing()) {
                    bottomViewersDialog.show();
                }
            }
        });

/*        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    String blockStatus = DBHelper.getInstance(PlayerActivity.this).getBlockStatus(GetSet.getUserId() + publisherId);
                    if (blockStatus.equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.unblock_follow_description));
                    } else {
                        followUnfollowUser(publisherId, true);
                    }
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });

        btnUnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    followUnfollowUser(publisherId, false);
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
            }
        });*/

        bottomDeleteLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeleteDialog(getString(R.string.really_delete_video));
            }
        });

        bottomReportLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomDialog != null && bottomDialog.isShowing()) {
                    bottomDialog.dismiss();
                }
                if (streamDetails == null) {
                    openReportDialog();
                } else if (streamDetails.getReported().equals(Constants.TAG_FALSE)) {
                    openReportDialog();
                } else {
                    sendReport("", false);
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

        btnDetail.setOnClickListener(v -> {
            if (bottomDialog != null && !bottomDialog.isShowing())
                bottomDialog.show();
        });

        playLay.setOnClickListener(v -> {
            btnPlay.setChecked(!btnPlay.isChecked());
        });

        btnRefresh.setOnClickListener(v -> {
            btnRefresh.setVisibility(GONE);
            btnPlay.setVisibility(VISIBLE);
            if (player != null) {
                player.seekTo(0);
            }
            btnPlay.setChecked(true);
        });

        btnComments.setOnClickListener(v -> {
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                bottomCommentsDialog.show();
            }

        });

        followLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (apiStreamData.getFollow().equals("true")) {
                    type = "Unfollow";
                } else if (apiStreamData.getFollow().equals("false")) {
                    type = "Follow";
                }

                followLay.startAnimation(AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.fade_out_fast));
                followAPI(apiStreamData.getPublisherId(), apiStreamData.getPublisherImage(), type);
            }
        });


        /* R.id.btnDetail, R.id.playLay, R.id.btnRefresh, R.id.btnComments*/


        chatHideLay.setOnClickListener(view -> {
            bottomDialog.dismiss();
            if (bottomGiftsDialog != null && !bottomGiftsDialog.isShowing()) {
                bottomGiftsDialog.show();
            }
        });

        /*bottomInternalShareLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*if (apiStreamData != null) {

                    isPlayerInitialized = false;
                    btnPlay.setChecked(false);
                    pausePlayer();
                    Intent intent = new Intent(PlayerActivity.this, MutualListActivity.class);
                    intent.putExtra(Constants.TAG_TEXT, apiStreamData.getInviteLink());
                    intent.putExtra(StreamConstants.TAG_STREAM_NAME, streamName);
                    intent.putExtra(Constants.TAG_TITLE, apiStreamData.getTitle());
                    intent.putExtra(Constants.TAG_USER_NAME, apiStreamData.getPublisherName());
                    intent.putExtra(Constants.TAG_USER_IMAGE, apiStreamData.getPublisherImage());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_SHARE);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(intent, Constants.MUTUAL_REQUEST_CODE);
                } else {
                    if (!NetworkReceiver.isConnected())
                        App.makeToast(getString(R.string.no_internet_connection));
                    else App.makeToast(getString(R.string.something_went_wrong));
                }*//*
            }
        });

        bottomExternalShareLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apiStreamData != null) {

//                String msg = GetSet.getName() + " " + getString(R.string.stream_share_description) + " " + apiStreamData.getInviteLink();
*//*                    String msg = apiStreamData.getInviteLink();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.join) + " " + getString(R.string.app_name));
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    startActivity(Intent.createChooser(intent, getString(R.string.share_link)));*//*
                } else {
                    *//*if (!NetworkReceiver.isConnected())
                        App.makeToast(getString(R.string.no_internet_connection));
                    else App.makeToast(getString(R.string.something_went_wrong));*//*
                }
            }
        });*/
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

/*    private void followUnfollowUser(String publisherId, boolean follow) {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (follow) {
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            } else {
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);
            }
            Call<FollowResponse> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<FollowResponse>() {
                @Override
                public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                    FollowResponse followResponse = response.body();
                    if (followResponse.getStatus().equals(Constants.TAG_TRUE)) {
                        if (followRequest.getType().equals(Constants.TAG_FOLLOW_USER)) {
//                            App.makeToast(getString(R.string.followed_successfully));
                            setFollowButton(true);
                        } else {
                            setFollowButton(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<FollowResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }*/

    private void initBottomViewersDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_viewers, null);
        bottomViewersDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomViewersDialog.setContentView(bottomSheet);

        bottomViewersLay = bottomSheet.findViewById(R.id.bottomViewersLay);
        bottomViewerCount = bottomSheet.findViewById(R.id.bottomViewerCount);
        bottomRecyclerView = bottomSheet.findViewById(R.id.bottomRecyclerView);
        txtBottomDuration = bottomSheet.findViewById(R.id.txtBottomDuration);
        bottomDurationLay = bottomSheet.findViewById(R.id.bottomDurationLay);
        bottomLikesLay = bottomSheet.findViewById(R.id.bottomLikesLay);
        txtTotalLikes = bottomSheet.findViewById(R.id.txtTotalLikes);

        bottomLikesLay.setVisibility(VISIBLE);
        bottomViewersLay.setVisibility(GONE);

        txtTotalLikes.setText(streamDetails.getLikes() != null ? streamDetails.getLikes() : "0");
        bottomViewersDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomViewersDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
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

    private void initBottomCommentsDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_comments, null);
        bottomCommentsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomCommentsDialog.setContentView(bottomSheet);

        bottomCommentsView = bottomSheet.findViewById(R.id.bottomCommentsView);
        txtNoCommands = bottomSheet.findViewById(R.id.txtNoCommands);
        bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

        commentsAdapter = new CommentsAdapter(this, commentList);
        bottomCommentsView.setLayoutManager(new LinearLayoutManager(this));
        bottomCommentsView.setAdapter(commentsAdapter);
        bottomCommentsView.setHasFixedSize(true);
        commentsAdapter.notifyDataSetChanged();

        /*if (commentList.size() == 0) {
            txtNoCommands.setVisibility(VISIBLE);
            bottomCommentsView.setVisibility(GONE);
        } else {
            txtNoCommands.setVisibility(GONE);
            bottomCommentsView.setVisibility(VISIBLE);
        }*/

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

   /* private void initBottomHashTagDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_hashtags, null);
        bottomHashTagsDialog = new BottomSheetDialog(this, R.style.Bottom_HashTagDialog); // Style here
        bottomHashTagsDialog.setContentView(bottomSheet);

        bottomHashTagView = bottomSheet.findViewById(R.id.bottomHashTagView);
        appUtils.setHashTagItemPadding(bottomHashTagView);

        bottomHashTagsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomHashTagsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }*/

    private void openReportDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_report_addon, null);
        reportDialog = new BottomSheetDialog(PlayerActivity.this, R.style.Bottom_StreamDialog); // Style here
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

        reportDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet1 = dialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
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

    public void getStreamDetails(String streamName) {
        if (NetworkReceiver.isConnected()) {
            Call<StreamDetails> call3 = apiInterface.getStreamDetails(GetSet.getToken(), GetSet.getUserId(), streamName);
            call3.enqueue(new Callback<StreamDetails>() {
                @Override
                public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                    if (response.isSuccessful()) {
                        Log.e(TAG, "onResponse: :::::::::::::::::Stream Details::" + new Gson().toJson(response.body()));

                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            apiStreamData = response.body();
                            streamDetails = response.body();
                            if (apiStreamData.getStreamBlocked() == 1) {
                                //App.makeToast(getString(R.string.broadcast_deactivated_by_admin));
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            if (!TextUtils.isEmpty(apiStreamData.getLink_url())) {
                                videoLinkLay.setVisibility(VISIBLE);
                            } else {
                                videoLinkLay.setVisibility(GONE);
                            }

                            if (apiStreamData.follow.equals("true")) {
                                profileFollowIcon.setText("Following");
                                followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorDarkGrey));
                                followLay.clearAnimation();
                                apiStreamData.setFollow("true");
                            } else {
                                profileFollowIcon.setText("Follow");
                                followLay.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                                followLay.clearAnimation();
                                apiStreamData.setFollow("false");
                            }


                            txtTotalLikes.setText(apiStreamData.getLikes());
                            bottomStreamTitle.setText(apiStreamData.getTitle());
                            bottomViewerCount.setText(apiStreamData.getWatchCount());
                            lftVoteCount.setText(apiStreamData.getLftVoteCount() != null ? apiStreamData.getLftVoteCount() : "0");
                            videoid.setText(apiStreamData.getVideoId() != null ? apiStreamData.getVideoId() : "0");

                            if (!("" + apiStreamData.getPublisherId()).equals(GetSet.getUserId())) {
                                bottomUserLay.setVisibility(VISIBLE);
                                txtPublisherName.setText(apiStreamData.getPostedBy());
                                Glide.with(getApplicationContext())
                                        .load(apiStreamData.getPublisherImage())
                                        .error(R.drawable.ic_account)
                                        .into(publisherImage);
                                //setFollowButton(apiStreamData.getFollow().equals(Constants.TAG_TRUE));
                                txtReport.setText(apiStreamData.getReported().equals(Constants.TAG_FALSE) ? getString(R.string.report_broadcast) :
                                        getString(R.string.undo_report_broadcast));
                            }
                            //     setHashTagUI(apiStreamData.getHashTags());
                            commentList.clear();
                            tempCommentList.addAll(apiStreamData.getCommentsList());
                            timeList = new ArrayList<>();
                            for (StreamDetails.Comments comment : apiStreamData.getCommentsList()) {
                                if (!comment.getType().equals(StreamConstants.TAG_LIKED) &&
                                        !comment.getType().equals(StreamConstants.TAG_STREAM_JOINED)) {
                                    commentList.add(comment);
                                }
                                timeList.add(comment.getTime());
                            }
                            commentsAdapter.notifyDataSetChanged();


                            if (commentList.size() == 0) {
                                txtNoCommands.setVisibility(VISIBLE);
                                bottomCommentsView.setVisibility(GONE);
                            } else {
                                txtNoCommands.setVisibility(GONE);
                                bottomCommentsView.setVisibility(VISIBLE);
                            }

                            giftsList.clear();
                            giftsList.addAll(apiStreamData.getGiftList());
                            bottomGiftCount.setText("" + giftsList.size());
                            giftsAdapter.notifyDataSetChanged();
                        } else {
                            if (response.body().getMessage() != null && response.body().getMessage().equals("No Stream found")) {
//                                App.makeToast(getString(R.string.no_stream_found));
                                finish();
                            } else {
                                // App.makeToast(getString(R.string.something_went_wrong));
                                finish();
                            }
                        }
                    } else {
                        finish();
                        //App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<StreamDetails> call, Throwable t) {
                    call.cancel();
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }

    /*private void setHashTagUI(ArrayList<String> hashTags) {
        if (hashTags.size() > 0) {
            hashTagsLay.setVisibility(VISIBLE);
            if (hashTags.size() >= 2) {
                btnMoreHashTag.setVisibility(GONE);
                List<String> temp = new ArrayList<>();
                temp.add(hashTags.get(0));
                temp.add(hashTags.get(1));
                if (hashTags.size() > 2) {
                    btnMoreHashTag.setVisibility(VISIBLE);
                }

                hashtagView.setData(temp, new HashtagView.DataTransform<String>() {
                    @Override
                    public CharSequence prepare(String item) {
                        return context.getString(R.string.hash_symbol) + item;
                    }
                });
            } else {
                hashtagView.setData(hashTags, new HashtagView.DataTransform<String>() {
                    @Override
                    public CharSequence prepare(String item) {
                        return context.getString(R.string.hash_symbol) + item;
                    }
                });
                btnMoreHashTag.setVisibility(GONE);
            }
            bottomHashTagView.setData(hashTags, new HashtagView.DataTransform<String>() {
                @Override
                public CharSequence prepare(String item) {
                    return context.getString(R.string.hash_symbol) + item;
                }
            });
        } else {
            hashTagsLay.setVisibility(GONE);
        }
    }*/

    /*public void deleteVideo() {
        if (NetworkReceiver.isConnected()) {
            Call<Map<String, String>> call = apiInterface.deleteVideo(GetSet.getUserId(), streamDetails.getName());
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (from != null && from.equals(Constants.TAG_TRENDING)) {
                                isStreamDeleted = true;
                                finish();
                            } else {
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                App.makeToast(getString(R.string.video_deleted_successfully));
                                finish();
                            }
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
//        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
//        App.makeToast(getString(R.string.no_internet_connection));
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*        if (requestCode == Constants.MUTUAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (bottomDialog != null && bottomDialog.isShowing()) {
                    bottomDialog.dismiss();
                }
            }
            if (btnPlay.isChecked()) {
                startPlayer();
            } else {
                pausePlayer();
            }
        }*/
    }

   /* @OnClick({R.id.btnDetail, R.id.playLay, R.id.btnRefresh, R.id.btnComments,
            R.id.btnClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnDetail:
                if (bottomDialog != null && !bottomDialog.isShowing())
                    bottomDialog.show();
                break;
            case R.id.playLay:
                btnPlay.setChecked(!btnPlay.isChecked());
                break;
            case R.id.btnRefresh:
                btnRefresh.setVisibility(GONE);
                btnPlay.setVisibility(VISIBLE);
                if (player != null) {
                    player.seekTo(0);
                }
                btnPlay.setChecked(true);
                break;
            case R.id.btnComments:
                if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                    bottomCommentsDialog.show();
                }
                break;
            case R.id.btnClose:
                onBackPressed();
                break;
            default:
                break;
        }
    }*/

    private void openDeleteDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.cancel();
            deleteVideo();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.cancel());
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

    public void deleteVideo() {
        Call<Map<String, String>> call = apiInterface.deleteVideo(GetSet.getToken(), GetSet.getUserId(), streamDetails.getName());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        isStreamDeleted = true;
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        Toast.makeText(PlayerActivity.this, getString(R.string.video_deleted_successfully), Toast.LENGTH_SHORT).show();
                        /*                        makeToast(getString(R.string.video_deleted_successfully));*/
                        finish();
                    }
                } else {
                    Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    /*                    makeToast(getString(R.string.something_wrong));*/
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!player.isPlaying()) {
            startPlayer();
        }
        pauseExternalAudio(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(timerUpdate);
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
        if (player != null && player.isPlaying()) {
            pausePlayer();
        }
        /*releasePlayer();*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(timerUpdate);
        releasePlayer();
        btnPlay.setChecked(false);
        resumeExternalAudio();
    }

    public void resumeExternalAudio() {
        if (Constants.isExternalPlay) {
            Constants.isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    public void pauseExternalAudio(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            Constants.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //App.getProxy(this).unregisterCacheListener(this);
    }

    private void releasePlayer() {
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            if (player != null) {
                shouldAutoPlay = player.getPlayWhenReady();
                updateResumePosition();
                player.release();
                player = null;
                trackSelector = null;
            }
        }
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentWindowIndex();
        resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getContentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void startPlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    @Override
    public void preparePlayback() {
        Log.i(TAG, "preparePlayback: ");
        player.retry();
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
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }

                Glide.with(context)
                        .load(/*Constants.IMAGE_URL + */gift.getUserImage())
                        .error(R.drawable.ic_account)
                        .placeholder(R.drawable.ic_account)
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
            RoundedImageView userImage, colorImage;
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

                itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isPlayerInitialized = false;
                        btnPlay.setChecked(false);
                        pausePlayer();
                       /* if (giftsList.get(getAdapterPosition()).userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(context, MyProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        } else {
                            Intent profile = new Intent(context, OthersProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_PARTNER_NAME, giftsList.get(getAdapterPosition()).getUserName());
                            profile.putExtra(Constants.TAG_PARTNER_IMAGE, giftsList.get(getAdapterPosition()).getUserImage());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        }*/
                    }
                });
            }
        }
    }

    public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<StreamDetails.Comments> commentList;
        Context context;
        Random rnd;
        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;

        public CommentsAdapter(Context context, ArrayList<StreamDetails.Comments> commentList) {
            this.commentList = commentList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_JOIN) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_GIFT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_gifts, parent, false);
                return new GiftViewHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                final MyViewHolder holder = (MyViewHolder) viewHolder;
                StreamDetails.Comments comments = commentList.get(position);
                holder.txtMessage.setText(comments.getMessage());
                holder.txtUserName.setText("@" + comments.getUserName());

            } else if (viewHolder instanceof JoiningViewHolder) {
                final JoiningViewHolder joinViewHolder = (JoiningViewHolder) viewHolder;
                joinViewHolder.itemLay.setVisibility(GONE);
                StreamDetails.Comments comments = commentList.get(position);
                joinViewHolder.txtJoined.setText("@" + comments.getUserName() + " Joined");
            } else if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                StreamDetails.Comments comments = commentList.get(position);
                Glide.with(context)
                        .load(/*Constants.IMAGE_URL +*/ comments.getGiftIcon())
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }
                String userName = "@" + comments.getUserName() + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + comments.getGiftTitle() + "\"";
                String gift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + gift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (commentList.get(position) != null) {
                String type = "" + commentList.get(position).getType();
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

        public class JoiningViewHolder extends RecyclerView.ViewHolder {

            TextView txtJoined;

            FrameLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);

                txtJoined = view.findViewById(R.id.txtJoined);
                itemLay = view.findViewById(R.id.itemLay);
                //ButterKnife.bind(this, view);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtUserName;

            TextView txtMessage;

            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                //  ButterKnife.bind(this, view);

                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {

            TextView txtGiftName;

            ImageView giftImage;

            RelativeLayout giftLay;

            public GiftViewHolder(View view) {
                super(view);
                //ButterKnife.bind(this, view);
                txtGiftName = view.findViewById(R.id.txtGiftName);
                giftImage = view.findViewById(R.id.giftImage);
            }
        }
    }

/*    public class GiftsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
                        .load(Constants.IMAGE_URL + gift.getGiftIcon())
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }

                Glide.with(context)
                        .load(Constants.IMAGE_URL + gift.getUserImage())
                        .error(R.drawable.no_video)
                        .placeholder(R.drawable.no_video)
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
                ButterKnife.bind(this, view);

                itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isPlayerInitialized = false;
                        btnPlay.setChecked(false);
                        pausePlayer();
                        if (giftsList.get(getAdapterPosition()).userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(context, MyProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        } else {
                            Intent profile = new Intent(context, OthersProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_PARTNER_NAME, giftsList.get(getAdapterPosition()).getUserName());
                            profile.putExtra(Constants.TAG_PARTNER_IMAGE, giftsList.get(getAdapterPosition()).getUserImage());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        }
                    }
                });
            }
        }
    }*/

    public class ReportAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<Report> reportList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

        public ReportAdapter(Context context, List<Report> reportList) {
            this.context = context;
            this.reportList = reportList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_report_addon, parent, false);
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtReport;

            LinearLayout itemReportLay;

            public MyViewHolder(View view) {
                super(view);
                //ButterKnife.bind(this, view);
                txtReport = view.findViewById(R.id.txtReport);
                itemReportLay = view.findViewById(R.id.itemReportLay);

                itemReportLay.setOnClickListener(v -> {

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

/*    private class Report {

        @SerializedName("title")
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }*/

    private void sendReport(String title, boolean report) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_NAME, streamName);
            map.put(Constants.TAG_TYPE, Constants.TAG_LIVE_STREAMING);
            if (report) {
                map.put(Constants.TAG_REPORT, title);
            }
            Log.d(TAG, "sendReport: params" + new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.reportStream(GetSet.getToken(), map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (report) {
                                Toast.makeText(PlayerActivity.this, getString(R.string.reported_successfully), Toast.LENGTH_SHORT).show();
                                if (streamDetails != null) {
                                    streamDetails.setReported(Constants.TAG_TRUE);
                                }
                                txtReport.setText(getString(R.string.undo_report_broadcast));
                            } else {
                                Toast.makeText(PlayerActivity.this, getString(R.string.undo_report_successfully), Toast.LENGTH_SHORT).show();
                                if (streamDetails != null) {
                                    streamDetails.setReported(Constants.TAG_FALSE);
                                }
                                txtReport.setText(getString(R.string.report_broadcast));
                            }
                            if (bottomDialog != null && bottomDialog.isShowing()) {
                                bottomDialog.dismiss();
                            }
                        } else
                            Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(PlayerActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    public class LiveCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;
        ArrayList<HashMap<String, String>> commentList;
        Context context;

        public LiveCommentsAdapter(Context context, ArrayList<HashMap<String, String>> commentList) {
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
            } else if (viewType == VIEW_TYPE_GIFT) {
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
                        Log.d(TAG, "Live Comments:comment" + map.get(Constants.TAG_MESSAGE));
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
                        Log.d(TAG, "Live Comments:join" + map.get(Constants.TAG_USER_NAME));
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(joinViewHolder.itemLay, joinViewHolder.getAdapterPosition());
                        joinViewHolder.itemLay.setVisibility(GONE);

                    }
                }.start();
            } else if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                Glide.with(context)
                        .load(Constants.IMAGE_URL + map.get(Constants.TAG_GIFT_ICON))
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

            FrameLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);
                //ButterKnife.bind(this, view);

                txtJoined = view.findViewById(R.id.txtJoined);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView txtUserName;

            TextView txtMessage;

            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                // ButterKnife.bind(this, view);

                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {

            TextView txtGiftName;
            ImageView giftImage;
            RelativeLayout giftLay;

            public GiftViewHolder(View view) {
                super(view);
                giftLay = view.findViewById(R.id.giftLay);
                giftImage = view.findViewById(R.id.giftImage);
                txtGiftName = view.findViewById(R.id.txtGiftName);
            }
        }

        private void setFadeAnimation(final View view, final int position) {
            /*sets animation from complete opaque state(1.0f) to complete transparent state(0.0f)*/
            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
            anim.setDuration(StreamConstants.FADE_DURATION);
            view.startAnimation(anim);
        }
    }

    public void sendGift(int finalGiftPosition) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();

            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_GIFT_ID, AdminData.giftList.get(finalGiftPosition).getGiftId());
            map.put(Constants.TAG_VIDEO_ID, streamDetails.getStreamId());
            map.put(Constants.TAG_GIFT, String.valueOf(AdminData.giftList.get(finalGiftPosition).getGiftGems()));
            Log.d(TAG, "SendGift:Params" + map);

            Call<Map<String, String>> call = apiInterface.sendGift(map);

            call.enqueue(new Callback<Map<String, String>>() {

                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Log.d(TAG, "onResponse::::::::::::::: " + response.body());

                    if (giftDialog != null && giftDialog.isShowing()) {
                        giftDialog.dismiss();
                    }


                    if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals("true")) {
                        Toast.makeText(PlayerActivity.this, response.body().get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                        animGiftLay.setVisibility(View.VISIBLE);
                        animGiftImage.setVisibility(VISIBLE);
                        animGiftLay.clearAnimation();

                        ObjectAnimator anim = ObjectAnimator
                                .ofFloat(animGiftLay, "translationX", 0, 25, -25, 25, -25, 15, -15, 10, -6, 0)
                                .setDuration(2000);
                        anim.addListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                animGiftLay.startAnimation(AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.anim_zoom_out_length));
                                Glide.with(PlayerActivity.this)
                                        .load(tempGiftList.get(giftPosition).getGiftIcon())
                                        .into(animGiftImage);
                            }

                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                animGiftLay.startAnimation(AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.anim_zoom_out));
                                animGiftImage.setVisibility(GONE);
                            }
                        });
                        anim.start();
                        getStreamDetails(streamName);
                    } else {
                        Toast.makeText(PlayerActivity.this, response.body().get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override

                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                    Log.e(TAG, "onFailure: ", t);
                }
            });

        } else {
            Toast.makeText(context, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
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
                Log.e("giftUrl", "-" + Constants.GIFT_IMAGE_URL + gift.getGiftIcon());
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

                ((MyViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
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