package com.app.binggbongg.fundoo.profile;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.fundoo.PrivacyActivity;
import com.app.binggbongg.fundoo.home.eventbus.UserBlocked;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.apprtc.util.AppRTCUtils;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.ChatActivity;
import com.app.binggbongg.fundoo.FollowersActivity;
import com.app.binggbongg.fundoo.ProfileVideos;
import com.app.binggbongg.fundoo.SingleImageActivity;
import com.app.binggbongg.fundoo.home.eventbus.FollowingFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.ForYouFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.OtherProfileToFollowingProfile;
import com.app.binggbongg.fundoo.home.eventbus.OtherProfileToForYouProfile;
import com.app.binggbongg.fundoo.home.eventbus.OtherProfileUpdate;
import com.app.binggbongg.fundoo.home.eventbus.SingleProfileUpdate;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.FollowRequest;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class OthersProfileActivity extends BaseFragmentActivity {
    private static final String TAG = OthersProfileActivity.class.getSimpleName();

    @BindView(R.id.btnEdit)
    ImageView btnEdit;
    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.profileImage)
    ShapeableImageView profileImage;

    @BindView(R.id.premiumImage)
    RoundedImageView premiumImage;

    @BindView(R.id.txtName)
    TextView txtName;

    @BindView(R.id.txtUserName)
    TextView txtUserName;

    @BindView(R.id.userBio)
    TextView userBio;

    @BindView(R.id.txtHeartCount)
    TextView txtHeartCount;


    @BindView(R.id.genderImage)
    ImageView genderImage;

    @BindView(R.id.txtFansCount)
    TextView txtFansCount;
    @BindView(R.id.txtFollowingsCount)
    TextView txtFollowingsCount;

    @BindView(R.id.txtOtherUserVCount)
    TextView txtOtherUserVCount;

    @BindView(R.id.btnFollow)
    MaterialButton btnFollow;

    @BindView(R.id.btnMessage)
    MaterialButton btnMessage;

    @BindView(R.id.btnVideoCall)
    MaterialButton btnVideoCall;

    //Audio Call Addon

    @BindView(R.id.fansLay)
    LinearLayout fansLay;

    @BindView(R.id.followingLay)
    LinearLayout followingLay;

    @BindView(R.id.contentLay)
    LinearLayout contentLay;

    @BindView(R.id.vCard)
    MaterialCardView vCard;

    @BindView(R.id.adView)
    AdView adView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.youtubeIV)
    ShapeableImageView YouTubeIV;
    @BindView(R.id.instaIV) ShapeableImageView instaIV;
    @BindView(R.id.facebookIV) ShapeableImageView facebookIV;
    @BindView(R.id.whatsappIV) ShapeableImageView whatsappIV;
    @BindView(R.id.linkedInIV) ShapeableImageView linkedInIV;
    @BindView(R.id.snapChatIV) ShapeableImageView snapChatIV;
    @BindView(R.id.twitterIV) ShapeableImageView twitterIV;
    @BindView(R.id.tv_receivedVotes) TextView totalVotes;
    @BindView(R.id.tv_totalStar) TextView totalStar;
    @BindView(R.id.tv_totalGifts) TextView totalGifts;
    @BindView(R.id.tv_totalDiamond) TextView totalDiamond;
    @BindView(R.id.btnBlock) MaterialButton btnBlock;
    @BindView(R.id.giftLay) LinearLayout sdgvLayout;

    ApiInterface apiInterface;

    ProfileResponse profile;

    private String getProfileUserId = "", from = "", getProfileUserName = "";
    private DBHelper dbHelper;
    String chatId ="";

    boolean videoEnabled=false;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OtherProfileUpdate event) {
        Timber.d("onMessageEvent: %s", event.id);
        getProfile(event.id, "");
        if (btnFollow != null) btnFollow.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_others_profile);
        ButterKnife.bind(this);

        from = getIntent().getStringExtra(Constants.TAG_FROM);

        // Waiting for API Data so the Views are disabled
        fansLay.setEnabled(false);
        profileImage.setEnabled(false);
        premiumImage.setEnabled(false);
        fansLay.setEnabled(false);
        followingLay.setEnabled(false);
        vCard.setEnabled(false);
        btnFollow.setEnabled(false);
        btnMessage.setEnabled(false);
        btnVideoCall.setEnabled(false);
        dbHelper = DBHelper.getInstance(this);
        chatId = GetSet.getUserId() + getProfileUserId;


        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initProfile();
        contentLay.setVisibility(View.VISIBLE);
        contentLay.setAlpha(0.3f);
        progressBar.setAlpha(1.0f);

        Glide.with(getApplicationContext())
                .load(getIntent().getStringExtra(Constants.TAG_USER_IMAGE))
                // .signature(new MediaStoreSignature("image/jpg", new Date().getTime(), 0))

                .error(R.drawable.default_profile_image)
                .placeholder(R.drawable.default_profile_image)
                /*.addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Profile Image: onLoadFailed: " + e.getMessage(), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })*/
                .into(profileImage);


        getProfileUserId = getIntent().getStringExtra(Constants.TAG_USER_ID);
        getProfileUserName = getIntent().getStringExtra(Constants.TAG_USER_NAME) == null ? "" : getIntent().getStringExtra(Constants.TAG_USER_NAME);
        Timber.i("getProfileUserName=> %s", getProfileUserName);
        getProfile(getProfileUserId, getProfileUserName);

        if(getProfileUserId.equals(GetSet.getUserId())){
            sdgvLayout.setVisibility(View.VISIBLE);
        }else{
            sdgvLayout.setVisibility(GONE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void initProfile() {

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .build();
        SlidrInterface slidrInterface = Slidr.attach(this, config);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setImageResource(R.drawable.arrow_w_l);
        btnBack.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.GONE);


    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }


    private void getProfile(String getUserId, String userName) {


        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(getUserId);
            if (!userName.equals("")) request.setUser_name(userName);
            else request.setProfileId(getUserId);
            Timber.d("getProfile params: %s", App.getGsonPrettyInstance().toJson(request));
            Call<ProfileResponse> call = apiInterface.getProfile(request);

            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        Timber.d("getprofile onResponse: %s", App.getGsonPrettyInstance().toJson(profile));
                        setDataView(profile);

                        progressBar.setAlpha(0.0f);
                        contentLay.animate()
                                .alpha(1.0f)
                                .setListener(null);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void setDataView(ProfileResponse profile) {
        if (profile.getPremiumMember().equals(Constants.TAG_TRUE))
            premiumImage.setVisibility(View.VISIBLE);
        else
            premiumImage.setVisibility(View.GONE);


        txtName.setText(profile.getName());
        txtUserName.setText(profile.getUserName());
        txtHeartCount.setText(String.valueOf(profile.getLikes()));

        if (profile.getBio().equals("")) userBio.setVisibility(View.GONE);
        else {
            userBio.setText(profile.getBio());
            userBio.setVisibility(View.VISIBLE);
        }

        txtFansCount.setText(String.valueOf(profile.getFollowers() != null ? profile.getFollowers() : ""));
        txtFollowingsCount.setText(String.valueOf(profile.getFollowings() != null ? profile.getFollowings() : ""));
        genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                getResources().getDrawable(R.drawable.men) : getResources().getDrawable(R.drawable.women));

        txtOtherUserVCount.setText(String.valueOf(profile.getVideosCount() != null ? profile.getVideosCount() : "0"));
        totalStar.setText(profile.getGoldenstar_count()!=null ?  profile.getGoldenstar_count(): "0");
        totalGifts.setText(profile.getGiftbox_count()!=null? profile.getGiftbox_count(): "0");
        totalVotes.setText(profile.getTotal_vote_count()!=null? profile.getTotal_vote_count(): "0");
        totalDiamond.setText(profile.getBluediamond_count()!=null? profile.getBluediamond_count(): "0");


        if (profile.getFollow().contains(Constants.TAG_TRUE))
            btnFollow.setText(getResources().getString(R.string.unfollow));
        else btnFollow.setText(getResources().getString(R.string.follow));

        if (btnFollow != null) btnFollow.setEnabled(true);

        if (profile.getBlockedByMe() != null && !TextUtils.isEmpty(profile.getBlockedByMe())) {
            btnBlock.setVisibility(View.VISIBLE);
            if (profile.getBlockedByMe().equals(Constants.TAG_TRUE)) {
                btnBlock.setText(getString(R.string.unblock));
            } else {
                btnBlock.setText(getString(R.string.block));
            }
        } else {
            btnBlock.setVisibility(View.GONE);
        }

        // Click event activated
        fansLay.setEnabled(true);
        profileImage.setEnabled(true);
        premiumImage.setEnabled(true);
        fansLay.setEnabled(true);
        followingLay.setEnabled(true);
        vCard.setEnabled(true);
        btnFollow.setEnabled(true);
        btnMessage.setEnabled(true);
        btnVideoCall.setEnabled(true);


        Glide.with(getApplicationContext())
                .load(profile.getUserImage())
                //  .signature(new MediaStoreSignature("image/jpg", new Date().getTime(), 0))

                .error(R.drawable.default_profile_image)
                .placeholder(R.drawable.default_profile_image)
                /*.addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Profile Image: onLoadFailed: " + e.getMessage(), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })*/
                .into(profileImage);

        if(!TextUtils.isEmpty(profile.getYoutube_link())){
            YouTubeIV.setVisibility(View.VISIBLE);
        }else{
            YouTubeIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getTwitter())){
            twitterIV.setVisibility(View.VISIBLE);
        }else{
            twitterIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getInstagram())){
            instaIV.setVisibility(View.VISIBLE);
        }else{
            instaIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getFb())){
            facebookIV.setVisibility(View.VISIBLE);
        }else{
            facebookIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getLinkedin())){
            linkedInIV.setVisibility(View.VISIBLE);
        }else{
            linkedInIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getSnapchat())){
            snapChatIV.setVisibility(View.VISIBLE);
        }else{
            snapChatIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getWhatsapp())){
            whatsappIV.setVisibility(View.VISIBLE);
        }else{
            whatsappIV.setVisibility(GONE);
        }

        twitterIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this, profile.getTwitter(), "com.twitter.android"));

        YouTubeIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this,profile.getYoutube_link(),"com.google.android.youtube"));

        instaIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this,profile.getInstagram(),"com.instagram.android"));

        facebookIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this,profile.getFb(),"com.facebook.katana"));

        linkedInIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this,profile.getLinkedin(),"com.inked"));

        snapChatIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this,profile.getSnapchat(),"com.snapchat.android"));

        whatsappIV.setOnClickListener(v-> GotoSocialMedia(OthersProfileActivity.this,profile.getWhatsapp(),"com.whatsapp"));

    }

    private void GotoSocialMedia(Context context, String link, String packageName) {
        if (link != null && !link.equals("")){
            if (packageName.equals("com.whatsapp")){
                link="https://wa.me/"+link;
            }
            if(URLUtil.isValidUrl(link)){
                Log.e("TAG", "GotoSocialMedia: ::::::::::::::::::::::::::::::::::::::::"+link );
                if (AppUtils.appInstalledOrNot(context,packageName)){
                  /*  Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(link.toString()));
                    startActivity(intent);*/
                    Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                    intent.setData(Uri.parse(link.toString()));
                    startActivity(intent);
                }else{
                    Toast.makeText(context, "Cannot find any App to perform this link", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, PrivacyActivity.class);
                    intent.putExtra("from", "video");
                    intent.putExtra("link_url", link.toString());
                    startActivity(intent);
                }
            }else{
                Toast.makeText(context, "Update the valid url", Toast.LENGTH_SHORT).show();
            }

        }else if(TextUtils.isEmpty(link)){
            Toast.makeText(context, "No link has found", Toast.LENGTH_SHORT).show();
        }
    }

    private void followUnFollowUser() {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            if (getProfileUserId != null) followRequest.setUserId(getProfileUserId);
            else followRequest.setUser_name(getProfileUserName);

            followRequest.setFollowerId(GetSet.getUserId());

            if (btnFollow.getText().equals(Constants.TAG_FOLLOW_USER))
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            else
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);


            Timber.d("followUnfollowUser: %s", App.getGsonPrettyInstance().toJson(followRequest));

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Map<String, String> followResponse = response.body();

                    if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));
                        txtFansCount.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_zoom_in));
                        int getCount = Integer.parseInt(txtFansCount.getText().toString().replace("-", "0"));

                        if (followResponse.get(Constants.TAG_MESSAGE).equals("Followed successfully"))
                            txtFansCount.setText(String.valueOf(getCount + 1));
                        else if (followResponse.get(Constants.TAG_MESSAGE).equals("Unfollowed successfully"))
                            txtFansCount.setText(String.valueOf(getCount - 1));

                        if (btnFollow.getText().equals(Constants.TAG_FOLLOW_USER)) {
                            btnFollow.setText(Constants.TAG_UNFOLLOW_USER);
                            EventBus.getDefault().post(new ForYouFollowFollowing(false, getProfileUserId));
                            EventBus.getDefault().post(new FollowingFollowFollowing(false, getProfileUserId));
                        } else {
                            btnFollow.setText(Constants.TAG_FOLLOW_USER);
                            EventBus.getDefault().post(new ForYouFollowFollowing(true, getProfileUserId));
                            EventBus.getDefault().post(new FollowingFollowFollowing(true, getProfileUserId));
                        }

                        EventBus.getDefault().post(new OtherProfileToForYouProfile(getProfileUserId));
                        EventBus.getDefault().post(new OtherProfileToFollowingProfile(getProfileUserId));

                        txtFansCount.startAnimation(AnimationUtils.loadAnimation(OthersProfileActivity.this, R.anim.anim_zoom_out));

                        if (from != null && from.equals(Constants.TAG_SINGLE_VIDEO)) {
                            EventBus.getDefault().post(new SingleProfileUpdate("update"));
                        }

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

    @OnClick({R.id.profileImage, R.id.fansLay, R.id.followingLay, R.id.vCard, R.id.btnFollow,
            R.id.btnBack, R.id.btnMessage, R.id.btnVideoCall, R.id.btnBlock})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fansLay:
                App.preventMultipleClick(fansLay);
                Intent followersIntent = new Intent(OthersProfileActivity.this, FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                startActivity(followersIntent);
                break;

            case R.id.followingLay:
                App.preventMultipleClick(followingLay);
                Intent followingsIntent = new Intent(OthersProfileActivity.this, FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                startActivity(followingsIntent);
                break;

            case R.id.vCard:
                App.preventMultipleClick(vCard);
                Intent intent = new Intent(OthersProfileActivity.this, ProfileVideos.class);
                intent.putExtra(Constants.TAG_PROFILE_ID, profile.getUserId());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_OTHER_PROFILE_UPDATE);
                startActivity(intent);
                break;

            case R.id.btnFollow:
                App.preventMultipleClick(btnFollow);
                followUnFollowUser();
                break;

            case R.id.btnBack:
                App.preventMultipleClick(btnBack);
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
                break;
            case R.id.btnBlock:
                App.preventMultipleClick(btnBlock);
                blockUnBlockUser(getProfileUserId);
                break;
            case R.id.btnMessage:

                App.preventMultipleClick(btnMessage);
                messageCheck();
                break;

            case R.id.btnVideoCall:
                App.preventMultipleClick(btnVideoCall);
                videoEnabled=true;
                if (profile.getBlockedByMe() != null && profile.getBlockedByMe().equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_call_description));
                }else{
                    mutualContact();
                }
                /*if (profile.getBlockedMe() != null && profile.getBlockedMe().equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_call_description));
                } else {
                    if (GetSet.getGems() >= AdminData.videoCallsGems) {
                        if (NetworkReceiver.isConnected()) {
                            AppRTCUtils appRTCUtils = new AppRTCUtils(OthersProfileActivity.this);
                            Intent callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_VIDEO);
                            callIntent.putExtra(Constants.TAG_USER_NAME, profile.getName());
                            callIntent.putExtra(Constants.TAG_USER_IMAGE, profile.getUserImage());
                            callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(callIntent);
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                    }
                }*/

                break;
            case R.id.profileImage:
                App.preventMultipleClick(btnMessage);
                Timber.d("onViewCreated: %s", profile.getUserImage());
                App.preventMultipleClick(profileImage);
                Intent singleImage = new Intent(OthersProfileActivity.this, SingleImageActivity.class);
                singleImage.putExtra("image", String.valueOf(profile.getUserImage()));
                startActivity(singleImage);
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
                break;

            default:
                break;
        }
    }

    private void blockUnBlockUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, partnerId);
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            String chatId = GetSet.getUserId() + profile.getUserId();
                            if (responseMap.get(Constants.TAG_BLOCK_STATUS).equals("1")) {
                                profile.setBlockedByMe(Constants.TAG_TRUE);
                                btnBlock.setText(getString(R.string.unblock));
                                App.makeToast(getString(R.string.user_blocked_successfully));
                                EventBus.getDefault().post(new UserBlocked(true, getProfileUserId));
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                                    dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                    dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, partnerId, profile.getUserName(), profile.getUserImage(), Constants.TAG_TRUE);
                                    if (dbHelper.isChatIdExists(chatId)) {
                                        dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                        dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                                    }
                                }
                                EventBus.getDefault().post(new UserBlocked(true, getProfileUserId));
                            } else {
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, partnerId, profile.getUserName(), profile.getUserImage(), Constants.TAG_FALSE);
                                }
                                btnBlock.setText(getString(R.string.block));
                                profile.setBlockedByMe(Constants.TAG_FALSE);
                                App.makeToast(getString(R.string.unblocked_successfully));
                                EventBus.getDefault().post(new UserBlocked(false, getProfileUserId));
                            }
                             getProfile(partnerId,"");
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    private void messageCheck() {

        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_PROFILE_ID, profile.getUserId());

            Timber.i("mutualcontact params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.messagecheck(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    if (response.body() != null) {
                        if (response.body().get("status").equals("true")) {
                            Intent chatIntent = new Intent(OthersProfileActivity.this, ChatActivity.class);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
                            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());

                            startActivity(chatIntent);

/*                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
*//*                            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());*//*
                            startActivity(chatIntent);*/
                        } else {
                            App.dialog(OthersProfileActivity.this, "", getResources().getString(R.string.user_alert_message), getResources().getColor(R.color.colorBlack));
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
            Toasty.error(OthersProfileActivity.this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }

    private void mutualContact() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_FOLLOWER_ID, profile.getUserId());

            Timber.i("mutualcontact params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.mutualcontact(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    Timber.i("mutualcontact response=> %s", response.body());

                    if (response.body() != null) {
                        if (Objects.equals(response.body().get("mutual_follow"), "1")) {

                            if (GetSet.getGems() >= AdminData.videoCallsGems) {
                                if (NetworkReceiver.isConnected()) {
                                    Log.e(TAG, "mutualcontact: ::::::if" );
                                    AppRTCUtils appRTCUtils = new AppRTCUtils(OthersProfileActivity.this);
                                    Intent callIntent=new Intent();
                                    if(videoEnabled) {
                                        callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_VIDEO);
                                    }
                                    else
                                    {
                                        callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_AUDIO);

                                    }
                                             callIntent.putExtra(Constants.TAG_USER_NAME, profile.getName());
                                    callIntent.putExtra(Constants.TAG_USER_IMAGE, profile.getUserImage());
                                    callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                                    callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(callIntent);
                                } else {
                                    Log.e(TAG, "mutualcontact: ::::::else" );
                                    App.makeToast(getString(R.string.no_internet_connection));
                                }
                            } else {
                                App.makeToast(getString(R.string.not_enough_gems));
                            }

                        } else if (Objects.equals(response.body().get("mutual_follow"), "0")) {
                            App.dialog(OthersProfileActivity.this, "", getResources().getString(R.string.mutual_followers_can_make_a_call), getResources().getColor(R.color.colorBlack));
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
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        //unregisterNetworkReceiver();
        super.onDestroy();
        /*if (!this.isDestroyed()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    /*@Override
    public void onNetworkChanged(boolean isConnected) {
        //AppUtils.showSnack(this, parentLay, isConnected);
    }*/

}


