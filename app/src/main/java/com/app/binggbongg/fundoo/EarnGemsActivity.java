package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class EarnGemsActivity extends BaseFragmentActivity implements RewardedVideoAdListener {

    private static final String TAG = EarnGemsActivity.class.getSimpleName();
    ApiInterface apiInterface;

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.txtGemsDesc)
    TextView txtGemsDesc;
    @BindView(R.id.btnSettings)
    ImageView btnCopy;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnWatchVideo)
    Button btnWatchVideo;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.btnRefer)
    Button btnRefer;
    @BindView(R.id.txtVideoTime)
    TextView txtVideoTime;
    boolean watchVideoButton = false;
    private LinearLayout.LayoutParams linearParams;
    private RewardedVideoAd mRewardedVideoAd;
    private CountDownTimer timer;
    private AppUtils appUtils;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }
        setContentView(R.layout.activity_earn_gems);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        progressDialog = new ProgressDialog(this, R.style.customProgressDialog);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.loading));
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
        btnCopy.setVisibility(View.VISIBLE);
        btnCopy.setImageResource(R.drawable.copy);

        txtGemsDesc.setText(String.format(getString(R.string.free_gems_content), AdminData.inviteCredits.toString()));

        linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.height = (int) (getDisplayHeight() * 0.35);
        //lottieImage.setLayoutParams(linearParams);

        if (SystemClock.elapsedRealtime() > SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0)) {
            btnWatchVideo.setEnabled(true);
            btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            txtVideoTime.setText("");
            txtVideoTime.setVisibility(View.INVISIBLE);
        } else {
            txtVideoTime.setVisibility(View.VISIBLE);
            long endTime = SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0);
            timer = new CountDownTimer(endTime - SystemClock.elapsedRealtime(), 1000) {
                @Override
                public void onTick(long l) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long hours = (((endTime - SystemClock.elapsedRealtime()) / 1000) / 60) / 60;
                            long minutes = ((endTime - SystemClock.elapsedRealtime()) / 1000) / 60;
                            long seconds = ((endTime - SystemClock.elapsedRealtime()) / 1000) % 60;
                            String videoTime = getString(R.string.video_time) + " " + AppUtils.twoDigitString(hours) +
                                    getString(R.string.h).toLowerCase() + " " + AppUtils.twoDigitString(minutes) + getString(R.string.m).toLowerCase() +
                                    " " + AppUtils.twoDigitString(seconds) + getString(R.string.s).toLowerCase();
                            txtVideoTime.setText(videoTime);
                            btnWatchVideo.setEnabled(false);
                            btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
                        }
                    });
                }

                @Override
                public void onFinish() {
                    runOnUiThread(() -> {
                        timer.cancel();
                        txtVideoTime.setText("");
                        txtVideoTime.setVisibility(View.INVISIBLE);
                        btnWatchVideo.setEnabled(true);
                        btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                    });
                }
            };
            timer.start();
        }

        btnCopy.setImageDrawable(getDrawable(R.drawable.copy));
        txtTitle.setText(getString(R.string.invite_friends));
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);

        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        if (!AdminData.showVideoAd.equals("1")) {
            btnWatchVideo.setVisibility(View.INVISIBLE);
        }
        loadAd();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


    }

    private void loadAd() {
        Log.d(TAG, "loadAd: " + AdminData.isAdEnabled());
        if (AdminData.isAdEnabled()) {
            MobileAds.initialize(this,
                    AdminData.googleAdsId);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.d(TAG, "onAdLoaded: ");
                    dismissLoading();
                }


                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    Log.d(TAG, "onAdOpened: ");
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                    Log.d(TAG, "onAdClicked: ");
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    Log.d(TAG, "onAdLeftApplication: ");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                    Log.d(TAG, "onAdClosed: ");
                }
            });
        }
    }

    private void dismissLoading() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void showLoading() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @OnClick({R.id.btnBack, R.id.btnSettings, R.id.btnWatchVideo, R.id.btnRefer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnCopy);
                copyReferCode();
                break;
            case R.id.btnWatchVideo:
                App.preventMultipleClick(btnWatchVideo);
                btnWatchVideo.setEnabled(false);
                watchVideoButton = true;
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    openVideo();
                }
                break;
            case R.id.btnRefer:
                App.preventMultipleClick(btnRefer);
                openShare();
                break;
        }
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.video_ad_id), new AdRequest.Builder().build());
    }

    private void openShare() {
        Task<ShortDynamicLink> shortLinkTask = appUtils.getDynamicLink();

        shortLinkTask.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String msg = String.format(getString(R.string.invite_description), getString(R.string.app_name), shortLink);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
            } else {
                // Error
                // ...
                Log.e(TAG, "onComplete: ");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "onFailure: " + e.getMessage()));
    }

    private void copyReferCode() {

        Task<ShortDynamicLink> shortLinkTask = appUtils.getDynamicLink();

        shortLinkTask.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Short link created
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String msg = String.format(getString(R.string.invite_description), getString(R.string.app_name), shortLink);
                ClipData clip = ClipData.newPlainText("label", "" + msg);
                clipboard.setPrimaryClip(clip);
                Toasty.success(EarnGemsActivity.this, getString(R.string.referral_code_copied), Toasty.LENGTH_SHORT).show();
            } else {
                // Error
                // ...
                Log.e(TAG, "onComplete: ");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "onFailure: " + e.getMessage()));

    }

    private void openVideo() {
        loadRewardedVideoAd();
        showLoading();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        if (watchVideoButton && mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
        dismissLoading();
        Log.d(TAG, "onRewardedVideoAdLoaded: ");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(TAG, "onRewardedVideoAdOpened: ");
        dismissLoading();
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d(TAG, "onRewardedVideoStarted: ");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(TAG, "onRewardedVideoAdClosed: ");
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        watchVideoButton = false;
        Log.d(TAG, "onRewarded: ");
        updateRewards(rewardItem.getAmount());
        SharedPref.putLong(SharedPref.VIDEO_END_TIME, (SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(AdminData.videoAdsDuration)));
        Long endTime = SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0);
        btnWatchVideo.setEnabled(false);
        btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
        txtVideoTime.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(TimeUnit.MINUTES.toMillis(AdminData.videoAdsDuration), 1000) {
            @Override
            public void onTick(long l) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnWatchVideo.setEnabled(false);
                        long hours = (((endTime - SystemClock.elapsedRealtime()) / 1000) / 60) / 60;
                        long minutes = ((endTime - SystemClock.elapsedRealtime()) / 1000) / 60;
                        long seconds = ((endTime - SystemClock.elapsedRealtime()) / 1000) % 60;
                        String videoTime = getString(R.string.video_time) + " " + AppUtils.twoDigitString(hours) + " " +
                                getString(R.string.h).toLowerCase() + " " + AppUtils.twoDigitString(minutes) + getString(R.string.m).toLowerCase() +
                                " " + AppUtils.twoDigitString(seconds) + getString(R.string.s).toLowerCase();
                        txtVideoTime.setText(videoTime);
                    }
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(() -> {
                    timer.cancel();
                    txtVideoTime.setVisibility(View.INVISIBLE);
                    btnWatchVideo.setEnabled(true);
                    btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                });
            }
        };
        timer.start();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d(TAG, "onRewardedVideoAdLeftApplication: ");
    }


    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

        Log.d(TAG, "onRewardedVideoAdFailedToLoad: ");
        dismissLoading();
        App.makeToast("Failed to ads load");

    }

    @Override
    public void onRewardedVideoCompleted() {
        Log.d(TAG, "onRewardedVideoCompleted: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnWatchVideo.setEnabled(true);
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        mRewardedVideoAd.resume(this);
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        mRewardedVideoAd.destroy(this);
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    private void updateRewards(int amount) {
        Call<Map<String, String>> call = apiInterface.updateVideoGems(GetSet.getUserId());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> map = response.body();
                    if (map.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        GetSet.setGems(Float.valueOf((map.get(Constants.TAG_TOTAL_GEMS))));
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        App.makeToast(getString(R.string.votes_purchase_response));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                call.cancel();
            }
        });
    }
}
