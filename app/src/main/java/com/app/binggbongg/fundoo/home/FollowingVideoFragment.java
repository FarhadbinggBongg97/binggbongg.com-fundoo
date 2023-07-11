package com.app.binggbongg.fundoo.home;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.view.View.GONE;

import static com.app.binggbongg.R2.id.publisher_vote_count;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.provider.Telephony;
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
import android.view.animation.Animation;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.app.binggbongg.fundoo.FollowersActivity;
import com.app.binggbongg.fundoo.GemsStoreActivity;
import com.app.binggbongg.fundoo.PrivacyActivity;
import com.app.binggbongg.fundoo.home.eventbus.AutoScrollEnabled;
import com.app.binggbongg.fundoo.home.eventbus.FollowAutoScrollEnabled;
import com.app.binggbongg.fundoo.home.eventbus.FollowingHideIcon;
import com.app.binggbongg.fundoo.home.eventbus.ForYouUpdateGiftMessageCount;
import com.app.binggbongg.fundoo.home.eventbus.ForyouHideIcon;
import com.app.binggbongg.fundoo.home.eventbus.HideIcon;
import com.app.binggbongg.fundoo.home.eventbus.UserBlocked;
import com.app.binggbongg.fundoo.home.eventbus.VideoAutoScrollEnabled;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hendraanggrian.appcompat.widget.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.app.binggbongg.R;
import com.app.binggbongg.data.model.Mention2;
import com.app.binggbongg.data.model.Tagged;
import com.app.binggbongg.external.CustomEditText;
import com.app.binggbongg.external.TimeAgo;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.ConvertGiftActivity;
import com.app.binggbongg.fundoo.HashTagActivity;
import com.app.binggbongg.fundoo.SoundTrackActivity;
import com.app.binggbongg.fundoo.home.eventbus.FollowingFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.FollowingSwipePrevent;
import com.app.binggbongg.fundoo.home.eventbus.FollowingUpdateGiftMessageCount;
import com.app.binggbongg.fundoo.home.eventbus.FollowingVideoFav;
import com.app.binggbongg.fundoo.home.eventbus.FollowingVideoLike;
import com.app.binggbongg.fundoo.home.eventbus.ForYouVideoLike;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.CustomLinearLayoutManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.OnVerticalScrollListener;
import com.app.binggbongg.helper.callback.NetworkResultCallback;
import com.app.binggbongg.helper.callback.ProfileImageClickListener;
import com.app.binggbongg.model.FollowRequest;
import com.app.binggbongg.model.FollowingHomeResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Gift;
import com.app.binggbongg.model.Report;
import com.app.binggbongg.model.Request.HomeRequest;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class FollowingVideoFragment extends Fragment {

    public static String TAG = FollowingVideoFragment.class.getSimpleName();
    String contest_text = "";

    TextView btm_hide_menu_tv, btm_hide_yes, btm_hide_no;

    private ForYouVideoFragment.onHideBottomBarEventListener btmBarEventListener;

    boolean isBottomBarHide = true;
    private boolean isAutoScroll;
    private int finalGiftPosition;
    private int ITEM_LIMIT = 8;


    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            btmBarEventListener = (ForYouVideoFragment.onHideBottomBarEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    public static final int LOAD_MORE_MAX_LIMIT = 10;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiInterface apiInterface;
    private Context context;

    private TextView nullText;
    private RelativeLayout nullLay;

    // Video Page
    ArrayList<FollowingHomeResponse.Result> homeApiResponse = new ArrayList<>();

    public FollowingExoPlayerRecyclerView followingExoplayerRecyclerView;
    private final HashSet<String> mentions = new HashSet();
    private VideoAdapter videoAdapter;
    private ReportAdapter reportAdapter;
    private RecyclerView reportsView;
    private View bottom_sheet_longpress;

    private ImageView btnBack;
    private TextView txtTitle;
    private MaterialButton btnReport;
    private String getSelectedReport = "";
    Boolean isShowLoadMore = false;


    // Bottomsheet
    public static BottomSheetDialog reportDialog, bottomSheetLongPressDi, videoCommandBottomDialog, VideoCommentSentDialog,
            giftDialog, shareDialog, hideBottomBarBS;
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

    private int SelectedVideoPosition = -1;
    private Boolean likePrevent = false;
    private final ArrayList<Mention2> mentionsCache = new ArrayList<>();
    private final boolean isLoading = true;

    //VideoComment
    TextView commentCount, addComment, sendComment, noCommands;
    SocialAutoCompleteTextView mCommentsEditText;
    ImageView closeButton;
    RecyclerView commentRec, userRecylerView;
    CommentAdapter commendAdapter;
    CommentUserAdapter commentUserAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<VideoCommentResponse.Result> commendList = new ArrayList<>();
    RelativeLayout progress, CommandLay, sendCommentLay, rootView;
    FrameLayout headerBottom;
    private SwipeRefreshLayout mSwipeVideoComment = null;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    // TODO: 23/10/21 @VishnuKumar
    public CustomLinearLayoutManager customLinearLayoutManager;
    String loadMoreCheckVideoCommand = "initial";
    private int lastOffsetVideo = 0;

    ArrayList<UserList.Result> liveUser = new ArrayList<>();

    private boolean firstTime = true;
    private int lastOffset = -1;
    String loadMoreCheck = "initial";
    OnVerticalScrollListener videoScrollListener;
    // Touchapp
    boolean tagging = true;
    String beforeTextChanged;

    String tagcomment = "", tagged_user_name = "", tagged_user_id = "";
    public ArrayList<String> list_tagged_user_names = new ArrayList<>();
    public ArrayList<String> list_tagged_user_ids = new ArrayList<>();


    // used for cache
    private MediaLoader mMediaLoader;

    ProfileImageClickListener profileImageClickListener;

    TextView tvPurchaseVote,tvNumberOfVote;

    public FollowingVideoFragment() {
        // Required empty public constructor
    }

    public static FollowingVideoFragment newInstance(ProfileImageClickListener mProfileImageClickListener) {

        FollowingVideoFragment followingVideoFragment = new FollowingVideoFragment();

        followingVideoFragment.profileImageClickListener = mProfileImageClickListener;
        return followingVideoFragment;
    }


    // update follow icon
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingFollowFollowing event) {
        Bundle payload = new Bundle();
        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getPublisherId().equals(event.PublisherId)) {
                if (homeApiResponse.get(i).getFollowedUser()) {
                    homeApiResponse.get(i).setFollowedUser(false);
                    payload.putString("follow", "false");
                } else {
                    homeApiResponse.get(i).setFollowedUser(true);
                    payload.putString("follow", "true");
                }
                // EventBus.getDefault().post(new FollowingProfileUpdate(event.PublisherId));
                videoAdapter.notifyItemChanged(i, payload);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UserBlocked event) {
        Bundle payload = new Bundle();
       /* for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getPublisherId().equals(event.PublisherId)) {
                if (homeApiResponse.get(i).getFollowedUser()) {
                    homeApiResponse.get(i).setFollowedUser(false);
                    payload.putString("follow", "false");
                } else {
                    homeApiResponse.get(i).setFollowedUser(true);
                    payload.putString("follow", "true");
                }
                // EventBus.getDefault().post(new FollowingProfileUpdate(event.PublisherId));
                videoAdapter.notifyItemChanged(i, payload);
            }
        }*/
        videoAdapter.notifyDataSetChanged();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowAutoScrollEnabled event) {
        isAutoScroll = event.isEnabled;
        videoAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingHideIcon event) {
        Log.e(TAG, "onMessageEvent: :::::::::::" + event.iconVisible);
        Bundle payload = new Bundle();
        isBottomBarHide = event.iconVisible;
        hide_btm_bar(isBottomBarHide);
        videoAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingVideoLike event) {
        Timber.d("FollowingVideoLike => %s", new Gson().toJson(event));
        Bundle payload = new Bundle();
        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getVideoId().equals(event.VideoId)) {
                if (event.LikeStatus.equals("unliked")) {
                    homeApiResponse.get(i).setIsLiked(false);
                    payload.putString("likes_status", "false");

                } else {
                    homeApiResponse.get(i).setIsLiked(true);
                    payload.putString("likes_status", "true");

                }
                payload.putString("likes_count", (event.LikeCount));
                homeApiResponse.get(i).setLikes(event.LikeCount);
                videoAdapter.notifyItemChanged(i, payload);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingUpdateGiftMessageCount event) {
        Timber.d("onMessageEvent: %s", event);

        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getVideoId().equals(event.VideoId)) {

                Bundle payload = new Bundle();
                if (event.giftOrMessage.equals("gift")) {
                    payload.putString("update_gift_count", event.Count);
                    payload.putString("vote_count", event.vote_count);
                    payload.putString("publisher_vote_count", event.publisher_vote);
                    homeApiResponse.get(i).setVotes_count(event.vote_count);
                    homeApiResponse.get(i).setPublisher_vote_count(event.publisher_vote);
                    homeApiResponse.get(i).setVideoGiftCounts(event.Count);

                } else if (event.giftOrMessage.equals("message")) {
                    payload.putString("update_video_comment_count", event.Count);
                    homeApiResponse.get(i).setVideoGiftCounts(event.Count);
                }
                videoAdapter.notifyItemChanged(i, payload);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingVideoFav event) {
        Timber.d("onMessageEvent: %s", new Gson().toJson(event));

        if (!event.isFav.equals("Sound")) {
            for (int i = 0; i < homeApiResponse.size(); i++) {
                if (homeApiResponse.get(i).getVideoId().equals(event.videoId)) {
                    Bundle payload = new Bundle();
                    if (event.isFav.equals("Report"))
                        homeApiResponse.get(i).setIsVideoReported(event.status);
                    else
                        homeApiResponse.get(i).setVideoIsFavorite(event.status);

                    payload.putString(event.isFav, String.valueOf(event.status));
                    videoAdapter.notifyItemChanged(i, payload);
                }
            }
        } else if (event.isFav.equals("Sound")) {
            for (int i = 0; i < homeApiResponse.size(); i++) {
                //Here videoId means soundId
                if (homeApiResponse.get(i).getSoundtracks().getSoundId().equals(event.videoId)) {
                    Bundle payload = new Bundle();
                    homeApiResponse.get(i).getSoundtracks().setFavorite(event.status);
                    payload.putString(event.isFav, String.valueOf(event.status));
                    videoAdapter.notifyItemChanged(i, payload);
                }
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        DisplayMetrics displayMetrics = requireContext().getResources().getDisplayMetrics();
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;


        // firstPositionVideo();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_following_video, container, false);


        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        mMediaLoader = MediaLoader.getInstance(getContext());
        isAutoScroll = SharedPref.getBoolean(SharedPref.isAUTO_SCROLL, false);

        initView(rootView);
//        hide_btm_bar(SharedPref.getBoolean(SharedPref.HIDE_ICONS, false));
        Log.e(TAG, "onMessageEvent: ::::::BTm:::::" + isBottomBarHide);
        return rootView;
    }

    public static Tagged getTagged(ArrayList<Mention2> mentionsCache, List<String> mentions) {
        Timber.d("getTagged feed: %s %s", mentionsCache, mentions);
        final Tagged tagged = new Tagged();

        for (String name : mentions) {
            Mention2 m = new Mention2("", name);
            Timber.d("Comparing: %s with %s", name, mentionsCache);
            if (mentionsCache.contains(m)) {
                int index = mentionsCache.indexOf(m);
                if (index >= 0 && index < mentionsCache.size()) {
                    final Mention2 m2 = mentionsCache.get(index);
                    tagged.addTaggedEntry(m2.getId(), m2.getUsername().toString());
                }
            }
        }
        return tagged;
    }


    private void firstPositionVideo(String type) {
        if (firstTime) {
            new Handler().postDelayed(() -> {
                followingExoplayerRecyclerView.playVideo(false);
                if (type.equals("play")) followingExoplayerRecyclerView.setPlayControl(true);
                else if (type.equals("pause")) followingExoplayerRecyclerView.setPlayControl(false);
            }, 1100);
            firstTime = false;
        }
    }

    private void initView(@NotNull View rootView) {

        followingExoplayerRecyclerView = rootView.findViewById(R.id.FollowingVideoRecyclerview);

        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        // Null Layout
        ImageView nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
        nullLay = rootView.findViewById(R.id.nullLay);
        nullText.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_video));


        nullLay.setVisibility(View.GONE);

        /*CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT);
        params.bottomMargin = AppUtils.dpToPx(context, HomeFragment.BOTTOM_MARGIN);
        nullLay.setGravity(Gravity.CENTER);
        nullLay.setLayoutParams(params);*/

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        //     swipeRefresh(true);


        swipeRefreshLayout.setOnRefreshListener(() -> {

            homeApiResponse.clear();
            videoAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(true);
            followingExoplayerRecyclerView.onPausePlayer();
            EventBus.getDefault().post(new FollowingSwipePrevent(true));
            firstTime = true;
            lastOffset = 0;
            getForYouDataFromApi(0, "play");

        });

        Log.e(TAG, "initView: :::::::::::::::::::" + (getArguments() != null ? getArguments().getBoolean("isShow") : false));
    }


    public void hideIcon(String videoId, int position){
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_hide_menu_bar, null);
        hideBottomBarBS = new BottomSheetDialog(getContext(), R.style.VideoCommentDialog); // Style here

        hideBottomBarBS.setContentView(sheetView);

        hideBottomBarBS.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        btm_hide_menu_tv = hideBottomBarBS.findViewById(R.id.alert_tv);
        btm_hide_yes = hideBottomBarBS.findViewById(R.id.hide_btm_bar_yes);
        btm_hide_no = hideBottomBarBS.findViewById(R.id.hide_btm_bar_no);

        if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
            btm_hide_menu_tv.setText(getContext().getString(R.string.hide_btm_bar_alert));
        } else {
            btm_hide_menu_tv.setText(getContext().getString(R.string.unhide_btm_bar_alert));
        }

        btm_hide_yes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Bundle payload = new Bundle();
                if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                   // btm_hide_menu_tv.setText(getContext().getString(R.string.hide_btm_bar_alert));
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, false);
                    isBottomBarHide = false;
                } else {
                   // btm_hide_menu_tv.setText(getContext().getString(R.string.unhide_btm_bar_alert));
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);
                    isBottomBarHide = true;
                }
                hide_btm_bar(isBottomBarHide);
                payload.putString("hide_icon", String.valueOf(isBottomBarHide));
                videoAdapter.notifyItemChanged(position, payload);
                EventBus.getDefault().post(new HideIcon(isBottomBarHide, videoId));
                EventBus.getDefault().post(new ForyouHideIcon(isBottomBarHide, videoId));
               // homeApiResponse.forEach(response -> response.setShow(isBottomBarHide));
                //videoAdapter.notifyDataSetChanged();
                Log.e(TAG, "onClick: ::::::::::::::" + isBottomBarHide);
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


    private void hide_btm_bar(boolean isBottomBarHide) {

        btmBarEventListener.onHideEvent(isBottomBarHide);
        if (hideBottomBarBS!= null && hideBottomBarBS.isShowing()) {
            hideBottomBarBS.dismiss();
        }

    }


    private void getForYouDataFromApi(int lastOffset, String type) {

        nullLay.setVisibility(View.GONE);

        if (NetworkReceiver.isConnected()) {

            if (lastOffset != 0) {
                videoAdapter.showLoader();
                Timber.d("Loading: ??");
            }

            HomeRequest homeRequest = new HomeRequest();

            homeRequest.setUserId(GetSet.getUserId());
            homeRequest.setToken(GetSet.getAuthToken());
            homeRequest.setType(Constants.TAG_FOLLOWING);
            homeRequest.setLimit(String.valueOf(LOAD_MORE_MAX_LIMIT));
            homeRequest.setOffset(String.valueOf(lastOffset));

            Timber.d("getFollowingDataFromApi: params %s", App.getGsonPrettyInstance().toJson(homeRequest));

            Call<FollowingHomeResponse> call = apiInterface.getFollowingHomeData(homeRequest);

            call.enqueue(new Callback<FollowingHomeResponse>() {
                @Override
                public void onResponse(@NotNull Call<FollowingHomeResponse> call, @NotNull Response<FollowingHomeResponse> response) {

                    if (response.code()==200&&response.body() != null && response.body().getStatus().equals(Constants.TAG_TRUE)) {

                        isAutoScroll = Boolean.parseBoolean(response.body().getScroll_enable());
                        contest_text = response.body().getContest_text();
                        videoAdapter.hideLoader();
                        nullLay.setVisibility(View.GONE);
                        Timber.d("getFollowingData Response: %s", new Gson().toJson(response.body()));


                        if (response.body().getResult() != null) {

                            isShowLoadMore = Boolean.valueOf(response.body().getNextloadmore());

                            Timber.i("Nextloadmore %s", isShowLoadMore);

                            if (lastOffset == 0) {
                                nullLay.setVisibility(View.GONE);
                                homeApiResponse.clear();
                                homeApiResponse.addAll(response.body().getResult());
                                swipeRefresh(false);
                                videoAdapter.notifyDataSetChanged();
                                if (!type.equals("offset")) {
                                    firstPositionVideo(type);
                                }


                            } else {
                                videoAdapter.hideLoader();
                                int tempPosition = homeApiResponse.size();
                                homeApiResponse.addAll(response.body().getResult());

                                videoAdapter.notifyItemRangeInserted(tempPosition, response.body().getResult().size());
                                Timber.d("Load more: pos start %d to %d", tempPosition, response.body().getResult().size());
                            }

                            // preDownloadVideos();

                        } else {
                            Toasty.info(requireContext(), "No Videos found", Toasty.LENGTH_SHORT).show();
                        }
                    }

                    // FollowingSwipePrevent used for HomeFollowing viewpager
                    if (homeApiResponse.size() == 0) {
                        swipeRefresh(false);
                        EventBus.getDefault().post(new FollowingSwipePrevent(true));
                        if (nullLay.getVisibility() != View.VISIBLE && getContext() != null) {
                            nullLay.setVisibility(View.VISIBLE);
                            nullText.setText(getResources().getString(R.string.no_broadcast_found));
                        }
                    } else {
                        nullLay.setVisibility(View.GONE);
                        EventBus.getDefault().post(new FollowingSwipePrevent(false));
                    }

                    //
                    if (videoAdapter != null)
                        videoAdapter.hideLoader();
                }

                @Override
                public void onFailure(@NotNull Call<FollowingHomeResponse> call, @NotNull Throwable t) {
                    Timber.e("onFailure: %s", t.getMessage());
                    if (videoAdapter != null)
                        videoAdapter.hideLoader();
                }
            });

        } else {
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
            swipeRefresh(false);
        }
    }

    /*@Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);

    }*/


    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions();
        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    /*private void preDownloadVideos() {

        for (FollowingHomeResponse.Result home : homeApiResponse) {
            if (home.getPlaybackUrl() != null && !home.getPlaybackUrl().isEmpty()) {

                boolean isCached = mMediaLoader.isCached(home.getPlaybackUrl());
                if (!isCached && getContext() != null)
                    DownloadManager.getInstance(getContext()).enqueue(new DownloadManager.Request(home.getPlaybackUrl()), this);

            }
        }

    }*/

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.MUSIC_REQUEST_CODE) {
            followingExoplayerRecyclerView.setPlayControl(true);
        } else if (requestCode == Constants.HASHTAG_REQUEST_CODE) {
            followingExoplayerRecyclerView.setPlayControl(true);
        } else if (requestCode == Constants.COMMENT_MUSIC_REQUEST_CODE) {
            followingExoplayerRecyclerView.setPlayControl(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("resume called");
        //followingExoplayerRecyclerView.setPlayControl(true);

        followingExoplayerRecyclerView.setPlayControl(videoCommandBottomDialog == null || !videoCommandBottomDialog.isShowing());

        if (!NetworkReceiver.isConnected()) {
            if (swipeRefreshLayout.isRefreshing()) swipeRefresh(false);
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause: ");
        super.onPause();
        followingExoplayerRecyclerView.setPlayControl(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /*@Override
    public void onProgress(String url, File file, int progress) {

        Timber.d("onProgress: progress %s ", progress);
        Timber.d("onProgress: url %s ", url);
    }

    @Override
    public void onError(Throwable e) {

    }*/

    /*public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

        ArrayList<FollowingHomeResponse.Result> mHomeApiResponse;
        RequestManager options;


        public VideoAdapter(ArrayList<FollowingHomeResponse.Result> homeApiResponse, RequestManager requestManager) {
            this.mHomeApiResponse = homeApiResponse;
            this.options = requestManager;
        }

        @NonNull
        @Override
        public VideoAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


            return new VideoAdapter.VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_following, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {


            Timber.d("onBindViewHolder: " + position);

            this.options
                    .load(mHomeApiResponse.get(position).getStreamThumbnail())
                    .into(holder.video_thumbnail);

            holder.onBind(mHomeApiResponse.get(position));

        }

        @Override
        public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);

            if (payloads.size() > 0) {
                Bundle payload = (Bundle) payloads.get(0);
                final String likesCount = payload.getString("likes_count", null);
                if (!TextUtils.isEmpty(likesCount)) {
                    holder.txt_heart_count.setText(likesCount);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mHomeApiResponse.size();
        }

        public class VideoViewHolder extends RecyclerView.ViewHolder {

            ConstraintLayout parentLay;
            ProgressBar progressBar, progress_duration;
            ImageView music_symbol, video_thumbnail, profileImage, video_play_pause;
            TextView txt_description, txt_title, txt_heart_count, txt_gift_count, txt_comment_count;
            LinearLayout giftLay, heartLay, videoCommandLay, shareLay;

            FrameLayout mediaContainer;

            public VideoViewHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setTag(this);

                music_symbol = itemView.findViewById(R.id.music_symbol);
                mediaContainer = itemView.findViewById(R.id.mediaContainer);
                video_thumbnail = itemView.findViewById(R.id.mediaCoverImage);
                txt_description = itemView.findViewById(R.id.txt_description);
                profileImage = itemView.findViewById(R.id.profileImage);
                txt_title = itemView.findViewById(R.id.txt_title);
                txt_heart_count = itemView.findViewById(R.id.txt_heart_count);
                txt_gift_count = itemView.findViewById(R.id.txt_gift_count);
                txt_comment_count = itemView.findViewById(R.id.txt_comment_count);
                giftLay = itemView.findViewById(R.id.giftLay);
                heartLay = itemView.findViewById(R.id.heartLay);
                videoCommandLay = itemView.findViewById(R.id.messageLay);
                video_play_pause = itemView.findViewById(R.id.video_play_pause);
                progressBar = itemView.findViewById(R.id.progressBar);
                parentLay = itemView.findViewById(R.id.parentLay);
                shareLay = itemView.findViewById(R.id.shareLay);
                progress_duration = itemView.findViewById(R.id.progress_duration);

                Glide.with(getContext())
                        .load(R.drawable.music)
                        .into(music_symbol);

                giftLay.setOnClickListener(v -> {
                    SelectedVideoPosition = getAdapterPosition();
                    openGiftDialog();
                });

                videoCommandLay.setOnClickListener(v -> {

                    videoDialog(mHomeApiResponse.get(getAdapterPosition()).getVideoId());

                });

                heartLay.setOnClickListener(v -> {
                    heartFun(getAdapterPosition());
                });

                parentLay.setOnLongClickListener(v -> {
                    reportAndFav(getAdapterPosition());
                    return true;
                });

                shareLay.setOnClickListener(v -> {
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        String shareMessage = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        startActivity(Intent.createChooser(shareIntent, "choose one"));
                    } catch (Exception e) {
                        Timber.d("shareLay error: %s", e.getMessage());
                    }
                });


                music_symbol.setOnClickListener(v -> {

                    followingExoplayerRecyclerView.setPlayControl(false);
                    App.preventMultipleClick(music_symbol);
                    Intent intent = new Intent(context, SoundTrackActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.TAG_SOUND_DATA, homeApiResponse.get(getAdapterPosition()).getSoundtracks());
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);
                });

                profileImage.setOnClickListener(v -> profileImageClickListener.onUserClicked(true));
                txt_title.setOnClickListener(v -> profileImageClickListener.onUserClicked(true));

            }


            void onBind(FollowingHomeResponse.Result videoItem) {


                Glide.with(itemView.getContext())
                        .load(videoItem.getPublisherImage())

                        .into(profileImage);

                txt_title.setText(String.format("@%s", videoItem.getPostedBy()));

                txt_heart_count.setText(videoItem.getLikes());
                txt_gift_count.setText(videoItem.getVideoGiftCounts());
                txt_comment_count.setText(videoItem.getVideoCommentCounts());


                String getHash = videoItem.getVideoDescription();
                SpannableString spannable = new SpannableString(getHash);
                final Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(getHash);
                while (matcher.find()) {
                    final String city = matcher.group(1);
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {

                            Toast.makeText(getContext(), city, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(context, HashTagActivity.class);
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_PUBLISH);
                            intent.putExtra(Constants.TAG_INTENT_DATA, city);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityForResult(intent, Constants.HASHTAG_REQUEST_CODE);

                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            ds.setColor(getResources().getColor(R.color.colorWhite));
                        }
                    };
                    int hashIndex = getHash.indexOf(city) - 1;
                    spannable.setSpan(clickableSpan, hashIndex, hashIndex + city.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                txt_description.setText(spannable);
                txt_description.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        private void videoDialog(String videoId) {


            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);
            videoCommandBottomDialog = new BottomSheetDialog(getContext(), R.style.FavAndReportBottomSheetDialog); // Style here
            videoCommandBottomDialog.setContentView(sheetView);
            videoCommandBottomDialog.show();

            *//*commandCount = sheetView.findViewById(R.id.commandCount);
            closeButton = sheetView.findViewById(R.id.closeButton);
            commentRec = sheetView.findViewById(R.id.commentRec);
            addComment = sheetView.findViewById(R.id.addComment);*//*

            commentRec = sheetView.findViewById(R.id.commentRec);



            *//*if (NetworkReceiver.isConnected()) {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                map.put(Constants.TAG_VIDEO_ID, videoId);
                map.put(Constants.TAG_OFFSET, "0");
                map.put(Constants.TAG_LIMIT, "10");

                Timber.d("getCommentParams: %s", new Gson().toJson(map));

                Call<VideoCommentResponse> call = apiInterface.getVideoComments(map);
                call.enqueue(new Callback<VideoCommentResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<VideoCommentResponse> call, @NotNull Response<VideoCommentResponse> response) {

                        Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body()));
                    }

                    @Override
                    public void onFailure(@NotNull Call<VideoCommentResponse> call, @NotNull Throwable t) {
                        call.cancel();
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                });
            } else {
                App.makeToast(getString(R.string.no_internet_connection));
            }*//*


        }
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastOffset = 0;
        // TODO: 23/10/21 @VishnuKumar
        customLinearLayoutManager = new CustomLinearLayoutManager(context, bottomSheetLongPressDi);
        followingExoplayerRecyclerView.setMediaObjects(homeApiResponse);
        videoAdapter = new VideoAdapter(homeApiResponse, initGlide());
        followingExoplayerRecyclerView.setAdapter(videoAdapter);
        followingExoplayerRecyclerView.setLayoutManager(customLinearLayoutManager);

        firstTime = true;

        if (GetSet.getUserId() != null)
            getForYouDataFromApi(0, "pause");

        OnVerticalScrollListener scrollListener = new OnVerticalScrollListener() {

            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                if (homeApiResponse.size() <= lastOffset) return;
                else lastOffset = homeApiResponse.size();


                if (isShowLoadMore) {
                    firstTime = false;
                    getForYouDataFromApi(lastOffset, "loadmore");
                }

            }

        };

        followingExoplayerRecyclerView.addOnScrollListener(scrollListener);

        SnapHelper snapHelper = new PagerSnapHelper() {

            /* Override to disable the snap on loading view */
            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                int position = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                Timber.d("SnapHelper: findSnapView %s %d", layoutManager, position);
                if (position == homeApiResponse.size()/* && false*/) {
                    return null;
                } else {
                    return super.findSnapView(layoutManager);
                }
            }
        };
        snapHelper.attachToRecyclerView(followingExoplayerRecyclerView); // Recyclerview one position take full screen
    }

    private void reportAndFav(int adapterPosition) {

        LinearLayout favLay, reportLay,linearHideShow;

        TextView txtFav, txtReport;

        bottom_sheet_longpress = getLayoutInflater().inflate(R.layout.bottmsheet_home_longpress, null);
        bottomSheetLongPressDi = new BottomSheetDialog(requireContext(), R.style.FavAndReportBottomSheetDialog); // Style here
        // TODO: 23/10/21 @VishnuKumar
        customLinearLayoutManager.setSheetDialog(bottomSheetLongPressDi);
        bottomSheetLongPressDi.setContentView(bottom_sheet_longpress);

        favLay = bottom_sheet_longpress.findViewById(R.id.favLay);
        txtFav = bottom_sheet_longpress.findViewById(R.id.txtFav);
        reportLay = bottom_sheet_longpress.findViewById(R.id.reportLay);
        txtReport = bottom_sheet_longpress.findViewById(R.id.txtReport);
        linearHideShow = bottom_sheet_longpress.findViewById(R.id.linearHideShow);

        linearHideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle payload = new Bundle();
                if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, false);
                    isBottomBarHide = false;
                } else {
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);
                    isBottomBarHide = true;
                }
                hide_btm_bar(isBottomBarHide);
                payload.putString("hide_icon", String.valueOf(isBottomBarHide));
                videoAdapter.notifyItemChanged(adapterPosition, payload);
                EventBus.getDefault().post(new HideIcon(isBottomBarHide, homeApiResponse.get(adapterPosition).getVideoId()));
                EventBus.getDefault().post(new ForyouHideIcon(isBottomBarHide, homeApiResponse.get(adapterPosition).getVideoId()));
                Log.e(TAG, "onClick: ::::::::::::::" + isBottomBarHide);
                bottomSheetLongPressDi.dismiss();
            }
        });
        if (homeApiResponse.get(adapterPosition).getVideoIsFavorite())
            txtFav.setText(R.string.unFav);
        else txtFav.setText(R.string.addafav);

        if (homeApiResponse.get(adapterPosition).getIsVideoReported())
            txtReport.setText(R.string.unreported);
        else txtReport.setText(R.string.report_user);

        favLay.setOnClickListener(v -> favApi(true, adapterPosition, "video"));

        reportLay.setOnClickListener(v -> {

            if (txtReport.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.report_user))) {
                followingExoplayerRecyclerView.setPlayControl(false);
                openReportDialog(adapterPosition);
            } else {
                sendReport("getSelectedReport", false, adapterPosition);
            }
        });
        bottomSheetLongPressDi.show();
    }

    public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final String TAG = VideoAdapter.class.getSimpleName();

        private final int VIEW_TYPE_NORMAL = 0;
        private final int VIEW_TYPE_LOADING = 1;

        ArrayList<FollowingHomeResponse.Result> mHomeApiResponse;
        RequestManager options;

        private boolean loading;

        public VideoAdapter(ArrayList<FollowingHomeResponse.Result> homeApiResponse, RequestManager requestManager) {
            this.mHomeApiResponse = homeApiResponse;
            this.options = requestManager;
        }

        public boolean isLoading() {
            return loading;
        }

        public void showLoader() {
            if (isLoading()) {
                IllegalStateException e = new IllegalStateException("Already loading");
                Log.w(TAG, e.getMessage(), e);
                return;
            }
            loading = true;
            // Hide after loaded
            notifyItemInserted(mHomeApiResponse.size());
        }

        public void hideLoader() {
            if (!loading) {
                IllegalStateException e = new IllegalStateException("Not currently loading");
                Log.w(TAG, e.getMessage(), e);
                return;
            }
            loading = false;
            notifyItemRemoved(mHomeApiResponse.size() + 1);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_NORMAL) {
                return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_following, parent, false));
            } else {
                return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_holder, parent, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isLoadingView(position)) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof VideoViewHolder) {
                Timber.d("onBindViewHolder: %s", App.getGsonPrettyInstance().toJson(mHomeApiResponse.get(position)));
                final RequestOptions options;
                if (Objects.equals(mHomeApiResponse.get(position).getVideoType(), "gallery")) {
                    options = new RequestOptions();
                } else {
                    options = new RequestOptions()
                            .centerCrop();
                }
                this.options
                        .load(mHomeApiResponse.get(position).getStreamThumbnail())
                        .apply(options)
                        .into(((VideoViewHolder) holder).video_thumbnail);

                ((VideoViewHolder) holder).onBind(mHomeApiResponse.get(position));
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);

            if (holder instanceof VideoViewHolder) {
                if (payloads.size() > 0) {
                    Bundle payload = (Bundle) payloads.get(0);

                    Timber.d("onBindViewHolder pay: %s", payload);

                    final String likesCount = payload.getString("likes_count", null);
                    final String likesStatus = payload.getString("likes_status", null);
                    final String giftCount = payload.getString("gift_count", null);
                    final String videoCommentCount = payload.getString("video_comment_count", null);
                    final String follow = payload.getString("follow", null);
                    final String updateGiftCount = payload.getString("update_gift_count", null);
                    final String follower_count = payload.getString("followers", "0");
                    final String view_count = payload.getString("video_views_count", null);
                    final String publisher_vote_count = payload.getString("publisher_vote_count", "0");
//                    final String view_count = payload.getString("video_views_count", null);
                    final String hideIcon = payload.getString("hide_icon", "true");
                    if (likesCount != null) {

                        if (likesStatus.equals("true")) {
                            ((VideoViewHolder) holder).heart.setImageResource(R.drawable.heart_color);
                        } else {
                            /*((VideoViewHolder) holder).heartAni.cancelAnimation();
                            ((VideoViewHolder) holder).heartAni.setVisibility(View.GONE);
                            ((VideoViewHolder) holder).heartAni.setAlpha(0f);*/
                            ((VideoViewHolder) holder).heart.setImageResource(R.drawable.heart_white);
                        }


                        /*((VideoViewHolder) holder).txt_heart_count.setText(likesCount);
                        ((VideoViewHolder) holder).heartAni.setVisibility(View.GONE);
                        ((VideoViewHolder) holder).heartAni.setAlpha(0f);

                        ((VideoViewHolder) holder).heart.setVisibility(View.VISIBLE);
                        ((VideoViewHolder) holder).txt_heart_count.setAlpha(1f);
                        ((VideoViewHolder) holder).heartAni.cancelAnimation();*/

                        ((VideoViewHolder) holder).heartAni.cancelAnimation();
                        ((VideoViewHolder) holder).heartAni.clearAnimation();
                        ((VideoViewHolder) holder).heartAni.setVisibility(View.GONE);
                        ((VideoViewHolder) holder).txt_heart_count.setText(likesCount);
                        ((VideoViewHolder) holder).heart.setVisibility(View.VISIBLE);
                        ((VideoViewHolder) holder).txt_heart_count.setVisibility(View.VISIBLE);


                    } else if (giftCount != null) {

                        ((VideoViewHolder) holder).animGiftLay.setVisibility(View.VISIBLE);
                        ((VideoViewHolder) holder).animGiftLay.clearAnimation();

                        ObjectAnimator anim = ObjectAnimator
                                .ofFloat(((VideoViewHolder) holder).animGiftLay, "translationX", 0, 25, -25, 25, -25, 15, -15, 10, -6, 0)
                                .setDuration(2000);
                        anim.addListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                ((VideoViewHolder) holder).animGiftLay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out_length));
                                Glide.with(getActivity())
                                        .load(tempGiftList.get(giftPosition).getGiftIcon())
                                        .into(((VideoViewHolder) holder).animgiftImage);
                            }

                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                                ((VideoViewHolder) holder).animGiftLay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));

                                ((VideoViewHolder) holder).giftImageView.setImageDrawable(getResources().getDrawable(R.drawable.eye));
                                ((VideoViewHolder) holder).animgiftImage.setVisibility(View.GONE);
                            }
                        });
                        anim.start();

                        ((VideoViewHolder) holder).publisher_vote_count.setText(publisher_vote_count);
                        ((VideoViewHolder) holder).txt_vote_count.setText(giftCount);

                    } else if (videoCommentCount != null) {
//                        ((VideoViewHolder) holder).txt_gift_count.setText(videoCommentCount);

                    } else if (follow != null) {
                        if (follow.equals("true"))
                            ((VideoViewHolder) holder).profileFollowIcon.setVisibility(View.GONE);
                        else
                            ((VideoViewHolder) holder).profileFollowIcon.setVisibility(View.VISIBLE);
                    } else if (updateGiftCount != null) {
//                        ((VideoViewHolder) holder).txt_gift_count.setText(updateGiftCount);
                    }else if(hideIcon!=null){

                        if (hideIcon.equals("true")) {
                            // videoLinkLay.setVisibility(View.GONE);
                            ((VideoViewHolder) holder).videoLabelsLay.setVisibility(View.VISIBLE);
                        } else {
                            //  videoLinkLay.setVisibility(View.VISIBLE);
                            ((VideoViewHolder) holder).videoLabelsLay.setVisibility(GONE);
                        }

                    }
                }
            }
        }

        private boolean isLoadingView(int position) {
            return loading && position == mHomeApiResponse.size();
        }

        @Override
        public int getItemCount() {
            return loading ? mHomeApiResponse.size() + 1 : mHomeApiResponse.size();
        }

        private class LoadingViewHolder extends RecyclerView.ViewHolder {

            public LoadingViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
            }
        }

        public class VideoViewHolder extends RecyclerView.ViewHolder {

            ConstraintLayout parentLay;
            ProgressBar progressBar, progress_duration;
            ImageView music_symbol, video_thumbnail, profileImage, video_play_pause, animgiftImage;
            TextView txt_description, txt_title, txt_heart_count, txt_gift_count, txt_comment_count,
                    txt_view_count, txt_vote_count;
            LinearLayout heartLay, videoLinkLay, videoCommandLay, shareLay /*, giftLay*/, hideBtmBarLay,
                    autoScrollLay, animGiftLay;
            FrameLayout mediaContainer;
            RelativeLayout leftSideView, videoLabelsLay, giftLay;
            LottieAnimationView heartAni;
            ShapeableImageView heart, giftImageView, autoScrollIV;

            Boolean isAnimated = false;

            LottieAnimationView icFavHeart;
            MaterialTextView profileFollowIcon, FollowersCount, time_reverse_tv, vote_promotionTV, publisher_vote_count;

            LinearLayout liveStreaming;

            public VideoViewHolder(@NonNull View itemView) {
                super(itemView);

                itemView.setTag(this);

                videoLinkLay = itemView.findViewById(R.id.videoLinkLay);
                hideBtmBarLay = itemView.findViewById(R.id.hideBtmBarLay);
                icFavHeart = itemView.findViewById(R.id.ic_fav_heart);
                music_symbol = itemView.findViewById(R.id.music_symbol);
                mediaContainer = itemView.findViewById(R.id.mediaContainer);
                video_thumbnail = itemView.findViewById(R.id.mediaCoverImage);
                txt_description = itemView.findViewById(R.id.txt_description);
                profileImage = itemView.findViewById(R.id.profileImage);
                txt_title = itemView.findViewById(R.id.txt_title);
                txt_heart_count = itemView.findViewById(R.id.txt_heart_count);
//                txt_gift_count = itemView.findViewById(R.id.txt_gift_count);
                txt_comment_count = itemView.findViewById(R.id.txt_comment_count);
                giftLay = itemView.findViewById(R.id.giftLay);
                heartLay = itemView.findViewById(R.id.heartLay);
                videoCommandLay = itemView.findViewById(R.id.messageLay);
                video_play_pause = itemView.findViewById(R.id.video_play_pause);
                progressBar = itemView.findViewById(R.id.progressBar);
                parentLay = itemView.findViewById(R.id.parentLay);
                shareLay = itemView.findViewById(R.id.shareLay);
                progress_duration = itemView.findViewById(R.id.progress_duration);
                profileFollowIcon = itemView.findViewById(R.id.profileFollowIcon);
                leftSideView = itemView.findViewById(R.id.leftSideView);
                heartAni = itemView.findViewById(R.id.heartAni);
                heart = itemView.findViewById(R.id.heart);
                giftImageView = itemView.findViewById(R.id.giftImageView);
                liveStreaming = itemView.findViewById(R.id.liveLay);
                //Live Streaming Addon
                FollowersCount = itemView.findViewById(R.id.total_followers_tv);
                videoLabelsLay = itemView.findViewById(R.id.videoLabelsLay);
                time_reverse_tv = itemView.findViewById(R.id.remain_duration_tv);
                autoScrollLay = itemView.findViewById(R.id.autoLay);
                autoScrollIV = itemView.findViewById(R.id.autoImageView);
                vote_promotionTV = itemView.findViewById(R.id.voteTV);
                publisher_vote_count = itemView.findViewById(R.id.publisher_vote_count);
                txt_view_count = itemView.findViewById(R.id.txt_view_count);
                txt_vote_count = itemView.findViewById(R.id.txt_vote_count);
                animGiftLay = itemView.findViewById(R.id.lay_animGift);
                animgiftImage = itemView.findViewById(R.id.img_animGift);


                autoScrollLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isAutoScroll) {
                            isAutoScroll = false;
                            Toast.makeText(getContext(), getString(R.string.auto_scroll_off), Toast.LENGTH_SHORT).show();
                        } else {
                            isAutoScroll = true;
                        }

                        AutoScrollAPI(isAutoScroll, getAdapterPosition());
                        updateAutoScrollUI();

                    }
                });


                hideBtmBarLay.setOnClickListener(view -> {
                  //  hideIcon(homeApiResponse.get(getAdapterPosition()).getVideoId(), getAdapterPosition());
                    Bundle payload = new Bundle();
                    if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                        SharedPref.putBoolean(SharedPref.HIDE_ICONS, false);
                        isBottomBarHide = false;
                    } else {
                        SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);
                        isBottomBarHide = true;
                    }
                    hide_btm_bar(isBottomBarHide);
                    payload.putString("hide_icon", String.valueOf(isBottomBarHide));
                    videoAdapter.notifyItemChanged(getAdapterPosition(), payload);
                    EventBus.getDefault().post(new HideIcon(isBottomBarHide, homeApiResponse.get(getAdapterPosition()).getVideoId()));
                    EventBus.getDefault().post(new ForyouHideIcon(isBottomBarHide, homeApiResponse.get(getAdapterPosition()).getVideoId()));
                    Log.e(TAG, "onClick: ::::::::::::::" + isBottomBarHide);
                });

                videoLinkLay.setOnClickListener(v -> {
                    App.preventMultipleClick(videoLinkLay);

                    if (!TextUtils.isEmpty(mHomeApiResponse.get(getAdapterPosition()).getLink_url())) {
                        Intent intent = new Intent(context, PrivacyActivity.class);
                        intent.putExtra("from", "video");
                        intent.putExtra("link_url", mHomeApiResponse.get(getAdapterPosition()).getLink_url().toString());
                        startActivity(intent);
                    } else {
                        Toast.makeText(context, "No video link found!", Toast.LENGTH_SHORT).show();
                    }
                });


                final GestureDetector gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        heartFun(getAdapterPosition());
                       /* if (!mHomeApiResponse.get(getAdapterPosition()).getIsLiked()) {

                            if (!likePrevent) {
                                likePrevent = true;
                                heartFun(getAdapterPosition());
                                *//*txt_heart_count.setVisibility(View.INVISIBLE);
                                heart.setVisibility(View.GONE);
                                heartAni.setVisibility(View.VISIBLE);
                                heartAni.playAnimation();
                                heartAni.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));*//*
                            }

                        }*/
                        icFavHeart.setVisibility(View.VISIBLE);
                        icFavHeart.playAnimation();
                        icFavHeart.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.heart_animation));


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

                                icFavHeart.clearAnimation();
                                icFavHeart.setVisibility(View.GONE);

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
                            followingExoplayerRecyclerView.togglePlay();
                        }
                        return super.onSingleTapConfirmed(e);
                    }
                });

                itemView.setOnTouchListener((v, event) -> {
                    Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                    return gestureDetector.onTouchEvent(event);
                });


                Glide.with(getContext())
                        .asGif()
                        .load(R.drawable.music)
                        .into(music_symbol);
                liveStreaming.setOnClickListener(v -> {
                   /* Intent stream = new Intent(context, PublishActivity.class);
                    stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    stream.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                    stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                    stream.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                    stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                    startActivity(stream);*/

                    Intent liveStreaming = new Intent(getContext(), LiveStreamActivity.class);
                    liveStreaming.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    liveStreaming.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                    liveStreaming.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                    liveStreaming.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                    liveStreaming.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                    startActivity(liveStreaming);

                });

                giftLay.setOnClickListener(v -> {

                    if (GetSet.getUserId().equals(mHomeApiResponse.get(getAdapterPosition()).getPublisherId())) {
                        Intent giftIntent = new Intent(getActivity(), ConvertGiftActivity.class);
                        giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(giftIntent);
                    } else {
                        App.preventMultipleClick(giftLay);
                        SelectedVideoPosition = getAdapterPosition();
                        openGiftDialog();
                    }
                });

                videoCommandLay.setOnClickListener(v -> {
                    followingExoplayerRecyclerView.setPlayControl(false);
                    App.preventMultipleClick(videoCommandLay);
                    videoDialog(mHomeApiResponse.get(getAdapterPosition()).getPublisherId(), mHomeApiResponse.get(getAdapterPosition()).getVideoId(), getAdapterPosition());

                });

                heartLay.setOnClickListener(v -> {

                    if (!likePrevent) {
                        String abc = mHomeApiResponse.get(getAdapterPosition()).getLikes();
                        int inum = Integer.parseInt(abc);
                        heartFun(getAdapterPosition());
                        likePrevent = true;

                        if (!mHomeApiResponse.get(getAdapterPosition()).getIsLiked()) {
                            // txt_heart_count.setVisibility(View.INVISIBLE);

                            if (heart.getDrawable().equals(getResources().getDrawable(R.drawable.heart_white))) {

                                /*heart.setVisibility(View.GONE);
                                heartAni.setVisibility(View.VISIBLE);
                                heartAni.playAnimation();
                                heartAni.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));*/

                                String aq = String.valueOf(inum + 1);
                                heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
                                txt_heart_count.setText(aq);

 /*                               txt_heart_count.setVisibility(View.INVISIBLE);
                                heart.setVisibility(View.GONE);
                                heartAni.setVisibility(View.VISIBLE);
                                heartAni.playAnimation();
                                heartAni.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));*/

                            } else {
                                heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_white));
                                txt_heart_count.setText(String.valueOf(inum));
                            }


                        } else {
                            if (heart.getDrawable().equals(getResources().getDrawable(R.drawable.heart_color))) {
                                String aq = String.valueOf(inum - 1);
                                heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_white));
                                txt_heart_count.setText(aq);
                            } else {
                                heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
                                txt_heart_count.setText(String.valueOf(inum));

                             /*   txt_heart_count.setVisibility(View.INVISIBLE);
                                heart.setVisibility(View.GONE);
                                heartAni.setVisibility(View.VISIBLE);
                                heartAni.playAnimation();
                                heartAni.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));*/
                            }
                        }

                      /*  heartAni.cancelAnimation();
                        heartAni.clearAnimation();
                        heartAni.setVisibility(View.GONE);
                        heart.setVisibility(View.VISIBLE);
                        txt_heart_count.setVisibility(View.VISIBLE);*/
                    }


                    //   }


                });

                parentLay.setOnLongClickListener(v -> {

                    if (!mHomeApiResponse.get(getAdapterPosition()).getPublisherId().equals(GetSet.getUserId())) {
                        App.preventMultipleClick(parentLay);
                        reportAndFav(getAdapterPosition());
                        return true;
                    }
                    return false;
                });


                shareLay.setOnClickListener(v -> {
                    App.preventMultipleClick(shareLay);
                    shareVideoDialog(mHomeApiResponse.get(getAdapterPosition()));
                });

                music_symbol.setOnClickListener(v -> {

                            followingExoplayerRecyclerView.setPlayControl(false);
                            App.preventMultipleClick(music_symbol);
                            Intent intent = new Intent(context, SoundTrackActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.TAG_SOUND_ID, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getSoundId());
                            bundle.putString(Constants.TAG_SOUND_TITLE, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getTitle());
                            bundle.putString(Constants.TAG_SOUND_URL, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getSoundUrl());
                            bundle.putString(Constants.TAG_SOUND_COVER, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getCoverImage());
                            bundle.putString(Constants.TAG_SOUND_DURATION, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getDuration());


                            intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                            startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);


                        }
                );


                profileFollowIcon.setOnClickListener(v -> {
                    profileFollowIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));
                    followAPI(mHomeApiResponse.get(getAdapterPosition()).getPublisherId(), mHomeApiResponse.get(getAdapterPosition()).getPublisherImage());
                });

                profileImage.setOnClickListener(v -> profileImageClickListener.onUserClicked(true));
                txt_title.setOnClickListener(v -> {
                    profileImageClickListener.onUserClicked(true);

                });

                //Live Streaming Addon

            }

            private void updateAutoScrollUI() {
                if (isAutoScroll) {
                    autoScrollIV.setImageDrawable(requireContext().getDrawable(R.drawable.scroll_active));
                } else {
                    autoScrollIV.setImageDrawable(requireContext().getDrawable(R.drawable.scroll));
                }
            }


            private void AutoScrollAPI(boolean isAutoScroll, int adapterPosition) {

                if (NetworkReceiver.isConnected()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("user_id", GetSet.getUserId());
                    map.put("scroll_enable", String.valueOf(isAutoScroll));

                    Call<Map<String, String>> autoscroll_call = apiInterface
                            .setAutoScroll(map);
                    autoscroll_call.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            Map<String, String> scroll_response = response.body();
                            Log.e(TAG, "onResponse: :::::::::::::auto scroll:::" + scroll_response);
                            SharedPref.putBoolean(SharedPref.isAUTO_SCROLL, isAutoScroll);
                            Bundle payload = new Bundle();
                            payload.putBoolean("auto_scroll", isAutoScroll);
                            for (int i = 0; i < homeApiResponse.size(); i++) {
                                if(i!=adapterPosition)
                                    videoAdapter.notifyItemChanged(i, payload);
                            }
                            EventBus.getDefault().post(new AutoScrollEnabled(isAutoScroll));
                            EventBus.getDefault().post(new VideoAutoScrollEnabled(isAutoScroll));
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Log.e(TAG, "onFailure: ::::::::::::::::auto scroll::::", t);
                            autoscroll_call.cancel();
                        }
                    });
                } else {
                    Toasty.error(requireContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                }

            }

            private void shareVideoDialog(FollowingHomeResponse.Result item) {

                String link = item.getShareLink();

                View shareView = getLayoutInflater().inflate(R.layout.share_video_dialog, null);
                shareDialog = new BottomSheetDialog(getActivity(), R.style.FavAndReportBottomSheetDialog); // Style here
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
                shareDialog.show();


                copyLinkLay.setOnClickListener(v -> {

                    ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
                    if (clipboardManager != null) {
                        ClipData data = ClipData.newPlainText("label", link);
                        clipboardManager.setPrimaryClip(data);
                        Toasty.success(getContext(), R.string.linkCopied, Toasty.LENGTH_SHORT).show();
                        shareDialog.dismiss();
                    }

                });


                copyWhatsAppLay.setOnClickListener(view -> {
                    boolean installed = appInstalledOrNot("com.whatsapp");
                    if (installed) {

                        callShareAPI(item, "whatsapp");

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
                        callShareAPI(item, "Facebook");

                        ShareLinkContent fbShare = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse(link))
                                .build();
                        ShareDialog.show(requireActivity(), fbShare);

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
                        callShareAPI(item, "messenger");

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

                copyInstaLay.setOnClickListener(v -> {
                    boolean installed = appInstalledOrNot("com.instagram.android");
                    if (installed) {
                        callShareAPI(item, "instagram");

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
                        callShareAPI(item, "twitter");

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
                        callShareAPI(item, "snapchat");
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

                copySMSLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callShareAPI(item,"sms");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
                        {
                            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(getActivity()); // Need to change the build to API 19

                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.setType("text/plain");
                            sendIntent.putExtra(Intent.EXTRA_TEXT, link);

                            if (defaultSmsPackageName != null)// Can be null in case that there is no default, then the user would be able to choose
                            // any app that support this intent.
                            {
                                sendIntent.setPackage(defaultSmsPackageName);
                            }
                            startActivity(sendIntent);

                        }
                        else // For early versions, do what worked for you before.
                        {
                            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                            smsIntent.setType("vnd.android-dir/mms-sms");
                            // smsIntent.putExtra("address","phoneNumber");
                            smsIntent.putExtra("sms_body",link);
                            startActivity(smsIntent);
                        }
                        shareDialog.dismiss();
                    }
                });

                copyInAppLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callShareAPI(item,"inapp");
                        Intent intent = new Intent(getActivity(), FollowersActivity.class);
                        intent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                        intent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                        intent.putExtra("from", "main");
                        intent.putExtra("share_link", link);
                        startActivity(intent);
                        shareDialog.dismiss();
                    }
                });

            }

            private boolean appInstalledOrNot(String uri) {

                PackageManager pm = getContext().getPackageManager();
                boolean app_installed;
                try {
                    pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
                    app_installed = true;
                } catch (PackageManager.NameNotFoundException e) {
                    app_installed = false;
                }
                return app_installed;
            }


            private void callShareAPI(FollowingHomeResponse.Result item, String type) {

                Map<String, String> params = new HashMap<>();
                params.put("video_id", item.getVideoId());
                params.put("user_id", GetSet.getUserId());
                params.put("share_type", type);

                Call<HashMap<String, String>> call = apiInterface.setShareCount(params);
                call.enqueue(new Callback<HashMap<String, String>>() {
                    @Override
                    public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                        if (response.body() != null) {
                            Log.e(TAG, "onResponse: ::::::::::::::::share:::" + new Gson().toJson(response.body()));
                            if (response.body().get("status").toString().equals("true")) {
                                Toast.makeText(context, "" + response.body().get("message").toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                        Log.e(TAG, "onFailure: :::::::::::::::::share failure::", t);
                    }
                });

            }


            private void followAPI(String publisherId, String publisherImage) {

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

                                EventBus.getDefault().post(new FollowingProfileUpdate(publisherId, publisherImage, userType));

                                Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));

                                profileFollowIcon.clearAnimation();

                                Bundle payload = new Bundle();
                                for (int i = 0; i < homeApiResponse.size(); i++) {
                                    if (homeApiResponse.get(i).getPublisherId().equals(publisherId)) {
                                        homeApiResponse.get(i).setFollowedUser(true);
                                        payload.putString("follow", "true");
                                        videoAdapter.notifyItemChanged(i, payload);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure
                                (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                            call.cancel();
                        }
                    });
                } else {
                    Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                }
            }


            void onBind(FollowingHomeResponse.Result videoItem) {

                Log.e(TAG, "onBind: ::::::::::::" + new Gson().toJson(videoItem));
                updateAutoScrollUI();
                //  if (videoItem.getPlaytype().equals(Constants.TAG_VIDEO)) {
                leftSideView.setVisibility(View.VISIBLE);
                vote_promotionTV.setText(contest_text + "");
                Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.tv_lhs_rhs);
                vote_promotionTV.startAnimation(a);

                if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                    videoLabelsLay.setVisibility(View.VISIBLE);
                } else {
                    videoLabelsLay.setVisibility(GONE);
                }

                if(!TextUtils.isEmpty(videoItem.getLink_url())){
                    videoLinkLay.setVisibility(View.VISIBLE);
                }else{
                    videoLinkLay.setVisibility(GONE);
                }

                if (GetSet.getUserId() != null && GetSet.getUserId().equals(videoItem.getPublisherId())) {
                    profileFollowIcon.setVisibility(View.GONE);
                    giftLay.setVisibility(View.GONE);
                } else {
                    giftLay.setVisibility(View.VISIBLE);
                    if (!isAutoScroll) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SetViewCount(videoItem);
                            }
                        }, 3000);
                    }


                    if (videoItem.getFollowedUser()) // If API come true that user already follow
                        profileFollowIcon.setVisibility(View.GONE);
                    else
                        profileFollowIcon.setVisibility(View.VISIBLE);

                }


                Glide.with(itemView.getContext())
                        .load(videoItem.getPublisherImage())

                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                FollowersCount.setText(videoItem.getFollowers() + " Followers");
                publisher_vote_count.setText(videoItem.getLifetimeVoteCount());
                txt_view_count.setText(videoItem.getVideo_views_count() + "");
                txt_vote_count.setText(videoItem.getVotes_count());

                if (videoItem.getIsLiked()) {
                    Glide.with(getContext())
                            .load(R.drawable.heart_color)

                            .into(heart);
                } else {

                    Glide.with(getContext())
                            .load(R.drawable.heart_white)

                            .into(heart);
                }

                txt_title.setText(String.format("@%s", videoItem.getPostedBy()));

                txt_heart_count.setText(videoItem.getLikes());
