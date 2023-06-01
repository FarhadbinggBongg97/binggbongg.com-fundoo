package com.app.binggbongg.fundoo.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.app.binggbongg.fundoo.PrivacyActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.ConvertGiftActivity;
import com.app.binggbongg.fundoo.DialogCreditGems;
import com.app.binggbongg.fundoo.FollowersActivity;
import com.app.binggbongg.fundoo.GemsStoreActivity;
import com.app.binggbongg.fundoo.ImageViewActivity;
import com.app.binggbongg.fundoo.PrimeActivity;
import com.app.binggbongg.fundoo.ProfileVideos;
import com.app.binggbongg.fundoo.SettingsActivity;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.view.View.GONE;
import static com.app.binggbongg.fundoo.App.fmtFloat;

@SuppressLint("NonConstantResourceId")
public class MyProfileActivity extends BaseFragmentActivity {

    private static String TAG = MyProfileActivity.class.getSimpleName();

    @BindView(R.id.profileImage)
    ShapeableImageView profileImage;

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnEdit)
    ImageView btnEdit;

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
    /*@BindView(R.id.btnRenewal)
    TextView btnRenewal;*/
    /*@BindView(R.id.renewalLay)
    RelativeLayout renewalLay;*/
    @BindView(R.id.genderImage)
    ImageView genderImage;

    /*@BindView(R.id.txtTitle)
    TextView txtTitle;*/
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    /*@BindView(R.id.btnShare)
    ImageView btnShare;*/

    @BindView(R.id.fansLay)
    LinearLayout fansLay;

    @BindView(R.id.followingLay)
    LinearLayout followingLay;

    @BindView(R.id.premiumImage)
    RoundedImageView premiumImage;
    /*@BindView(R.id.scrollView)
    ScrollView scrollView;*/
    /*@BindView(R.id.txtPrimeTitle)
    TextView txtPrimeTitle;*/
    /*@BindView(R.id.txtPrice)
    TextView txtPrice;*/
    /*@BindView(R.id.premiumLay)
    RelativeLayout premiumLay;*/
    /*@BindView(R.id.btnSubscribe)
    Button btnSubscribe;*/
    /*@BindView(R.id.subscribeLay)
    RelativeLayout subscribeLay;*/

    /*@BindView(R.id.gemsLay)
    CardView gemsLay;*/
    /*@BindView(R.id.giftsLay)
    RelativeLayout giftsLay;*/
    @BindView(R.id.adView)
    AdView adView;
    /*@BindView(R.id.txtSubTitle)
    TextView txtSubTitle;*/

    /*@BindView(R.id.imageLay)
    RelativeLayout imageLay;*/
    /*@BindView(R.id.divider)
    View divider;*/
    /*@BindView(R.id.txtMyVideo)
    TextView txtMyVideo;*/
    @BindView(R.id.txtVideoCount)
    TextView txtVideoCount;
    /*@BindView(R.id.videoCountLay)
    CardView videoCountLay;*/
    /*@BindView(R.id.iconDiamond)
    ImageView iconDiamond;*/
    /*@BindView(R.id.txtRenewalTitle)
    TextView txtRenewalTitle;*/
    /*@BindView(R.id.txtRenewalDes)
    TextView txtRenewalDes;*/
    /*@BindView(R.id.contentLay)
    LinearLayout contentLay;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;*/
    /*@BindView(R.id.primeBgLay)
    RelativeLayout primeBgLay;*/
    /*@BindView(R.id.renewalBgLay)
    RelativeLayout renewalBgLay;*/

    /*@BindView(R.id.txtFollowers)
    TextView txtFollowers;
    @BindView(R.id.txtFollowings)
    TextView txtFollowings;*/

    @BindView(R.id.userBio)
    TextView userBio;

    @BindView(R.id.txtUserName)
    TextView txtUserName;

    @BindView(R.id.favCard)
    MaterialCardView favCard;

    @BindView(R.id.videoCard)
    MaterialCardView videoCard;

    @BindView(R.id.coinsCard)
    MaterialCardView coinsCard;

    @BindView(R.id.giftsCard)
    MaterialCardView giftsCard;

    @BindView(R.id.premiumLay)
    RelativeLayout premiumLay;

    @BindView(R.id.renewalLay)
    MaterialCardView renewalLay;

    @BindView(R.id.subscribeLay)
    MaterialCardView subscribeLay;

    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;

    @BindView(R.id.txtPrimeTitle)
    TextView txtPrimeTitle;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.votesLay) LinearLayout votesLay;
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





    /*@BindView(R.id.favCard)
    MaterialCardView txtFollowings;*/


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        /*if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }*/

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .build();
        SlidrInterface slidrInterface = Slidr.attach(this, config);

        initProfile();
        getProfile();

       // initPrimeView();
    }

    /*private void initView() {
        appUtils = new AppUtils(this);
        storageUtils = StorageUtils.getInstance(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AppUtils.getStatusBarHeight(getApplicationContext());
        *//*toolbar.setLayoutParams(params);
        btnShare.setVisibility(View.VISIBLE);*//*
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
    }*/

    @SuppressLint("UseCompatLoadingForDrawables")
    public void initProfile() {

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setImageResource(R.drawable.arrow_w_l);
        btnBack.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
        btnSettings.setImageDrawable(getResources().getDrawable(R.drawable.settings_white));

        if (GetSet.getUserId() != null) {
            profileResponse = new ProfileResponse();
            profileResponse.setUserId(GetSet.getUserId());
            /*profileResponse.setLoginId(GetSet.getLoginId());*/
            profileResponse.setName(GetSet.getName());
            profileResponse.setUserName(GetSet.getUserName());
            profileResponse.setUserImage(GetSet.getUserImage());
            profileResponse.setDob(GetSet.getDob());
            profileResponse.setGender(GetSet.getGender());
            profileResponse.setAge(Integer.valueOf(GetSet.getAge()));
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
            /*profileResponse.setPrivacyAge(GetSet.getPrivacyAge());
            profileResponse.setPrivacyContactMe(GetSet.getPrivacyContactMe());*/
            profileResponse.setBio(GetSet.getBio());
            setProfile(profileResponse);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }


    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null && NetworkReceiver.isConnected()) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        ProfileResponse profileResponse = profile;
                        GetSet.setUserId(profile.getUserId());
                        /*GetSet.setLoginId(profile.getLoginId());*/
                        GetSet.setName(profile.getName());
                        GetSet.setDob(profile.getDob());
                        GetSet.setAge(String.valueOf(profile.getAge()));
                        GetSet.setGender(profile.getGender());
                        GetSet.setUserImage(profile.getUserImage());
                        /*GetSet.setLocation(profile.getLocation());*/
                        GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                        GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                      //  GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                        GetSet.setGems(profile.getPurChasedVotes() != null ? Float.parseFloat(profile.getPurChasedVotes()) : 0F);
                        GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0L);
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        /*GetSet.setPrivacyAge(profile.getPrivacyAge());
                        GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());*/
                        GetSet.setShowNotification(String.valueOf(profile.getShowNotification()));
                        GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                        GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                        GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));
                        GetSet.setReferalLink(profile.getReferalLink());
                        GetSet.setCreatedAt(profile.getCreatedAt());

                        GetSet.setPostCommand(profile.getCommentPrivacy());
                        GetSet.setSendMessage(profile.getMessagePrivacy());

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
                        SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                        SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                        SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
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



                        setProfile(profile);
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void setProfile(ProfileResponse profile) {

        Timber.d("setProfile: %s", profile.getUserImage());

        setPrimeView(profile);
        txtName.setText(profile.getName() + ", " + profile.getAge());
        Glide.with(getBaseContext())
                .load(profile.getUserImage())

                .error(R.drawable.default_profile_image)
                .placeholder(R.drawable.default_profile_image)
                //.apply(App.getProfileImageRequest())
                .into(profileImage);


        if (profile.getBio().equals("")) userBio.setVisibility(View.GONE);
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

        txtFollowersCount.setText(String.valueOf(profile.getFollowers() != null ? profile.getFollowers() : ""));
        txtFollowingsCount.setText(String.valueOf(profile.getFollowings() != null ? profile.getFollowings() : ""));
        genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                getResources().getDrawable(R.drawable.men) : getResources().getDrawable(R.drawable.women));


        /*txtGemsCount.setText((String.valueOf(profile.getAvailableGems() != null ? profile.getAvailableGems() : "0")));*/
        txtVideoCount.setText(String.valueOf(profile.getVideosCount() != null ? profile.getVideosCount() : 0));
        txtGemsCount.setText(fmtFloat(profile.getAvailableGems()));
        txtGiftsCount.setText(String.valueOf((profile.getAvailableGifts() != null ? profile.getAvailableGifts() : "0")));
        txtHeartCount.setText(String.valueOf(profile.getLikes() != null ? profile.getLikes() : 0));

        totalVotes.setText(profile.getTotal_vote_count()!=null ? profile.getTotal_vote_count() : "0");
        giftBoxCount.setText(profile.getGiftbox_count()!=null ? profile.getGiftbox_count():"0");
        blueDiamond.setText(profile.getBluediamond_count()!=null? profile.getBluediamond_count():"0");
        goldenStarCount.setText(profile.getGoldenstar_count()!=null? profile.getGoldenstar_count():"0");
        receivedVotes.setText(profile.getTotal_vote_count()!=null ? profile.getTotal_vote_count():"0");

        viewCount.setText(profile.getTotal_views_count()!=null? profile.getTotal_views_count():"0");
        shareCount.setText(profile.getTotal_share_count()!=null? profile.getTotal_share_count():"0");
        referralCount.setText(profile.getTotal_referals_count()!=null? profile.getTotal_referals_count():"0");

        YouTubeIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getYoutube_link(),"com.google.android.youtube"));

        twitterIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getTwitter(),"com.twitter.android"));

        instaIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getInstagram(),"com.instagram.android"));

        facebookIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getFb(),"com.facebook.katana"));

        linkedInIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getLinkedin(),"com.inked"));

        snapChatIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getSnapchat(),"com.snapchat.android"));

        whatsappIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getWhatsapp(),"com.whatsapp"));

    }

    private void GotoSocialMedia(Context context, String link, String packageName) {
        if (link != null && !link.equals("")){
            if (packageName.equals("com.whatsapp")){
                link="https://wa.me/"+link;
            }
            if (AppUtils.appInstalledOrNot(context,packageName)){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(link.toString()));
                startActivity(intent);
            }else{
                Toast.makeText(context, "Cannot find any App to perform this link", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, PrivacyActivity.class);
                intent.putExtra("from", "video");
                intent.putExtra("link_url", link.toString());
                startActivity(intent);
            }

        }else if(link == null){
            Toast.makeText(context, "No link has found", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Invalid url", Toast.LENGTH_SHORT).show();
        }

    }

    private void initPrimeView() {
        /*Init Prime View*/
        if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            renewalLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            premiumLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(this));
            txtPrice.setText(AppUtils.getPrimeContent(this));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(GONE);
        } else {
            premiumLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(this));
            txtPrice.setText(AppUtils.getPrimeContent(this));
        }
    }

    private void setPrimeView(ProfileResponse profile) {
        if (profile.getPremiumMember().equals(Constants.TAG_TRUE)) {
            premiumImage.setVisibility(View.VISIBLE);
            /*renewalLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            premiumLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            txtPrice.setText(AppUtils.getPrimeContent(context));*/
        } else if (GetSet.isOncePurchased()) {
            /*renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(View.GONE);*/
            premiumImage.setVisibility(View.GONE);
        } else {
            premiumImage.setVisibility(View.GONE);
            /*premiumLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            txtPrice.setText(AppUtils.getPrimeContent(context));*/
        }
    }


    @OnClick({R.id.profileImage, R.id.btnEdit, /*R.id.btnRenewal,*/ /*R.id.renewalLay,*/ R.id.btnSettings, R.id.fansLay,
            R.id.followingLay,/* R.id.btnSubscribe, R.id.subscribeLay*//*, R.id.gemsLay,*/ R.id.giftsLay,/*, R.id.videoCountLay,
            R.id.btnShare*/ R.id.favCard, R.id.videoCard, R.id.coinsCard, R.id.giftsCard, R.id.btnBack})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.favCard: {
                App.preventMultipleClick(favCard);
                Intent intent = new Intent(MyProfileActivity.this, FavouritesActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.profileImage: {
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(MyProfileActivity.this, ImageViewActivity.class);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_OWN_PROFILE);
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                MyProfileActivity.this.overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
            }
            break;
            case R.id.btnEdit:
                App.preventMultipleClick(btnEdit);
                Intent profileIntent = new Intent(MyProfileActivity.this, EditProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_PROFILE_DATA, profileResponse);
                startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);
                break;
            case R.id.btnRenewal:
            case R.id.renewalLay:
            case R.id.btnSubscribe:
            case R.id.subscribeLay:
                if (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    Intent primeIntent = new Intent(MyProfileActivity.this, PrimeActivity.class);
                    primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(primeIntent);
                }
                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnSettings);
                Intent primeIntent = new Intent(MyProfileActivity.this, SettingsActivity.class);
                primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(primeIntent);
                break;
            /*case R.id.followersLay:
                Intent followersIntent = new Intent(MyProfileActivity.this, FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                followersIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followersIntent);
                break;*/
            case R.id.followingLay:
                App.preventMultipleClick(followingLay);
                Intent followersIntent = new Intent(MyProfileActivity.this, FollowersActivity.class);
                followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
                followersIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                startActivity(followersIntent);
                break;
            case R.id.fansLay:
                App.preventMultipleClick(fansLay);
                Intent followingsIntent = new Intent(MyProfileActivity.this, FollowersActivity.class);
                followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
                followingsIntent.putExtra(Constants.TAG_PARTNER_ID, GetSet.getUserId());
                followingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(followingsIntent);
                break;

            case R.id.btnBack:
                /*finish();*/
                App.preventMultipleClick(btnBack);

                backPressed();

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
                    alertDialog.setContext(MyProfileActivity.this);
                    alertDialog.setMessage(getString(R.string.you_dont_have_any_gifts));
                    alertDialog.show(getChildFragmentManager(), TAG);
                } else {*/
                Intent giftIntent = new Intent(MyProfileActivity.this, ConvertGiftActivity.class);
                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(giftIntent);
//                }
                break;
            case R.id.coinsCard: {
                App.preventMultipleClick(coinsCard);
                Intent gemsIntent = new Intent(MyProfileActivity.this, GemsStoreActivity.class);
                gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(gemsIntent);
            }
            break;
            case R.id.videoCard:

                Intent intent = new Intent(MyProfileActivity.this, ProfileVideos.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_OWN_PROFILE);
                intent.putExtra(Constants.TAG_PROFILE_ID, GetSet.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
                /*Intent profile = new Intent(context, UserVideoActivity.class);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                profile.putExtra(Constants.TAG_PARTNER_ID, profileResponse.getUserId());
                profile.putExtra(Constants.TAG_PARTNER_NAME, profileResponse.getName());
                profile.putExtra(Constants.TAG_PARTNER_IMAGE, profileResponse.getUserImage());
                profile.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                startActivity(profile);*/

            /*case R.id.videoHistoryLay: {
             *//*Intent intent = new Intent(context, UserVideoActivity.class);
                intent.putExtra(Constants.TAG_PARTNER_ID, profileResponse.getUserId());
                intent.putExtra(Constants.TAG_PARTNER_NAME, profileResponse.getName());
                intent.putExtra(Constants.TAG_PARTNER_IMAGE, profileResponse.getUserImage());
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_HISTORY);
                intent.putExtra(Constants.IS_VIDEO_DELETED, isVideoDeleted);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent, StreamConstants.DELETE_REQUEST_CODE);*//*
            }
            break;*/
            case R.id.btnShare:
                /*btnShare.setEnabled(false);
                new GetQRCodeTask().execute();*/
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {

            Glide.with(MyProfileActivity.this)
                    .load(GetSet.getUserImage())
                    .apply(App.getProfileImageRequest())
                    .into(profileImage);
        } else if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_REQUEST_CODE) {
            if (MyProfileActivity.this != null) {
                ProfileResponse profileResponse = (ProfileResponse) data.getSerializableExtra(Constants.TAG_PROFILE_DATA);
                setProfile(profileResponse);
            }
        } /*else if (resultCode == RESULT_OK && requestCode == StreamConstants.DELETE_REQUEST_CODE) {
            isVideoDeleted = data.getBooleanExtra(Constants.IS_VIDEO_DELETED, false);
        }*/
    }


    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
        if (!this.isDestroyed()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        //AppUtils.showSnack(this, parentLay, isConnected);
    }

    public void showLoading() {
        /*Disable touch options*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onBackPressed() {
        /*        super.onBackPressed();*/
        backPressed();
    }


    public void backPressed() {
        setResult(RESULT_OK);
        finish();
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }

}


