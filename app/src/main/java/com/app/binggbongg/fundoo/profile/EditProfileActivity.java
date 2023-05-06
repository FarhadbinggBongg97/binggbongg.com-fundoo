package com.app.binggbongg.fundoo.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.ImageViewActivity;
import com.app.binggbongg.fundoo.PrimeActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.card.MaterialCardView;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.view.View.GONE;

import java.util.Objects;

@SuppressLint("NonConstantResourceId")
public class EditProfileActivity extends BaseFragmentActivity {

    private static final String TAG = "EditProfileActivity";

    @BindView(R.id.profileImage)
    ImageView profileImage;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnEdit)
    ImageView btnEdit;

    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    /*@BindView(R.id.txtPrimeTitle)
    TextView txtPrimeTitle;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.premiumImage)
    ImageView premiumImage;
    @BindView(R.id.premiumLay)
    RelativeLayout premiumLay;
    @BindView(R.id.btnSubscribe)
    Button btnSubscribe;
    @BindView(R.id.primeBgLay)
    RelativeLayout primeBgLay;
    @BindView(R.id.subscribeLay)
    MaterialCardView subscribeLay;
    @BindView(R.id.txtRenewalTitle)
    TextView txtRenewalTitle;
    @BindView(R.id.txtRenewalDes)
    TextView txtRenewalDes;
    @BindView(R.id.btnRenewal)
    AppCompatButton btnRenewal;
    @BindView(R.id.renewalBgLay)
    RelativeLayout renewalBgLay;

    @BindView(R.id.renewalLay)
    MaterialCardView renewalLay;*/
    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.txtGender)
    TextView txtGender;
    @BindView(R.id.txtDob)
    TextView txtDob;
    @BindView(R.id.edtBio)
    EditText edtBio;

    @BindView(R.id.edtPayPal)
    AppCompatEditText edtPayPal;
    @BindView(R.id.paymentLay)
    MaterialCardView paymentLay;
    @BindView(R.id.btnSave)
    TextView btnSave;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    @BindView(R.id.lay_socialMedia)
    TextView socialMediaLay;
    @BindView(R.id.et_cityName) TextView cityName;
    @BindView(R.id.et_countryName) TextView countryName;
    @BindView(R.id.et_stateName) TextView stateName;
    @BindView(R.id.et_mobileNumber) TextView mobileNumber;
    @BindView(R.id.et_emailId) TextView emailId;
    @BindView(R.id.et_website) AppCompatEditText website;

    private ApiInterface apiInterface;
    private ProfileResponse profileResponse;
    private AppUtils appUtils;

    /*RequestOptions profileImageRequest = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .dontAnimate();*/

    private String strLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        profileResponse = (ProfileResponse) getIntent().getSerializableExtra(Constants.TAG_PROFILE_DATA);
        initView();

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
            profileResponse.setPaypalId(GetSet.getPaypal_id());
            profileResponse.setCountry(GetSet.getCountryName());
            profileResponse.setState(GetSet.getStateName());
            profileResponse.setCity(GetSet.getCityName());
            profileResponse.setPhoneNumber(GetSet.getPhoneNumber());
            profileResponse.setEmail(GetSet.getEmail());
            profileResponse.setWebLink(GetSet.getWebsiteLink());
            setProfile(profileResponse);
        } /*else {

        }*/


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        btnEdit.setVisibility(GONE);

        appUtils = new AppUtils(this);
        edtName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        edtPayPal.setFilters(new InputFilter[]{AppUtils.EMOJI_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
        edtName.setSelection(edtName.getText().length());
        edtName.requestFocus();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setImageResource(R.drawable.arrow_w_l);

        // paymentLay.setVisibility((AdminData.showMoneyConversion.equals("1")) ? View.VISIBLE : GONE);

        /*edtBio.setOnKeyListener((v, keyCode, event) -> {
            Timber.d("initView: %s", keyCode);
            return false;
        });*/

        loadAd();
    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            MobileAds.initialize(this,
                    AdminData.googleAdsId);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
        }
    }


    @OnClick({R.id.btnSave, R.id.btnBack, R.id.profileImage,/*R.id.btnSubscribe,R.id.subscribeLay,
            R.id.btnRenewal, R.id.renewalLay*/ /*R.id.txtLocation,*/ R.id.paymentLay, R.id.lay_socialMedia})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                if (TextUtils.isEmpty(edtName.getText().toString()))
                    edtName.setError(getString(R.string.enter_name));

                hideKeyboard(EditProfileActivity.this);
                saveProfile();
                break;
            case R.id.btnBack:
                hideKeyboard(EditProfileActivity.this);
                onBackPressed();
                break;
          /*  case R.id.btnSubscribe:
            case R.id.subscribeLay:
            case R.id.btnRenewal:
            case R.id.renewalLay:
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    if (NetworkReceiver.isConnected()) {
                        Intent prime = new Intent(getApplicationContext(), PrimeActivity.class);
                        startActivity(prime);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                        Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                    }

                }
                break;*/
            case R.id.profileImage:
                App.preventMultipleClick(profileImage);
                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
                break;
            case R.id.paymentLay:

                edtPayPal.requestFocus();
                edtPayPal.setFocusableInTouchMode(true);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtPayPal, InputMethodManager.SHOW_FORCED);
                break;
            /*case R.id.txtLocation:*/
            //case R.id.BioLay:
                /*Intent location = new Intent(this, LocationFilterActivity.class);
                location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                location.putExtra(Constants.TAG_LOCATION, strLocation != null ? strLocation : "");
                startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
*/
            //  break;
            case R.id.lay_socialMedia:
                App.preventMultipleClick(socialMediaLay);
                Intent link= new Intent(EditProfileActivity.this, SocialMediaLink.class);
                startActivity(link);
                break;
        }
    }


    private void saveProfile() {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setName(edtName.getText().toString());
            request.setPayPalId("" + edtPayPal.getText());
            request.setBio(edtBio.getText().toString());
            request.setWebsite(Objects.requireNonNull(website.getText()).toString());

            //request.setLocation("" + edtBio.getText());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {

                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.profile_updated_successfully));
