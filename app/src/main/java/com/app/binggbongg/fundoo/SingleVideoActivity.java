package com.app.binggbongg.fundoo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.app.binggbongg.fundoo.home.FollowingExoPlayerRecyclerView.AppName;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.app.binggbongg.fundoo.home.eventbus.FollowingHideIcon;
import com.app.binggbongg.fundoo.home.eventbus.ForyouHideIcon;
import com.app.binggbongg.fundoo.home.eventbus.HideIcon;
import com.app.binggbongg.model.HomeResponse;
import com.app.binggbongg.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
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
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import com.hendraanggrian.appcompat.widget.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.data.model.Mention2;
import com.app.binggbongg.data.model.Tagged;
import com.app.binggbongg.external.CustomEditText;
import com.app.binggbongg.external.TimeAgo;
import com.app.binggbongg.fundoo.home.FollowingProfileUpdate;
import com.app.binggbongg.fundoo.home.FollowingVideoFragment;
import com.app.binggbongg.fundoo.home.eventbus.FollowingFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.FollowingUpdateGiftMessageCount;
import com.app.binggbongg.fundoo.home.eventbus.FollowingVideoFav;
import com.app.binggbongg.fundoo.home.eventbus.FollowingVideoLike;
import com.app.binggbongg.fundoo.home.eventbus.ForYouFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.ForYouProfileUpdate;
import com.app.binggbongg.fundoo.home.eventbus.ForYouUpdateGiftMessageCount;
import com.app.binggbongg.fundoo.home.eventbus.ForYouVideoFav;
import com.app.binggbongg.fundoo.home.eventbus.ForYouVideoLike;
import com.app.binggbongg.fundoo.home.eventbus.OtherProfileUpdate;
import com.app.binggbongg.fundoo.home.eventbus.SingleProfileUpdate;
import com.app.binggbongg.fundoo.profile.MyProfileActivity;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.OnVerticalScrollListener;
import com.app.binggbongg.helper.callback.NetworkResultCallback;
import com.app.binggbongg.model.FollowRequest;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Gift;
import com.app.binggbongg.model.Report;
import com.app.binggbongg.model.SingleVideoResponse;
import com.app.binggbongg.model.UserList;
import com.app.binggbongg.model.VideoCommentResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.view.CustomSpannableTextView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vincan.medialoader.MediaLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class SingleVideoActivity extends BaseFragmentActivity {
    private static final String TAG = SingleVideoActivity.class.getSimpleName();

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.progress_duration)
    ProgressBar progress_duration;
    @BindView(R.id.music_symbol)
    ImageView music_symbol;
    @BindView(R.id.mediaCoverImage)
    ImageView mediaCoverImage;
    @BindView(R.id.profileImage)
    ImageView profileImage;
    @BindView(R.id.volumeControl)
    ImageView volumeControl;
    @BindView(R.id.profileFollowIcon)
    TextView profileFollowIcon;

    @BindView(R.id.txt_description)
    TextView txt_description;
    @BindView(R.id.txt_title)
    TextView txt_title;
    @BindView(R.id.txt_heart_count)
    TextView txt_heart_count;
    @BindView(R.id.txt_gift_count)
    TextView txt_gift_count;
    @BindView(R.id.txt_comment_count)
    TextView txt_comment_count;

    @BindView(R.id.commentTxt)
    TextView commentTxt;

    @BindView(R.id.giftLay)
    RelativeLayout giftLay;
    @BindView(R.id.heartLay)
    LinearLayout heartLay;
    @BindView(R.id.messageLay)
    LinearLayout messageLay;
    @BindView(R.id.shareLay)
    LinearLayout shareLay;
    @BindView(R.id.hideBtmBarLay)
    LinearLayout hideBtmBarLay;

    @BindView(R.id.mainLay)
    RelativeLayout mainLay;
    @BindView(R.id.lay_details)
    RelativeLayout layDetails;

    @BindView(R.id.giftImageView)
    ShapeableImageView giftImageView;

    @BindView(R.id.parentLay)
    ConstraintLayout parentLay;


    @BindView(R.id.heart)
    ShapeableImageView heart;

    @BindView(R.id.mediaContainer)
    FrameLayout mediaContainer;

    @BindView(R.id.parentBtnBack)
    ImageView parentBtnBack;

    @BindView(R.id.ic_fav_heart)
    LottieAnimationView icFavHeart;

    @BindView(R.id.publisher_vote_count) MaterialTextView publisherVoteCount;
    @BindView(R.id.txt_vote_count) MaterialTextView voteCount;
    @BindView(R.id.txt_view_count) MaterialTextView viewCount;
    @BindView(R.id.videoLinkLay) LinearLayout videoLinkLay;

    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;

    private String getVideoId, getVideoThumnail, getStatus = "";
    private ApiInterface apiInterface;
    private Runnable runnable;
    private ScheduledExecutorService mExecutorService; // ProgressBar
    private PlayState videoState; // video pause and play
    private MediaLoader mMediaLoader; // used for cache
    private boolean isCached = false; // check video is already saved local or not
    private Boolean likePrevent = false;

    SingleVideoResponse singleVideoResponse;

    /*private View bottom_sheet_longpress;*/
    private BottomSheetDialog reportDialog;
    private View bottom_sheet_longpress;
    private BottomSheetDialog bottomSheetLongPressDiloag, hideBottomBarBS;
    private BottomSheetDialog videoCommandBottomDialog;
    private BottomSheetDialog VideoCommentSentDialog;
    private BottomSheetDialog giftDialog;
    private BottomSheetDialog shareDialog;
    TextView btm_hide_menu_tv, btm_hide_yes, btm_hide_no;

    private RecyclerView reportsView;
    private ImageView btnBack;
    private TextView txtTitle;
    private MaterialButton btnReport;
    private String getSelectedReport = "";
    ReportAdapter reportAdapter;

    ViewPager viewPager;
    private LinearLayout sendLay;
    CircleIndicator pagerIndicator;
    private TextView txtAttachmentName, txtSend;
    List<Gift> tempGiftList = new ArrayList<>();
    int displayHeight, displayWidth;
    GiftAdapter giftAdapter;
    private int giftPosition = 0;
    LinearLayoutManager stickerLayoutManager;
    private RecyclerView recyclerView;
    private final int ITEM_LIMIT = 8;
    private String from = "", comment_open = "";
    private boolean isVideoViewAdded = false;
    boolean isBottomBarHide = true;

    TextView commentCount, addComment, sendComment, noCommands;
    SocialAutoCompleteTextView mCommentsEditText;
    ImageView closeButton;
    RecyclerView commentRec, userRecylerView;
    CommentAdapter commendAdapter;
    CommentUserAdapter commentUserAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<VideoCommentResponse.Result> commendList = new ArrayList<>();
    String loadMoreCheckVideoCommand = "initial";
    private int lastOffsetVideo = -1;
    RelativeLayout progress, CommandLay, sendCommentLay, rootView;
    FrameLayout headerBottom;
    private SwipeRefreshLayout mSwipeVideoComment = null;
    private int currentPage = 0;
    OnVerticalScrollListener videoScrollListener;
    ArrayList<UserList.Result> liveUser = new ArrayList<>();
    // Touchapp
    boolean tagging = true;
    String beforeTextChanged;
    String tagcomment = "", tagged_user_name = "", tagged_user_id = "";
    public ArrayList<String> list_tagged_user_names = new ArrayList<>();
    public ArrayList<String> list_tagged_user_ids = new ArrayList<>();
    private TextView nullText;
    private RelativeLayout nullLay;

    private HashSet<String> mentions = new HashSet();
    private ArrayList<Mention2> mentionsCache = new ArrayList<>();

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void testAnd(SingleProfileUpdate){

    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SingleProfileUpdate event) {
        getResponse("update");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_video);

        ButterKnife.bind(this);

        Toast.makeText(this, "OpenSingleVideo", Toast.LENGTH_SHORT).show();

        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        mMediaLoader = MediaLoader.getInstance(SingleVideoActivity.this);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        getVideoId = getIntent().getStringExtra(Constants.TAG_VIDEO_ID);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        comment_open = getIntent().getStringExtra(Constants.TAG_COMMENT_OPEN);

        Timber.d("onCreate: from %s", from);

        if (getIntent().getStringExtra(Constants.TAG_VIDEO_THUMBANIL) != null)
            Glide.with(getApplicationContext())
                    .load(getIntent().getStringExtra(Constants.TAG_VIDEO_THUMBANIL))

                    .into(mediaCoverImage);

        progressBar.setAlpha(1.0f);
        //isBottomBarHide = SharedPref.getBoolean(SharedPref.HIDE_ICONS, true);

        mainLay.setAlpha(0.2f);
        mainLay.setEnabled(false);

        getResponse("");

    }

    private void shareVideoDialog(SingleVideoResponse.Result item) {
        String link=item.getShareLink();
        View shareView = getLayoutInflater().inflate(R.layout.share_video_dialog, null);
        shareDialog = new BottomSheetDialog(SingleVideoActivity.this, R.style.FavAndReportBottomSheetDialog); // Style here
        shareDialog.setContentView(shareView);

        shareDialog.setOnDismissListener(dialogInterface -> {

        });

        RelativeLayout copyLinkLay = shareView.findViewById(R.id.copyLinkLay);
        RelativeLayout copyWhatsAppLay = shareView.findViewById(R.id.whatsappLay);
        RelativeLayout copyFacebookLay = shareView.findViewById(R.id.facebookLay);
        RelativeLayout copyMessagerLay = shareView.findViewById(R.id.messagerLay);
        RelativeLayout copyInstaLay = shareView.findViewById(R.id.instaLay);
        RelativeLayout copyTwitterLay = shareView.findViewById(R.id.twitterLay);
        RelativeLayout copyMailLay = shareView.findViewById(R.id.gmailLay);
        RelativeLayout copySMSLay = shareView.findViewById(R.id.smsLay);
        RelativeLayout copySnapchatLay = shareView.findViewById(R.id.snapchatLay);
        RelativeLayout copyInAppLay = shareView.findViewById(R.id.inAppLay);

        copyInstaLay.setOnClickListener(v -> {
            boolean installed = appInstalledOrNot("com.instagram.android");
            if (installed) {
                callShareAPI(item,"instagram");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.instagram.android");
                startActivity(sendIntent);
            } else {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.instagram.android"));
                startActivity(intent);
            }
            shareDialog.dismiss();
        });

        copyTwitterLay.setOnClickListener(v -> {
            boolean installed = appInstalledOrNot("com.twitter.android");
            if (installed) {
                callShareAPI(item,"twitter");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.twitter.android");
                startActivity(sendIntent);
            } else {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.twitter.android"));
                startActivity(intent);
            }
            shareDialog.dismiss();
        });

        copySnapchatLay.setOnClickListener(v -> {
            boolean installed = appInstalledOrNot("com.snapchat.android");
            if (installed) {
                callShareAPI(item,"snapchat");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.snapchat.android");
                startActivity(sendIntent);
            } else {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.snapchat.android"));
                startActivity(intent);
            }
            shareDialog.dismiss();
        });

        copyMailLay.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SEND);
