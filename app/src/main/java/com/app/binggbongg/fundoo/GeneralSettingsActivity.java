package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.helper.AppWebSocket;
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
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class GeneralSettingsActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents {

    private static final String TAG = "GeneralSettingsActivity";

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btnPrivateChat)
    SwitchMaterial btnPrivateChat;

    @BindView(R.id.privateChatLay)
    RelativeLayout privateChatLay;
    /*@BindView(R.id.btnDnD)
    SwitchMaterial btnDnD;
    @BindView(R.id.dndLay)
    RelativeLayout dndLay;*/
    @BindView(R.id.btnFollow)
    SwitchMaterial btnFollow;
    @BindView(R.id.followLay)
    RelativeLayout followLay;
    @BindView(R.id.txtFollow)
    TextView txtFollow;

    @BindView(R.id.adView)
    AdView adView;


    private ApiInterface apiInterface;
    private AppUtils appUtils;
    private DBHelper dbHelper;
    private StorageUtils storageUtils;

    private Boolean isChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_settings);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        dbHelper = DBHelper.getInstance(this);
        storageUtils = StorageUtils.getInstance(this);
        initView();
    }

    private void initView() {

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        txtTitle.setText(getString(R.string.general_settings));
        setSettings();
        loadAd();

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

    }

    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            MobileAds.initialize(this, AdminData.googleAdsId);
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


    private void setSettings() {
        btnPrivateChat.setChecked(GetSet.getChatNotification().equals(Constants.TAG_TRUE));
        txtFollow.setText(getString(R.string.notify_if_anyone_follows));
        btnFollow.setChecked(GetSet.getFollowNotification().equals(Constants.TAG_TRUE));
    }

    private void updateSettings(String type, ProfileRequest request) {
        if (NetworkReceiver.isConnected()) {
//            showLoading();
            if (GetSet.getUserId() != null) {
                request.setUserId(GetSet.getUserId());

                request.setProfileId(GetSet.getUserId());
                Timber.d("request: %s", new Gson().toJson(request));

                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            Timber.d("onResponse: %s", new Gson().toJson(profile));

                            if (type.equalsIgnoreCase("chat")) {
                                GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                                SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                                btnPrivateChat.setChecked(GetSet.getChatNotification().equals(Constants.TAG_TRUE));
                                btnPrivateChat.setEnabled(true);
                            } else if (type.equalsIgnoreCase("follow")) {
                                GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                                SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                                btnFollow.setChecked(GetSet.getFollowNotification().equals(Constants.TAG_TRUE));
                                btnFollow.setEnabled(true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
//                        hideLoading();
                        call.cancel();
                    }
                });
            }
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
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
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {

    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @OnClick({R.id.btnBack, R.id.btnPrivateChat, R.id.privateChatLay, R.id.btnFollow, R.id.followLay, R.id.adView})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnPrivateChat:
                if (NetworkReceiver.isConnected()) {
                    updatePrivateChat();
                }
                break;
            /*case R.id.privateChatLay:
                if (NetworkReceiver.isConnected()) {
                    if(btnPrivateChat.isChecked())
                    updatePrivateChat();
                }
                break;*/
            /*case R.id.btnDnD:
                if (NetworkReceiver.isConnected()) {
                    if (btnDnD.isChecked()) {
                        btnPrivateChat.setChecked(false);
                        btnFollow.setChecked(false);
                    }
                    updateDND();
                }
                break;
            case R.id.dndLay:
                if (NetworkReceiver.isConnected()) {
                    btnDnD.setChecked(!btnDnD.isChecked());
                    if (btnDnD.isChecked()) {
                        btnPrivateChat.setChecked(false);
                        btnFollow.setChecked(false);
                    }
                    updateDND();
                }
                break;*/
            case R.id.btnFollow:
                if (NetworkReceiver.isConnected()) {
                    updateFollow();
                }
                break;
            /*case R.id.followLay:
                if (NetworkReceiver.isConnected()) {
                    btnFollow.setChecked(!btnFollow.isChecked());
                    updateFollow();
                }
                break;*/
            case R.id.adView:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == Constants.PRIME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setSettings();
            loadAd();
        }*/
    }

    private void updatePrivateChat() {
        btnPrivateChat.setEnabled(false);
        ProfileRequest request = new ProfileRequest();
        request.setChatNotification("" + btnPrivateChat.isChecked());
        updateSettings("chat", request);
    }

    private void updateDND() {
        /*ProfileRequest request = new ProfileRequest();
        request.setShowNotification("" + btnDnD.isChecked());
        if (btnDnD.isChecked()) {
            request.setChatNotification("" + btnPrivateChat.isChecked());
            request.setFollowNotification("" + btnFollow.isChecked());
        }
        updateSettings(request);*/
    }

    private void updateFollow() {
        btnFollow.setEnabled(false);
        ProfileRequest request = new ProfileRequest();
        request.setFollowNotification("" + btnFollow.isChecked());
        updateSettings("follow", request);
    }

}
