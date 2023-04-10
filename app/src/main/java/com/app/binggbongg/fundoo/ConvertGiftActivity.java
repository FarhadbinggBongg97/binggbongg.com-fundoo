package com.app.binggbongg.fundoo;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.callback.OnOkCancelClickListener;
import com.app.binggbongg.model.ConvertGiftRequest;
import com.app.binggbongg.model.ConvertGiftResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class ConvertGiftActivity extends BaseFragmentActivity {

    private static final String TAG = ConvertGiftActivity.class.getSimpleName();

    /*@BindView(R.id.btnEdit)
    ImageView btnEdit;*/

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtGiftsCount)
    TextView txtGiftsCount;

    @BindView(R.id.countDisplay)
    TextView countDisplay;

    @BindView(R.id.btnWithdraw)
    Button btnWithdraw;
    ApiInterface apiInterface;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.btnGems)
    RadioButton btnGems;
    //Gift to Money Conversion Addon
    //Declation here
    AlertDialog dialog;
    @BindView(R.id.optionLay)
    LinearLayout optionLay;

    @BindView(R.id.topLay)
    LinearLayout topLay;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.nullLay)
    RelativeLayout nullLay;

    @BindView(R.id.nullText)
    TextView nullText;

    @BindView(R.id.tv_voteCount) TextView totalVotes;
    @BindView(R.id.tv_blueDiamond) TextView blueDiamond;
    @BindView(R.id.tv_giftCount) TextView giftBoxCount;
    @BindView(R.id.goldstar_count) TextView goldenStarCount;


    DialogCreditGems alertDialog;
    private AppUtils appUtils;

    ProfileResponse profileResponse;

    // Gems to money addon
    public String conversiongems = null;
    public String conversioncash = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_convert_gift);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);

        profileResponse = new ProfileResponse();


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        initView();
    }

    private void initView() {

        progressBar.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        btnBack.setImageDrawable(getDrawable(R.drawable.arrow_w_l));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AppUtils.getStatusBarHeight(getApplicationContext());
        toolbar.setLayoutParams(params);

        txtTitle.setText(getResources().getString(R.string.withdraw));
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        btnSettings.setVisibility(View.VISIBLE);
        btnSettings.setImageResource(R.drawable.ic_round_history_24);

        topLay.setAlpha(0.4f);
        btnBack.setEnabled(false);
        btnWithdraw.setEnabled(false);
        /*btnEdit.setVisibility(View.GONE);*/

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

    public void showDialog(Activity activity, String goldStar){
        TextView starCount, increment, decrement;
        MaterialButton requestBtn;

        final Dialog dialog = new Dialog(activity, R.style.CustomTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.goldstar_dialog);

        starCount = dialog.findViewById(R.id.tv_count);
        increment = dialog.findViewById(R.id.tv_increment);
        decrement = dialog.findViewById(R.id.tv_decrement);
        requestBtn = dialog.findViewById(R.id.btn_request);

        starCount.setText(goldStar);

        increment.setOnClickListener(v -> {
            countIncrement(starCount, goldStar);

        });


        decrement.setOnClickListener(v -> {
            countDecrement(starCount);
        });


        requestBtn.setOnClickListener(v -> {
            if(TextUtils.isEmpty(GetSet.getPaypal_id())){
                Toast.makeText(activity, "Add your payment-id", Toast.LENGTH_SHORT).show();
            }else{
                goldStarToMoney(starCount);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void goldStarToMoney(TextView view){
        showLoading();
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", GetSet.getUserId());
        params.put("paypalid", GetSet.getPaypal_id());
        params.put("golden_stars", view.getText().toString());
        Log.d(TAG, "goldToMoney:Params" + params);
        Call<HashMap<String, String>> call = apiInterface.withdrawMoney(params);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, String>> call,
                                   @NonNull Response<HashMap<String, String>> response) {
                Log.d(TAG, "goldToMoney:response" + new Gson().toJson(response.body()));
                hideLoading();
                if(response.body()!=null && Objects.equals(response.body().get("status"), "true")){
                    Toast.makeText(ConvertGiftActivity.this, response.body().get("message"), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ConvertGiftActivity.this, response.body().get("message"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, String>> call, @NonNull Throwable t) {
                Log.e(TAG, "goldToMoney:onFailure: ", t);
                call.cancel();
                hideLoading();
            }
        });

    }

    private void countDecrement(TextView view) {
        String countString = view.getText().toString();
        int count = Integer.parseInt(countString);
        count = count - 1;
        if(count > 0)
            view.setText(Integer.toString(count));
    }

    private void countIncrement(TextView view, String goldStar) {
        String countString = view.getText().toString();
        int count = Integer.parseInt(countString);
        count = count + 1;
        Log.d(TAG, "increment" + count +" goldstar" + goldStar);
        if(count <= Integer.parseInt(goldStar))
            view.setText(Integer.toString(count));
    }


    @SuppressLint("StringFormatInvalid")
    private void getGems() {

        if (GetSet.getGifts() > 0) {
            countDisplay.setVisibility(View.VISIBLE);
            countDisplay.setText(String.valueOf(GetSet.getGifts()));
            txtGiftsCount.setVisibility(View.VISIBLE);
            btnWithdraw.setVisibility(View.VISIBLE);

            optionLay.setVisibility((("" + AdminData.showMoneyConversion).equals("1")) ? View.VISIBLE : View.GONE);

            if ((AdminData.showMoneyConversion).equals("1")) {
                if (btnGems.isChecked())
                    txtGiftsCount.setText(String.format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
                //Gifts to Money Conversion code here

            } else {
                txtGiftsCount.setText(String.format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
            }

        } else {
            txtGiftsCount.setText(getResources().getString(R.string.no_gift));
            txtGiftsCount.setVisibility(View.VISIBLE);
            btnWithdraw.setVisibility(View.INVISIBLE);
            countDisplay.setVisibility(View.INVISIBLE);
            optionLay.setVisibility(View.GONE);
        }

        topLay.setAlpha(1f);
        btnBack.setEnabled(true);
        btnWithdraw.setEnabled(true);

        progressBar.setVisibility(View.GONE);


        Timber.d("getGems: %s", GetSet.getGifts());
        Timber.d("getGift Earnings: %s", GetSet.getGiftEarnings());
    }

    @SuppressLint("StringFormatInvalid")
    private void getcash() {

        Double gems = Double.valueOf(profileResponse.getMoneyPriceValue());

        txtGiftsCount.setText(format(getString(R.string.gift_to_cash), " " + gems, "" + profileResponse.getMoneyCurrencySymbol()));

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnWithdraw, R.id.btnGems, R.id.btnSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnGems:
                txtGiftsCount.setText(String.format(getString(R.string.gift_to_gems), "" + GetSet.getGifts(), GetSet.getGiftEarnings()));
                btnWithdraw.setText(getString(R.string.withdraw));
                break;
            //Gifts To Money Conversion code here

            case R.id.btnWithdraw:
                if (Integer.parseInt(goldenStarCount.getText().toString()) > 0) {
                    /*if (btnGems.isChecked()) {
                        App.preventMultipleClick(btnWithdraw);
                        showConfirmDialog("");
                    }*/
                    //Gift to Money Conversion
                    btnWithdraw.setText(getString(R.string.withdraw));
                    showDialog(this, goldenStarCount.getText().toString());

                } else showAlertDialog(getString(R.string.you_dont_have_any_gold));
                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnSettings);
                Intent history = new Intent(this, HistoryActivity.class);
                history.putExtra(Constants.TAG_TYPE, "withdraw");
                history.putExtra(Constants.TAG_TITLE, "Withdraw");
                startActivity(history);
                break;
        }
    }


    //
    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConvertGiftActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(getString(R.string.convert_gifts_to_money_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                convertGems(dialog);
               /* String payPalId = (String) o;
                updatePayment(payPalId);*/
//                updatePayment(dialog);
                updatePayments(dialog);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
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

    private void updatePayments(DialogInterface dialog) {
        if (NetworkReceiver.isConnected()) {

            String pay = SharedPref.getString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());

            ConvertGiftRequest request = new ConvertGiftRequest();
            request.setGemsRequested(GetSet.getGiftCoversionEarnings());
            request.setUserId(GetSet.getUserId());
            request.setUserName(GetSet.getUserName());
            request.setPayPalId(pay);

            Log.i(TAG, "updatePaymentconver: " + request);
            Log.i(TAG, "updatePaymentconvert: " + profileResponse.getMoneyPriceValue() + GetSet.getUserId() + GetSet.getUserName() + GetSet.getPaypal_id());

            Timber.d("updatePayments: params %s", new Gson().toJson(request));

            Call<ConvertGiftResponse> call = apiInterface.convertToMoney(request);
            call.enqueue(new Callback<ConvertGiftResponse>() {
                @Override
                public void onResponse(Call<ConvertGiftResponse> call, Response<ConvertGiftResponse> response) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        Log.i(TAG, "updatePaymentconvertonResponse: " + response);
                        Toast.makeText(ConvertGiftActivity.this, "Waiting for Admin Approval...Money Will be transfer to your Account.", Toast.LENGTH_SHORT).show();
                        Log.e("Successful: ", "Waiting for Admin Approval...Money Will be transfer to your Account.");
                        finish();
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                        /*dialogPayPal.hideLoading();
                        dialogPayPal.dismissAllowingStateLoss();*/
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ConvertGiftResponse> call, Throwable t) {
                    t.printStackTrace();
                    Log.i(TAG, "updatePaymentconvertonFailure: " + t.getMessage());
                }
            });

        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void showConfirmDialog(String paypalID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConvertGiftActivity.this);
        if (paypalID.equals(""))
            builder.setMessage(getString(R.string.convert_gifts_to_gems_desc));
        else builder.setMessage(getString(R.string.convert_gifts_to_money_desc));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                convertGems(dialog, paypalID);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
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

    private void convertGems(DialogInterface dialog, String paypal) {
        progressBar.setVisibility(View.VISIBLE);
        ConvertGiftRequest request = new ConvertGiftRequest();
        request.setUserId(GetSet.getUserId());
        request.setType(btnGems.isChecked() ? Constants.TAG_GEMS : Constants.TAG_CASH);
        //Gifts to Money Conversion

        Timber.d("convertGems: params %s", new Gson().toJson(request));

        Call<ConvertGiftResponse> call = apiInterface.convertGifts(request);
        call.enqueue(new Callback<ConvertGiftResponse>() {
            @Override
            public void onResponse(Call<ConvertGiftResponse> call, Response<ConvertGiftResponse> response) {

                dialog.cancel();

                Timber.d("convertGems: response status %s", response.body().getStatus());
                Timber.d("convertGems: response message %s", response.body().getMessage());

                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                    if (btnGems.isChecked()) {
                        GetSet.setGems(response.body().getTotalGems());
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        getGems();
                        showAlertDialog(getString(R.string.gems_purchase_response));
                    }//Gift to Money Conversion
                } else {

                    dialog.dismiss();
                    Toast.makeText(ConvertGiftActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConvertGiftResponse> call, Throwable t) {
                Timber.d("onFailure: %s", t.getMessage());
                dialog.cancel();
            }
        });
    }

    private void showAlertDialog(String message) {
        alertDialog = new DialogCreditGems();
        alertDialog.setContext(ConvertGiftActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                finish();
            }

            @Override
            public void onCancelClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
                finish();
            }
        });
        progressBar.setVisibility(View.GONE);
        alertDialog.show(getSupportFragmentManager(), TAG);
    }

    private void getProfile() {

        // if (isConnected && GetSet.isIsLogged()) {

        if (NetworkReceiver.isConnected()) {

            if (GetSet.getUserId() != null && apiInterface != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());
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
                            //GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                            GetSet.setGems(profile.getPurChasedVotes() != null ? Float.parseFloat(profile.getPurChasedVotes()) : 0F);
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

                            GetSet.setPaypal_id(profile.getPaypalId());

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

                            totalVotes.setText(profile.getTotal_vote_count()!=null ? profile.getTotal_vote_count() : "0");
                            giftBoxCount.setText(profile.getGiftbox_count()!=null ? profile.getGiftbox_count():"0");
                            blueDiamond.setText(profile.getBluediamond_count()!=null? profile.getBluediamond_count():"0");
                            goldenStarCount.setText(profile.getGoldenstar_count()!=null? profile.getGoldenstar_count():"0");

                            loadAd();
                            //getGems();
                            topLay.setAlpha(1f);
                            btnBack.setEnabled(true);
                            btnWithdraw.setEnabled(true);
                            progressBar.setVisibility(View.GONE);

                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        call.cancel();
                    }
                });
            }
        } else {
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
        getProfile();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    private void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }
}