//                txt_gift_count.setText(videoItem.getVideoGiftCounts());
                txt_comment_count.setText(videoItem.getVideoCommentCounts());


                String getHash = videoItem.getVideoDescription();
               /* SpannableString spannable = new SpannableString(getHash);
                final Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(getHash);

                while (matcher.find()) {
                    final String getHashTagName = matcher.group(1);
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onClick(View textView) {
                            followingExoplayerRecyclerView.setPlayControl(false);
                            Intent intent = new Intent(context, HashTagActivity.class);
                            intent.putExtra(Constants.TAG_SELECT_HASH_TAG, getHashTagName);
                            startActivityForResult(intent, Constants.HASHTAG_REQUEST_CODE);

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
                }*/

                txt_description.setText(getHash);
                txt_description.setMovementMethod(LinkMovementMethod.getInstance());

            }
        }


        @SuppressLint("ClickableViewAccessibility")
        private void videoDialog(String publisherId, String videoId, int adapterPosition) {

            commendList.clear();
            tagged_user_id = "";
            tagged_user_name = "";
            list_tagged_user_ids.clear();
            list_tagged_user_names.clear();

            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);
            videoCommandBottomDialog = new BottomSheetDialog(getContext(), R.style.VideoCommentDialog); // Style here

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

            mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));

            videoCommandBottomDialog.show();


            commendAdapter = new CommentAdapter(context, commendList, publisherId, videoId, adapterPosition);
            linearLayoutManager = new LinearLayoutManager(getContext());
            commentRec.setLayoutManager(linearLayoutManager);
            commentRec.setAdapter(commendAdapter);


            videoScrollListener = new OnVerticalScrollListener() {
                @Override
                public void onScrolledToBottom() {
                    super.onScrolledToBottom();
                    if (commendList.size() <= lastOffsetVideo) return;
                    else lastOffsetVideo = commendList.size();

                    loadMoreCheckVideoCommand = "loadmore";

                    getCommand(lastOffsetVideo, videoId, adapterPosition);

                }
            };

            commentRec.addOnScrollListener(videoScrollListener);

            swipeRefresh(true, videoId, adapterPosition);

            closeButton.setOnClickListener(v -> {
                if (mCommentsEditText != null)
                    addComment.setText(mCommentsEditText.getText().toString());
                loadMoreCheckVideoCommand = "initial";
                commendList.clear();
                followingExoplayerRecyclerView.setPlayControl(true);
                videoCommandBottomDialog.setDismissWithAnimation(true);
                if (videoCommandBottomDialog.isShowing()) videoCommandBottomDialog.dismiss();
                if (VideoCommentSentDialog != null && VideoCommentSentDialog.isShowing())
                    VideoCommentSentDialog.dismiss();
            });

            sendCommentLay.setOnClickListener(v -> {
                topBottomSheet(videoId, adapterPosition, addComment.getText().toString());
            });

            videoCommandBottomDialog.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    Timber.i("keyCode check %s" , VideoCommentSentDialog.isShowing());

                    if (VideoCommentSentDialog != null && VideoCommentSentDialog.isShowing()) {
                        VideoCommentSentDialog.dismiss();
                        followingExoplayerRecyclerView.setPlayControl(false);
                        return true;
                    } else {
                        followingExoplayerRecyclerView.setPlayControl(true);
                        return false;
                    }
                }
                Timber.i("keyCode check %s", "return");
                return false;
            });
        }

        private void topBottomSheet(String videoId, int adapterPosition, String setText) {

            View commentSentDialog = getLayoutInflater().inflate(R.layout.bottom_sheet_comment_sent, null);
            commentSentDialog.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            VideoCommentSentDialog = new BottomSheetDialog(getContext(), R.style.Command);

//            VideoCommentSentDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            VideoCommentSentDialog.setContentView(commentSentDialog);
            VideoCommentSentDialog.getBehavior().setHideable(false);
//            VideoCommentSentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

//            commentEditText = commentSentDialog.findViewById(R.id.commentEditText);
            sendComment = commentSentDialog.findViewById(R.id.sendComment);

//            commentEditText.requestFocus();

            mCommentsEditText = commentSentDialog.findViewById(R.id.socialAutoCompleteTextView);
            mCommentsEditText.setThreshold(1);
            mCommentsEditText.setDropDownAnchor(R.id.rootView);
            mCommentsEditText.setDropDownHeight(0);  // don't show popup
            MentionArrayAdapter<Mention> adapter = new MentionArrayAdapter<>(requireContext());
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

                    if (s.toString().length() >= 250) {
                        Toasty.info(getContext(), R.string.max_reached, Toasty.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if ((s.toString().length() - s.toString().replaceAll("@", "").length()) > 5) {
                        if (singleTonToast.length > 0 && singleTonToast[0] != null)
                            singleTonToast[0].cancel();
                        singleTonToast[0] = Toasty.info(requireContext(),
                                "Only 5 tags allowed");
                        singleTonToast[0].show();
                        return;
                    }
                    final String q = getMentionsSuggestionQuery(s.toString(), mCommentsEditText.getSelectionEnd());
//                Log.d(TAG, String.format("Mentis.equals("@") && on: query %s", q));
                    Log.d(TAG, String.format("Mention: q=%s", q));
                    if (q.length() > 0) {
                        // TODO: fetch users for q
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
                    Toasty.info(requireContext(),
                            "Already tagged!").show();
                    return;
                }
                getReplacedText(mCommentsEditText, selected, mCommentsEditText.getSelectionEnd());

                mentionsList.setVisibility(View.GONE);
            });

//            userRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));
//            commentUserAdapter = new CommentUserAdapter(liveUser);
//            userRecylerView.setAdapter(commentUserAdapter);
//            userRecylerView.setHasFixedSize(true);

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mCommentsEditText, InputMethodManager.SHOW_IMPLICIT);
            VideoCommentSentDialog.show();
            lastOffsetVideo = 0;

            mCommentsEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    VideoCommentSentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                } else {
                    VideoCommentSentDialog.dismiss();
                }
            });

            mCommentsEditText.requestFocus();

            VideoCommentSentDialog.setCanceledOnTouchOutside(false);

            sendComment.setOnClickListener(v -> {
                if (!Objects.requireNonNull(mCommentsEditText.getText()).toString().isEmpty()) {
                    postComments(mCommentsEditText.getText().toString(), videoId, adapterPosition);
                    mCommentsEditText.setText("");
                    addComment.setText("");
                }
            });

            VideoCommentSentDialog.setOnCancelListener(dialog -> {
                if (mCommentsEditText != null)
                    addComment.setText(mCommentsEditText.getText().toString());
                loadMoreCheckVideoCommand = "initial";
                commendList.clear();
                followingExoplayerRecyclerView.setPlayControl(true);
                videoCommandBottomDialog.setDismissWithAnimation(true);
                videoCommandBottomDialog.dismiss();
                VideoCommentSentDialog.dismiss();
            });

            CustomEditText.OnKeyPreImeListener onKeyPreImeListener = () -> {
                if (mCommentsEditText != null)
                    addComment.setText(mCommentsEditText.getText().toString());
                //VideoCommentSentDialog.dismiss();
            };