//                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{ });
//                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT, link);

            //need this to prompts email client only
            email.setType("text/plain");

            startActivity(Intent.createChooser(email, "Choose an Email client :"));
            shareDialog.dismiss();
        });

        shareDialog.show();


        copyLinkLay.setOnClickListener(v -> {

            ClipboardManager clipboardManager = (ClipboardManager) getApplication().getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData data = ClipData.newPlainText("label", link);
                clipboardManager.setPrimaryClip(data);
                Toasty.success(SingleVideoActivity.this, R.string.linkCopied, Toasty.LENGTH_SHORT).show();
                shareDialog.dismiss();
            }

        });


        copyWhatsAppLay.setOnClickListener(view -> {
            boolean installed = appInstalledOrNot("com.whatsapp");
            if (installed) {
                callShareAPI(item,"whatsapp");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);

            } else {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"));
                startActivity(intent);
            }
            shareDialog.dismiss();
        });


        copyFacebookLay.setOnClickListener(v -> {

            boolean installed = appInstalledOrNot("com.facebook.katana");
            if (installed) {
                callShareAPI(item,"Facebook");
                ShareLinkContent fbShare = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(link))
                        .build();
                ShareDialog.show(SingleVideoActivity.this, fbShare);

            } else {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.katana"));
                startActivity(intent);
            }
            shareDialog.dismiss();
        });

        copyMessagerLay.setOnClickListener(v -> {
            boolean installed = appInstalledOrNot("com.facebook.orca");
            if (installed) {
                callShareAPI(item,"messenger");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.facebook.orca");
                startActivity(sendIntent);
            } else {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.orca"));
                startActivity(intent);
            }
            shareDialog.dismiss();
        });

    }

    private boolean appInstalledOrNot(String uri) {

        PackageManager pm = getApplication().getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void callShareAPI(SingleVideoResponse.Result item, String type) {

        Map<String ,String> params=new HashMap<>();
        params.put("video_id",item.getVideoId());
        params.put("user_id",GetSet.getUserId());
        params.put("share_type",type);

        Call<HashMap<String,String>> call=apiInterface.setShareCount(params);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.body() != null){
                    Log.e(TAG, "onResponse: ::::::::::::::::share:::"+new Gson().toJson(response.body()) );
                    if (response.body().get("status").toString().equals("true")){
                        Toast.makeText(SingleVideoActivity.this, ""+response.body().get("message").toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                Log.e(TAG, "onFailure: :::::::::::::::::share failure::",t );
            }
        });

    }


    public void playerpause(Boolean state) {
        if (videoPlayer != null) videoPlayer.setPlayWhenReady(state);
        if (volumeControl != null && volumeControl.getVisibility() == VISIBLE)
            volumeControl.setVisibility(View.GONE);
    }


    private void addVideoView() {
        mediaContainer.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
    }

    public void profile() {
        if ((from != null && from.equals(Constants.TAG_FOR_YOU_PROFILE_UPDATE)) ||
                ((from != null && from.equals(Constants.TAG_OTHER_PROFILE_UPDATE))) ||
                ((from != null && from.equals(Constants.TAG_FOLLOWING_PROFILE_UPDATE)))) {
            setResult(Constants.PROFILE_VIDEO_RESET);
            finish();
            overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
        } else {
            playerpause(false);
            Intent profile;
            if (singleVideoResponse.getResult().get(0).getPublisherId().equals(GetSet.getUserId())) {
                profile = new Intent(SingleVideoActivity.this, MyProfileActivity.class);
            } else {
                profile = new Intent(SingleVideoActivity.this, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, singleVideoResponse.getResult().get(0).getPublisherId());
                profile.putExtra(Constants.TAG_USER_IMAGE, singleVideoResponse.getResult().get(0).getPublisherImage());
            }
            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SINGLE_VIDEO);
            startActivityForResult(profile, Constants.SINGLE_ACTIVITY);
        }
    }

    private void followAPI(String publisherId, String publishImage) {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            followRequest.setType(Constants.TAG_FOLLOW_USER);


            Timber.d("followUnfollowUser: %s", App.getGsonPrettyInstance().toJson(followRequest));

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Map<String, String> followResponse = response.body();

                    if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                        String userType = "";
                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }

                        if (from != null) {
                            switch (from) {
                                case Constants.TAG_FOR_YOU_PROFILE_UPDATE:
                                    EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publishImage, userType)); // Foryouprofile
                                    break;
                                case Constants.TAG_OTHER_PROFILE_UPDATE:
                                    EventBus.getDefault().post(new OtherProfileUpdate(publisherId)); // OtherProfileUpdate
                                    break;
                                case Constants.TAG_FOLLOWING_PROFILE_UPDATE:
                                    EventBus.getDefault().post(new FollowingProfileUpdate(publisherId, publishImage, userType)); // FollowingProfileUpdate
                                    break;
                            }
                        }


                        EventBus.getDefault().post(new ForYouFollowFollowing(false, publisherId)); // ForYouVideoFragment
                        EventBus.getDefault().post(new FollowingFollowFollowing(false, publisherId));

                        profileFollowIcon.clearAnimation();
                        profileFollowIcon.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void heartFun() {

        Map<String, String> map = new HashMap<>();

        map.put("user_id", GetSet.getUserId());
        map.put("video_id", singleVideoResponse.getResult().get(0).getVideoId());
        map.put("token", GetSet.getAuthToken());

        Timber.d("heartFun: %s", App.getGsonPrettyInstance().toJson(map));

        Call<Map<String, String>> call = apiInterface.getHeartStatus(map);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    Timber.d("onResponse: %s", response.body());

                    getStatus = response.body().get(Constants.TAG_MESSAGE);
                    txt_heart_count.setText(response.body().get("likecount"));

                    if (response.body().get(Constants.TAG_MESSAGE).equals("Liked Successfully")) {
                        heartLay.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));
                        EventBus.getDefault().post(new FollowingVideoLike(singleVideoResponse.getResult().get(0).getVideoId(), "liked", txt_heart_count.getText().toString()));
                        EventBus.getDefault().post(new ForYouVideoLike(singleVideoResponse.getResult().get(0).getVideoId(), "liked", txt_heart_count.getText().toString()));
                        heart.setImageResource(R.drawable.heart_color);
                        singleVideoResponse.getResult().get(0).setIsLiked(true);
                    } else {
                        EventBus.getDefault().post(new FollowingVideoLike(singleVideoResponse.getResult().get(0).getVideoId(), "unliked", txt_heart_count.getText().toString()));
                        EventBus.getDefault().post(new ForYouVideoLike(singleVideoResponse.getResult().get(0).getVideoId(), "unliked", txt_heart_count.getText().toString()));
                        heart.setImageResource(R.drawable.heart_white);
                        singleVideoResponse.getResult().get(0).setIsLiked(false);
                    }

                    String userType = "", publisherId = singleVideoResponse.getResult().get(0).getPublisherId(),
                            publishImage = singleVideoResponse.getResult().get(0).getPublisherImage();
                    if (GetSet.getUserId() != null) {
                        userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                        Timber.i("userType %s", userType);
                    }

                    if (from != null) {
                        switch (from) {
                            case Constants.TAG_FOR_YOU_PROFILE_UPDATE:
                                EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publishImage, userType)); // Foryouprofile
                                break;
                            case Constants.TAG_OTHER_PROFILE_UPDATE:
                                EventBus.getDefault().post(new OtherProfileUpdate(publisherId)); // OtherProfileUpdate
                                break;
                            case Constants.TAG_FOLLOWING_PROFILE_UPDATE:
                                EventBus.getDefault().post(new FollowingProfileUpdate(publisherId, publishImage, userType)); // FollowingProfileUpdate
                                break;
                        }
                    }


                    likePrevent = false;
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {


            }
        });


    }

    @Override
    public void onBackPressed() {
        cusBack();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if (videoCommandBottomDialog != null && videoCommandBottomDialog.isShowing())

        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        } else {
            if (videoCommandBottomDialog != null && videoCommandBottomDialog.isShowing())
                playerpause(false);
            else playerpause(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.SINGLE_ACTIVITY || resultCode == Constants.SINGLE_ACTIVITY) {
            giftLay.setClickable(false);
            profileImage.setClickable(false);
            profileFollowIcon.setClickable(false);
            heartLay.setClickable(false);
            music_symbol.setClickable(false);
            txt_title.setClickable(false);
            shareLay.setClickable(false);
            messageLay.setClickable(false);
            mediaContainer.setClickable(false);

            mainLay.setAlpha(0.8f);
            mainLay.setEnabled(false);
            getResponse("update");
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


    }

    private void setViewPager() {

        int count;
        if (AdminData.giftList.size() <= ITEM_LIMIT) {
            count = 1;
        } else {
            count = AdminData.giftList.size() % ITEM_LIMIT == 0 ? AdminData.giftList.size() / ITEM_LIMIT : (AdminData.giftList.size() / ITEM_LIMIT) + 1;
        }

        Toast.makeText(this, "Single Video Activity", Toast.LENGTH_SHORT).show();

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

    private void getResponse(String update) {

        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> request = new HashMap<>();

            request.put("user_id", GetSet.getUserId());
            request.put("token", GetSet.getAuthToken());
            request.put("video_id", getVideoId);

            Timber.d("getParams: %s", App.getGsonPrettyInstance().toJson(request));
            Call<SingleVideoResponse> call = apiInterface.getSingleVideo(request);


            call.enqueue(new Callback<SingleVideoResponse>() {
                @Override
                public void onResponse(@NotNull Call<SingleVideoResponse> call, @NotNull Response<SingleVideoResponse> response) {

                       Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body().getResult()));

                    if (response.body()!=null && response.body().isSuccessFul()) {

                        singleVideoResponse = response.body();

                        giftLay.setClickable(true);
                        profileImage.setClickable(true);
                        profileFollowIcon.setClickable(true);
                        heartLay.setClickable(true);
                        music_symbol.setClickable(true);
                        txt_title.setClickable(true);
                        shareLay.setClickable(true);
                        messageLay.setClickable(true);
                        mediaContainer.setClickable(true);

                        if(!TextUtils.isEmpty(singleVideoResponse.getResult().get(0).getLink_url())){
                            videoLinkLay.setVisibility(VISIBLE);
                        }else{
                            videoLinkLay.setVisibility(GONE);
                        }

                        if (singleVideoResponse.getResult().get(0).getIsLiked()) {
                            getStatus = "Liked Successfully";
                            heart.setImageResource(R.drawable.heart_color);
                            /*Glide.with(getApplicationContext())
                                    .load(R.drawable.heart_color)

                                    .into(heart);*/
                        } else {
                            getStatus = "UnLiked Successfully";
                            heart.setImageResource(R.drawable.heart_white);
                            /*Glide.with(getApplicationContext())
                                    .load(R.drawable.heart_white)

                                    .into(heart);*/
                        }

                        mainLay.setVisibility(VISIBLE);

                        mainLay.animate()
                                .alpha(1.0f)
                                .setDuration(500).setStartDelay(0)
                                .setListener(null);


                        Glide.with(getApplicationContext())
                                .asGif()
                                .load(R.drawable.music)
                                .into(music_symbol);

                        if (update.equals("")) {
                            initialize(response.body().getResult().get(0).getPlaybackUrl());
                        } else {
                            progressBar.setAlpha(0.0f);
                        }

                        bindView(response.body().getResult().get(0));

                        if (comment_open != null && comment_open.equals(Constants.TAG_COMMENT_OPEN)) {
                            videoDialog(singleVideoResponse.getResult().get(0).getPublisherId(), singleVideoResponse.getResult().get(0).getVideoId());
                        }

                        if (GetSet.getUserId().equals(singleVideoResponse.getResult().get(0).getPublisherId())) {
                            giftLay.setVisibility(View.GONE);
                        } else {
                            giftLay.setVisibility(VISIBLE);
                        }


                        giftLay.setOnClickListener(v -> {
                            App.preventMultipleClick(giftLay);
                            if (GetSet.getUserId().equals(singleVideoResponse.getResult().get(0).getPublisherId())) {

                                Intent giftIntent = new Intent(SingleVideoActivity.this, ConvertGiftActivity.class);
                                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(giftIntent);
                            } else {
                                openGiftDialog();
                            }

                        });

                        profileImage.setOnClickListener(v -> {
                                    App.preventMultipleClick(profileImage);
                                    profile();
                                }
                        );

                        videoLinkLay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                App.preventMultipleClick(videoLinkLay);
                                if (!TextUtils.isEmpty(singleVideoResponse.getResult().get(0).getLink_url())) {
                                    Intent intent = new Intent(SingleVideoActivity.this, PrivacyActivity.class);
                                    intent.putExtra("from", "video");
                                    intent.putExtra("link_url", singleVideoResponse.getResult().get(0).getLink_url());
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(SingleVideoActivity.this, "No video link found!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SetViewCount(singleVideoResponse.getResult().get(0).getVideoId());
                            }
                        }, 3000);

                        profileFollowIcon.setOnClickListener(v -> {
                            App.preventMultipleClick(profileFollowIcon);
                            profileFollowIcon.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));
                            followAPI(singleVideoResponse.getResult().get(0).getPublisherId(), singleVideoResponse.getResult().get(0).getPublisherImage());
                        });


                        hideBtmBarLay.setOnClickListener(view -> {
                            hideIcons(singleVideoResponse.getResult().get(0).getVideoId());
                        });


                        heartLay.setOnClickListener(v -> {
                            if (!likePrevent) {
                                likePrevent = true;
                                //heartLay.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_in));
                                heartFun();
                            }

                        });

                        music_symbol.setOnClickListener(v -> {

                            App.preventMultipleClick(music_symbol);
                            playerpause(false);
                            Intent intent = new Intent(SingleVideoActivity.this, SoundTrackActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.TAG_SOUND_ID, singleVideoResponse.getResult().get(0).getSoundtracks().getSoundId());
                            bundle.putString(Constants.TAG_SOUND_TITLE, singleVideoResponse.getResult().get(0).getSoundtracks().getTitle());
                            bundle.putString(Constants.TAG_SOUND_URL, singleVideoResponse.getResult().get(0).getSoundtracks().getSoundUrl());
                            bundle.putString(Constants.TAG_SOUND_IS_FAV, String.valueOf(singleVideoResponse.getResult().get(0).getSoundtracks().getIsFavorite()));
                            bundle.putString(Constants.TAG_SOUND_COVER, singleVideoResponse.getResult().get(0).getSoundtracks().getCoverImage());
                            bundle.putString(Constants.TAG_SOUND_DURATION, singleVideoResponse.getResult().get(0).getSoundtracks().getDuration());
                            intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                            startActivityForResult(intent, Constants.SINGLE_ACTIVITY);
                        });


                        txt_title.setOnClickListener(v -> profile());

                        shareLay.setOnClickListener(v -> {
                            App.preventMultipleClick(shareLay);
                            shareVideoDialog(singleVideoResponse.getResult().get(0));
                        });

                        messageLay.setOnClickListener(v -> {
                            playerpause(false);
                            App.preventMultipleClick(messageLay);
                            videoDialog(singleVideoResponse.getResult().get(0).getPublisherId(), singleVideoResponse.getResult().get(0).getVideoId());
                        });
                        commentTxt.setOnClickListener(v -> {
                            playerpause(false);
                            App.preventMultipleClick(commentTxt);
                            videoDialog(singleVideoResponse.getResult().get(0).getPublisherId(), singleVideoResponse.getResult().get(0).getVideoId());
                        });

                        //  mediaContainer.setOnClickListener(v -> togglePlay());

                        /*mediaContainer.setOnLongClickListener(v -> {

                            if (!singleVideoResponse.getResult().get(0).getPublisherId().equals(GetSet.getUserId())) {
                                reportAndFav();
                                return true;
                            }
                            return false;
                        });*/

                        parentBtnBack.setOnClickListener(v -> {
                            cusBack();
                        });


                        // TODO: add double-tap heart
                        final GestureDetector gestureDetector = new GestureDetector(SingleVideoActivity.this, new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDoubleTap(MotionEvent e) {

                                if (!singleVideoResponse.getResult().get(0).getIsLiked()) {

                                    if (!likePrevent) {
                                        likePrevent = true;
                                        heartFun();
                                    }

                                }

                                icFavHeart.setVisibility(View.VISIBLE);
                                icFavHeart.playAnimation();
                                icFavHeart.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this,
                                        R.anim.heart_animation));

                                icFavHeart.addAnimatorListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        Timber.i("ANimation start");


                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {

                                        Timber.i("ANimation end");
                                        icFavHeart.clearAnimation();
                                        icFavHeart.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        Timber.i("ANimation cancel");

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                        Timber.i("ANimation repeat");

                                    }
                                });


                                return super.onDoubleTap(e);
                            }

                            @Override
                            public boolean onSingleTapConfirmed(MotionEvent e) {
                                if (icFavHeart.getVisibility() != View.VISIBLE) {
                                    togglePlay();
                                }
                                return super.onSingleTapConfirmed(e);
                            }

                            @Override
                            public void onLongPress(MotionEvent e) {

                                if (!singleVideoResponse.getResult().get(0).getPublisherId().equals(GetSet.getUserId())) {
                                    reportAndFav();
                                }
                                super.onLongPress(e);


                            }
                        });

                        mediaContainer.setOnTouchListener((v, event) -> {
                            Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                            return gestureDetector.onTouchEvent(event);
                        });
                    } else {
                        Toasty.error(SingleVideoActivity.this, R.string.story_not_found, Toasty.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<SingleVideoResponse> call, @NotNull Throwable t) {
                    Timber.d("onFailure: %s", t.getMessage());
                    finish();
                }
            });
        }


    }


    public void hideIcons(String videoId){
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_hide_menu_bar, null);
        hideBottomBarBS = new BottomSheetDialog(SingleVideoActivity.this, R.style.VideoCommentDialog); // Style here

        hideBottomBarBS.setContentView(sheetView);

        hideBottomBarBS.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        btm_hide_menu_tv = hideBottomBarBS.findViewById(R.id.alert_tv);
        btm_hide_yes = hideBottomBarBS.findViewById(R.id.hide_btm_bar_yes);
        btm_hide_no = hideBottomBarBS.findViewById(R.id.hide_btm_bar_no);

        if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
            btm_hide_menu_tv.setText(Objects.requireNonNull(SingleVideoActivity.this).getString(R.string.unhide_btm_bar_alert));
        } else {
            btm_hide_menu_tv.setText(Objects.requireNonNull(this).getString(R.string.hide_btm_bar_alert));
        }

        btm_hide_yes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                    // btm_hide_menu_tv.setText(Objects.requireNonNull(getContext()).getString(R.string.unhide_btm_bar_alert));
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, false);
                    isBottomBarHide = false;
                    layDetails.setVisibility(GONE);
                   // videoLinkLay.setVisibility(GONE);
                } else {
                    //  btm_hide_menu_tv.setText(Objects.requireNonNull(getContext()).getString(R.string.hide_btm_bar_alert));
                    layDetails.setVisibility(VISIBLE);
                  //  videoLinkLay.setVisibility(VISIBLE);
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);
                    isBottomBarHide = true;
                }
                EventBus.getDefault().post(new HideIcon(isBottomBarHide, videoId));
                EventBus.getDefault().post(new FollowingHideIcon(isBottomBarHide, videoId));
                EventBus.getDefault().post(new ForyouHideIcon(isBottomBarHide, videoId));
                Log.e(TAG, "onClick: ::::::::::::::" + isBottomBarHide);
                hideBottomBarBS.dismiss();
            }
        });


        btm_hide_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (hideBottomBarBS.isShowing()) {
                    hideBottomBarBS.dismiss();
                }
            }
        });

        hideBottomBarBS.show();
    }


    private void SetViewCount(String videoId) {

            Map<String, String> viewCountMap = new HashMap<>();
            viewCountMap.put("video_id", videoId);
            viewCountMap.put("user_id", GetSet.getUserId());

            Call<Map<String, String>> call = apiInterface.setViewCount(viewCountMap);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    Log.e(TAG, "onResponse: :::::ViewCount: " + new Gson().toJson(response.body()));

                    if (response.body().get("status").toString().equals("true")) {

//                        Bundle payload = new Bundle();
//                        payload.putString("video_views_count", String.valueOf(Integer.parseInt(videoItem.getVideo_views_count()) + 1));
//                        homeApiResponse.get(SelectedVideoPosition).setVideo_views_count( String.valueOf(Integer.parseInt(videoItem.getVideo_views_count()) + 1));
//                        videoAdapter.notifyItemChanged(SelectedVideoPosition, payload);
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Log.e(TAG, "onFailure: :::::::::::::::::ViewCount: ", t);
                }
            });


    }

    private void cusBack() {
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(false);
            videoPlayer.release();
        }
        if (videoSurfaceView != null) {
            videoSurfaceView.removeAllViews();
        }
        isVideoViewAdded = true;
        if (videoCommandBottomDialog != null) videoCommandBottomDialog.cancel();

        setResult(Constants.PROFILE_VIDEO_SINGLE_ACTIVITY, getIntent());
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    private void reportAndFav() {

        LinearLayout favLay, reportLay;

        TextView txtFav, txtReport;

        bottom_sheet_longpress = getLayoutInflater().inflate(R.layout.bottmsheet_home_longpress, null);
        bottomSheetLongPressDiloag = new BottomSheetDialog(this, R.style.FavAndReportBottomSheetDialog); // Style here
        bottomSheetLongPressDiloag.setContentView(bottom_sheet_longpress);

        favLay = bottom_sheet_longpress.findViewById(R.id.favLay);
        txtFav = bottom_sheet_longpress.findViewById(R.id.txtFav);
        reportLay = bottom_sheet_longpress.findViewById(R.id.reportLay);
        txtReport = bottom_sheet_longpress.findViewById(R.id.txtReport);

        if (singleVideoResponse.getResult().get(0).getVideoIsFavorite())
            txtFav.setText(R.string.unFav);
        else txtFav.setText(R.string.addafav);


        Timber.d("reportAndFav: %s", singleVideoResponse.getResult().get(0).getVideoIsFavorite());


        // True means already fav
        if (singleVideoResponse.getResult().get(0).getIsVideoReported())
            txtReport.setText(R.string.unreported);
        else txtReport.setText(R.string.report_user);

        favLay.setOnClickListener(v -> {
            App.preventMultipleClick(favLay);
            favApi();
        });

        reportLay.setOnClickListener(v -> {
            App.preventMultipleClick(reportLay);
            if (txtReport.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.report_user))) {
                playerpause(false);
                openReportDialog1();

            } else {
                sendReport(getSelectedReport, false);
            }
        });

        bottomSheetLongPressDiloag.show();

    }

    private void openReportDialog1() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Style here
        reportDialog.setContentView(bottomSheet);
        ((View) bottomSheet.getParent()).setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        reportsView = bottomSheet.findViewById(R.id.reportView);
        btnBack = bottomSheet.findViewById(R.id.btnBack);
        txtTitle = bottomSheet.findViewById(R.id.txtTitle);

        btnReport = bottomSheet.findViewById(R.id.btnReport);

        txtTitle.setText(getString(R.string.report_user));

        btnBack.setOnClickListener(v -> {
            reportDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {

            App.preventMultipleClick(btnReport);

            Timber.d("openReportDialog: %s", getSelectedReport);
            sendReport(getSelectedReport, true);

        });

        reportDialog.setOnKeyListener((v, keyCode, event) -> {

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return false;
            }
            return false;
        });


        reportDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            @SuppressLint("CutPasteId") View bottomSheet1 = dialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        loadReports();
    }

    private void favApi() {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, singleVideoResponse.getResult().get(0).getVideoId());
            map.put(Constants.TAG_FAV_TYPE, "video");

            Timber.d("sendReport: %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();

                        Timber.d("setFav onResponse: %s", new Gson().toJson(response.body()));

                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_UNN_FAV)) {
                                EventBus.getDefault().post(new ForYouVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Fav", false));
                                EventBus.getDefault().post(new FollowingVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Fav", false));
                                singleVideoResponse.getResult().get(0).setVideoIsFavorite(false);
                                App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.add_fav_video_removed), getResources().getColor(R.color.colorBlack));
                            } else {
                                EventBus.getDefault().post(new ForYouVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Fav", true));
                                EventBus.getDefault().post(new FollowingVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Fav", false));
                                singleVideoResponse.getResult().get(0).setVideoIsFavorite(true);
                                App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.add_fav_video_success), getResources().getColor(R.color.color_green));
                            }

                            if (bottomSheetLongPressDiloag.isShowing())
                                bottomSheetLongPressDiloag.dismiss();

                        } else
                            App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(this, AdminData.reportList);
        reportsView.setLayoutManager(new LinearLayoutManager(this));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }


    private void sendReport(String title, boolean report) {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_VIDEO_ID, singleVideoResponse.getResult().get(0).getVideoId());
            if (report) {
                map.put(Constants.TAG_REPORT, title);
            }

            Timber.d("sendReport: %s", new Gson().toJson(map));


            Call<Map<String, String>> call = apiInterface.reportStream(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                            if (reportResponse.get(Constants.TAG_MESSAGE).equals("Reported successfully")) {
                                App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.report_success_alert), getResources().getColor(R.color.color_green));
                                singleVideoResponse.getResult().get(0).setIsVideoReported(true);
                                playerpause(true);
                                EventBus.getDefault().post(new ForYouVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Report", true));
                                EventBus.getDefault().post(new FollowingVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Report", true));

                            } else if (reportResponse.get(Constants.TAG_MESSAGE).equals("Report Undo successfully")) {
                                App.dialog(SingleVideoActivity.this, "", getResources().getString(R.string.undo_report_successfully), getResources().getColor(R.color.colorBlack));
                                EventBus.getDefault().post(new ForYouVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Report", false));
                                EventBus.getDefault().post(new FollowingVideoFav(singleVideoResponse.getResult().get(0).getVideoId(), "Report", false));
                                singleVideoResponse.getResult().get(0).setIsVideoReported(false);
                            }

                            if (reportDialog != null && reportDialog.isShowing())
                                reportDialog.dismiss();

                            getSelectedReport = "";
                            if (bottomSheetLongPressDiloag.isShowing())
                                bottomSheetLongPressDiloag.dismiss();

                        } else
                            App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void togglePlay() {

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
                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);

                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                        volumeControl.animate().alpha(0f)
                                .setDuration(500).setStartDelay(100);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void videoDialog(String publisherId, String videoId) {

        commendList.clear();
        tagged_user_id = "";
        tagged_user_name = "";
        list_tagged_user_ids.clear();
        list_tagged_user_names.clear();

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);
        videoCommandBottomDialog = new BottomSheetDialog(SingleVideoActivity.this, R.style.VideoCommentDialog); // Style here

        videoCommandBottomDialog.setContentView(sheetView);
        videoCommandBottomDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        addComment = sheetView.findViewById(R.id.addComment);
        commentCount = sheetView.findViewById(R.id.commentCount);
        commentRec = sheetView.findViewById(R.id.commentRec);
        sendCommentLay = sheetView.findViewById(R.id.sendCommentLay);
        rootView = sheetView.findViewById(R.id.rootView);
        headerBottom = sheetView.findViewById(R.id.headerBottom);
        mSwipeVideoComment = sheetView.findViewById(R.id.mSwipeVideoComment);
        closeButton = sheetView.findViewById(R.id.closeButton);
        noCommands = sheetView.findViewById(R.id.noCommands);

        mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));

        videoCommandBottomDialog.show();


        commendAdapter = new CommentAdapter(this, commendList, publisherId, videoId);
        linearLayoutManager = new LinearLayoutManager(this);
        commentRec.setLayoutManager(linearLayoutManager);
        commentRec.setAdapter(commendAdapter);

        commentRec.setNestedScrollingEnabled(true);

        //     mSwipeVideoComment.setOnRefreshListener(this::swipeRefresh);

        mSwipeVideoComment.setOnRefreshListener(() -> swipeRefresh(true, videoId));


        videoScrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();


                if (commendList.size() <= lastOffsetVideo) return;
                else lastOffsetVideo = commendList.size();

                loadMoreCheckVideoCommand = "loadmore";
                getCommand(lastOffsetVideo, videoId);

            }
        };

        commentRec.addOnScrollListener(videoScrollListener);


        swipeRefresh(true, videoId);

        closeButton.setOnClickListener(v -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commendList.clear();
            // exoplayerRecyclerViewForYou.setPlayControl(true);
            videoCommandBottomDialog.setDismissWithAnimation(true);
            if (videoCommandBottomDialog.isShowing()) videoCommandBottomDialog.dismiss();
            if (VideoCommentSentDialog != null && VideoCommentSentDialog.isShowing())
                VideoCommentSentDialog.dismiss();

            playerpause(true);
        });


        sendCommentLay.setOnClickListener(v -> {

            topBottomSheet(videoId, addComment.getText().toString());

        });

        videoCommandBottomDialog.setOnKeyListener((v, keyCode, event) -> {


            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    Timber.i("keyCode check %s" , VideoCommentSentDialog.isShowing());

                if (VideoCommentSentDialog != null && VideoCommentSentDialog.isShowing()) {
                    VideoCommentSentDialog.dismiss();
                    playerpause(false);
                    return true;
                } else {
                    playerpause(true);
                    return false;
                }
            }
            Timber.i("keyCode check %s", "return");
            return false;
        });


    }

    private void topBottomSheet(String videoId, String setText) {

        View commentSentDialog = getLayoutInflater().inflate(R.layout.bottom_sheet_comment_sent, null);
        VideoCommentSentDialog = new BottomSheetDialog(this, R.style.Command);

        VideoCommentSentDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        VideoCommentSentDialog.setContentView(commentSentDialog);
        VideoCommentSentDialog.getBehavior().setHideable(false);

        mCommentsEditText = commentSentDialog.findViewById(R.id.socialAutoCompleteTextView);
        sendComment = commentSentDialog.findViewById(R.id.sendComment);

        mCommentsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                VideoCommentSentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            } else {
                VideoCommentSentDialog.dismiss();
            }
        });

        lastOffsetVideo = 0;

        mCommentsEditText = commentSentDialog.findViewById(R.id.socialAutoCompleteTextView);
        mCommentsEditText.requestFocus();
        /*mCommentsEditText.setThreshold(1);
        mCommentsEditText.setDropDownAnchor(R.id.rootView);
        mCommentsEditText.setDropDownHeight(0);  // don't show popup*/
        MentionArrayAdapter<Mention> adapter = new MentionArrayAdapter<>(this);
