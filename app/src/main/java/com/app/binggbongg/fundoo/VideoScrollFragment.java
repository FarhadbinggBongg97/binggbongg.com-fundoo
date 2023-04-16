package com.app.binggbongg.fundoo;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.view.View.GONE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
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
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.app.binggbongg.R;
import com.app.binggbongg.data.model.Mention2;
import com.app.binggbongg.data.model.Tagged;
import com.app.binggbongg.external.TimeAgo;
import com.app.binggbongg.fundoo.home.FollowingVideoFragment;
import com.app.binggbongg.fundoo.home.eventbus.AutoScrollEnabled;
import com.app.binggbongg.fundoo.home.eventbus.ExoPlayerRecyclerView;
import com.app.binggbongg.fundoo.home.eventbus.FollowAutoScrollEnabled;
import com.app.binggbongg.fundoo.home.eventbus.FollowingHideIcon;
import com.app.binggbongg.fundoo.home.eventbus.FollowingUpdateGiftMessageCount;
import com.app.binggbongg.fundoo.home.eventbus.FollowingVideoLike;
import com.app.binggbongg.fundoo.home.eventbus.ForYouFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.ForYouProfileUpdate;
import com.app.binggbongg.fundoo.home.eventbus.ForYouUpdateGiftMessageCount;
import com.app.binggbongg.fundoo.home.eventbus.ForYouVideoFav;
import com.app.binggbongg.fundoo.home.eventbus.ForYouVideoLike;
import com.app.binggbongg.fundoo.home.eventbus.ForyouHideIcon;
import com.app.binggbongg.fundoo.home.eventbus.HideIcon;
import com.app.binggbongg.fundoo.home.eventbus.UserBlocked;
import com.app.binggbongg.fundoo.home.eventbus.VideoAutoScrollEnabled;
import com.app.binggbongg.fundoo.home.eventbus.homeForYouSwipePrevent;
import com.app.binggbongg.fundoo.profile.MyProfileActivity;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.CustomLinearLayoutManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.OnVerticalScrollListener;
import com.app.binggbongg.helper.callback.NetworkResultCallback;
import com.app.binggbongg.helper.callback.ProfileImageClickListener;
import com.app.binggbongg.model.FollowRequest;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Gift;
import com.app.binggbongg.model.HomeResponse;
import com.app.binggbongg.model.Report;
import com.app.binggbongg.model.UserList;
import com.app.binggbongg.model.VideoCommentResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.app.binggbongg.view.CustomSpannableTextView;
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
import com.hendraanggrian.appcompat.widget.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
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
import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class VideoScrollFragment extends Fragment {

    public static String TAG = VideoScrollFragment.class.getSimpleName();

    TextView btm_hide_menu_tv, btm_hide_yes, btm_hide_no;


    boolean isBottomBarHide= true, isAutoScroll;

    String contest_text="";
    String type, tagName = "", soundId = "", profileId = "", jumpToVideoId = "", offset = "",
            searchQuery = "", from="";
    private int finalGiftPosition;
    private int ITEM_LIMIT = 8;


    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    public static final int LOAD_MORE_MAX_LIMIT = 10;

    public ExoPlayerRecyclerView exoplayerRecyclerViewForYou;
    private final HashSet<String> mentions = new HashSet();
    public ArrayList<String> list_tagged_user_names = new ArrayList<>();
    public ArrayList<String> list_tagged_user_ids = new ArrayList<>();

    // Video Page
    ArrayList<HomeResponse.Result> homeApiResponse = new ArrayList<>();
    ViewPager viewPager;
    CircleIndicator pagerIndicator;
    List<Gift> tempGiftList = new ArrayList<>();
    int displayHeight, displayWidth;
     GiftAdapter giftAdapter;
    LinearLayoutManager stickerLayoutManager;

    //VideoComment
    TextView commentCount, sendComment, noCommands, addComment;
    SocialAutoCompleteTextView mCommentsEditText;
    ImageView closeButton;
    RecyclerView commentRec, userRecylerView;
    CommentAdapter commendAdapter;
    CommentUserAdapter commentUserAdapter;
    LinearLayoutManager linearLayoutManager;


    // Bottomsheet
    ArrayList<VideoCommentResponse.Result> commendList = new ArrayList<>();
    RelativeLayout sendCommentLay, rootView;
    FrameLayout headerBottom;
    String loadMoreCheckVideoCommand = "initial";
    ArrayList<UserList.Result> liveUser = new ArrayList<>();
    OnVerticalScrollListener videoScrollListener;

    boolean tagging = true, isShowLoadMore = false;
    String tagcomment = "", tagged_user_name = "", tagged_user_id = "";
    ProfileImageClickListener profileImageClickListener;
    Handler handler = new Handler();
    ProgressBar loadMoreProgress;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiInterface apiInterface;
    private Context context;
    private TextView nullText;
    private RelativeLayout nullLay;
    private VideoAdapter videoAdapter;
    private ReportAdapter reportAdapter;
    private RecyclerView reportsView;
    private View bottom_sheet_longpress;
    public static BottomSheetDialog reportDialog, bottomSheetLongPressDiloag, videoCommandBottomDialog,
            VideoCommentSentDialog, giftDialog, shareDialog, hideBottomBarBS;
    private ImageView btnBack;
    private TextView txtTitle;
    private MaterialButton btnReport;
    private String getSelectedReport = "";
    private int SelectedVideoPosition = -1;
    private Boolean isViewPagerVisible = false;


    Runnable r = new Runnable() {
        @Override
        public void run() {
            Timber.i("runnable");
            if (isViewPagerVisible) {
                Timber.i("isViewPagerVisible enter");
                exoplayerRecyclerViewForYou.playVideo(false);
                exoplayerRecyclerViewForYou.setPlayControl(true);
                handler.removeCallbacks(r);
            } else {
                handler.postDelayed(r, 1000);
            }


        }
    };
    private Boolean likePrevent = false;
    private final ArrayList<Mention2> mentionsCache = new ArrayList<>();
    // TODO: 23/10/21 @VishnuKumar
    public CustomLinearLayoutManager layoutManager;
    private LinearLayout sendLay;
    private TextView txtAttachmentName, txtSend;
    private int giftPosition = 0;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeVideoComment = null;
    private int currentPage = 0;
    // used for cache
    private int lastOffsetVideo = -1;
    private int lastOffset = -1;
    private MediaLoader mMediaLoader;
    private ProgressDialog progressDialog;


    public VideoScrollFragment() {

    }


    public static VideoScrollFragment newInstance(ProfileImageClickListener mProfileImageClickListener) {

        VideoScrollFragment forYouVideoFragment = new VideoScrollFragment();
        forYouVideoFragment.profileImageClickListener = mProfileImageClickListener;
        return forYouVideoFragment;
    }

    // EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ForYouFollowFollowing event) {
        Timber.d("ForYouFollowFollowing isFollow=>  %s", event.isFollow);

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
                //  EventBus.getDefault().post(new ForYouProfileUpdate(event.PublisherId));
                videoAdapter.notifyItemChanged(i, payload);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HideIcon event) {
        Bundle payload = new Bundle();
        Log.e(TAG, "onMessageEvent: :::::::::::"+ event.iconVisible);
        isBottomBarHide = event.iconVisible;
      //  SharedPref.putBoolean(SharedPref.HIDE_ICONS, event.iconVisible);
        videoAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ForYouVideoLike event) {
        Timber.d("ForYouVideoLike =>  %s", event);

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
    public void onMessageEvent(UserBlocked event) {
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
        videoAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoAutoScrollEnabled event) {
        isAutoScroll = event.isEnabled;
        videoAdapter.notifyDataSetChanged();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ForYouUpdateGiftMessageCount event) {
        Timber.d("onMessageEvent: %s", event);

        for (int i = 0; i < homeApiResponse.size(); i++) {
            if (homeApiResponse.get(i).getVideoId().equals(event.videoId)) {

                Bundle payload = new Bundle();
                if (event.giftOrMessage.equals("gift")) {
                    payload.putString("update_gift_count", event.Count);
                    payload.putString("vote_count", event.vote_count);
                    payload.putString("publisher_vote_count", event.publisher_vote);
                    homeApiResponse.get(i).setVideoGiftCounts(event.Count);
                    homeApiResponse.get(i).setVotes_count(event.vote_count);
                    homeApiResponse.get(i).setPublisher_vote_count(event.publisher_vote);
                } else if (event.giftOrMessage.equals("message")) {
                    payload.putString("update_video_comment_count", event.Count);
                    homeApiResponse.get(i).setVideoGiftCounts(event.Count);
                }
                videoAdapter.notifyItemChanged(i, payload);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ForYouVideoFav event) {
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
        } else /*if (event.isFav.equals("Sound"))*/ {
            for (int i = 0; i < homeApiResponse.size(); i++) {
                //Here videoId means soundId
                if (homeApiResponse.get(i).getSoundtracks().getSoundId().equals(event.videoId)) {
                    Bundle payload = new Bundle();
                    homeApiResponse.get(i).getSoundtracks().setIsFavorite(event.status);
                    payload.putString(event.isFav, String.valueOf(event.status));
                    videoAdapter.notifyItemChanged(i, payload);
                }
            }
        }


    }

    public void setData(Bundle bundle) {
        if (!bundle.isEmpty()) {
            type = bundle.getString("type");
            tagName = bundle.getString("tag_name");
            soundId = bundle.getString("sound_id");
            profileId = bundle.getString("profile_id");
            jumpToVideoId = bundle.getString(Constants.EXTRA_JUMP_TO_VIDEO_ID);
            offset = bundle.getString("offset");
            searchQuery = bundle.getString("text");
            from = bundle.getString(Constants.TAG_FROM);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.video_scroll_fragment, container, false);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        mMediaLoader = MediaLoader.getInstance(getContext());

        isAutoScroll = SharedPref.getBoolean(SharedPref.isAUTO_SCROLL, false);

        initView(rootView);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Loading..");
        progressDialog.setIndeterminate(true);
        Log.e(TAG, "onMessageEvent: ::::::BTm:::::"+isBottomBarHide );
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastOffset = 0;

        exoplayerRecyclerViewForYou = view.findViewById(R.id.rcy_videoScroll);

        if(homeApiResponse!=null)
            exoplayerRecyclerViewForYou.setMediaObjects(homeApiResponse);

        videoAdapter = new VideoAdapter(homeApiResponse, initGlide());
        // TODO: 23/10/21 @VishnuKumar
        layoutManager = new CustomLinearLayoutManager(getContext());
        exoplayerRecyclerViewForYou.setLayoutManager(layoutManager);

        exoplayerRecyclerViewForYou.setAdapter(videoAdapter);

        OnVerticalScrollListener scrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                Timber.d("Load more: %d", homeApiResponse.size());
                if (homeApiResponse.size() <= lastOffset) return;
                else lastOffset = homeApiResponse.size();

                exoplayerRecyclerViewForYou.stopScroll();
                if (isShowLoadMore)
                    getForYouDataFromApi(lastOffset);
            }

            @Override
            public void onScrolledDown(int dx, int dy) {
                super.onScrolledDown(dx, dy);
                Timber.d("Load more: dx: %d dy: %d", dx, dy);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                /*if (icFavHeart != null && icFavHeart.isAnimating()) {
                    Timber.d("onScrollStateChanged: animation stop enter");
                    *//*icFavHeart.setVisibility(View.GONE);
                    icFavHeart.clearAnimation();*//*
                    //  icFavHeart.cancelAnimation();
                    //icFavHeart.getAnimation().cancel();
                    // icFavHeart.
                    // ();
                    //icFavHeart.removeAllLottieOnCompositionLoadedListener();
                }*/
            }
        };


        exoplayerRecyclerViewForYou.addOnScrollListener(scrollListener);

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
        snapHelper.attachToRecyclerView(exoplayerRecyclerViewForYou); // Recyclerview one position take full screen

    }


    private void initView(@NotNull View rootView) {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        loadMoreProgress = rootView.findViewById(R.id.loadMoreProgress);
        // Null Layout
        ImageView nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
        nullLay = rootView.findViewById(R.id.nullLay);
        nullText.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_video));

        nullLay.setVisibility(View.GONE);

        EventBus.getDefault().post(new homeForYouSwipePrevent(true, ""));

        isViewPagerVisible = true;

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh(true);

        if (GetSet.getUserId() != null)
            getForYouDataFromApi(0);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeApiResponse.clear();
            videoAdapter.notifyDataSetChanged();
            swipeRefresh(true);
            exoplayerRecyclerViewForYou.onPausePlayer();
            EventBus.getDefault().post(new homeForYouSwipePrevent(true, ""));
            Timber.d("initView: swipe called");

            isViewPagerVisible = true;
            lastOffset = 0;
            getForYouDataFromApi(lastOffset);

        });

        Log.e(TAG, "initView: :::::::::::::::::::" + (getArguments() != null ? getArguments().getBoolean("isShow") : false));
    }

    public void hideIcons(String videoId, int position){
        View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_hide_menu_bar, null);
        hideBottomBarBS = new BottomSheetDialog(getContext(), R.style.VideoCommentDialog); // Style here

        hideBottomBarBS.setContentView(sheetView);

        hideBottomBarBS.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        btm_hide_menu_tv = hideBottomBarBS.findViewById(R.id.alert_tv);
        btm_hide_yes = hideBottomBarBS.findViewById(R.id.hide_btm_bar_yes);
        btm_hide_no = hideBottomBarBS.findViewById(R.id.hide_btm_bar_no);

        if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
            btm_hide_menu_tv.setText(requireContext().getString(R.string.unhide_btm_bar_alert));
        } else {
            btm_hide_menu_tv.setText(requireContext().getString(R.string.hide_btm_bar_alert));
        }

        btm_hide_yes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Bundle payload = new Bundle();
                if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, false);
                    isBottomBarHide = false;
                } else {
                    SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);
                    isBottomBarHide = true;
                }
                payload.putString("hide_icon", String.valueOf(isBottomBarHide));
                videoAdapter.notifyItemChanged(position, payload);
                if (hideBottomBarBS!=null && hideBottomBarBS.isShowing()) {
                    hideBottomBarBS.dismiss();
                }
                // videoAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new ForyouHideIcon(isBottomBarHide, videoId));
                EventBus.getDefault().post(new FollowingHideIcon(isBottomBarHide, videoId));
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

    private void getForYouDataFromApi(int lastOffset) {
        nullLay.setVisibility(View.GONE);
        if (NetworkReceiver.isConnected()) {

            if (lastOffset != 0) {
                videoAdapter.showLoader();
                Timber.d("Loading: ??");
            }
            HashMap<String, String> params = new HashMap<>();
            params.put("offset", offset);
            params.put("limit","10");
            params.put("user_id", GetSet.getUserId());
            if(!TextUtils.isEmpty(profileId)){
                params.put("profile_id", profileId);
            }else{
                params.put("profile_id", "");
            }

            params.put("type", type);

            if(!TextUtils.isEmpty(tagName)){
                params.put("hashtag_name", tagName);
            }else{
                params.put("hashtag_name", "");
            }
            if(!TextUtils.isEmpty(searchQuery)){
                params.put("search_key", searchQuery);
            }else{
                params.put("search_key", "");
            }
            if(!TextUtils.isEmpty(soundId)){
                params.put("sound_id", soundId);
            }else{
                params.put("sound_id", "");
            }

            Timber.d("getVideoApi: params %s", params);

            Call<HomeResponse> call = apiInterface.getScrollVideos(params);
            call.enqueue(new Callback<HomeResponse>() {

                @Override
                public void onResponse(@NotNull Call<HomeResponse> call, @NotNull Response<HomeResponse> response) {

                    Timber.d("getVideoApi: params %s", new Gson().toJson(response.body()));
                    if (response != null && response.body().getStatus() != null && response.body().getStatus().equals(Constants.TAG_TRUE)) {

                        //  Timber.i("Nextloadmore %s", response.body().getLoadMore());

                        videoAdapter.hideLoader();
                        nullLay.setVisibility(View.GONE);
                        swipeRefresh(false);
                        Timber.d("onResponse for you: %s", App.getGsonPrettyInstance().toJson(response.body().getResult()));
                        if (response.body().getResult() != null) {

                            //  isShowLoadMore = Boolean.parseBoolean(response.body().getLoadMore());
                            contest_text = response.body().getContest_text();
                            if (lastOffset == 0) {
                                homeApiResponse.clear();
                                homeApiResponse.addAll(response.body().getResult());
                                videoAdapter.notifyDataSetChanged();
                                handler.postDelayed(r, 1000);

                            } else {
                                videoAdapter.hideLoader();
                                int tempPosition = homeApiResponse.size();
                                homeApiResponse.addAll(response.body().getResult());

                                videoAdapter.notifyItemRangeInserted(tempPosition, response.body().getResult().size());
                                Timber.d("Load more: pos start %d to %d", tempPosition, response.body().getResult().size());

                            }
                            //  preDownloadVideos();
                        } else {
                            Toasty.info(requireContext(), "No Videos found", Toasty.LENGTH_SHORT).show();
                        }

                    }

                    if (homeApiResponse.size() == 0) {
                        swipeRefresh(false);
                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, ""));
                        if (nullLay.getVisibility() != View.VISIBLE) {
                            if (GetSet.getUserId() != null) {
                                nullLay.setVisibility(View.VISIBLE);
                                nullText.setText(getString(R.string.no_broadcast_found));
                            } else {
                                swipeRefresh(true);
                            }
                        }
                    } else EventBus.getDefault().post(new homeForYouSwipePrevent(false, ""));

                    videoAdapter.hideLoader();
                }


                @Override
                public void onFailure(@NotNull Call<HomeResponse> call, @NotNull Throwable t) {
                    Timber.e("onFailure: %s", t.getMessage());
                    videoAdapter.hideLoader();
                }
            });
        } else {
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
            swipeRefresh(false);
        }
    }


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


    private void preDownloadVideos() {

        for (HomeResponse.Result home : homeApiResponse) {
            if (home.getPlaybackUrl() != null && !home.getPlaybackUrl().isEmpty()) {

                boolean isCached = mMediaLoader.isCached(home.getPlaybackUrl());
                //     if (!isCached && getContext() != null)
                //DownloadManager.getInstance(getContext()).enqueue(new DownloadManager.Request(home.getPlaybackUrl()), this);
            }
        }

    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.MUSIC_REQUEST_CODE) {
            exoplayerRecyclerViewForYou.setPlayControl(true);
        } else if (requestCode == Constants.HASHTAG_REQUEST_CODE) {
            exoplayerRecyclerViewForYou.setPlayControl(true);
        } else if (requestCode == Constants.COMMENT_MUSIC_REQUEST_CODE) {
            exoplayerRecyclerViewForYou.setPlayControl(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("resume called");
        isViewPagerVisible = true;

        exoplayerRecyclerViewForYou.setPlayControl(videoCommandBottomDialog == null || !videoCommandBottomDialog.isShowing());

        if (!NetworkReceiver.isConnected()) {
            if (swipeRefreshLayout.isRefreshing()) swipeRefresh(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        exoplayerRecyclerViewForYou.setPlayControl(false);
        isViewPagerVisible = false;

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

    private void shareVideoDialog(HomeResponse.Result item) {

        String link=item.getShareLink();

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

            ContentResolver contentResolver = requireActivity().getContentResolver();

            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData data = ClipData.newUri(contentResolver, "label", Uri.parse(link));
                clipboardManager.setPrimaryClip(data);
                Toasty.success(getContext(), R.string.linkCopied, Toasty.LENGTH_SHORT).show();
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
            Log.d(TAG, "App installed" + installed);
            if (installed) {
                callShareAPI(item,"Facebook");
                ShareLinkContent fbShare = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse( link))
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
            Log.d(TAG, "App installed" + installed);
            if (installed) {
                callShareAPI(item,"messenger");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,  link);
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
            Log.d(TAG, "App installed" + installed);
            if (installed) {
                callShareAPI(item,"instagram");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,link);
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
            Log.d(TAG, "App installed" + installed);
            if (installed) {
                callShareAPI(item,"twitter");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,  link);
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
            Log.d(TAG, "App installed" + installed);
            if (installed) {
                callShareAPI(item,"snapchat");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,  link);
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
//            callShareAPI(item,"more");
            Intent email = new Intent(Intent.ACTION_SEND);
//                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{ });
//                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
            email.putExtra(Intent.EXTRA_TEXT,  link);

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

    private void callShareAPI(HomeResponse.Result item, String type) {

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
                        Toast.makeText(context, ""+response.body().get("message").toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                Log.e(TAG, "onFailure: :::::::::::::::::share failure::",t );
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
            Log.e(TAG, "App installed Exception " + e);
            app_installed = false;
        }
        return app_installed;
    }

    private void reportAndFav(int adapterPosition) {

        LinearLayout favLay, reportLay;

        TextView txtFav, txtReport;

        bottom_sheet_longpress = getLayoutInflater().inflate(R.layout.bottmsheet_home_longpress, null);
        bottomSheetLongPressDiloag = new BottomSheetDialog(requireContext(), R.style.FavAndReportBottomSheetDialog); // Style here
        // TODO: 23/10/21 @VishnuKumar
        layoutManager.setSheetDialog(bottomSheetLongPressDiloag);
        bottomSheetLongPressDiloag.setContentView(bottom_sheet_longpress);

        favLay = bottom_sheet_longpress.findViewById(R.id.favLay);
        txtFav = bottom_sheet_longpress.findViewById(R.id.txtFav);
        reportLay = bottom_sheet_longpress.findViewById(R.id.reportLay);
        txtReport = bottom_sheet_longpress.findViewById(R.id.txtReport);


        Timber.d("foryou VideoReported: %s", homeApiResponse.get(0).getIsVideoReported());
        Timber.d("foryou VideoIsFavorite: %s", homeApiResponse.get(0).getVideoIsFavorite());

        if (homeApiResponse.get(adapterPosition).getVideoIsFavorite())
            txtFav.setText(R.string.unFav);
        else txtFav.setText(R.string.addafav);

        if (homeApiResponse.get(adapterPosition).getIsVideoReported())
            txtReport.setText(R.string.unreported);
        else txtReport.setText(R.string.report_user);

        favLay.setOnClickListener(v -> favApi(true, adapterPosition, "video"));


        reportLay.setOnClickListener(v -> {

            if (txtReport.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.report_user))) {
                exoplayerRecyclerViewForYou.setPlayControl(false);
                openReportDialog(adapterPosition);
            } else {
                sendReport("getSelectedReport", false, adapterPosition);
            }

        });
        bottomSheetLongPressDiloag.show();
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
            exoplayerRecyclerViewForYou.setPlayControl(true);
            reportDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            App.preventMultipleClick(btnReport);
            Timber.d("openReportDialog: %s", getSelectedReport);
            sendReport(getSelectedReport, true, adapterPosition);
        });

        reportDialog.setOnKeyListener((v, keyCode, event) -> {

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                exoplayerRecyclerViewForYou.setPlayControl(true);
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

            Call<Map<String, String>> call = apiInterface.reportStream(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();

                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (reportResponse.get(Constants.TAG_MESSAGE).equals("Reported successfully")) {
                                App.dialog(getContext(), "", getResources().getString(R.string.report_success_alert), getResources().getColor(R.color.color_green));
                                homeApiResponse.get(adapterPosition).setIsVideoReported(true);
                                exoplayerRecyclerViewForYou.setPlayControl(true);

                            } else if (reportResponse.get(Constants.TAG_MESSAGE).equals("Report Undo successfully")) {
                                App.dialog(getContext(), "", getResources().getString(R.string.undo_report_successfully), getResources().getColor(R.color.colorBlack));
                                homeApiResponse.get(adapterPosition).setIsVideoReported(false);
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

                            if (bottomSheetLongPressDiloag.isShowing()) {
                                exoplayerRecyclerViewForYou.setPlayControl(true);
                                bottomSheetLongPressDiloag.dismiss();
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
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void heartFun(int adapterPosition) {
        if (NetworkReceiver.isConnected()) {

            Map<String, String> map = new HashMap<>();

            map.put("user_id", GetSet.getUserId());
            map.put("video_id", homeApiResponse.get(adapterPosition).getVideoId());
            map.put("token", GetSet.getAuthToken());

            Timber.d("heartFun: %s", App.getGsonPrettyInstance().toJson(map));

            Call<Map<String, String>> call = apiInterface.getHeartStatus(map);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {


                    Timber.d("onResponse: %s", response.body());

                    if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                        likePrevent = false;

                        String userType = "", publisherId = homeApiResponse.get(adapterPosition).getPublisherId();

                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }


                        Bundle payload = new Bundle();
                        if (response.body().get("message").equals("UnLiked Successfully")) {

                            homeApiResponse.get(adapterPosition).setIsLiked(false);
                            payload.putString("likes_status", "false");
                            EventBus.getDefault().post(new FollowingVideoLike((homeApiResponse.get(adapterPosition).getVideoId()), "unliked", response.body().get("likecount")));
                        } else {
                            homeApiResponse.get(adapterPosition).setIsLiked(true);
                            payload.putString("likes_status", "true");
                            EventBus.getDefault().post(new FollowingVideoLike((homeApiResponse.get(adapterPosition).getVideoId()), "liked", response.body().get("likecount")));
                        }

                        payload.putString("likes_count", response.body().get("likecount"));
                        homeApiResponse.get(adapterPosition).setLikes(response.body().get("likecount"));
                        videoAdapter.notifyItemChanged(adapterPosition, payload);


                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }
                        EventBus.getDefault().post(new ForYouProfileUpdate(homeApiResponse.get(adapterPosition).getPublisherId(), homeApiResponse.get(adapterPosition).getPublisherImage(), userType));


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
        giftDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialog); // Style here
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

       /* txtSend.setOnClickListener(view -> {

            sendGift();

            sendLay.setVisibility(View.GONE);
            txtAttachmentName.setText("");

        });*/

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
        Toast.makeText(getContext(), "Video Scroll Fragment", Toast.LENGTH_SHORT).show();

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(context, count, Constants.TYPE_GIFTS);
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
        giftAdapter = new GiftAdapter(context, tempGiftList);
        Timber.d("loadGifts: %s", new Gson().toJson(tempGiftList));
        stickerLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();


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
                        Timber.d("onResponse::::::::::::::: %s", response.body());
                        giftDialog.dismiss();
                        if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            Timber.d("onResponse: %s", response.body());

                            // update home following video
                            EventBus.getDefault().post(new FollowingUpdateGiftMessageCount(homeApiResponse.get(SelectedVideoPosition).getVideoId(),
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

                        }else{
                            Toast.makeText(context, response.body().get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override

                    public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                        Log.e(TAG, "onFailure: :::::::::::::Send Gift Fail:::::",t );
                    }
                });
            }

        } else {
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }


    private void testfun(String test) {

        Timber.d("testfun: hello");

        int abc;


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

    public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final String TAG = VideoAdapter.class.getSimpleName();

        private final int VIEW_TYPE_NORMAL = 0;
        private final int VIEW_TYPE_LOADING = 1;

        ArrayList<HomeResponse.Result> mHomeApiResponse;
        RequestManager options;

        private boolean loading;

        public VideoAdapter(ArrayList<HomeResponse.Result> homeApiResponse, RequestManager requestManager) {
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

            if (holder instanceof VideoAdapter.VideoViewHolder) {

                Timber.d("VideoAdapter onBindViewHolder: %s", App.getGsonPrettyInstance().toJson(mHomeApiResponse.get(position)));
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
                    final String updateGiftCount = payload.getString("update_gift_count", null);
                    final String videoCommentCount = payload.getString("video_comment_count", null);
                    final String follow = payload.getString("follow", null);
                    final String Report = payload.getString("Report", null);
                    final String Fav = payload.getString("Fav", null);
                    final String follower_count = payload.getString("followers", "0");
                    final String publisher_vote_count = payload.getString("publisher_vote_count", "0");
                    final String view_count = payload.getString("video_views_count", null);
                    final String hideIcon = payload.getString("hide_icon", "true");

                    if (view_count != null) {
                        ((VideoViewHolder) holder).txt_view_count.setText(view_count);
                    } else {
                        ((VideoViewHolder) holder).txt_view_count.setText("0");
                    }

                    if (likesCount != null) {
                        if (likesStatus.equals("true")) {
                            ((VideoViewHolder) holder).heart.setImageResource(R.drawable.heart_color);
                        } else {
                            ((VideoViewHolder) holder).heart.setImageResource(R.drawable.heart_white);
                        }
                        ((VideoViewHolder) holder).heartAni.cancelAnimation();
                        ((VideoViewHolder) holder).heartAni.clearAnimation();
                        ((VideoViewHolder) holder).heartAni.setVisibility(View.GONE);
                        ((VideoViewHolder) holder).txt_heart_count.setText(likesCount);
                        ((VideoViewHolder) holder).heart.setVisibility(View.VISIBLE);
                        ((VideoViewHolder) holder).txt_heart_count.setVisibility(View.VISIBLE);

                    } else if (giftCount != null) {

                        ((VideoViewHolder) holder).animGiftlay.setVisibility(View.VISIBLE);
                        ((VideoViewHolder) holder).animGiftlay.clearAnimation();


                        ObjectAnimator anim = ObjectAnimator
                                .ofFloat(((VideoViewHolder) holder).animGiftlay, "translationX", 0, 25, -25, 25, -25, 15, -15, 10, -6, 0)
                                .setDuration(2000);
                        anim.addListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                ((VideoViewHolder) holder).animGiftlay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out_length));
                                Glide.with(getActivity())
                                        .load(tempGiftList.get(giftPosition).getGiftIcon())
                                        .into(((VideoViewHolder) holder).animGiftImage);
                            }

                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                                ((VideoViewHolder) holder).animGiftlay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));

                                ((VideoViewHolder) holder).giftImageView.setImageDrawable(getResources().getDrawable(R.drawable.eye));
                                ((VideoViewHolder) holder).animGiftImage.setVisibility(View.GONE);
                            }
                        });
                        anim.start();
                        ((VideoViewHolder) holder).txt_vote_count.setText(giftCount);
                        ((VideoViewHolder) holder).publisher_vote_count.setText(publisher_vote_count);

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
                            //   videoLinkLay.setVisibility(View.GONE);
                            ((VideoViewHolder) holder).videoLabelsLay.setVisibility(View.GONE);
                        } else {
                            // videoLinkLay.setVisibility(View.VISIBLE);
                            ((VideoViewHolder) holder).videoLabelsLay.setVisibility(View.VISIBLE);
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

            /*mSwipeVideoComment.setOnRefreshListener(() -> swipeRefresh(true, videoId, adapterPosition));*/


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
                exoplayerRecyclerViewForYou.setPlayControl(true);
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
                        exoplayerRecyclerViewForYou.setPlayControl(false);
                        return true;
                    } else {
                        exoplayerRecyclerViewForYou.setPlayControl(true);
                        return false;
                    }
                }
                Timber.i("keyCode check %s", "return");
                return false;
            });
        }

        private void topBottomSheet(String videoId, int adapterPosition, String setText) {

            View commentSentDialog = getLayoutInflater().inflate(R.layout.bottom_sheet_comment_sent, null);
            VideoCommentSentDialog = new BottomSheetDialog(getContext(), R.style.Command);

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
            /*mCommentsEditText.setThreshold(1);
            mCommentsEditText.setDropDownAnchor(R.id.rootView);
            mCommentsEditText.setDropDownHeight(0);  // don't show popup*/

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
                        adapter.notifyDataSetChanged();
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
            mCommentsEditText.requestFocus();

            mCommentsEditText.setText(setText);

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
                exoplayerRecyclerViewForYou.setPlayControl(true);
                videoCommandBottomDialog.setDismissWithAnimation(true);
                videoCommandBottomDialog.dismiss();
                VideoCommentSentDialog.dismiss();
            });


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

                            Timber.d("getCommentResponse=> %s", new Gson().toJson(response.body()));

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


        private class LoadingViewHolder extends RecyclerView.ViewHolder {

            public LoadingViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
            }
        }


        public class VideoViewHolder extends RecyclerView.ViewHolder {

            //private final ScaleAnimation scaleAnimation;
            public ConstraintLayout parentLay;
            public ProgressBar progressBar, progress_duration;
            public ImageView music_symbol, video_thumbnail, profileImage, video_play_pause, animGiftImage;
            public TextView txt_description, txt_title, txt_heart_count, txt_gift_count,
                    txt_comment_count, txt_view_count, txt_vote_count;
            public LinearLayout heartLay, videoLinkLay, videoCommandLay, shareLay,/* giftLay,*/
                    hideBtmBarLay, autoScrollLay, animGiftlay;
            public FrameLayout mediaContainer;
            public RelativeLayout leftSideView, videoLabelsLay, giftLay;
            public LottieAnimationView heartAni;
            public ShapeableImageView heart, giftImageView, autoScrollIV;
            public LottieAnimationView icFavHeart;

            public MaterialTextView profileFollowIcon, FollowersCount, time_reverse_tv, vote_promotionTV, publisher_vote_count;
            public LinearLayout liveStreaming;

            @SuppressLint("UseCompatLoadingForDrawables")

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
                FollowersCount = itemView.findViewById(R.id.total_followers_tv);
                videoLabelsLay = itemView.findViewById(R.id.videoLabelsLay);
                time_reverse_tv = itemView.findViewById(R.id.remain_duration_tv);
                autoScrollLay = itemView.findViewById(R.id.autoLay);
                autoScrollIV = itemView.findViewById(R.id.autoImageView);
                vote_promotionTV = itemView.findViewById(R.id.voteTV);
                publisher_vote_count = itemView.findViewById(R.id.publisher_vote_count);
                txt_view_count = itemView.findViewById(R.id.txt_view_count);
                txt_vote_count = itemView.findViewById(R.id.txt_vote_count);
                animGiftlay = itemView.findViewById(R.id.lay_animGift);
                animGiftImage = itemView.findViewById(R.id.img_animGift);


                autoScrollLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isAutoScroll) {
                            isAutoScroll = false;
                            Toast.makeText(getContext(), getString(R.string.auto_scroll_off), Toast.LENGTH_SHORT).show();
                        } else {
                            isAutoScroll = true;
                            Toast.makeText(getContext(), getString(R.string.auto_scroll_on), Toast.LENGTH_SHORT).show();
                        }

                        AutoScrollAPI(isAutoScroll, getAdapterPosition());
                        updateAutoScrollUI();

                    }
                });

                hideBtmBarLay.setOnClickListener(view -> {
                    //hideBottomBarBS.show();
                   // hideIcons(mHomeApiResponse.get(getAdapterPosition()).getVideoId(), getAdapterPosition());
                    Bundle payload = new Bundle();
                    if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                        Toast.makeText(requireActivity(), "Your i-cons are now unhidden", Toast.LENGTH_LONG).show();
                        SharedPref.putBoolean(SharedPref.HIDE_ICONS, false);
                        isBottomBarHide = false;
                    } else {
                        Toast.makeText(requireActivity(), "Your i-cons are now hidden", Toast.LENGTH_LONG).show();
                        SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);
                        isBottomBarHide = true;
                    }
                    payload.putString("hide_icon", String.valueOf(isBottomBarHide));
                    videoAdapter.notifyItemChanged(getAdapterPosition(), payload);
                    EventBus.getDefault().post(new ForyouHideIcon(isBottomBarHide, homeApiResponse.get(getAdapterPosition()).getVideoId()));
                    EventBus.getDefault().post(new FollowingHideIcon(isBottomBarHide, homeApiResponse.get(getAdapterPosition()).getVideoId()));
                });


                // TODO: add double-tap heart
                final GestureDetector gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        heartFun(getAdapterPosition());
                        Timber.d("onDoubleTap: enter");
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

                        }
                        */
                        icFavHeart.setVisibility(View.VISIBLE);
                        /*icFavHeart.playAnimation();
                        icFavHeart.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.heart_animation));*/

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

                        icFavHeart.post(() -> {
                            icFavHeart.playAnimation();
                            icFavHeart.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.heart_animation)); // need clear animation
                        });


                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (icFavHeart.getVisibility() != View.VISIBLE) {
                            exoplayerRecyclerViewForYou.togglePlay();
                        }
                        return super.onSingleTapConfirmed(e);
                    }
                });