//            commentEditText.setOnKeyPreImeListener(onKeyPreImeListener);

            /*TextWatcher textWatcher = new TextWatcher() {
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
                        Timber.d("afterTextChanged: %s", commentEditText.getText().toString() + " length " + s.length());
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
                                    Timber.d("afterTextChanged: %s", commentEditText.getText().toString().equals(text));
                                    if (commentEditText.getText().toString().equals(text)) {
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
                                    Toast.makeText(getContext(), "You can tag upto 5 people", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }, 500);

                    } else {
                        userRecylerView.setVisibility(View.GONE);
                        tagging = true;
                    }
                }
            };

            commentEditText.addTextChangedListener(textWatcher);*/


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

        public void postComments(final String comments, String videoId, int adapterPosition) {

            if (NetworkReceiver.isConnected()) {

                Tagged tagged = getTagged(mentionsCache, mCommentsEditText.getMentions());
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


                                list_tagged_user_ids.clear();
                                list_tagged_user_names.clear();
                                tagcomment = "";
                                tagcomment = "";
                                tagged_user_name = "";
                                tagged_user_id = "";

                                lastOffsetVideo = 0;
                                currentPage = 0;

                                Bundle payload = new Bundle();
                                homeApiResponse.get(adapterPosition).setVideoCommentCounts(data.get("commentcount"));
                                payload.putString("video_comment_count", data.get("commentcount"));
                                videoAdapter.notifyItemChanged(adapterPosition, payload);

                                commentRec.clearOnScrollListeners();
                                commentRec.scrollToPosition(0);
                                loadMoreCheckVideoCommand = "initial";
                                getCommand(0, videoId, adapterPosition);

                                commentCount.setText(data.get("commentcount") + " " + getResources().getString(R.string.comments));
                                commentCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));

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

            } else {
                Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
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

        /*public void getAllLiveusers(String name) {

            if (NetworkReceiver.isConnected()) {

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

                            if (liveUser.size() == 0 || commentEditText.getText().toString().isEmpty()) {
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
            } else {
                Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            }
        }*/


        /*public void getCommand(int currentPage, String videoId, int adapterPosition) {

            if (noCommands.getVisibility() == View.VISIBLE) noCommands.setVisibility(View.GONE);

            if (NetworkReceiver.isConnected()) {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                map.put(Constants.TAG_VIDEO_ID, videoId);
                map.put(Constants.TAG_OFFSET, String.valueOf(currentPage));
                map.put(Constants.TAG_LIMIT, "10");

                Timber.d("getCommentParams: %s", new Gson().toJson(map));

                Call<VideoCommentResponse> call = apiInterface.getVideoComments(map);
                call.enqueue(new Callback<VideoCommentResponse>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NotNull Call<VideoCommentResponse> call, @NotNull Response<VideoCommentResponse> response) {

                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                            noCommands.setVisibility(View.GONE);
                            commentRec.setVisibility(View.VISIBLE);

                            if (response.body().getIsUserCanComment()) {
                                sendCommentLay.setVisibility(View.VISIBLE);
                                Timber.d("getcommand onResponse: true");
                            } else {
                                Timber.d("getcommand  onResponse: false");
                                sendCommentLay.setVisibility(View.INVISIBLE);
                            }

                            Bundle payload = new Bundle();
                            homeApiResponse.get(adapterPosition).setVideoCommentCounts(String.valueOf(response.body().getCommentcount()));
                            payload.putString("video_comment_count", String.valueOf(response.body().getCommentcount()));
                            videoAdapter.notifyItemChanged(adapterPosition, payload);

                            *//*commentRec.clearOnScrollListeners();
                            commendAdapter.notifyDataSetChanged();
                            commentRec.smoothScrollToPosition(0);
                            commentRec.addOnScrollListener(videoScrollListener);*//*


                            if (loadMoreCheckVideoCommand.equals("initial")) {
                                commendList.clear();
                                commendList.addAll(response.body().getResult());
                                swipeRefresh(false, videoId, adapterPosition);
                                mSwipeVideoComment.setEnabled(false);

                            } else {
                                commendList.addAll(response.body().getResult());
                            }

                            commentCount.setText(response.body().getCommentcount() + " " + getResources().getString(R.string.comments));
                            commentCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));
                            commendAdapter.notifyDataSetChanged();

                        } else if (response.body().getStatus().equals(Constants.TAG_FALSE)) {
                            swipeRefresh(false, videoId, adapterPosition);
                            mSwipeVideoComment.setEnabled(false);

                            if (response.body().getIsUserCanComment()) {
                                sendCommentLay.setVisibility(View.VISIBLE);
                                Timber.d("getcommand onResponse: true");
                            } else {
                                Timber.d("getcommand  onResponse: false");
                                sendCommentLay.setVisibility(View.INVISIBLE);
                            }

                            noCommands.setVisibility(View.VISIBLE);
                            commentRec.setVisibility(View.GONE);
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

        }*/

        public void getCommand(int currentPage, String videoId, int adapterPosition) {

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

                            Timber.d("getCommentResponse=> %s",
                                    new GsonBuilder().setLenient().setPrettyPrinting().create().toJson(response.body()));

                            noCommands.setVisibility(View.GONE);
                            commentRec.setVisibility(View.VISIBLE);

                            if (response.body().getIsUserCanComment()) {
                                sendCommentLay.setVisibility(View.VISIBLE);

                            } else {

                                sendCommentLay.setVisibility(View.INVISIBLE);
                            }

                            if (loadMoreCheckVideoCommand.equals("initial")) {
                                commendList.clear();
                                commendList.addAll(response.body().getResult());
                                swipeRefresh(false, videoId, adapterPosition);
                                mSwipeVideoComment.setEnabled(false);
                                commentRec.addOnScrollListener(videoScrollListener);

                                Bundle payload = new Bundle();
                                homeApiResponse.get(adapterPosition).setVideoCommentCounts(String.valueOf(response.body().getCommentcount()));
                                payload.putString("video_comment_count", String.valueOf(response.body().getCommentcount()));
                                videoAdapter.notifyItemChanged(adapterPosition, payload);

                                commentCount.setText(response.body().getCommentcount() + " " + getResources().getString(R.string.comments));
                                //commentCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));

                            } else if (loadMoreCheckVideoCommand.equals("loadmore")) {
                                commendList.addAll(response.body().getResult());
                            }

                            commendAdapter.notifyDataSetChanged();

                        } else if (response.body().getStatus().equals(Constants.TAG_FALSE)) {
                            swipeRefresh(false, videoId, adapterPosition);
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
                Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            }

        }

        private void swipeRefresh(final boolean refresh, String videoId, int adapterPosition) {

            mSwipeVideoComment.setRefreshing(refresh);

            if (refresh) {
                lastOffsetVideo = 0;
                currentPage = 0;
                loadMoreCheckVideoCommand = "initial";
                getCommand(currentPage, videoId, adapterPosition);
            }
        }

        private void SetViewCount(FollowingHomeResponse.Result videoItem) {

            Map<String, String> viewCountMap = new HashMap<>();
            viewCountMap.put("video_id", videoItem.getVideoId());
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


    }

    public class ReportAdapter extends RecyclerView.Adapter {
        private final Context context;
        private final List<Report> reportList;
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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
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

    public static void getReplacedText(EditText editText, String selected, int lastCursorPosition) {
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

    private void openReportDialog(int adapterPosition) {
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialog); // Style here
        reportDialog.setContentView(bottomSheet);
        ((View) bottomSheet.getParent()).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        reportsView = bottomSheet.findViewById(R.id.reportView);
        btnBack = bottomSheet.findViewById(R.id.btnBack);
        txtTitle = bottomSheet.findViewById(R.id.txtTitle);

        btnReport = bottomSheet.findViewById(R.id.btnReport);

        txtTitle.setText(R.string.report_user);

        btnBack.setOnClickListener(v -> {
            followingExoplayerRecyclerView.setPlayControl(true);
            reportDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {

            App.preventMultipleClick(btnReport);

            Timber.d("openReportDialog: %s", getSelectedReport);
            sendReport(getSelectedReport, true, adapterPosition);

        });

        reportDialog.setOnKeyListener((v, keyCode, event) -> {

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                followingExoplayerRecyclerView.setPlayControl(true);
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

        /*reportDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet12 = dialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet12).setHideable(true);
        });*/

        loadReports();
    }

    private void loadReports() {
        reportAdapter = new ReportAdapter(getContext(), AdminData.reportList);
        reportsView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
    }

    private void sendReport(String title, boolean report, int adapterPosition) {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_VIDEO_ID, homeApiResponse.get(adapterPosition).getVideoId());
            if (report) {
                map.put(Constants.TAG_REPORT, title);
            }

            Timber.d("sendReport: %s", new Gson().toJson(map));

            /*reportDialog.dismiss();
            if (bottomSheetLongPressDi != null && bottomSheetLongPressDi.isShowing()) {
                bottomSheetLongPressDi.dismiss();
            }*/

            Call<Map<String, String>> call = apiInterface.reportStream(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    /*if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (report) {
                                homeApiResponse.get(adapterPosition).setIsVideoReported(true);
                                App.dialog(getContext(), "", getResources().getString(R.string.report_success_alert), getResources().getColor(R.color.color_green));

                            } else {
                                homeApiResponse.get(adapterPosition).setIsVideoReported(false);
                                App.makeToast(getString(R.string.undo_report_successfully));
                            }

                            followingExoplayerRecyclerView.setPlayControl(true);
                            reportDialog.dismiss();
                            getSelectedReport = "";

                            if (bottomSheetLongPressDi.isShowing())
                                bottomSheetLongPressDi.dismiss();

                        } else
                            App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }*/


                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();

                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (reportResponse.get(Constants.TAG_MESSAGE).equals("Reported successfully")) {
                                App.dialog(getContext(), "", getResources().getString(R.string.report_success_alert), getResources().getColor(R.color.color_green));
                                homeApiResponse.get(adapterPosition).setIsVideoReported(true);
                                followingExoplayerRecyclerView.setPlayControl(true);

                            } else if (reportResponse.get(Constants.TAG_MESSAGE).equals("Report Undo successfully")) {
                                App.dialog(getContext(), "", getResources().getString(R.string.undo_report_successfully), getResources().getColor(R.color.colorBlack));
                                homeApiResponse.get(adapterPosition).setIsVideoReported(false);
                            }

                            if (reportDialog != null && reportDialog.isShowing())
                                reportDialog.dismiss();

                            getSelectedReport = "";
                            if (bottomSheetLongPressDi.isShowing())
                                bottomSheetLongPressDi.dismiss();
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
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }


    private void favApi(boolean report, int adapterPosition, String type) {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, homeApiResponse.get(adapterPosition).getVideoId());
            map.put(Constants.TAG_FAV_TYPE, type);

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
                                homeApiResponse.get(adapterPosition).setVideoIsFavorite(false);
                                App.dialog(getContext(), "", getResources().getString(R.string.add_fav_video_removed), getResources().getColor(R.color.colorBlack));
                            } else {
                                homeApiResponse.get(adapterPosition).setVideoIsFavorite(true);
                                App.dialog(getContext(), "", getResources().getString(R.string.add_fav_video_success), getResources().getColor(R.color.color_green));
                            }

                            if (bottomSheetLongPressDi.isShowing()) {
                                followingExoplayerRecyclerView.setPlayControl(true);
                                bottomSheetLongPressDi.dismiss();
                            }

                            //  videoAdapter.notifyItemChanged(adapterPosition);

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
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }


    private void heartFun(int adapterPosition) {

        if (NetworkReceiver.isConnected()) {

            Map<String, String> map = new HashMap<>();

            map.put("user_id", GetSet.getUserId());
            map.put("video_id", homeApiResponse.get(adapterPosition).getVideoId());
            map.put("token", GetSet.getAuthToken());

            Call<Map<String, String>> call = apiInterface.getHeartStatus(map);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    Timber.d("onResponse: %s", response.body());

                    if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {


                        Timber.d("onResponse: %s", response.body());
                        likePrevent = false;

                        String userType = "", publisherId = homeApiResponse.get(adapterPosition).getPublisherId();

                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }

                        Bundle payload = new Bundle();
                        if (response.body().get("message").equals("UnLiked Successfully")) {
                            EventBus.getDefault().post(new ForYouVideoLike((homeApiResponse.get(adapterPosition).getVideoId()), "unliked", response.body().get("likecount")));
                            homeApiResponse.get(adapterPosition).setIsLiked(false);
                            payload.putString("likes_status", "false");
                        } else {
                            EventBus.getDefault().post(new ForYouVideoLike((homeApiResponse.get(adapterPosition).getVideoId()), "liked", response.body().get("likecount")));
                            homeApiResponse.get(adapterPosition).setIsLiked(true);
                            payload.putString("likes_status", "true");
                        }
                        payload.putString("likes_count", response.body().get("likecount"));
                        homeApiResponse.get(adapterPosition).setLikes(response.body().get("likecount"));
                        videoAdapter.notifyItemChanged(adapterPosition, payload);
                        EventBus.getDefault().post(new FollowingProfileUpdate(homeApiResponse.get(adapterPosition).getPublisherId(), homeApiResponse.get(adapterPosition).getPublisherImage(), userType));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {


                }
            });

        } else {
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }


    private void openGiftDialog() {

        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_gift_home, null);
        giftDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog); // Style here
        giftDialog.setContentView(sheetView);

        giftDialog.setOnDismissListener(dialogInterface -> {

        });

        viewPager = sheetView.findViewById(R.id.view);
        sendLay = sheetView.findViewById(R.id.sendLay);
        txtAttachmentName = sheetView.findViewById(R.id.txtAttachmentName);
        txtSend = sheetView.findViewById(R.id.txtSend);
        pagerIndicator = sheetView.findViewById(R.id.pagerIndicator);

        tvPurchaseVote=sheetView.findViewById(R.id.tvPurchaseVote);
        tvNumberOfVote=sheetView.findViewById(R.id.tvNumberOfVote);

        setViewPager();

        tvNumberOfVote.setText("Vote Available "+GetSet.getGems().intValue());

        tvPurchaseVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
            }
        });


        sendLay.setVisibility(View.GONE);
        giftDialog.show();

        giftDialog.setOnShowListener(dialog -> {

        });


    }


    private void setViewPager() {
        //        int count;
//        if (AdminData.giftList.size() <= ITEM_LIMIT) {
//            count = 1;
//        } else {
//            count = AdminData.giftList.size() % ITEM_LIMIT == 0 ? AdminData.giftList.size() / ITEM_LIMIT : (AdminData.giftList.size() / ITEM_LIMIT) + 1;
//        }

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(context, 1, Constants.TYPE_GIFTS);
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


    private void loadItems() {

        /*Set Array list by 8*/

//        int start = viewPager.getCurrentItem() * ITEM_LIMIT;
//        int end = start + (ITEM_LIMIT - 1);
//
//        if (end > AdminData.giftList.size()) {
//            end = AdminData.giftList.size() - 1;
//        } else if (AdminData.giftList.size() <= end) {
//            end = AdminData.giftList.size() - 1;
//        }
        loadGifts(0, 11);
    }


    private void loadGifts(int start, int end) {

        tempGiftList = new ArrayList<>();

        /*tempGiftList used only for display, For send AdminData.giftList used*/

        for (int i = start; i <= end; i++) {
            tempGiftList.add(AdminData.giftList.get(i));
        }
        giftAdapter = new GiftAdapter(context, tempGiftList);
        Timber.d("loadGifts: %s", new Gson().toJson(tempGiftList));
        stickerLayoutManager = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();


        txtSend.setOnClickListener(view -> {
            int position = (viewPager.getCurrentItem() * ITEM_LIMIT) + giftPosition;
            sendLay.setVisibility(GONE);
            txtAttachmentName.setText("");
            finalGiftPosition = position;
            sendGift(finalGiftPosition);
           /* sendGift();

            sendLay.setVisibility(View.GONE);
            txtAttachmentName.setText("");*/

        });

    }

    public void sendGift(int finalGiftPosition) {

        if (NetworkReceiver.isConnected()) {

            if (SelectedVideoPosition >= 0) {
                HashMap<String, String> map = new HashMap<>();

                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                map.put(Constants.TAG_GIFT_ID, AdminData.giftList.get(finalGiftPosition).getGiftId());
                map.put(Constants.TAG_VIDEO_ID, String.valueOf(homeApiResponse.get(SelectedVideoPosition).getVideoId()));
                map.put(Constants.TAG_GIFT, String.valueOf(AdminData.giftList.get(finalGiftPosition).getGiftGems()));
                Timber.d("sendGift: %s", App.getGsonPrettyInstance().toJson(map));

                Call<Map<String, String>> call = apiInterface.sendGift(map);


                call.enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                        Timber.d("onResponse: %s", response.body());
                        giftDialog.dismiss();

                        if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            Timber.d("onResponse: %s", response.body());

                            // Update home foryou video
                            // update home following video
                            EventBus.getDefault().post(new ForYouUpdateGiftMessageCount(homeApiResponse.get(SelectedVideoPosition).getVideoId(),
                                    "gift",
                                    response.body().get("giftcount"),
                                    response.body().get("video_vote_count"),
                                    response.body().get("lifetime_vote_count")));

                            Bundle payload = new Bundle();
                            payload.putString("gift_count", response.body().get("video_vote_count"));
                            payload.putString("publisher_vote_count", response.body().get("lifetime_vote_count"));
                            homeApiResponse.get(SelectedVideoPosition).setVotes_count(response.body().get("lifetime_vote_count"));
                            homeApiResponse.get(SelectedVideoPosition).setVideoGiftCounts(response.body().get("giftcount"));
                            videoAdapter.notifyItemChanged(SelectedVideoPosition, payload);
                            Toast.makeText(context, "Vote send successfully!", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override

                    public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {

                    }
                });
            }

        } else {
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
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

    public class GiftAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private final List<Gift> giftList;
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

            if (holder instanceof GiftAdapter.MyViewHolder) {

                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(View.INVISIBLE);

                Glide.with(context)
                        .load(gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setImageDrawable(context.getDrawable(R.drawable.gift));
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((GiftAdapter.MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setImageDrawable(resource);
                                ((GiftAdapter.MyViewHolder) holder).giftImage.setVisibility(View.VISIBLE);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.gift))
                        .into(((GiftAdapter.MyViewHolder) holder).giftImage);


                ((GiftAdapter.MyViewHolder) holder).txtGiftPrice.setText((GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" + gift.getGiftGems());


            } else if (holder instanceof GiftAdapter.FooterViewHolder) {
                GiftAdapter.FooterViewHolder footerHolder = (GiftAdapter.FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
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
                        (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                                GetSet.getGems() >= giftList.get(getAdapterPosition()).getGiftGems())) {
                    giftPosition = getAdapterPosition();
                    sendLay.setVisibility(View.VISIBLE);
                    txtSend.setVisibility(View.VISIBLE);
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
        int getPosition;
        TimeAgo timeAgo;

        public CommentAdapter(Context mContext, ArrayList<VideoCommentResponse.Result> commendList, String publisherId, String videoId, int adapterPosition) {
            this.mContext = mContext;
            this.commendList = commendList;
            this.getVideoId = videoId;
            this.getPosition = adapterPosition;
            this.publisherId = publisherId;

            timeAgo = new TimeAgo(mContext);
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_comment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {


            Glide.with(getContext())
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
                        Intent profile = new Intent(getActivity(), OthersProfileActivity.class);
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

                    if (NetworkReceiver.isConnected()) {

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

                                    Map<String, String> data = response.body();

                                    Timber.d("delete onResponse: %s", new Gson().toJson(response.body()));

                                    if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {


                                        commendList.remove(getAdapterPosition());
                                        /*commendAdapter.notifyItemRemoved(getAdapterPosition());*/
                                        commendAdapter.notifyItemRangeRemoved(getAdapterPosition(), 1);

                                        mSwipeVideoComment.setEnabled(false);
                                        mSwipeVideoComment.setRefreshing(false);

                                        commentCount.setText(commendList.size() + " " + getResources().getString(R.string.comments));
                                        commentCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));

                                        Bundle payload = new Bundle();
                                        homeApiResponse.get(getPosition).setVideoCommentCounts(String.valueOf(commendList.size()));
                                        payload.putString("video_comment_count", String.valueOf(commendList.size()));
                                        videoAdapter.notifyItemChanged(getPosition, payload);
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

                    } else {
                        Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }
                });

                profileImage.setOnClickListener(v -> {
                    Intent profile;
                    /*if (commendList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        profile = new Intent(getContext(), MyProfileActivity.class);
                    } else {
                        profile = new Intent(getContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, commendList.get(getAdapterPosition()).getUserImage());
                    }*/
                    profile = new Intent(getContext(), OthersProfileActivity.class);
                    profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                    profile.putExtra(Constants.TAG_USER_IMAGE, commendList.get(getAdapterPosition()).getUserImage());
                    startActivityForResult(profile, Constants.COMMENT_MUSIC_REQUEST_CODE);
                });

                userName.setOnClickListener(v -> {
                    Intent profile;
                    /*if (commendList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        profile = new Intent(getContext(), MyProfileActivity.class);
                        startActivity(profile);
                    } else {

                    }*/
                    profile = new Intent(getContext(), OthersProfileActivity.class);
                    profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                    profile.putExtra(Constants.TAG_USER_IMAGE, commendList.get(getAdapterPosition()).getUserImage());
                    startActivityForResult(profile, Constants.COMMENT_MUSIC_REQUEST_CODE);

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
            return new MyViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(final CommentUserAdapter.MyViewHolder holder, int position) {
            try {
                final UserList.Result tempMap = Items.get(position);


                Glide.with(getContext()).load(tempMap.getUserImage())
                        .thumbnail(0.5f)
                        .apply(new RequestOptions().placeholder(R.drawable.default_profile_image).error(R.drawable.default_profile_image))
                        .into(holder.img);

                holder.user_name.setText(tempMap.getName());

                holder.user_name.setOnClickListener(v -> {
                   /* tagging = false;
                    char first;
                    String cmt = commentEditText.getText().toString().trim();
                    String[] cmt_arr = cmt.split(" ");
                    if (cmt_arr.length > 1) {
                        cmt = cmt_arr[cmt_arr.length - 1];
                        first = cmt.charAt(0);
                    } else {
                        first = cmt.charAt(0);
                    }
                    if (first == '@' && cmt_arr.length == 1) {
                        *//*String pos = "@" + holder.user_name.getText().toString().trim() + " ";*//*
                        String pos = "@" + tempMap.getUserName().trim() + " ";

                        if (tagcomment != null && !tagcomment.equals("")) {
                            *//*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*//*
                            tagcomment += " " + "@" + tempMap.getUserName().trim() + " ";
                            ;

                        } else {
                            *//*tagcomment += "@" + holder.user_name.getText().toString().trim();*//*
                            tagcomment += "@" + tempMap.getUserName().trim() + " ";
                            ;

                        }
                        commentEditText.setText(pos);
                        commentEditText.setSelection(pos.length());

                    } else if (first == '@' && cmt_arr.length > 1) {
                        *//*String pos = "@" + holder.user_name.getText().toString() + " ";*//*
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

                        if (cmt_arr.length <= 5) {
                            if (tagcomment != null && !tagcomment.equals("")) {
                                *//*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*//*
                                tagcomment += " " + "@" + tempMap.getUserName().trim();

                            } else {
                                *//*tagcomment += "@" + holder.user_name.getText().toString().trim();*//*
                                tagcomment += "@" + tempMap.getUserName().trim();

                            }
                            commentEditText.setText(pos);
                            commentEditText.setSelection(pos.length());
                        } else {

                            Timber.i("not allowed");
                        }

                    } else {
                        String[] cmtAry = cmt.split(" @");

                        *//*String pos = cmtAry[0] + " @" + holder.user_name.getText().toString().trim() + " ";*//*

                        if (cmtAry.length <= 5) {
                            String pos = cmtAry[0] + " @" + tempMap.getUserName().trim() + " ";


                            if (tagcomment != null && !tagcomment.equals("")) {
                                *//*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*//*
                                tagcomment += " " + "@" + tempMap.getUserName().trim();

                            } else {
                                *//*tagcomment += "@" + holder.user_name.getText().toString().trim();*//*
                                tagcomment += "@" + tempMap.getUserName().trim();

                            }
                            commentEditText.setText(pos);
                            commentEditText.setSelection(pos.length());
                        } else {
                            Timber.i("not allowed");
                        }

                    }


                    tagged_user_name = tempMap.getUserName();
                    list_tagged_user_names.add(tagged_user_name);

                    tagged_user_id = tempMap.getUserId();
                    list_tagged_user_ids.add(tagged_user_id);


                    Timber.d("list_tagged_user_names: %s", list_tagged_user_names);
                    Timber.d("list_tagged_user_ids: %s", list_tagged_user_ids);

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

*/
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
}