//            adapter.add(new Mention("groot", "Groot"));
//            adapter.add(new Mention("groot_kid", "Groot Kid"));
//            adapter.add(new Mention("haris", "Haris"));
//            adapter.add(new Mention("harris_jeyaraj", "Harris Jeyaraj"));
//            adapter.add(new Mention("harry", "Harry"));
//            adapter.add(new Mention("greenie", "Greenie"));
        mCommentsEditText.setMentionAdapter(adapter);

        ListView mentionsList = commentSentDialog.findViewById(R.id.userRecylerView);
        mentionsList.setAdapter(adapter);

        Toast[] singleTonToast = new Toast[1];
        mCommentsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Timber.d("onTextChanged: %s", s);
                if (s.toString().contains("@") && validMentionSearch(mCommentsEditText, s.toString())) {
                    mentionsList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() >= 250) {
                    Toasty.info(SingleVideoActivity.this, R.string.max_reached, Toasty.LENGTH_SHORT).show();
                }

                if ((s.toString().length() - s.toString().replaceAll("@", "").length()) > 5) {
                    if (singleTonToast.length > 0 && singleTonToast[0] != null)
                        singleTonToast[0].cancel();
                    singleTonToast[0] = Toasty.info(SingleVideoActivity.this,
                            "Only 5 tags allowed");
                    singleTonToast[0].show();
                    return;
                }
                final String q = getMentionsSuggestionQuery(s.toString(), mCommentsEditText.getSelectionEnd());
