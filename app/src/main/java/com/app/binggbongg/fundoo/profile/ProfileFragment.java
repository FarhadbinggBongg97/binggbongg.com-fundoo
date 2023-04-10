package com.app.binggbongg.fundoo.profile;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static com.app.binggbongg.fundoo.App.fmtFloat;
import static com.app.binggbongg.fundoo.App.isPreventMultipleClick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.app.binggbongg.Deepar.DeeparActivity;
import com.app.binggbongg.fundoo.HistoryActivity;
import com.app.binggbongg.fundoo.PrivacyActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.ConvertGiftActivity;
import com.app.binggbongg.fundoo.DialogCreditGems;
import com.app.binggbongg.fundoo.FollowersActivity;
import com.app.binggbongg.fundoo.GemsStoreActivity;
import com.app.binggbongg.fundoo.ImageViewActivity;
import com.app.binggbongg.fundoo.ProfileVideos;
import com.app.binggbongg.fundoo.SettingsActivity;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.model.event.GetProfileEvent;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private boolean mPermissionsGranted = false;
    private String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };

    private final String[] mRequiredPermissions12 = new String[]{
            CAMERA,
            RECORD_AUDIO
    };
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    @BindView(R.id.profileImage)
    ShapeableImageView profileImage;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnEdit)
    ImageView btnEdit;
    @BindView(R.id.liveIV)
    LottieAnimationView liveIV;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtFollowersCount)
    TextView txtFollowersCount;
    @BindView(R.id.txtFollowingsCount)
    TextView txtFollowingsCount;
    @BindView(R.id.txtHeartCount)
    TextView txtHeartCount;
    @BindView(R.id.txtGemsCount)
    TextView txtGemsCount;
    @BindView(R.id.txtGiftsCount)
    TextView txtGiftsCount;
    /*@BindView(R.id.btnRenewal) TextView btnRenewal;*/
    /*@BindView(R.id.renewalLay) RelativeLayout renewalLay;*/
    @BindView(R.id.genderImage)
    ImageView genderImage;
    /*@BindView(R.id.txtTitle) TextView txtTitle;*/
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    /*@BindView(R.id.btnShare) ImageView btnShare;*/
    @BindView(R.id.lay_like) LinearLayout likeLay;
    @BindView(R.id.fansLay) LinearLayout fansLay;
    @BindView(R.id.followingLay) LinearLayout followingLay;
    @BindView(R.id.votesLay) LinearLayout votesLay;
    @BindView(R.id.premiumImage) RoundedImageView premiumImage;
    /*@BindView(R.id.scrollView) ScrollView scrollView;*/
    /*@BindView(R.id.txtPrimeTitle) TextView txtPrimeTitle;*/
    /*@BindView(R.id.txtPrice)  TextView txtPrice;*/
    /*@BindView(R.id.premiumLay) RelativeLayout premiumLay;*/
    /*@BindView(R.id.btnSubscribe) Button btnSubscribe;*/
    /*@BindView(R.id.subscribeLay) RelativeLayout subscribeLay;*/
    /*@BindView(R.id.gemsLay) CardView gemsLay;*/
    /*@BindView(R.id.giftsLay) RelativeLayout giftsLay;*/
    @BindView(R.id.adView)
    AdView adView;
    /*@BindView(R.id.txtSubTitle) TextView txtSubTitle;*/
    /*@BindView(R.id.imageLay) RelativeLayout imageLay;*/
    /*@BindView(R.id.divider) View divider;*/
    /*@BindView(R.id.txtMyVideo) TextView txtMyVideo;*/
    @BindView(R.id.txtVideoCount)
    TextView txtVideoCount;
    /*@BindView(R.id.videoCountLay)  CardView videoCountLay;*/
    /*@BindView(R.id.iconDiamond) ImageView iconDiamond;*/
    /*@BindView(R.id.txtRenewalTitle) TextView txtRenewalTitle;*/
    /*@BindView(R.id.txtRenewalDes)  TextView txtRenewalDes;*/
    /*@BindView(R.id.contentLay)  LinearLayout contentLay;
    @BindView(R.id.parentLay) FrameLayout parentLay;*/
    /*@BindView(R.id.primeBgLay) RelativeLayout primeBgLay;*/
    /*@BindView(R.id.renewalBgLay) RelativeLayout renewalBgLay;*/
    /*@BindView(R.id.txtFollowers) TextView txtFollowers;
    @BindView(R.id.txtFollowings) TextView txtFollowings;*/
    @BindView(R.id.userBio) TextView userBio;
    @BindView(R.id.txtUserName) TextView txtUserName;
    @BindView(R.id.favCard) MaterialCardView favCard;
    @BindView(R.id.videoCard) MaterialCardView videoCard;
    @BindView(R.id.coinsCard) MaterialCardView coinsCard;
    @BindView(R.id.giftsCard) MaterialCardView giftsCard;
    @BindView(R.id.shareCard) MaterialCardView shareCard;
    @BindView(R.id.referralCard) MaterialCardView referralCard;
    @BindView(R.id.viewsCard) MaterialCardView viewsCard;
    @BindView(R.id.premiumLay) RelativeLayout premiumLay;
    @BindView(R.id.renewalLay) MaterialCardView renewalLay;
    @BindView(R.id.subscribeLay) MaterialCardView subscribeLay;
    @BindView(R.id.btnSubscribe) Button btnSubscribe;
    @BindView(R.id.txtPrimeTitle) TextView txtPrimeTitle;
    @BindView(R.id.txtPrice) TextView txtPrice;
    @BindView(R.id.referralLay) RelativeLayout referralLay;
    @BindView(R.id.shareLay) RelativeLayout shareLay;
    @BindView(R.id.viewsLay) RelativeLayout viewsLay;
    @BindView(R.id.tv_totalVotes) TextView totalVotes;
    @BindView(R.id.tv_blueDiamond) TextView blueDiamond;
    @BindView(R.id.tv_giftBoxCount) TextView giftBoxCount;
    @BindView(R.id.tv_goldenStar) TextView goldenStarCount;
    @BindView(R.id.txtVotesCount) TextView receivedVotes;
    @BindView(R.id.txtReferralCount) TextView referralCount;
    @BindView(R.id.txtShareCount) TextView shareCount;
    @BindView(R.id.txtViewssCount) TextView viewCount;
    @BindView(R.id.youtubeIV) ShapeableImageView YouTubeIV;
    @BindView(R.id.instaIV) ShapeableImageView instaIV;
    @BindView(R.id.facebookIV) ShapeableImageView facebookIV;
    @BindView(R.id.whatsappIV) ShapeableImageView whatsappIV;
    @BindView(R.id.linkedInIV) ShapeableImageView linkedInIV;
    @BindView(R.id.snapChatIV) ShapeableImageView snapChatIV;
    @BindView(R.id.twitterIV) ShapeableImageView twitterIV;

    /*@BindView(R.id.liveStreaming) RoundedImageView liveStreaming;*/
    /*@BindView(R.id.favCard) MaterialCardView txtFollowings;*/

    private Context context;
    ApiInterface apiInterface;
    ProfileResponse profileResponse;
    DialogCreditGems alertDialog;
    private String userId = null;
    private AppUtils appUtils;
    private String qrImagePath;
    private boolean isVideoDeleted = false;
    private Bitmap qrBitMap;
    private StorageUtils storageUtils;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_USER_ID)) {
            userId = getArguments().getString(Constants.TAG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, rootView);

        btnBack.setVisibility(GONE);
        btnEdit.setImageResource(R.drawable.edit);
        btnSettings.setImageResource(R.drawable.settings_white);
        btnSettings.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.VISIBLE);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        // appUtils = new AppUtils(context);
        storageUtils = StorageUtils.getInstance(context);
        setHasOptionsMenu(true);
        //btnShare.setVisibility(View.GONE);
        isVideoDeleted = false;

        /*if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }*/

