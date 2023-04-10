package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.binggbongg.fundoo.signup.SignupActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.AddDeviceRequest;
import com.app.binggbongg.model.AdminMessageResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SignInRequest;
import com.app.binggbongg.model.SignInResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.DeviceTokenPref;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;

import org.json.JSONException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class LoginActivity extends BaseFragmentActivity /*implements OSSubscriptionObserver*/ {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    @BindView(R.id.btnMobile)
    MaterialButton btnMobile;
    @BindView(R.id.btnFacebook)
    MaterialButton btnFacebook;
    @BindView(R.id.txtTerms)
    TextView txtTerms;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.iconAppLogo)
    ImageView iconAppLogo;

    @BindView(R.id.mcheckbox)
    MaterialCheckBox mcheckbox;

    @BindView(R.id.lay_signup)
    RelativeLayout signup;

    private CallbackManager callbackManager;
    private ApiInterface apiInterface;
    String fbImage, fbName, fbEmail;
    private MediaPlayer mMediaPlayer;
    // MediaPlayer instance to control playback of video file.
    private DBHelper dbHelper;
    String oneSignalId = null, mobileNumber="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      /*  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
  /*      View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        int uiOptions = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);*/

        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        dbHelper = DBHelper.getInstance(this);

        //App intro screen background video



        /*parentLay.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            int bottomNavHeight = view.getPaddingBottom() + windowInsets.getSystemWindowInsetBottom() + AppUtils.dpToPx(LoginActivity.this, 10);
            txtTerms.setTranslationY(-bottomNavHeight);
            return windowInsets.consumeSystemWindowInsets();
        });*/

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        txtTerms.setText(Html.fromHtml(getString(R.string.terms_and_conditions_des)));
        //OneSignal.addSubscriptionObserver(this);

    }


    @OnClick({R.id.btnMobile, R.id.btnFacebook, R.id.txtTerms, R.id.lay_signup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnMobile:
                if (mcheckbox.isChecked()) {
                    App.preventMultipleClick(btnMobile);
                    verifyMobileNumber();
                } else alertToast();

                break;
            case R.id.btnFacebook:
                if (mcheckbox.isChecked()) {
                    App.preventMultipleClick(btnFacebook);
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
                    loginWithFacebook();
                } else alertToast();

                break;
            case R.id.txtTerms:
                App.preventMultipleClick(txtTerms);
                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                startActivity(intent);
                break;
            case R.id.lay_signup:
                App.preventMultipleClick(signup);
                Intent signupIntent = new Intent(LoginActivity.this, SignupActivity.class);
                signupIntent.putExtra("type_of_login","");
                signupIntent.putExtra("login_Id", "");
                startActivity(signupIntent);
                break;
        }
    }

    private void alertToast() {
        Toasty.info(LoginActivity.this, R.string.terms_of_use_warning, Toasty.LENGTH_SHORT).show();
    }

    private void verifyMobileNumber() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .setTheme(R.style.CustomFirebase)
                        .build(),
                RC_SIGN_IN);
    }


    public void printKeyHash(Context pContext) {

        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }


    private void loginWithFacebook() {

        printKeyHash(LoginActivity.this);

        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (profile, response) -> {
                    try {
                        fbImage = "https://graph.facebook.com/" + profile.getString("id") + "/picture?type=large";
                        fbName = profile.getString("name");
                        fbEmail = profile.getString("email");
                        signIn(profile.optString(Constants.TAG_ID), Constants.TAG_FACEBOOK, fbEmail, fbName, fbImage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link, email, first_name, last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Logging.i(TAG, "onCancel: ");
            }

            @Override
            public void onError(FacebookException error) {
                Logging.e(TAG, "onError: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getPhoneNumber() != null) {
                    mobileNumber = user.getPhoneNumber();
                    signIn(user.getPhoneNumber(), Constants.TAG_PHONENUMBER, null, null, null);
                }
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signIn(String loginId, String type, String email, String name, String image) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            SignInRequest request = new SignInRequest();
            request.setLoginId(loginId);
            request.setType(type);

            Timber.i("signin parmas=> %s", new Gson().toJson(request));

            Call<SignInResponse> call = apiInterface.callSignIn(request);
            call.enqueue(new Callback<SignInResponse>() {
                @Override
                public void onResponse(Call<SignInResponse> call, Response<SignInResponse> response) {
                    Timber.i("signin response=> %s", new Gson().toJson(response.body()));
                    if (response.isSuccessful()) {
                        Timber.i("signin response=> %s", new Gson().toJson(response.body()));
                        SignInResponse signin = response.body();
                        if (signin.getStatus().equals(Constants.TAG_TRUE)) {
                            GetSet.setLogged(true);
                            SharedPref.putBoolean(SharedPref.IS_LOGGED, true);
                            SharedPref.putString(SharedPref.LOGIN_ID, loginId);
                            SharedPref.putString(SharedPref.LOGIN_TYPE, type);
                            if (type.equalsIgnoreCase(Constants.TAG_FACEBOOK)) {
                                SharedPref.putString(SharedPref.FACEBOOK_NAME, name);
                                SharedPref.putString(SharedPref.FACEBOOK_EMAIL, email);
                                SharedPref.putString(SharedPref.FACEBOOK_IMAGE, image);
                            }
                            GetSet.setLoginId(loginId);
                            GetSet.setLoginType(type);
                            if (!signin.isAccountExists()) {
                                hideLoading();
                                //goToMain();
                                goToSignUP(loginId, type);
                            } else {

                                // App.makeToast(signin.getLocation());

                                GetSet.setUserId(signin.getUserId());
                                GetSet.setLoginId(signin.getLoginId());
                                GetSet.setName(signin.getName());
                                GetSet.setDob(signin.getDob());
                                GetSet.setAge(signin.getAge());
                                GetSet.setGender(signin.getGender());
                                /*GetSet.setLocation(signin.getLocation());*/
                                GetSet.setAuthToken(signin.getAuthToken());
                                GetSet.setUserImage(signin.getUserImage());
                                GetSet.setFollowersCount("0");
                                GetSet.setFollowingCount("0");
                                GetSet.setInterestsCount(0);
                                GetSet.setFriendsCount(0);
                                GetSet.setUnlocksLeft(0);
                                GetSet.setGems(0F);
                                GetSet.setGifts(0L);
                                GetSet.setPremiumMember(Constants.TAG_TRUE);
                                GetSet.setPremiumExpiry("");
                                GetSet.setPrivacyAge(signin.getPrivacyAge());
                                GetSet.setPrivacyContactMe(signin.getPrivacyContactMe());
                                GetSet.setFollowNotification(signin.getFollowNotification());
                                GetSet.setChatNotification(signin.getChatNotification());
                                GetSet.setShowNotification(signin.getShowNotification());
                                GetSet.setInterestNotification(signin.getInterestNotification());

                                GetSet.setPremiumMember(signin.getPremiumMember());


                                AppUtils.callerList.clear();
                                AppUtils.callerList.add(GetSet.getUserId());

                                SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                                SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                                SharedPref.putString(SharedPref.NAME, GetSet.getName());
                                SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                                SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                                SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                                /*SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());*/
                                SharedPref.putString(SharedPref.AUTH_TOKEN, GetSet.getAuthToken());
                                SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                                SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                                SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                                SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                                SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                                SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
                                SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                                SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                                SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                                SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                                SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                                SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                                SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());

                                addDeviceID();
                            }
                        } else if (signin.getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.contact_admin_desc));
                            hideLoading();
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                            hideLoading();
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                        hideLoading();
                    }

                }

                @Override
                public void onFailure(Call<SignInResponse> call, Throwable t) {
                    Timber.e("signin Error=> %s", t);
                    hideLoading();
                    call.cancel();
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void goToSignUP(String loginId, String typeOfLogin) {

        if (GetSet.getUserId() != null) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(this);
        }
        hideLoading();
        Intent i;
        i = new Intent(getApplicationContext(), SignupActivity.class);
        i.putExtra("type_of_login", typeOfLogin);
        i.putExtra("login_id", mobileNumber);
        startActivity(i);
        //finish();

    }

    private void goToMain() {

        if (GetSet.getUserId() != null) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(this);
        }
        hideLoading();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();

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

    private void addDeviceID() {
       /* OSDeviceState deviceState = OneSignal.getDeviceState();
        if (deviceState != null) {
            oneSignalId = deviceState.getUserId();
            Log.i(TAG, "addDeviceId: " + oneSignalId);
        }*/
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        //request.setDeviceToken(oneSignalId);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());
        //request.setOneSignalId(oneSignalId);

        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                String msgAt = "" + (System.currentTimeMillis() / 1000);
                String createdAt = AppUtils.getCurrentUTCTime(getApplicationContext());

                AdminMessageResponse.MessageData messageData = new AdminMessageResponse().new MessageData();
                messageData.setMsgFrom(Constants.TAG_ADMIN);
                messageData.setMsgId(GetSet.getUserId() + (System.currentTimeMillis() / 1000));
                messageData.setMsgType(Constants.TAG_TEXT);
                messageData.setMsgData("" + AdminData.welcomeMessage);
                messageData.setMsgAt(msgAt);
                messageData.setCreateaAt(createdAt);
                dbHelper.addAdminMessage(messageData);

                messageData.setMsgAt(createdAt);
                messageData.setCreateaAt(msgAt);
                int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
                unseenCount = unseenCount + 1;
                dbHelper.addAdminRecentMessage(messageData, unseenCount);
                goToMain();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    /*@Override
    public void onOSSubscriptionChanged(OSSubscriptionStateChanges osSubscriptionStateChanges) {
        if (!osSubscriptionStateChanges.getFrom().isSubscribed() &&
                osSubscriptionStateChanges.getTo().isSubscribed()) {
            if (osSubscriptionStateChanges.getTo().getUserId() != null) {
                oneSignalId = osSubscriptionStateChanges.getTo().getUserId();
                Log.i(TAG, "onOSSubscriptionChanged: " + oneSignalId);
            }
        }

    }*/
}