//                Log.d(TAG, String.format("Mentis.equals("@") && on: query %s", q));
                Log.d(TAG, String.format("Mention: q=%s", q));
                if (q.length() > 0) {
                    getLiveUsersForQuery(q, result -> {
//                            mentionsCache.clear();
                        adapter.clear();
                        mentionsCache.addAll(
                                Stream.of(result)
                                        .map(result1 -> {
                                                    // for some difficulties we have to send the Mention
                                                    adapter.add(new Mention(result1.getUsername(),
                                                            result1.getDisplayname(), result1.getAvatar()));
                                                    return new Mention2(result1.getUserId(), result1.getUsername(),
                                                            result1.getName(), result1.getAvatar());
                                                }
                                        )
                                        .collect(Collectors.toList())
                        );
                        if (adapter.getCount() > 0) {
                            adapter.getFilter().filter(q);
                            adapter.notifyDataSetChanged();
                            mentionsList.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    adapter.clear();
                    mentionsList.setVisibility(View.GONE);
                }
            }
        });

        mentionsList.setOnItemClickListener((parent, view, position, id) -> {
            final String selected = adapter.getItem(position).toString();
            Log.d(TAG, "onCreate: selected " + position + " " + adapter.getItem(position));
            if (mCommentsEditText.getMentions().contains(selected)) {
                Toasty.info(this,
                        "Already tagged!").show();
                return;
            }
            getReplacedText(mCommentsEditText, selected, mCommentsEditText.getSelectionEnd());

            mentionsList.setVisibility(View.GONE);
        });

        mCommentsEditText.setText(setText);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentsEditText, InputMethodManager.SHOW_IMPLICIT);
        VideoCommentSentDialog.show();


        mCommentsEditText.requestFocus();

        VideoCommentSentDialog.setCanceledOnTouchOutside(false);

        sendComment.setOnClickListener(v -> {
            if (!Objects.requireNonNull(mCommentsEditText.getText()).toString().isEmpty()) {
                postComments(mCommentsEditText.getText().toString(), videoId);
                mCommentsEditText.setText("");
                addComment.setText("");
            }
        });

        VideoCommentSentDialog.setOnCancelListener(dialog -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commendList.clear();
            //followingExoplayerRecyclerView.setPlayControl(true);
            videoCommandBottomDialog.setDismissWithAnimation(true);
            videoCommandBottomDialog.dismiss();
            VideoCommentSentDialog.dismiss();


        });

        CustomEditText.OnKeyPreImeListener onKeyPreImeListener = () -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            //VideoCommentSentDialog.dismiss();
        };