//        initPrimeView();

        /* Interstial Ads Addon*/


        return rootView;
    }

    /* Interstial Ads addon*/


    @SuppressLint("UseCompatLoadingForDrawables")
    private void initProfile() {


        if (GetSet.getUserId() != null) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            /*profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setName(GetSet.getName());
            profileResponse.setUserName(GetSet.getUserName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            /*profileResponse.setAge(GetSet.getAge());*/
            profileResponse.setFollowings(Integer.valueOf(GetSet.getFollowingCount()));
            profileResponse.setFollowers(Integer.valueOf(GetSet.getFollowersCount()));
            /*profileResponse.setLocation(GetSet.getLocation());
            profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setAvailableGems(GetSet.getGems());
            profileResponse.setAvailableGifts(Integer.valueOf("" + GetSet.getGifts()));
            profileResponse.setVideosCount(Integer.valueOf("" + GetSet.getVideos()));
            /*profileResponse.setWatchedCount(GetSet.getVideosHistory());*/
            profileResponse.setPremiumMember(GetSet.getPremiumMember());
            profileResponse.setPremiumExpiryDate(GetSet.getPremiumExpiry());
            /*profileResponse.setPrivacyAge(GetSet.getPrivacyAge());*/
            /*profileResponse.setUserCanMessage(GetSet.geti());*/
            profileResponse.setBio(GetSet.getBio());
            profileResponse.setLikes(GetSet.getLikes());
            profileResponse.setAge(Integer.valueOf(GetSet.getAge()));
            setProfile(profileResponse);
        }
    }

    private void initPrimeView() {
        /*Init Prime View*/

/*        Timber.d("initPrimeView: GetSet.getPremiumMember() %s", GetSet.getPremiumMember());
        Timber.d("initPrimeView: GetSet.getPrimeContent() %s", AppUtils.getPrimeContent(getContext()));*/


        if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            renewalLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            premiumLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getActivity()));
            //txtPrice.setText(AppUtils.getPrimeContent(getActivity()));
            txtPrice.setText(GetSet.getGetPrimeContent());
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(GONE);
        } else {
            premiumLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getActivity()));
            txtPrice.setText(AppUtils.getPrimeContent(getActivity()));
        }
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());

            Log.d(TAG, "getProfile: params=. " + new Gson().toJson(request));

            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {

                        Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body()));


                        profileResponse = profile;
                        GetSet.setUserId(profile.getUserId());
                        /*GetSet.setLoginId(profile.getLoginId());*/
                        GetSet.setName(profile.getName());
                        GetSet.setUserName(profile.getUserName());
                        GetSet.setDob(profile.getDob());
                        GetSet.setAge(String.valueOf(profile.getAge()));
                        GetSet.setGender(profile.getGender());
                        GetSet.setUserImage(profile.getUserImage());
                        /*GetSet.setLocation(profile.getLocation());*/
                        GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                        GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                        //GetSet.setGems(profile.getAvailableGems() != null ? (profile.getAvailableGems()) : 0F);
                        GetSet.setGems(profile.getPurChasedVotes() != null ? Float.parseFloat((profile.getPurChasedVotes())) : 0F);
                        GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0L);
                        /*GetSet.setVideosHistory(profile.getWatchedCount() != null ? profile.getWatchedCount() : 0L);*/
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        /*GetSet.setPrivacyAge(profile.getPrivacyAge());*/
                        GetSet.setPrivacyContactMe(String.valueOf(profile.getUserCanMessage()));
                        /*GetSet.setShowNotification(profile.getShowNotification());*/
                        GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                        GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                        GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));
                        GetSet.setReferalLink(profile.getReferalLink());
                        GetSet.setCreatedAt(profile.getCreatedAt());
                        GetSet.setBio(profile.getBio());
                        GetSet.setLikes(profile.getLikes());

                        GetSet.setAge(String.valueOf(profile.getAge()));
                        GetSet.setPostCommand(profile.getCommentPrivacy());
                        GetSet.setSendMessage(profile.getMessagePrivacy());

                        GetSet.setYoutubeLink(profile.getYoutube_link());
                        GetSet.setSnapChatLink(profile.getSnapchat());
                        GetSet.setWhatsappLink(profile.getWhatsapp());
                        GetSet.setTwitterLink(profile.getTwitter());
                        GetSet.setInstaLink(profile.getInstagram());
                        GetSet.setPinterestLink(profile.getPinterest());
                        GetSet.setTiktokLink(profile.getTiktok());
                        GetSet.setLinkedInLink(profile.getLinkedin());
                        GetSet.setFbLink(profile.getFb());

                        GetSet.setCountryName(profile.getCountry());
                        GetSet.setStateName(profile.getState());
                        GetSet.setCityName(profile.getCity());
                        GetSet.setEmail(profile.getEmail());
                        GetSet.setPhoneNumber(profile.getPhoneNumber());
                        GetSet.setWebsiteLink(profile.getWebLink());

                        // Gift to money
                        GetSet.setPaypal_id(profile.getPaypalId());
                        SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

                        SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                        SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                        SharedPref.putString(SharedPref.NAME, GetSet.getName());
                        SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                        SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                        SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                        SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                        SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                        /*SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());*/
                        SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                        SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                        SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                        SharedPref.putLong(SharedPref.VIDEOS_HISTORY, GetSet.getVideosHistory());
                        SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                        SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                        SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                        SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                        SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                        SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                        SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                        SharedPref.putBoolean(SharedPref.INTEREST_NOTIFICATION, GetSet.getInterestNotification());
                        SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                        SharedPref.putString(SharedPref.REFERAL_LINK, GetSet.getReferalLink());
                        SharedPref.putString(SharedPref.CREATED_AT, GetSet.getCreatedAt());

                        SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                        SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());


                        if (profile.getVideosCount() != null && Long.parseLong(String.valueOf(profile.getVideosCount())) > 0) {
                            isVideoDeleted = false;
                        }
                        setProfile(profile);
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: "+t);
                    call.cancel();
                }
            });
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    private void setProfile(ProfileResponse profile) {

        if (profile != null && getActivity() != null && context != null) {

//            setPrimeView(profile);

            txtName.setText(profile.getName() + ", " + profile.getAge());
            Glide.with(context)
                    .load(profile.getUserImage())

                    //   .signature(new MediaStoreSignature("image/jpg", new Date().getTime(), 0))
                    .error(R.drawable.default_profile_image)
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
                    .placeholder(R.drawable.default_profile_image)
                    .into(profileImage);

            Timber.d("setProfile profile Image: %s", profile.getUserImage());


            if (profile.getBio() == null || profile.getBio().equals(""))
                userBio.setVisibility(View.GONE);
            else {
                userBio.setText(profile.getBio());
                userBio.setVisibility(View.VISIBLE);
            }

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


            txtUserName.setText(profile.getUserName());

            txtFollowersCount.setText(String.valueOf(profile.getFollowers() != null ? profile.getFollowers() : 0));
            txtFollowingsCount.setText(String.valueOf(profile.getFollowings() != null ? profile.getFollowings() : 0));
            genderImage.setImageResource(profile.getGender().equals(Constants.TAG_MALE) ?
                    R.drawable.men : R.drawable.women);

            txtVideoCount.setText(String.valueOf(profile.getVideosCount() != null ? profile.getVideosCount() : 0));

           // txtGemsCount.setText(String.valueOf((profile.getAvailableGems() != null ? profile.getAvailableGems() : 0)));
           // txtGemsCount.setText(fmtFloat(profile.getAvailableGems()));
            txtGemsCount.setText(profile.getPurChasedVotes()!=null? profile.getPurChasedVotes():"0");
            // txtGemsCount.setText(String.valueOf((profile.getAvailableGems() != null ? profile.getAvailableGems() : 0)));
            txtGemsCount.setText(fmtFloat(profile.getAvailableGems()));
            txtGiftsCount.setText(String.valueOf((profile.getAvailableGifts() != null ? profile.getAvailableGifts() : 0)));
            txtHeartCount.setText(String.valueOf(profile.getLikes() != null ? profile.getLikes() : 0));


            twitterIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getTwitter(),"com.twitter.android"));
            totalVotes.setText(profile.getTotal_vote_count()!=null ? profile.getTotal_vote_count() : "0");
            giftBoxCount.setText(profile.getGiftbox_count()!=null ? profile.getGiftbox_count():"0");
            blueDiamond.setText(profile.getBluediamond_count()!=null? profile.getBluediamond_count():"0");
            goldenStarCount.setText(profile.getGoldenstar_count()!=null? profile.getGoldenstar_count():"0");
            receivedVotes.setText(profile.getTotal_vote_count()!=null ? profile.getTotal_vote_count():"0");

            viewCount.setText(profile.getTotal_views_count()!=null? profile.getTotal_views_count():"0");
            shareCount.setText(profile.getTotal_share_count()!=null? profile.getTotal_share_count():"0");
            referralCount.setText(profile.getTotal_referals_count()!=null? profile.getTotal_referals_count():"0");

            YouTubeIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getYoutube_link(),"com.google.android.youtube"));

            instaIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getInstagram(),"com.instagram.android"));

            facebookIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getFb(),"com.facebook.katana"));

            linkedInIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getLinkedin(),"com.inked"));

            snapChatIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getSnapchat(),"com.snapchat.android"));

            whatsappIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getWhatsapp(),"com.whatsapp"));

            twitterIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getTwitter(),"com.twitter.android"));

        }

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
                    Intent intent = requireActivity().getPackageManager().getLaunchIntentForPackage(packageName);
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

    private void setPrimeView(ProfileResponse profile) {
        if (profile.getPremiumMember().equals(Constants.TAG_TRUE)) {
            premiumImage.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            premiumLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            txtPrice.setText(AppUtils.getPrimeContent(context));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(View.GONE);
            premiumImage.setVisibility(View.GONE);
        } else {
            premiumImage.setVisibility(View.GONE);
            premiumLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            txtPrice.setText(AppUtils.getPrimeContent(context));
        }
    }

    @OnClick({R.id.profileImage, R.id.btnEdit,/*R.id.btnRenewal,*/ /*R.id.renewalLay,*/ R.id.btnSettings, R.id.fansLay, R.id.followingLay,
            /*R.id.followingsLay, R.id.btnSubscribe, R.id.subscribeLay*//*, R.id.gemsLay,*/ R.id.giftsLay,/*, R.id.videoCountLay,
            R.id.btnShare*/ R.id.favCard, R.id.videoCard, R.id.coinsCard, R.id.giftsCard, R.id.btnSubscribe, R.id.subscribeLay,
            R.id.btnRenewal, R.id.renewalLay,R.id.liveIV, R.id.referralCard, R.id.shareCard, R.id.viewsCard, R.id.lay_like
    , R.id.votesLay})

    /*@OnClick({R.id.btnSave, R.id.btnBack, R.id.profileImage, R.id.btnSubscribe, R.id.subscribeLay,
            R.id.btnRenewal, R.id.renewalLay, *//*R.id.txtLocation,*//*})*/

    public void onViewClicked(View view) {
        if (isPreventMultipleClick())
            return;
        switch (view.getId()) {

            case R.id.favCard: {
                App.preventMultipleClick(favCard);
                Intent intent = new Intent(getActivity(), FavouritesActivity.class);
                startActivity(intent);
/*                Intent intent = new Intent(getActivity(), StreamListActivity.class);
                startActivity(intent);*/

            }
            break;

            case R.id.profileImage: {
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                animateZoom(getActivity());
            }
            break;
            case R.id.btnEdit:
                App.preventMultipleClick(btnEdit);

                /* Interstial Ads Addon*/

                Intent profileIntent = new Intent(getActivity(), EditProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                requireActivity().overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);

                break;
//            case R.id.btnRenewal:
//            case R.id.renewalLay:
//            case R.id.btnSubscribe:
//            case R.id.subscribeLay:
//                App.preventMultipleClick(renewalLay);
//                App.preventMultipleClick(subscribeLay);
//                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
//                    Intent primeIntent = new Intent(getActivity(), PrimeActivity.class);
//                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    startActivity(primeIntent);
//                }
//                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnSettings);
                Intent primeIntent = new Intent(getActivity(), SettingsActivity.class);
                primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(primeIntent);
                /*animateZoom(getContext());*/
                break;
            case R.id.followingLay:
                App.preventMultipleClick(followingLay);
                Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followersIntent.putExtra(Constants.TAG_FROM,"profile");
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followersIntent);
                break;
            case R.id.fansLay:
                App.preventMultipleClick(fansLay);
                Intent followingsIntent = new Intent(getActivity(), FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followingsIntent.putExtra(Constants.TAG_FROM,"profile");
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followingsIntent);
                break;
            case R.id.giftsCard:
                App.preventMultipleClick(giftsCard);
             /*   if (GetSet.getGifts() == 0) {
                    if (alertDialog != null && alertDialog.isAdded()) {
                        return;
                    }
                    alertDialog = new DialogCreditGems();
                    alertDialog.setCallBack(new OnOkCancelClickListener() {
                        @Override
                        public void onOkClicked(Object o) {
                            alertDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onCancelClicked(Object o) {

                        }
                    });
                    alertDialog.setContext(getActivity());
                    alertDialog.setMessage(getString(R.string.you_dont_have_any_gifts));
                    alertDialog.show(getChildFragmentManager(), TAG);
                } else {*/
                Intent giftIntent = new Intent(getActivity(), ConvertGiftActivity.class);
                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(giftIntent);
//                }
                break;
            case R.id.coinsCard: {
                App.preventMultipleClick(coinsCard);
                Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
            }
            break;
            case R.id.videoCard:
                App.preventMultipleClick(videoCard);
                Intent intent = new Intent(getContext(), ProfileVideos.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_OWN_PROFILE);
                intent.putExtra(Constants.TAG_PROFILE_ID, GetSet.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;

            case R.id.liveIV:
//                Toast.makeText(getContext(), "Live", Toast.LENGTH_SHORT).show();
                App.preventMultipleClick(liveIV);
                Intent stream = new Intent(context, PublishActivity.class);

                Intent liveStreaming = new Intent(getContext(), LiveStreamActivity.class);
                liveStreaming.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                liveStreaming.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                liveStreaming.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                liveStreaming.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                liveStreaming.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                startActivity(liveStreaming);
              /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mPermissionsGranted = hasPermissions(requireActivity(), mRequiredPermissions);
                    if (!mPermissionsGranted) {
                        ActivityCompat.requestPermissions(requireActivity(), mRequiredPermissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                    } else {
                        mPermissionsGranted = true;
                        intentToPublishActivity();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mPermissionsGranted = hasPermissions(requireActivity(), mRequiredPermissions);
                    if (!mPermissionsGranted) {
                        ActivityCompat.requestPermissions(requireActivity(), mRequiredPermissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                    } else {
                        mPermissionsGranted = true;
                        intentToPublishActivity();
                    }
                } else {
                    mPermissionsGranted = true;
                    intentToPublishActivity();
                }*/
                break;
            case R.id.shareCard:
                App.preventMultipleClick(shareLay);
                Intent shareIntent = new Intent(getActivity(), HistoryActivity.class);
                shareIntent.putExtra(Constants.TAG_TYPE,"share");
                shareIntent.putExtra(Constants.TAG_TITLE, "Share");
                startActivity(shareIntent);
                break;
            case R.id.viewsCard:
                App.preventMultipleClick(viewsLay);
                Intent viewIntent = new Intent(getActivity(), HistoryActivity.class);
                viewIntent.putExtra(Constants.TAG_TYPE, "views");
                viewIntent.putExtra(Constants.TAG_TITLE, "View History");
                startActivity(viewIntent);
                break;
            case R.id.referralCard:
                App.preventMultipleClick(referralLay);
                Intent referral = new Intent(getActivity(), HistoryActivity.class);
                referral.putExtra(Constants.TAG_TYPE,"referral");
                referral.putExtra(Constants.TAG_TITLE,"Referral History");
                startActivity(referral);
                break;
            case R.id.lay_like:
                App.preventMultipleClick(likeLay);
                Intent like = new Intent(getActivity(), HistoryActivity.class);
                like.putExtra(Constants.TAG_TYPE,Constants.HIS_LIKES);
                like.putExtra(Constants.TAG_TITLE, "Like History");
                startActivity(like);
                break;
            case R.id.votesLay:
                App.preventMultipleClick(votesLay);
                Intent votes = new Intent(getActivity(), HistoryActivity.class);
                votes.putExtra(Constants.TAG_TYPE, "receivedvotes");
                votes.putExtra(Constants.TAG_TITLE,"Votes History");
                startActivity(votes);
                break;
            default:
                break;
        }
    }

    private void intentToPublishActivity() {
        Intent stream = new Intent(context, PublishActivity.class);
        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
        stream.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
        stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
        stream.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
        stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
        startActivity(stream);

    }

    public static void animateZoom(Context context) {
        ((Activity) context).
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!NetworkReceiver.isConnected()) {
            initProfile();
        } else {
            getProfile();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProfileUpdated(GetProfileEvent profileEvent) {
        Timber.d("onProfileUpdated: uptadted");
        setProfile(profileEvent.getProfileResponse());
    }

    /*@Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        App.makeToast(TAG + ":" + hidden);
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {
            if (context != null) {
                /*Glide.with(context)
                        .load(  GetSet.getUserImage())
                        .apply(App.getProfileImageRequest())
                        .into(profileImage);*/

                Glide.with(context)
                        .load(GetSet.getUserImage())
                        .error(R.drawable.default_profile_image)
                        .placeholder(R.drawable.default_profile_image)
                        // .apply(App.getProfileImageRequest())
                        .into(profileImage);
            }
        } else if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_REQUEST_CODE) {
            if (context != null) {
                profileResponse = (ProfileResponse) data.getSerializableExtra(Constants.TAG_PROFILE_DATA);
                setProfile(profileResponse);
            }
        } /*else if (resultCode == RESULT_OK && requestCode == StreamConstants.DELETE_REQUEST_CODE) {
            isVideoDeleted = data.getBooleanExtra(Constants.IS_VIDEO_DELETED, false);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }
    public void updateProfile() {
        getProfile();
    }
    public boolean isPermissionsGranted(String[] permissions) {
        boolean isGranted = false;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            } else {
                isGranted = true;
            }
        }
        return isGranted;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsGranted = true;
        switch (requestCode) {

            case hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE: {
                if (!isPermissionsGranted(permissions)) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                            requestPermissions(permissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                        } else {
                            Toast.makeText(getContext(), "Enable Camera and MicroPhone permissions", Toast.LENGTH_SHORT).show();
//                            openPermissionDialog(permissions);
                            //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireActivity().getPackageName()));
                            startActivity(i);
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                        } else {
                            Toast.makeText(getContext(), "Enable Camera, MicroPhone and Storage permissions", Toast.LENGTH_SHORT).show();
//                            openPermissionDialog(permissions);
                            //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + requireActivity().getPackageName()));
                            startActivity(i);
                        }
                    }
                } else {
                    intentToPublishActivity();
                }
            }
        }
    }
}