/*                itemView.setOnTouchListener((v, event) -> {
                    return gestureDetector.onTouchEvent(event);
                });*/

                itemView.setOnTouchListener((v, event) -> {
                    Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                    return gestureDetector.onTouchEvent(event);
                });


                Glide.with(getActivity())
                        .asGif()
                        .load(R.drawable.music)
                        .into(music_symbol);

                liveStreaming.setVisibility(View.GONE);

                liveStreaming.setOnClickListener(v -> {

                    Intent stream = new Intent(context, PublishActivity.class);
                    stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    stream.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                    stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                    stream.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                    stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                    startActivity(stream);


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
                    App.preventMultipleClick(videoCommandLay);
                    exoplayerRecyclerViewForYou.setPlayControl(false);
                    videoDialog(mHomeApiResponse.get(getAdapterPosition()).getPublisherId(), mHomeApiResponse.get(getAdapterPosition()).getVideoId(), getAdapterPosition());

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

                heartLay.setOnClickListener(v -> {

                    if (!likePrevent) {
                        String abc = mHomeApiResponse.get(getAdapterPosition()).getLikes();
                        int inum = Integer.parseInt(abc);
                        heartFun(getAdapterPosition());
                        likePrevent = true;

                        if (!mHomeApiResponse.get(getAdapterPosition()).getIsLiked()) {
                            // txt_heart_count.setVisibility(View.INVISIBLE);

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
                        if (mHomeApiResponse.get(getAdapterPosition()).getPlaytype().equals(Constants.TAG_VIDEO)) {
                            App.preventMultipleClick(parentLay);
                            reportAndFav(getAdapterPosition());
                            return true;
                        }
                    }
                    return false;
                });


                //Live Streaming Addon

                shareLay.setOnClickListener(v -> {
                    App.preventMultipleClick(shareLay);
                    shareVideoDialog(mHomeApiResponse.get(getAdapterPosition()));
                });

                music_symbol.setOnClickListener(v -> {

                            exoplayerRecyclerViewForYou.setPlayControl(false);
                            App.preventMultipleClick(music_symbol);

                            Intent intent = new Intent(context, SoundTrackActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.TAG_SOUND_ID, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getSoundId());
                            bundle.putString(Constants.TAG_SOUND_TITLE, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getTitle());
                            bundle.putString(Constants.TAG_SOUND_URL, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getSoundUrl());
                            bundle.putString(Constants.TAG_SOUND_IS_FAV, String.valueOf(homeApiResponse.get(getAdapterPosition()).getSoundtracks().getIsFavorite()));
                            bundle.putString(Constants.TAG_SOUND_COVER, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getCoverImage());
                            bundle.putString(Constants.TAG_SOUND_DURATION, homeApiResponse.get(getAdapterPosition()).getSoundtracks().getDuration());
                            intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                            startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);

                        }
                );

                profileFollowIcon.setOnClickListener(v -> {
                    Toast.makeText(getActivity(), "Clickedddd", Toast.LENGTH_SHORT).show();
                    profileFollowIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));
                    followAPI(mHomeApiResponse.get(getAdapterPosition()).getPublisherId(), mHomeApiResponse.get(getAdapterPosition()).getPublisherImage());
                });
                profileImage.setOnClickListener(v ->
                        profileImageClickListener.onUserClicked(true)
                );
                txt_title.setOnClickListener(v -> {
                    Toast.makeText(getActivity(), "Clickedddd", Toast.LENGTH_SHORT).show();

                    if (mHomeApiResponse.get(getAdapterPosition()).getPlaytype().equals("video"))
                        profileImageClickListener.onUserClicked(true);
                });

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
                            EventBus.getDefault().post(new FollowAutoScrollEnabled(isAutoScroll));
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

            private void updateAutoScrollUI() {
                if (isAutoScroll) {
                    autoScrollIV.setImageDrawable(getContext().getDrawable(R.drawable.scroll_active));
                } else {
                    autoScrollIV.setImageDrawable(getContext().getDrawable(R.drawable.scroll));
                }
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

                                EventBus.getDefault().post(new ForYouProfileUpdate(publisherId, publisherImage, userType));

                                Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));

                                Log.e(TAG, "onResponse: ::::::::::::::::Follow:::" + App.getGsonPrettyInstance().toJson(followResponse));

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


            void onBind(HomeResponse.Result videoItem) {

                Log.e(TAG, "onBind: :::::::::::::::::;" + new Gson().toJson(videoItem));
                updateAutoScrollUI();

                Log.e(TAG, "onBind: :::::::::::::::::::::"+contest_text );

                Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.tv_lhs_rhs);
                vote_promotionTV.startAnimation(a);
                vote_promotionTV.setText(contest_text+"hee this is check");

                if (SharedPref.getBoolean(SharedPref.HIDE_ICONS, true)) {
                    //   videoLinkLay.setVisibility(View.GONE);
                    videoLabelsLay.setVisibility(View.GONE);
                } else {
                    // videoLinkLay.setVisibility(View.VISIBLE);
                    videoLabelsLay.setVisibility(View.VISIBLE);
                }

                if(!TextUtils.isEmpty(videoItem.getLink_url())){
                    videoLinkLay.setVisibility(View.VISIBLE);
                }else{
                    videoLinkLay.setVisibility(GONE);
                }

                if(homeApiResponse.size()==1){
                    autoScrollLay.setVisibility(GONE);
                }else{
                    autoScrollLay.setVisibility(View.VISIBLE);
                }


                if (videoItem.getPlaytype().equals(Constants.TAG_VIDEO)) {
                    leftSideView.setVisibility(View.VISIBLE);

                    if (GetSet.getUserId().equals(videoItem.getPublisherId())) {
                        giftLay.setVisibility(View.GONE);
                        profileFollowIcon.setVisibility(View.GONE);
                    } else {
                        giftLay.setVisibility(View.VISIBLE);

                        //To set View count for videos it will trigger on 3sec delay
                        if(!isAutoScroll){
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


                    Log.e(TAG, "onBind: ::::::::::::::::::::;" + videoItem);


                    Glide.with(itemView.getContext())
                            .load(videoItem.getPublisherImage())
                            .placeholder(R.drawable.default_profile_image)
                            .into(profileImage);

                    Log.e(TAG, "onBind: ::::::::::::::" + videoItem.getFollowers());
                    FollowersCount.setText(videoItem.getFollowers() + " Followers");
                    publisher_vote_count.setText(videoItem.getLifetimeVoteCount());
                    txt_view_count.setText(videoItem.getVideo_views_count() + "");
                    txt_vote_count.setText(videoItem.getVotes_count());

                    if (videoItem.getIsLiked()) {
                        heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_color));
                    } else {
                        heart.setImageDrawable(getResources().getDrawable(R.drawable.heart_white));
                    }

                    txt_title.setText(String.format("@%s", videoItem.getPostedBy()));

                    txt_heart_count.setText(videoItem.getLikes());
//                    txt_gift_count.setText(videoItem.getVideoGiftCounts()+"");
                    txt_comment_count.setText(videoItem.getVideoCommentCounts() + "");

                    String getHash = videoItem.getVideoDescription();
                   /* if(!TextUtils.isEmpty(getHash)){
                        SpannableString spannable = new SpannableString(getHash);
                        final Matcher matcher = Pattern.compile("#\\s*(\\w+)").matcher(getHash);

                        while (matcher.find()) {
                            final String getHashTagName = matcher.group(1);
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @SuppressLint("UseCompatLoadingForDrawables")
                                @Override
                                public void onClick(View textView) {
                                    exoplayerRecyclerViewForYou.setPlayControl(false);
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
                        }


                    }*/
                    txt_description.setText(getHash);
                    txt_description.setMovementMethod(LinkMovementMethod.getInstance());

                } else {
                    leftSideView.setVisibility(View.GONE);
                    txt_title.setText(videoItem.getPostedBy());
                    txt_description.setText(videoItem.getVideoDescription());
                }
            }
        }

        private void SetViewCount(HomeResponse.Result videoItem) {

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
            viewHolder = new MyViewHolder(itemView);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());

            ((MyViewHolder) holder).txtReport.setOnClickListener(v -> {

                getSelectedReport = ((MyViewHolder) holder).txtReport.getText().toString();
                selectedPosition = position;
                notifyDataSetChanged();
            });

            ((MyViewHolder) holder).radioButton.setOnClickListener(v -> {
                selectedPosition = position;
                getSelectedReport = ((MyViewHolder) holder).txtReport.getText().toString();
                notifyDataSetChanged();
            });

            ((MyViewHolder) holder).radioButton.setChecked(selectedPosition == position);
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

    @SuppressLint("NonConstantResourceId")
    public class GiftAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;
        private final List<Gift> giftList;
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        //   private String selectedGiftId = "";


        public GiftAdapter(Context context, List<Gift> giftList) {
            Timber.d("GiftAdapter: instance %s", giftList);
            this.context = context;
            this.giftList = giftList;

        }

        @Override
        public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {

            final Gift gift = giftList.get(position);

            if (holder instanceof MyViewHolder) {

                ((MyViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).giftImage.setVisibility(View.INVISIBLE);


                Glide.with(context)
                        .load(gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                ((MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((MyViewHolder) holder).giftImage.setImageDrawable(context.getDrawable(R.drawable.gift));
                                ((MyViewHolder) holder).giftImage.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((MyViewHolder) holder).progressBar.setVisibility(View.GONE);
                                ((MyViewHolder) holder).giftImage.setImageDrawable(resource);
                                ((MyViewHolder) holder).giftImage.setVisibility(View.VISIBLE);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.gift))
                        .into(((MyViewHolder) holder).giftImage);


                ((MyViewHolder) holder).txtGiftPrice.setText((GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" + gift.getGiftGems());


            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
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

            @BindView(R.id.selectionBackgroundLay)
            RelativeLayout selectionBackgroundLay;


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
                if ((GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
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
            /*holder.userComment.setText(commendList.get(position).getComment());*/

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
                            @SuppressLint("SetTextI18n")
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
                    if (commendList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        profile = new Intent(getContext(), MyProfileActivity.class);
                    } else {
                        profile = new Intent(getContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, "");
                    }
                    startActivityForResult(profile, Constants.COMMENT_MUSIC_REQUEST_CODE);
                });

                userName.setOnClickListener(v -> {
                    Intent profile;
                    if (commendList.get(getAdapterPosition()).getUserId().equals(GetSet.getUserId())) {
                        profile = new Intent(getContext(), MyProfileActivity.class);
                        startActivity(profile);
                    } else {
                        profile = new Intent(getContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, commendList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, commendList.get(getAdapterPosition()).getUserImage());
                    }
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
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_username, parent, false);
            return new MyViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            try {
                final UserList.Result tempMap = Items.get(position);


                Glide.with(getContext()).load(tempMap.getUserImage())
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
                        String pos = "@" + holder.user_name.getText().toString().trim() + " ";
                        //String pos = "@" + tempMap.getUserName().trim() + " ";

                        if (tagcomment != null && !tagcomment.equals("")) {
                            tagcomment += " " + "@" + holder.user_name.getText().toString().trim();
                            //  tagcomment += " " + "@" + tempMap.getUserName().trim() + " ";

                        } else {
                            tagcomment += "@" + holder.user_name.getText().toString().trim();
                            /*tagcomment += "@" + tempMap.getUserName().trim() + " ";*/

                        }
                        mCommentsEditText.setText(pos);
                        mCommentsEditText.setSelection(pos.length());

                    } else if (first == '@' && cmt_arr.length > 1) {
                        String pos = "@" + holder.user_name.getText().toString() + " ";
                        /*String pos = "@" + tempMap.getUserName().trim() + " ";*/
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

                        /*if (cmt_arr.length <= 5) {
                            if (tagcomment != null && !tagcomment.equals("")) {
                                *//*tagcomment += " " + "@" + holder.user_name.getText().toString().trim();*//*
                                tagcomment += " " + "@" + tempMap.getUserName().trim();

                            } else {
                                *//*tagcomment += "@" + holder.user_name.getText().toString().trim();*//*
                                tagcomment += "@" + tempMap.getUserName().trim();

                            }
                            mCommentsEditText.setText(pos);
                            mCommentsEditText.setSelection(pos.length());
                        } else {

                            Timber.i("not allowed");
                        }*/

                        if (tagcomment != null && !tagcomment.equals("")) {
                            tagcomment += " " + "@" + holder.user_name.getText().toString().trim();

                        } else {
                            tagcomment += "@" + holder.user_name.getText().toString().trim();

                        }
//                        commentEditText.setText(pos);
//                        commentEditText.setSelection(pos.length());

                    } else {
                        /*String[] cmtAry = cmt.split(" @");
                        if (cmtAry.length <= 5) {
                            String pos = cmtAry[0] + " @" + tempMap.getUserName().trim() + " ";


                            if (tagcomment != null && !tagcomment.equals("")) {
                                tagcomment += " " + "@" + tempMap.getUserName().trim();

                            } else {
                                tagcomment += "@" + tempMap.getUserName().trim();

                            }
                            mCommentsEditText.setText(pos);
                            mCommentsEditText.setSelection(pos.length());
                        } else {
                            Timber.i("not allowed");
                        }*/

                        String[] cmtAry = cmt.split(" @");
                        Log.e("tagggg", " " + cmtAry[0]);
                        String pos = cmtAry[0] + " @" + holder.user_name.getText().toString().trim() + " ";
                        if (tagcomment != null && !tagcomment.equals("")) {
                            tagcomment += " " + "@" + holder.user_name.getText().toString().trim();

                        } else {
                            tagcomment += "@" + holder.user_name.getText().toString().trim();

                        }
//                        commentEditText.setText(pos);
//                        commentEditText.setSelection(pos.length());


                    }

                    tagged_user_id = tempMap.getUserName();
                    list_tagged_user_ids.add(tagged_user_id);


                    // Log.e("selectedtags", " " + liveusertagid);
                    StringBuilder strBuilder = new StringBuilder();
                    String comma = "";
                    /*if (list_tagged_user_names.size() > 0) {*/
                    if (list_tagged_user_ids.size() > 0) {
                        for (int i = 0; i < list_tagged_user_ids.size(); i++) {
                            strBuilder.append(comma);
                            comma = ",";
                            strBuilder.append(list_tagged_user_ids.get(i));

                        }

                    }
                    tagged_user_id = strBuilder.toString();





                    /*tagged_user_name = tempMap.getUserName();
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

                    tagged_user_name = strBuilder.toString();*/


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