//        mCommentsEditText.setOnKeyPreImeListener(onKeyPreImeListener);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String text = s.toString();
                beforeTextChanged = text;
                if (count == 0 && text.isEmpty()) {
                    liveUser.clear();
                    commentUserAdapter.notifyDataSetChanged();
                    userRecylerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Timber.d("afterTextChanged: %s", s.length());


                if (s.length() != 0 && !s.toString().equals("@")) {
                    Timber.d("afterTextChanged: %s", mCommentsEditText.getText().toString() + " length " + s.length());
                    final String text = s.toString();
                    String textArr[] = text.split(" ");

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String regex = "[A-Za-z0-9&&[^@]]+";
                            String final_text = text.replaceAll(regex, "").replaceAll(" ", "");
                            boolean check4 = final_text.contains("@@@@@@");
                            if (!check4) {
                                Timber.d("afterTextChanged: %s", mCommentsEditText.getText().toString().equals(text));
                                if (mCommentsEditText.getText().toString().equals(text)) {
                                    if (!text.isEmpty() && text.charAt(0) == '@' && textArr.length == 1 && tagging) {

                                        if (!text.replace("@", "").equalsIgnoreCase("")) {
                                            getAllLiveusers(text);
                                        }
                                    } else if (text.contains(" @") && tagging) {
                                        if (text.lastIndexOf("@") > text.lastIndexOf(" ")) {
                                            String tag = text.substring(text.lastIndexOf("@"));// search for @mention...

                                            if (!tag.replace("@", "").equalsIgnoreCase("")) {
                                                getAllLiveusers(tag);
                                                tagging = false;
                                            }
                                        }
                                    } else if (text.contains(" @") && !tagging) {
                                        tagging = true;
                                        liveUser.clear();
                                        commentUserAdapter.notifyDataSetChanged();
                                        userRecylerView.setVisibility(View.GONE);
                                    } else {
                                        liveUser.clear();
                                        commentUserAdapter.notifyDataSetChanged();
                                        userRecylerView.setVisibility(View.GONE);
                                    }
                                }
                            } else if (beforeTextChanged.length() < text.length()) {
                                Toast.makeText(SingleVideoActivity.this, "You can tag upto 5 people", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, 500);

                } else {
                    userRecylerView.setVisibility(View.GONE);
                    tagging = true;
                }
            }
        };