//                        MainActivity.profileResponse = profile;
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
                        GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0);
                       // GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                        GetSet.setGems(profile.getPurChasedVotes() != null ? Float.parseFloat(profile.getPurChasedVotes()) : 0F);
                        GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0);
                        /*GetSet.setVideosHistory(profile.getWatchedCount() != null ? profile.getWatchedCount() : 0);*/
                        GetSet.setPremiumMember(profile.getPremiumMember());
                        GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                        /*GetSet.setPrivacyAge(profile.getPrivacyAge());*/
                        GetSet.setPrivacyContactMe(profile.getBlockedByMe());
                        GetSet.setShowNotification(String.valueOf(profile.getShowNotification()));
                        GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                        GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                        GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));
                        GetSet.setBio(profile.getBio());

                        GetSet.setPostCommand(profile.getCommentPrivacy());
                        GetSet.setSendMessage(profile.getMessagePrivacy());

                        GetSet.setPaypal_id(profile.getPaypalId());

                        GetSet.setYoutubeLink(profile.getYoutube_link());
                        GetSet.setSnapChatLink(profile.getSnapchat());
                        GetSet.setWhatsappLink(profile.getWhatsapp());
                        GetSet.setTwitterLink(profile.getTwitter());
                        GetSet.setInstaLink(profile.getInstagram());
                        GetSet.setPinterestLink(profile.getPinterest());
                        GetSet.setTiktokLink(profile.getTiktok());
                        GetSet.setLinkedInLink(profile.getLinkedin());
                        GetSet.setFbLink(profile.getFb());

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
                        SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                        SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                        SharedPref.putString(SharedPref.BIO, GetSet.getBio());
                        SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

                        SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                        SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());


                        hideLoading();

                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_PROFILE_DATA, profile);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {

                    Log.d(TAG, "API onFailure: " + t.getMessage());

                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            /*App.makeToast(getString(R.string.no_internet_connection));*/
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void setProfile(ProfileResponse profile) {


        Glide.with(getApplicationContext())
                .load(profile.getUserImage())
                .error(R.drawable.default_profile_image)
                .placeholder(R.drawable.default_profile_image)
                .into(profileImage);

        txtGender.setText(profile.getGender());
        txtDob.setText(profile.getDob());
        edtBio.setText(profile.getBio());
        edtPayPal.setText(profile.getPaypalId());
        edtName.setText(profile.getName());
        countryName.setText(profile.getCountry());
        stateName.setText(profile.getState());
        cityName.setText(profile.getCity());
        mobileNumber.setText(profile.getPhoneNumber());
        emailId.setText(profile.getEmail());
        website.setText(profile.getWebLink());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.INTENT_REQUEST_CODE) {

            profileResponse.setUserImage(GetSet.getUserImage());
            Glide.with(getApplicationContext())
                    .load(GetSet.getUserImage())
                    .into(profileImage);
        }
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(GONE);
    }

/*
    private void initPrimeView() {
        */
/*Init Prime View*//*


        Timber.d("initPrimeView: GetSet.getPremiumMember() %s", GetSet.getPremiumMember());
        Timber.d("initPrimeView: GetSet.getPrimeContent() %s", AppUtils.getPrimeContent(getApplicationContext()));

        if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            renewalLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            premiumLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(GONE);
        } else {
            premiumLay.setVisibility(GONE);
            subscribeLay.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(GONE);
            txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
            txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        }
    }
*/

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if ((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(EditProfileActivity.this);
        }
        return super.dispatchTouchEvent(ev);
    } */

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
        /*initPrimeView();*/
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

}