//        mCommentsEditText.addTextChangedListener(textWatcher);

    }

    @NonNull
    private String getMentionsSuggestionQuery(@NonNull String s, int position) {
        final int end = position;
        int start = -1;
        position--;
        while (position >= 0) {
            char c = s.charAt(position);
            if (CharMatcher.whitespace().matches(c)) {
                return "";  // User just typed space, we don't show suggestions
            } else if (c == '@') {
                if (start != -1) return s.substring(start, end);
                else return "";
            } else {
                start = position;
                position--;
            }
        }
        return "";
    }

    private boolean validMentionSearch(SocialAutoCompleteTextView ed, String s) {
        return ed.getMentions().size() != mentions.size();
    }


    private void swipeRefresh(final boolean refresh, String videoId) {
        mSwipeVideoComment.setRefreshing(refresh);

        if (refresh) {
            lastOffsetVideo = 0;
            currentPage = 0;
            loadMoreCheckVideoCommand = "initial";
            getCommand(currentPage, videoId);
        }
    }

    private void initialize(String playbackUrl) {

        isCached = mMediaLoader.isCached(playbackUrl);

        String mediaUrl = null;
        MediaSource videoSource = null;

        videoSurfaceView = new PlayerView(SingleVideoActivity.this);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(SingleVideoActivity.this);
        trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd());
        videoPlayer = new SimpleExoPlayer.Builder(SingleVideoActivity.this)
                .setTrackSelector(trackSelector)
                .build();


        /*BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);*/

        //Create the player using ExoPlayerFactory
        videoPlayer = ExoPlayerFactory.newSimpleInstance(SingleVideoActivity.this, trackSelector);
        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        videoPlayer.setVolume(1f);


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
                        mediaCoverImage.setAlpha(1f);
                        break;
                    case Player.STATE_ENDED:
                        videoPlayer.seekTo(0);
                        break;
                    case Player.STATE_IDLE:
                        break;

                    case Player.STATE_READY:
                        mediaCoverImage.animate().alpha(0f)
                                .setDuration(300).setStartDelay(280);
                        progressBar.setAlpha(0.0f);
                        if (!isVideoViewAdded) {
                            addVideoView();
                            videoState = PlayState.ON;
                            progressBar();
                        }

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        final String videoType = singleVideoResponse.getResult().size() > 0
                ? singleVideoResponse.getResult().get(0).getVideoType()
                : "normal";

        if (Objects.equals(videoType, "gallery")) {
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else {
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        }

        if (!isVideoViewAdded) {
            com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                    SingleVideoActivity.this, Util.getUserAgent(this, AppName));

            if (isCached) {
                mediaUrl = String.valueOf(mMediaLoader.getCacheFile(playbackUrl));

            } else {
                mediaUrl = playbackUrl;
            }

            videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mediaUrl));
            videoPlayer.prepare(videoSource);
            videoPlayer.setPlayWhenReady(true);
        }


    }

    public void getCommand(int currentPage, String videoId) {

        if (noCommands.getVisibility() == View.VISIBLE) noCommands.setVisibility(View.GONE);

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_VIDEO_ID, videoId);
            map.put(Constants.TAG_OFFSET, String.valueOf(currentPage));
            map.put(Constants.TAG_LIMIT, "20");

            Timber.d("getCommentParams: %s", new Gson().toJson(map));

            Call<VideoCommentResponse> call = apiInterface.getVideoComments(map);
            call.enqueue(new Callback<VideoCommentResponse>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(@NotNull Call<VideoCommentResponse> call, @NotNull Response<VideoCommentResponse> response) {

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                        Timber.d("getCommentResponse=> %s", new Gson().toJson(response.body()));

                        noCommands.setVisibility(View.GONE);
                        commentRec.setVisibility(View.VISIBLE);

                        if (response.body().getIsUserCanComment()) {
                            sendCommentLay.setVisibility(View.VISIBLE);
                            Timber.d("getcommand onResponse: true");
                        } else {
                            Timber.d("getcommand  onResponse: false");
                            sendCommentLay.setVisibility(View.INVISIBLE);
                        }

                        /*Bundle payload = new Bundle();
                        homeApiResponse.get(adapterPosition).setVideoCommentCounts(String.valueOf(response.body().getCommentcount()));
                        payload.putString("video_comment_count", String.valueOf(response.body().getCommentcount()));
                        videoAdapter.notifyItemChanged(adapterPosition, payload);*/

                            /*commentRec.clearOnScrollListeners();
                            commendAdapter.notifyDataSetChanged();
                            commentRec.smoothScrollToPosition(0);
                            commentRec.addOnScrollListener(videoScrollListener);*/


                        if (loadMoreCheckVideoCommand.equals("initial")) {
                            commendList.clear();
                            commendList.addAll(response.body().getResult());
                            swipeRefresh(false, videoId);
                            mSwipeVideoComment.setEnabled(false);
                            commentRec.addOnScrollListener(videoScrollListener);
                            commentCount.setText(response.body().getCommentcount() + " " + getResources().getString(R.string.comments));

                            txt_comment_count.setText(String.valueOf(response.body().getCommentcount()));

                        } else if (loadMoreCheckVideoCommand.equals("loadmore")) {
                            commendList.addAll(response.body().getResult());
                        }

                        commendAdapter.notifyDataSetChanged();


                    } else if (response.body().getStatus().equals(Constants.TAG_FALSE)) {
                        swipeRefresh(false, videoId);
                        mSwipeVideoComment.setEnabled(false);

                        if (response.body().getIsUserCanComment()) {
                            sendCommentLay.setVisibility(View.VISIBLE);
                            Timber.d("getcommand onResponse: true");
                        } else {
                            Timber.d("getcommand  onResponse: false");
                            sendCommentLay.setVisibility(View.INVISIBLE);
                        }

                        if (commendList.size() == 0) {
                            noCommands.setVisibility(View.VISIBLE);
                            commentRec.setVisibility(View.GONE);
                        }
                    }

                }

                @Override
                public void onFailure(@NotNull Call<VideoCommentResponse> call, @NotNull Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }

    }

    public void postComments(final String comments, String videoId) {

        Tagged tagged = FollowingVideoFragment.getTagged(mentionsCache, mCommentsEditText.getMentions());
        Timber.d("Mentions: ids=%s names=%s", tagged.getCommaSeparatedTaggedIdList(),
                tagged.getCommaSeparatedTaggedNameList());
                    /*tagged_user_id = getTaggedUserIds(mentionsCache, mCommentsEditText.getMentions());
                    tagged_user_name = getTaggedUserNames(mentionsCache, mCommentsEditText.getMentions());*/

        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("video_id", videoId);
        map.put("tagged_user_id", tagged.getCommaSeparatedTaggedNameList());
        map.put("tagged_id", tagged.getCommaSeparatedTaggedIdList());
        map.put("token", GetSet.getAuthToken());
        map.put("comment", comments);

        Timber.d("postComments: params %s", map);

        loadMoreCheckVideoCommand = "initial";
        mSwipeVideoComment.setEnabled(true);
        mSwipeVideoComment.setRefreshing(true);
        VideoCommentSentDialog.dismiss();

        Call<Map<String, String>> call3 = apiInterface.postComments(map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                try {

                    Map<String, String> data = response.body();

                    Timber.d("onResponse: %s", new Gson().toJson(response.body()));

                    if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {


                        /*list_tagged_user_ids.clear();
                        list_tagged_user_names.clear();
                        tagcomment = "";
                        tagcomment = "";
                        tagged_user_name = "";
                        tagged_user_id = "";

                        lastOffsetVideo = 0;
                        currentPage = 0;

                        *//*Bundle payload = new Bundle();
                        homeApiResponse.get(adapterPosition).setVideoCommentCounts(data.get("commentcount"));
                        payload.putString("video_comment_count", data.get("commentcount"));
                        videoAdapter.notifyItemChanged(adapterPosition, payload);*//*


                        getCommand(currentPage, videoId);

                        commentRec.clearOnScrollListeners();
                        commendAdapter.notifyDataSetChanged();
                        commentRec.smoothScrollToPosition(0);
                        commentRec.addOnScrollListener(videoScrollListener);*/


                        list_tagged_user_ids.clear();
                        list_tagged_user_names.clear();
                        tagcomment = "";
                        tagcomment = "";
                        tagged_user_name = "";
                        tagged_user_id = "";

                        lastOffsetVideo = 0;
                        currentPage = 0;

                        /*Bundle payload = new Bundle();
                        homeApiResponse.get(adapterPosition).setVideoCommentCounts(data.get("commentcount"));
                        payload.putString("video_comment_count", data.get("commentcount"));
                        videoAdapter.notifyItemChanged(adapterPosition, payload);*/

                        commentRec.clearOnScrollListeners();
                        commentRec.scrollToPosition(0);
                        loadMoreCheckVideoCommand = "initial";
                        getCommand(0, videoId);

                        commentCount.setText(data.get("commentcount") + " " + getResources().getString(R.string.comments));
                        commentCount.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));

                        txt_comment_count.setText(data.get("commentcount"));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Timber.d("onFailure: %s", "response- failure");
            }
        });

    }

    private void bindView(SingleVideoResponse.Result result) {


        if (GetSet.getUserId().equals(result.getPublisherId())) {
            profileFollowIcon.setVisibility(View.GONE);
        } else {

            if (result.getFollowedUser()) // If API come true that user already follow
                profileFollowIcon.setVisibility(View.GONE);
            else
                profileFollowIcon.setVisibility(VISIBLE);
        }


        Glide.with(getApplicationContext())
                .load(result.getPublisherImage())

                .placeholder(R.drawable.default_profile_image)
                .into(profileImage);

        /*Glide.with(getApplicationContext())
                .load(result.getStreamThumbnail())

                .into(mediaCoverImage);*/


        txt_title.setText(String.format("@%s", result.getPostedBy()));

        txt_heart_count.setText(result.getLikes());
        txt_gift_count.setText(result.getVideoGiftCounts());
        txt_comment_count.setText(result.getVideoCommentCounts());
        viewCount.setText(result.getVideo_views_count());
        voteCount.setText(result.getVotes_count());
        publisherVoteCount.setText(result.getLifetimeVoteCount());


        String getHash = result.getVideoDescription();
        if(getHash!=null){
            SpannableString spannable = new SpannableString(getHash);
            final Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(getHash);

            while (matcher.find()) {
                final String getHashTagName = matcher.group(1);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {

                        //exoplayerRecyclerView.setPlayControl(false);

                        App.preventMultipleClick(txt_description);
                        playerpause(false);
                        Intent intent = new Intent(SingleVideoActivity.this, HashTagActivity.class);
                        intent.putExtra(Constants.TAG_SELECT_HASH_TAG, getHashTagName);
                        startActivityForResult(intent, Constants.SINGLE_ACTIVITY);

                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                        ds.setColor(getResources().getColor(R.color.colorWhite));
                    }
                };
                int hashIndex = getHash.indexOf(getHashTagName) - 1;
                spannable.setSpan(clickableSpan, hashIndex, hashIndex + getHashTagName.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            txt_description.setText(spannable);
            txt_description.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }


    private void progressBar() {
        runnable = () -> {
            if (videoSurfaceView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration()), true);
                } else
                    progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration()));
            }
            mExecutorService.schedule(runnable, 100, TimeUnit.MILLISECONDS);
        };

        mExecutorService.execute(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer != null) videoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //  unregisterNetworkReceiver();
    }

    public enum PlayState {
        ON, OFF
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
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
        Timber.d("loadGifts: %s", new Gson().toJson(tempGiftList));
        stickerLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();


        txtSend.setOnClickListener(view -> {


            int abc = Integer.parseInt((txt_gift_count.getText().toString()));
            txt_gift_count.setText(String.valueOf(abc + 1));


            giftDialog.dismiss();

            giftLay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_zoom_in));

            /*ObjectAnimator
                    .ofFloat(giftLay, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                    .setDuration(2000)
                    .start();*/

            Glide.with(getApplicationContext())
                    .load(tempGiftList.get(giftPosition).getGiftIcon())
                    .into(giftImageView);

            sendGift();

            sendLay.setVisibility(View.GONE);
            txtAttachmentName.setText("");

        });

    }


    public void sendGift() {


        HashMap<String, String> map = new HashMap<>();

        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_GIFT_ID, AdminData.giftList.get(giftPosition).getGiftId());
        map.put(Constants.TAG_VIDEO_ID, String.valueOf(singleVideoResponse.getResult().get(0).getVideoId()));

        Timber.d("sendReport: %s", App.getGsonPrettyInstance().toJson(map));

        Call<Map<String, String>> call = apiInterface.sendGift(map);


        call.enqueue(new Callback<Map<String, String>>() {

            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                Timber.d("onResponse: %s", response.body());


                if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    Timber.d("onResponse: %s", response.body());

                    // Update homeforyouvideo and homefollowingvideo page counts
                    EventBus.getDefault().post(new ForYouUpdateGiftMessageCount(singleVideoResponse.getResult().get(0).getVideoId(),
                            "gift",
                            response.body().get("giftcount"),
                            response.body().get("video_vote_count"),
                            response.body().get("publisher_vote_count")));

                    EventBus.getDefault().post(new FollowingUpdateGiftMessageCount(singleVideoResponse.getResult().get(0).getVideoId(),
                            "gift",
                            response.body().get("giftcount"),
                            response.body().get("video_vote_count"),
                            response.body().get("publisher_vote_count")));

                    txt_gift_count.setText(response.body().get("giftcount"));

                    giftImageView.setImageResource(R.drawable.gift_w);

                    giftLay.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));


                }
            }

            @Override

            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        DisplayMetrics displayMetrics = Objects.requireNonNull(SingleVideoActivity.this).getResources().getDisplayMetrics();
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;
    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("NonConstantResourceId")
    public class GiftAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private List<Gift> giftList;
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public GiftAdapter(Context context, List<Gift> giftList) {

            this.context = context;
            this.giftList = giftList;

        }

        @Override
        public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_gifts, parent, false);
                viewHolder = new GiftAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new GiftAdapter.FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {

            final Gift gift = giftList.get(position);


            Timber.d("onBindViewHolder: %s", App.getGsonPrettyInstance().toJson(gift));

            if (holder instanceof GiftAdapter.MyViewHolder) {

                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(VISIBLE);
                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(View.INVISIBLE);

                Glide.with(getApplicationContext())
                        .load(gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setImageDrawable(context.getDrawable(R.drawable.gift));
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setImageDrawable(resource);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(VISIBLE);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.gift))
                        .into(((GiftAdapter.MyViewHolder) holder).giftImage);


                ((GiftAdapter.MyViewHolder) holder).txtGiftPrice.setText((GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" + gift.getGiftGems());


            } else if (holder instanceof GiftAdapter.FooterViewHolder) {
                GiftAdapter.FooterViewHolder footerHolder = (GiftAdapter.FooterViewHolder) holder;
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
            @BindView(R.id.giftImage)
            ImageView giftImage;
            @BindView(R.id.txtGiftPrice)
            TextView txtGiftPrice;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;
            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                ViewGroup.LayoutParams params = itemLay.getLayoutParams();
                // Changes the height and width to the specified *pixels*
                params.width = displayWidth / 4;
                itemLay.setLayoutParams(params);
            }


            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                if ((GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                        GetSet.getGems() >= giftList.get(getAdapterPosition()).getGiftGemsPrime()) ||
                        (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                                GetSet.getGems() >= giftList.get(getAdapterPosition()).getGiftGems())) {
                    giftPosition = getAdapterPosition();
                    sendLay.setVisibility(VISIBLE);
                    txtSend.setVisibility(VISIBLE);
                    txtAttachmentName.setText(giftList.get(getAdapterPosition()).getGiftTitle());
                } else {
                    App.makeToast(getString(R.string.not_enough_gems));
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

        Context mContext;
        ArrayList<VideoCommentResponse.Result> commendList;
        String getVideoId, publisherId;
        TimeAgo timeAgo;

        public CommentAdapter(Context mContext, ArrayList<VideoCommentResponse.Result> commendList, String publisherId, String videoId) {
            this.mContext = mContext;
            this.commendList = commendList;
            this.getVideoId = videoId;
            /*this.getPosition = adapterPosition;*/
            this.publisherId = publisherId;
            timeAgo = new TimeAgo(mContext);
        }

        @NonNull
        @Override
        public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new CommentAdapter.CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_comment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {


            Glide.with(mContext)
                    .load(commendList.get(position).getUserImage())
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_profile_image)

                    .into(holder.profileImage);

            holder.userName.setText(commendList.get(position).getName());

            String timeText = timeAgo.timeAgo(AppUtils.utcToMillis(commendList.get(position).getCommentPostedTime()));
            String getHash = commendList.get(position).getComment();

            holder.userComment.setText(getHash + "\t" + " " + timeText);
            VideoCommentResponse.Result data = getItem(position);
            final List<Pair<String, View.OnClickListener>> tagsList = new ArrayList<>();
            for (int i = 0; i < data.getTaggedUser().size(); i++) {
                String tagItem = data.getTaggedUser().get(i);
                if (!TextUtils.isEmpty(tagItem)) {
                    tagsList.add(Pair.create("@" + tagItem, v -> {
                        Intent profile = new Intent(SingleVideoActivity.this, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_NAME, tagItem);
                        profile.putExtra(Constants.TAG_USER_IMAGE, "");
                        startActivityForResult(profile, Constants.COMMENT_MUSIC_REQUEST_CODE);
                    }));
                }
            }
            if (tagsList.size() > 0) {
                holder.userComment.makeLinks(tagsList);
            }


            if (commendList.get(position).getUserId().equals(GetSet.getUserId()) || publisherId.equals(GetSet.getUserId()))
                holder.deleteIcon.setVisibility(View.VISIBLE);
            else holder.deleteIcon.setVisibility(View.GONE);

        }

        private VideoCommentResponse.Result getItem(int position) {
            return commendList.get(position);
        }

        @Override
        public int getItemCount() {
            return commendList.size();
        }

        public class CommentViewHolder extends RecyclerView.ViewHolder {

            RoundedImageView profileImage;
            ImageView deleteIcon;
            TextView userName;
            CustomSpannableTextView userComment;

            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);

                profileImage = itemView.findViewById(R.id.profileImage);
                deleteIcon = itemView.findViewById(R.id.deleteIcon);
                userName = itemView.findViewById(R.id.userName);
                userComment = itemView.findViewById(R.id.userComment);

                deleteIcon.setOnClickListener(v -> {

                    Map<String, String> map = new HashMap<>();
                    map.put("user_id", GetSet.getUserId());
                    map.put("token", GetSet.getAuthToken());
                    map.put("video_id", getVideoId);
                    map.put("comment_id", commendList.get(getAdapterPosition()).getCommentId());


                    Timber.d("delete Comments: params %s", map);

                    loadMoreCheckVideoCommand = "initial";
                    mSwipeVideoComment.setEnabled(true);
                    mSwipeVideoComment.setRefreshing(true);

                    Call<Map<String, String>> call3 = apiInterface.deleteComment(map);
                    call3.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                            try {

                                /*Map<String, String> data = response.body();

                                Timber.d("delete onResponse: %s", new Gson().toJson(response.body()));

                                if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {

                                    lastOffsetVideo = 0;
                                    currentPage = 0;


                                    getCommand(currentPage, getVideoId);


                                    commentRec.clearOnScrollListeners();
                                    commentRec.addOnScrollListener(videoScrollListener);
                                    commendAdapter.notifyDataSetChanged();
                                    commentRec.smoothScrollToPosition(0);*/

                                Map<String, String> data = response.body();

                                Timber.d("delete onResponse: %s", new Gson().toJson(response.body()));

                                if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {


                                    commendList.remove(getAdapterPosition());
                                    /*commendAdapter.notifyItemRemoved(getAdapterPosition());*/
                                    commendAdapter.notifyItemRangeRemoved(getAdapterPosition(), 1);

                                    mSwipeVideoComment.setEnabled(false);
                                    mSwipeVideoComment.setRefreshing(false);

                                    commentCount.setText(commendList.size() + " " + getResources().getString(R.string.comments));
                                    commentCount.startAnimation(AnimationUtils.loadAnimation(SingleVideoActivity.this, R.anim.anim_zoom_out));

                                    txt_comment_count.setText(String.valueOf(commendList.size()));

                                        /*Bundle payload = new Bundle();
                                        homeApiResponse.get(getPosition).setVideoCommentCounts(String.valueOf(commendList.size()));
                                        payload.putString("video_comment_count", String.valueOf(commendList.size()));
                                        videoAdapter.notifyItemChanged(getPosition, payload);
*/
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            call.cancel();
                            Timber.d("onFailure: %s", "response- failure");
                        }
                    });

                });

                profileImage.setOnClickListener(v -> {
                    playerpause(false);
                    Intent profile;
                    if (commendList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        profile = new Intent(SingleVideoActivity.this, MyProfileActivity.class);
                    } else {
                        profile = new Intent(SingleVideoActivity.this, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, commendList.get(getAdapterPosition()).getUserImage());
                    }
                    startActivity(profile);
                });

                userName.setOnClickListener(v -> {
                    playerpause(false);
                    Intent profile;
                    if (commendList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        profile = new Intent(SingleVideoActivity.this, MyProfileActivity.class);
                        startActivity(profile);
                    } else {
                        profile = new Intent(SingleVideoActivity.this, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, commendList.get(getAdapterPosition()).getUserImage());
                    }
                    startActivity(profile);
                });
            }
        }
    }


    class CommentUserAdapter extends RecyclerView.Adapter<CommentUserAdapter.MyViewHolder> {

        ArrayList<UserList.Result> Items;


        public CommentUserAdapter(ArrayList<UserList.Result> liveUser) {
            this.Items = liveUser;
        }

        @Override
        public CommentUserAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_username, parent, false);
            return new CommentUserAdapter.MyViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(final CommentUserAdapter.MyViewHolder holder, int position) {
            try {
                final UserList.Result tempMap = Items.get(position);


                Glide.with(SingleVideoActivity.this).load(tempMap.getUserImage())
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().placeholder(R.drawable.default_profile_image).error(R.drawable.default_profile_image))
                        .into(holder.img);

                holder.user_name.setText(tempMap.getName());

                holder.user_name.setOnClickListener(v -> {
                    tagging = false;
                    char first;
                    String cmt = mCommentsEditText.getText().toString().trim();
                    String[] cmt_arr = cmt.split(" ");
                    if (cmt_arr.length > 1) {
                        cmt = cmt_arr[cmt_arr.length - 1];
                        first = cmt.charAt(0);
                    } else {
                        first = cmt.charAt(0);
                    }
                    if (first == '@' && cmt_arr.length == 1) {
                        /*String pos = "@" + holder.user_name.getText().toString().trim() + " ";*/
                        String pos = "@" + tempMap.getUserName().trim() + " ";

                        if (tagcomment != null && !tagcomment.equals("")) {
                            /*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*/
                            tagcomment += " " + "@" + tempMap.getUserName().trim() + " ";
                            ;

                        } else {
                            /*tagcomment += "@" + holder.user_name.getText().toString().trim();*/
                            tagcomment += "@" + tempMap.getUserName().trim() + " ";
                            ;

                        }
                        mCommentsEditText.setText(pos);
                        mCommentsEditText.setSelection(pos.length());

                    } else if (first == '@' && cmt_arr.length > 1) {
                        /*String pos = "@" + holder.user_name.getText().toString() + " ";*/
                        String pos = "@" + tempMap.getUserName().trim() + " ";
                        switch (cmt_arr.length) {
                            case 1:
                                if (!cmt_arr[0].equals(pos)) {
                                    pos = cmt_arr[0] + " " + pos;
                                } else {
                                    pos = cmt_arr[0];
                                }
                                break;
                            case 2:
                                String cmp_20 = cmt_arr[0].replaceAll("@", "").replace(" ", "");
                                String cmp2 = pos.replaceAll("@", "").replace(" ", "");
                                if (!cmp_20.contains(cmp2)) {
                                    pos = cmt_arr[0] + " " + pos;
                                } else {
                                    pos = cmt_arr[0] + " ";
                                }
                                break;
                            case 3:
                                String cmp_30 = cmt_arr[0].replaceAll("@", "").replace(" ", "");
                                String cmp_31 = cmt_arr[1].replaceAll("@", "").replace(" ", "");
                                String cmp3 = pos.replaceAll("@", "").replace(" ", "");
                                if (!cmp_30.equals(cmp3) && !cmp_31.equals(cmp3)) {
                                    pos = cmt_arr[0] + " " + cmt_arr[1] + " " + pos;
                                } else {
                                    pos = cmt_arr[0] + " " + cmt_arr[1];
                                }
                                break;
                            case 4:
                                String cmp_40 = cmt_arr[0].replaceAll("@", "").replace(" ", "");
                                String cmp_41 = cmt_arr[1].replaceAll("@", "").replace(" ", "");
                                String cmp_42 = cmt_arr[2].replaceAll("@", "").replace(" ", "");
                                String cmp4 = pos.replaceAll("@", "").replace(" ", "");
                                if (!cmp_40.equals(cmp4) && !cmp_41.equals(cmp4) && !cmp_42.equals(cmp4)) {
                                    pos = cmt_arr[0] + " " + cmt_arr[1] + " " + cmt_arr[2] + " " + pos;
                                } else {
                                    pos = cmt_arr[0] + " " + cmt_arr[1] + " " + cmt_arr[2];
                                }
                                break;
                            case 5:
                                String cmp_50 = cmt_arr[0].replaceAll("@", "").replace(" ", "");
                                String cmp_51 = cmt_arr[1].replaceAll("@", "").replace(" ", "");
                                String cmp_52 = cmt_arr[2].replaceAll("@", "").replace(" ", "");
                                String cmp_53 = cmt_arr[3].replaceAll("@", "").replace(" ", "");
                                String cmp5 = pos.replaceAll("@", "").replace(" ", "");
                                if (!cmp_50.equals(cmp5) && !cmp_51.equals(cmp5) && !cmp_52.equals(cmp5) && !cmp_53.equals(cmp5)) {
                                    pos = cmt_arr[0] + " " + cmt_arr[1] + " " + cmt_arr[2] + " " + cmt_arr[3] + " " + pos;
                                } else {
                                    pos = cmt_arr[0] + " " + cmt_arr[1] + " " + cmt_arr[2] + " " + cmt_arr[3];
                                }
                                break;
                        }
//                            pos = cmt_arr[0]+" "+ pos;
                        if (tagcomment != null && !tagcomment.equals("")) {
                            /*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*/
                            tagcomment += " " + "@" + tempMap.getUserName().trim();

                        } else {
                            /*tagcomment += "@" + holder.user_name.getText().toString().trim();*/
                            tagcomment += "@" + tempMap.getUserName().trim();

                        }
                        mCommentsEditText.setText(pos);
                        mCommentsEditText.setSelection(pos.length());
                    } else {
                        String[] cmtAry = cmt.split(" @");

                        /*String pos = cmtAry[0] + " @" + holder.user_name.getText().toString().trim() + " ";*/
                        String pos = cmtAry[0] + " @" + tempMap.getUserName().trim() + " ";


                        if (tagcomment != null && !tagcomment.equals("")) {
                            /*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*/
                            tagcomment += " " + "@" + tempMap.getUserName().trim();

                        } else {
                            /*tagcomment += "@" + holder.user_name.getText().toString().trim();*/
                            tagcomment += "@" + tempMap.getUserName().trim();

                        }
                        mCommentsEditText.setText(pos);
                        mCommentsEditText.setSelection(pos.length());
                    }
                    tagged_user_name = tempMap.getUserName();
                    list_tagged_user_names.add(tagged_user_name);

                    tagged_user_id = tempMap.getUserId();
                    list_tagged_user_ids.add(tagged_user_id);

                    Timber.d("onBindViewHolder: %s", list_tagged_user_names);

                    StringBuilder strBuilder = new StringBuilder();
                    String comma = "";
                    if (list_tagged_user_names.size() > 0) {
                        for (int i = 0; i < list_tagged_user_names.size(); i++) {
                            strBuilder.append(comma);
                            comma = ",";
                            strBuilder.append(list_tagged_user_names.get(i));

                        }

                    }

                    tagged_user_name = strBuilder.toString();


                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView user_name;

            public MyViewHolder(View view) {
                super(view);

                img = itemView.findViewById(R.id.profileImage);
                user_name = itemView.findViewById(R.id.userName);
            }
        }
    }

    public void getAllLiveusers(String name) {

        String[] name_arr = name.split(" ");
        if (name_arr.length > 1) {
            name = name_arr[1];
        } else {
            name = name;
        }
        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("token", GetSet.getAuthToken());
        if (name.length() > 0) {
            map.put("search_key", name.substring(1));
        }

        Timber.d("getAllLiveusers: %s", map);

        Call<UserList> call3 = apiInterface.getUsers(map);
        call3.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                UserList data = response.body();

                Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(data));

                try {
                    tagging = true;
                    assert data != null;
                    if (data.getStatus().equals("true")) {
                        if (!data.getResult().isEmpty()) {
                            liveUser.clear();
                            liveUser.addAll(data.getResult());
                        } else {
                            liveUser.clear();
                        }
                    }

                    if (liveUser.size() == 0 || mCommentsEditText.getText().toString().isEmpty()) {
                        commentUserAdapter.notifyDataSetChanged();
                        userRecylerView.setVisibility(View.GONE);
                    } else {
                        commentUserAdapter.notifyDataSetChanged();
                        userRecylerView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    liveUser.clear();
                    userRecylerView.setVisibility(View.GONE);
                    commentUserAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable t) {
                if (liveUser.size() == 0) {
                    userRecylerView.setVisibility(View.GONE);
                }
                call.cancel();

                Timber.d("onFailure: response- failure");

            }
        });
    }

    public class ReportAdapter extends RecyclerView.Adapter {
        private final Context context;
        private List<Report> reportList;
        private RecyclerView.ViewHolder viewHolder;

        private int selectedPosition = -1;// no selection by default

        public ReportAdapter(Context context, List<Report> reportList) {
            this.context = context;
            this.reportList = reportList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_report, parent, false);
            viewHolder = new ReportAdapter.MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ReportAdapter.MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());

            ((ReportAdapter.MyViewHolder) holder).txtReport.setOnClickListener(v -> {

                getSelectedReport = ((ReportAdapter.MyViewHolder) holder).txtReport.getText().toString();

                selectedPosition = position;
                notifyDataSetChanged();
            });

            ((ReportAdapter.MyViewHolder) holder).radioButton.setOnClickListener(v -> {
                selectedPosition = position;
                getSelectedReport = ((ReportAdapter.MyViewHolder) holder).txtReport.getText().toString();
                notifyDataSetChanged();
            });

            ((ReportAdapter.MyViewHolder) holder).radioButton.setChecked(selectedPosition == position);
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        @SuppressLint("NonConstantResourceId")
        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txtReport)
            TextView txtReport;
            @BindView(R.id.itemReportLay)
            RelativeLayout itemReportLay;
            @BindView(R.id.radioButton)
            RadioButton radioButton;


            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

        }
    }

    public void getLiveUsersForQuery(
            @NonNull String q,
            @NonNull NetworkResultCallback<List<UserList.Result>> callback
    ) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("token", GetSet.getAuthToken());
            map.put("search_key", q);

            Call<UserList> call = apiInterface.getUsers(map);
            call.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Success
                        if (response.body().getResult() != null)
                            callback.onResult(response.body().getResult());
                    } else {
                        onFailure(call, new Exception(String.format(Locale.ENGLISH, "Unexpected response %d", response.code())));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable t) {
                    call.cancel();
                    // Handle err
                    Timber.e(t);
                }
            });
        }
    }

    private void getReplacedText(EditText editText, String selected, int lastCursorPosition) {
        String toString = editText.getText().toString();
        StringBuilder builder = new StringBuilder(toString);
        int end = lastCursorPosition;
        int start = -1;
        lastCursorPosition--;
        while (lastCursorPosition >= 0) {
            char c = toString.charAt(lastCursorPosition);
            if (c == '@') {
                if (start == -1) start = lastCursorPosition;
                break;
            } else {
                start = lastCursorPosition;
                lastCursorPosition--;
            }
        }
        builder.replace(start, end, selected);
        editText.setText(builder.toString());
        editText.setSelection(builder.toString().length());
    }

}