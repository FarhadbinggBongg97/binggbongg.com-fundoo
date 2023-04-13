package com.app.binggbongg.fundoo;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.Deepar.DeeparActivity;
import com.app.binggbongg.fundoo.home.ForYouVideoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.fundoo.home.CustomViewPager;
import com.app.binggbongg.fundoo.home.HomeFragment;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.fundoo.profile.ProfileFragment;
import com.app.binggbongg.fundoo.search.SearchFragment;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.callback.CompositeOnNetworkChangedListener;
import com.app.binggbongg.helper.callback.FingerPrintCallBack;
import com.app.binggbongg.model.AddDeviceRequest;
import com.app.binggbongg.model.AdminMessageResponse;
import com.app.binggbongg.model.AppDefaultResponse;
import com.app.binggbongg.model.ChatResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.MembershipPackages;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.model.ReferFriendResponse;
import com.app.binggbongg.model.RenewalRequest;
import com.app.binggbongg.model.SignInRequest;
import com.app.binggbongg.model.SignInResponse;
import com.app.binggbongg.model.event.GetProfileEvent;
import com.app.binggbongg.model.event.GetSearchEvent;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.DeviceTokenPref;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import butterknife.BindView;
import butterknife.ButterKnife;
import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.PlayerActivity;
import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.SubscribeActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseFragmentActivity implements PurchasesUpdatedListener,
      /*  OSSubscriptionObserver,*/ ForYouVideoFragment.onHideBottomBarEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.parentLay)
    CoordinatorLayout parentLay;

    @BindView(R.id.fragment_home_viewpager)
    CustomViewPager viewPager;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;

    @BindView(R.id.splashLay)
    RelativeLayout splashLay;

    @BindView(R.id.menuAddStory)
    ImageView menuAddStory;

    ProfileFragment profileFragment;
    HomeFragment homeFragment;
    SearchFragment searchFragment;
    ChatFragment chatFragment;

    private MenuItem prevMenuItem;
    private ApiInterface apiInterface;
    DialogProfile profileDialog;
    DialogProfileImage imageDialog;
    DialogFreeGems gemsDialog;
    DialogInterest interestDialog;
    DialogOverLayPermission dialogOverLayPermission;
    boolean doubleBackToExit = false;
    private BillingClient billingClient;
    Handler onlineHandler = new Handler();
    int delay = 5000; //milliseconds
    private int previousPosition;
    DBHelper dbHelper;
    List<String> skuList = new ArrayList<>();
    private String from = null;
    private BiometricPrompt.PromptInfo promptInfo;
    private ExecutorService executor;
    private BiometricPrompt biometricPrompt;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    Cipher defaultCipher;
    Cipher cipherNotInvalidated;
    private AppUtils appUtils;

    SignInRequest signUpRequest;

    Boolean isSettinsIn = false;
    Boolean isAutoStartIn = false;
    String oneSignalId = null;

    public  SharedPreferences pref;
    SharedPreferences.Editor editor;
    HashMap<String, String> mParams = new HashMap<>();

    private boolean isShow=false;

    boolean isFirstTime=true;
    String[] recordPermissions = new String[]{
            CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};
    String[] recordPermissions12 = new String[]{
            CAMERA, RECORD_AUDIO};
    public CompositeOnNetworkChangedListener compositeOnNetworkChangedListener = new CompositeOnNetworkChangedListener();

    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkReceiver.isConnected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_UPDATE_LIVE);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_TIMESTAMP, AppUtils.getCurrentUTCTime(getApplicationContext()));
                    if(GetSet.getUserId()!=null){
                        AppWebSocket.getInstance(MainActivity.this).send(jsonObject.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            onlineHandler.postDelayed(onlineRunnable, delay);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      /*  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        getGooglePlayCurrency();

        mParams = (HashMap<String, String>) getIntent().getSerializableExtra("data");
        SharedPref.putBoolean(SharedPref.HIDE_ICONS, true);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        isFirstTime=false;


        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancelAll();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dbHelper = DBHelper.getInstance(this);
        appUtils = new AppUtils(this);
        executor = Executors.newSingleThreadExecutor();

        checkUserIsActive();

        setupCustomViewPager(viewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recordPermissions = recordPermissions12;
        }

        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        @SuppressLint("HardwareIds") final String deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        /*Logging.i(TAG, "onCreate: " + token);
        Logging.i(TAG, "onCreate: " + deviceId);*/
        registerNetworkReceiver();

        if (getIntent().hasExtra(Constants.NOTIFICATION)) {
            from = getIntent().getStringExtra(Constants.NOTIFICATION);
            if (getIntent().getBooleanExtra(Constants.TAG_FROM_FOREGROUND, false)) {
                checkFromNotification();
            } else {
                if (App.isAppOpened) {
                    checkFromNotification();
                } else {
                    if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
                        splashLay.setVisibility(View.VISIBLE);
                        bottomNavigation.setVisibility(View.INVISIBLE);
                        checkFingerPrintEnabled();
                    } else {
                        SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
                        checkFromNotification();
                    }
                }
            }
        } else if (getIntent().hasExtra(Constants.TAG_FROM)) {
            /*if (getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_LIVE)) {
                getStreamDetails(getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME));
            }*/
        }

        sendPing();
        //OneSignal.addSubscriptionObserver(this);
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }//        showProfileDialog();
        }
*/
//        showGemsDialog();
//        showProfileImageDialog();

        if (GetSet.getName() == null) {
            showProfileImageDialog();
         //   showProfileDialog();
        } else {
           // showProfileDialog();
            getUserProfile(GetSet.getUserId());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                initPermissionDialog();
            }
        }



/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS,READ_PHONE_STATE,BLUETOOTH_CONNECT}, 100);
            } else {
                Log.e(TAG, "onCreate: ::::::else1" );
//                new SplashActivity.GetAppDefaultTask().execute();
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onCreate: ::::::::::::::::::::::if1" );
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_PHONE_STATE,BLUETOOTH_CONNECT}, 100);
            } else {
                Log.e(TAG, "onCreate: ::::::else2" );
//                new SplashActivity.GetAppDefaultTask().execute();
            }
        }*//*else {
            Log.e(TAG, "onCreate: ::::::else2" );
            new SplashActivity.GetAppDefaultTask().execute();
        }*/

    }

/*    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
    }*/

    private void checkFromNotification() {

        Log.e(TAG, "checkFromNotification: :::::::::::" );
        App.isAppOpened = true;
        splashLay.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.VISIBLE);

        switch (from) {
            case Constants.TAG_FOLLOW: {
                Intent profileIntent = new Intent(getApplicationContext(), OthersProfileActivity.class);
                profileIntent.putExtra(Constants.TAG_USER_ID, getIntent().getStringExtra(Constants.TAG_PARTNER_ID));
                profileIntent.putExtra(Constants.TAG_USER_IMAGE, getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE));
                profileIntent.putExtra(Constants.TAG_FROM, Constants.NOTIFICATION);
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_PROFILE: {
                Intent profileIntent = new Intent(getApplicationContext(), ChatActivity.class);
                profileIntent.putExtra(Constants.TAG_PARTNER_ID, getIntent().getStringExtra(Constants.TAG_PARTNER_ID));
                profileIntent.putExtra(Constants.TAG_PARTNER_NAME, getIntent().getStringExtra(Constants.TAG_PARTNER_NAME));
                profileIntent.putExtra(Constants.TAG_PARTNER_IMAGE, getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE));
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_ADMIN: {
                Intent profileIntent = new Intent(getApplicationContext(), ChatActivity.class);
                ChatResponse adminChat = dbHelper.getAdminChat(GetSet.getUserId());
                profileIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
                profileIntent.putExtra(Constants.TAG_PARTNER_NAME, adminChat.getUserName());
                startActivity(profileIntent);
                break;
            }
            case Constants.TAG_NOTIFICATION_PAGE:

                startActivity(new Intent(MainActivity.this, NotificationActivity.class));

                break;
            case Constants.TAG_LIVE: {
                getStreamDetails(getIntent().getStringExtra(StreamConstants.TAG_STREAM_NAME));
                break;
            }
            case Constants.TAG_GIFT:
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.TAG_TYPE, "receivedvotes");
                intent.putExtra(Constants.TAG_TITLE,"Votes History");
                startActivity(intent);
                break;
        }
        finish();
    }

    private void checkUserIsActive() {
        Call<HashMap<String, String>> call = apiInterface.isActive(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<HashMap<String, String>> call, @NotNull Response<HashMap<String, String>> response) {
                HashMap<String, String> Response = response.body();

                Timber.i("checkUserIsActive=> %s", new Gson().toJson(response.body()));

                if (response.isSuccessful()) {
                    if (Objects.requireNonNull(Response.get(Constants.TAG_STATUS)).equals(Constants.TAG_FALSE)) {

                        App.makeToast(getString(R.string.account_deactivated_by_admin));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(MainActivity.this).clearDB();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setupCustomViewPager(CustomViewPager viewPager) {
        bottomNavigation.setItemIconTintList(null);
        bottomNavigation.getMenu().clear();
        bottomNavigation.inflateMenu(R.menu.fundoo_home_menu);
        viewPager.setOffscreenPageLimit(4);

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(adapter);

        if (isShow){
            bottomNavigation.getMenu().removeItem(R.id.menuLive);
            bottomNavigation.getMenu().removeItem(R.id.menuSearch);
            bottomNavigation.getMenu().removeItem(R.id.menuChat);
            bottomNavigation.getMenu().removeItem(R.id.menuProfile);
        }else{
            bottomNavigation.getMenu().clear();
            bottomNavigation.inflateMenu(R.menu.fundoo_home_menu);
            setLivzaNavigation(0);
        }

//        setLivzaNavigation(0);
        viewPager.setCurrentItem(0, false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        // Resume video play screen
                        if (homeFragment != null) {
                            if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                                if (homeFragment.homeForYou != null)
                                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
                            } else if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                                if (homeFragment.homeFollowing != null)
                                    homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
                            }
                        }
                        break;
                    case 3:
                    case 4:
                        //resetTyping();

                        break;
                    case 1:
                        //resetTyping();
                        break;
                    case 2:
                        //resetTyping();
                        break;
                    default:
                        break;
                }

                /*if (prevMenuItem != null)
                    prevMenuItem.setChecked(false);*/

//                if (bottomNavigation.getMenu())
             /*   bottomNavigation.getMenu().getItem(0).setChecked(false);
                bottomNavigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigation.getMenu().getItem(position);*/
                setLivzaNavigation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        bottomNavigation.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.menuLive:
                    viewPager.setCurrentItem(0, false);
                    break;
                case R.id.menuSearch:
                    viewPager.setCurrentItem(1, false);
                    break;
                case R.id.menuVideo:
                /*Intent intent = new Intent(context, HashTagActivity.class);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_PUBLISH);
                intent.putExtra(Constants.TAG_INTENT_DATA, getHashTagName);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent, Constants.HASHTAG_REQUEST_CODE);
                viewPager.setCurrentItem(2);*/
                    break;
                case R.id.menuChat:
                    viewPager.setCurrentItem(3, false);
                    break;
                case R.id.menuProfile:
                    viewPager.setCurrentItem(4, false);
                    break;
            }
            return false;
        });


    }


    /*private void resetTyping() {
        if (chatFragment != null) {
            dbHelper.updateChatsTyping();
            dbHelper.updateChatsOnline();
        }
    }*/

    @Override
    public void onNetworkChanged(boolean isConnected) {
        Timber.i("onNetworkChanged isConnected %s", isConnected);

        compositeOnNetworkChangedListener.onNetworkChanged(isConnected);

        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
        getAppDefaultData();
        if (isConnected && GetSet.isIsLogged()) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(MainActivity.this);
            onlineHandler.post(onlineRunnable);

        } else {
            onlineHandler.removeCallbacks(onlineRunnable);
            AppWebSocket.getInstance(this).disconnect();
        }

    }

    private void sendPing() {
        onlineHandler.post(onlineRunnable);
    }

    private void removePing() {
        onlineHandler.removeCallbacks(onlineRunnable);
    }

    public void onClickAddStory(View view) {
        Log.e(TAG, "onClickAddStory: ::::::::::::::" );
        try {
            if (homeFragment != null && homeFragment.homeForYou != null)
                homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);

            checkRecordPermissions();

        } catch (Exception e) {
            Timber.i("Error onClickAddStory %s", e.getMessage());
        }

    }

    public static void animateZoom(Context context) {
        ((Activity) context).overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle extras = intent.getExtras();
        viewPager.setCurrentItem(2, false);

    }

 /*   @Override
    public void onOSSubscriptionChanged(OSSubscriptionStateChanges osSubscriptionStateChanges) {
        if (!osSubscriptionStateChanges.getFrom().isSubscribed() &&
                osSubscriptionStateChanges.getTo().isSubscribed()) {
            if (osSubscriptionStateChanges.getTo().getUserId() != null) {
                oneSignalId = osSubscriptionStateChanges.getTo().getUserId();
                Log.i(TAG, "onOSSubscriptionChanged: " + oneSignalId);
            }
        }

    }*/
 public void checkRecordPermissions() {

     if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
         if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                 ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(MainActivity.this, recordPermissions,
                     Constants.CAMERA_REQUEST_CODE);
         } else {
             Intent newIntent = new Intent(MainActivity.this, DeeparActivity.class);
             startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
             animateZoom(MainActivity.this);
         }
     }else{
         if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                 ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                 ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

             ActivityCompat.requestPermissions(MainActivity.this, recordPermissions,
                     Constants.CAMERA_REQUEST_CODE);
         } else{
             Intent newIntent = new Intent(MainActivity.this, DeeparActivity.class);
             startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
             animateZoom(MainActivity.this);
         }
     }


 }
    @Override
    public void onHideEvent(boolean s) {
        Log.e(TAG, "onHideEvent: :::::::::::::"+s );

        if (s){
            bottomNavigation.getMenu().removeItem(R.id.menuLive);
            bottomNavigation.getMenu().removeItem(R.id.menuSearch);
            bottomNavigation.getMenu().removeItem(R.id.menuChat);
            bottomNavigation.getMenu().removeItem(R.id.menuProfile);
        }else{
            bottomNavigation.getMenu().clear();
            bottomNavigation.inflateMenu(R.menu.fundoo_home_menu);
            setLivzaNavigation(0);
        }

        isShow=s;

    }


    public class MainPagerAdapter extends FragmentPagerAdapter {

        Activity activity;

        public MainPagerAdapter(FragmentManager fm, MainActivity mainActivity) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            activity = mainActivity;

        }

        @Override
        public @NotNull Fragment getItem(int position) {
            switch (position) {

                case 0:
                default:
                    homeFragment = new HomeFragment();
                    return homeFragment;

                case 1:

                    searchFragment = new SearchFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_FROM, Constants.TAG_BROADCAST);
                    searchFragment.setArguments(bundle);
                    return searchFragment;
                case 2:

                /*    cameraFragment = new CameraFragment();
                    return cameraFragment;*/

                  /*  cameraFragment = new OpenCameraFragment();
                    return cameraFragment;*/

                case 3:

                    chatFragment = new ChatFragment();
                    Bundle chat = new Bundle();
                    chatFragment.setArguments(chat);
                    return chatFragment;
                case 4:


                    profileFragment = new ProfileFragment();
                    Bundle profile = new Bundle();
                    profile.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                    profileFragment.setArguments(profile);
                    return profileFragment;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setLivzaNavigation(int position) {
        switch (position) {
            case 0:
                bottomNavigation.setVisibility(View.VISIBLE);
                if(bottomNavigation!=null && bottomNavigation.getMenu().findItem(R.id.menuLive)!=null){
                    bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_primary));
                    bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.search_white));
                    bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.message_white));
                    bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.profile_white));
                }
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBottomNavigation));
                break;
            case 1:

                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);

                if (homeFragment != null && homeFragment.homeFollowing != null)
                    homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);

                bottomNavigation.setVisibility(View.VISIBLE);
                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_gery));
                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.search_primary));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.message_grey));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.profile_grey));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                break;
            case 2:
                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);

                if (homeFragment != null && homeFragment.homeFollowing != null)
                    homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);

                bottomNavigation.setVisibility(View.GONE);
                break;
            case 3:


                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);

                if (homeFragment != null && homeFragment.homeFollowing != null)
                    homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);

                bottomNavigation.setVisibility(View.VISIBLE);
                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_gery));
                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.search_grey));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.message_primary));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.profile_grey));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorWhite));


                break;
            case 4:


                if (homeFragment != null && homeFragment.homeForYou != null)
                    homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                if (homeFragment != null && homeFragment.homeFollowing != null)
                    homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);

                bottomNavigation.setVisibility(View.VISIBLE);
                bottomNavigation.getMenu().findItem(R.id.menuLive).setIcon(getDrawable(R.drawable.home_white));
                bottomNavigation.getMenu().findItem(R.id.menuSearch).setIcon(getDrawable(R.drawable.search_white));
                bottomNavigation.getMenu().findItem(R.id.menuChat).setIcon(getDrawable(R.drawable.message_white));
                bottomNavigation.getMenu().findItem(R.id.menuProfile).setIcon(getDrawable(R.drawable.profile_primary));
                bottomNavigation.setBackgroundColor(getResources().getColor(R.color.colorBlack));
                break;
        }
    }


    public void checkFingerPrintEnabled() {
        if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to get an instance of KeyStore", e);
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mKeyGenerator = KeyGenerator
                            .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                }
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                    cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
            }

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    openBioMetricDialog();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    openFingerPrintDialog();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

                if (fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints()) {
                    createKey(Constants.DEFAULT_KEY_NAME, true);
                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
                    openFingerPrintDialog();
                } else if (km != null && km.isKeyguardSecure()) {
                    Intent i = km.createConfirmDeviceCredentialIntent(getString(R.string.authentication_required), getString(R.string.password));
                    startActivityForResult(i, Constants.DEVICE_LOCK_REQUEST_CODE);
                } else {
                    checkFromNotification();
                }
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
            checkFromNotification();
        }
    }

    private void openBioMetricDialog() {
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.i(TAG, "onAuthenticationError: " + errorCode);
                if (errorCode == 7) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            App.makeToast(getString(R.string.too_many_attempts));
                            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                            if (km != null && km.isKeyguardSecure()) {
                                Intent i = km.createConfirmDeviceCredentialIntent(getString(R.string.authentication_required), getString(R.string.password));
                                startActivityForResult(i, Constants.DEVICE_LOCK_REQUEST_CODE);
                            }
                        }
                    });
                } else if (errorCode == 13) {
                    Logging.i(TAG, "onAuthenticationError: " + "Cancel");
                }
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkFromNotification();
                    }
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.security))
                .setSubtitle("")
                .setDescription(getString(R.string.touch_fingerprint_description))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        biometricPrompt.authenticate(promptInfo);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openFingerPrintDialog() {
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
        if (fingerprintManager != null && !fingerprintManager.hasEnrolledFingerprints()) {
            checkFromNotification();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initCipher(defaultCipher, Constants.DEFAULT_KEY_NAME);
            }
            DialogFingerPrint dialogFingerPrint = new DialogFingerPrint();
            dialogFingerPrint.setContext(MainActivity.this);
            dialogFingerPrint.setCallBack(new FingerPrintCallBack() {
                @Override
                public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {
                    if (withFingerprint) {
                        // If the user has authenticated with fingerprint, verify that using cryptography and
                        // then show the confirmation message.
                        assert cryptoObject != null;
                        tryEncrypt(cryptoObject.getCipher());
                    } else {
                        // Authentication happened with backup password. Just show the confirmation message.

                    }
                }

                @Override
                public void onError(String errorMsg) {
                    App.makeToast(errorMsg);
                }
            });
            dialogFingerPrint.setCryptoObject(new FingerprintManager.CryptoObject(defaultCipher));
            dialogFingerPrint.show(getSupportFragmentManager(), TAG);
        }
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName                          the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(Constants.SECRET_MESSAGE.getBytes());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkFromNotification();
                }
            });
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            App.makeToast("Failed to encrypt the data with the generated key. " + "Retry the purchase");
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEVICE_LOCK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkFromNotification();
            } else {
                App.makeToast(getString(R.string.unable_to_verify));
                finish();
            }
        } else if (requestCode == Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE) {

            //Timber.d("onActivityResult: %s" , HomeForYou home = new HomeForYou().viewPager.getCurrentItem());


        } else {
            if (requestCode == Constants.OVERLAY_REQUEST_CODE) {
                if (!checkOverLayPermission()) {
                    openPermissionDialog();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivityonResume: ");
        Constants.isInVideoCall = false;
//        RandouCallActivity.isInCall = false;
        VideoCallActivity.isInCall = false;

        if (checkOverLayPermission()) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableProtectesApps();
            }
        }

        if (isSettinsIn) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableAutoStart();
            }
        }

        if (!checkOverLayPermission()) {
            openPermissionDialog();
        }


        if (!NetworkReceiver.isConnected()) {
            Timber.i("onNetworkChanged onResume %s", NetworkReceiver.isConnected());
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(MainActivity.this, READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(MainActivity.this, BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onResume: ::::::if");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS, READ_PHONE_STATE, BLUETOOTH_CONNECT}, 100);
                } else {
                    Log.e(TAG, "onResume: ::::::else1");
//                new SplashActivity.GetAppDefaultTask().execute();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(MainActivity.this, BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onResume: ::::::if1");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_PHONE_STATE, BLUETOOTH_CONNECT}, 100);
                } else {

                    Log.e(TAG, "onResume: ::::::else2");
//                new SplashActivity.GetAppDefaultTask().execute();
                }


        }

    }
    private void pauseVideoPlayed() {
        if (Constants.overLaysDialogShown) {
            if (homeFragment != null) {
                if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                    if (homeFragment.homeForYou != null)
                        homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                } else if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                    if (homeFragment.homeFollowing != null)
                        homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
                }
            }
        }
    }

    private void resumeVideoPlayed() {
        if (!Constants.overLaysDialogShown) {
            if (homeFragment != null) {
                if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
                    if (homeFragment.homeForYou != null)
                        homeFragment.homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
                } else if (homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
                    if (homeFragment.homeFollowing != null)
                        homeFragment.homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }

    private void initPermissionDialog() {
        dialogOverLayPermission = new DialogOverLayPermission();
        dialogOverLayPermission.setContext(this);
        dialogOverLayPermission.setCallBack((o, trim) -> {
            boolean isAllow = (boolean) o;
            if (isAllow) {
                requestOverLayPermission();
            }
            dialogOverLayPermission.dismissAllowingStateLoss();
           // resumeVideoPlayed();
        });
    }



    private void openPermissionDialog() {
        if (SharedPref.getBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, true)) {
            if (dialogOverLayPermission != null && !dialogOverLayPermission.isAdded()) {
                dialogOverLayPermission.show(getSupportFragmentManager(), "PermissionDialog");
               // pauseVideoPlayed();
            }
        }
    }

    private boolean checkOverLayPermission() {
        //Android M Or Over
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isOverLayEnabled = Settings.canDrawOverlays(this);
            Log.d(TAG, "checkOverLayPermission: " + isOverLayEnabled);
            SharedPref.putBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, !isOverLayEnabled);
            return isOverLayEnabled;
        }
        return true;
    }

    private void requestOverLayPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE);
        }
    }

    private void enableAutoStart() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        pref = getSharedPreferences("MyPref", 0);
        editor = pref.edit();
        isAutoStartIn = pref.getBoolean("isAutoStartIn", false);
        if (!isAutoStartIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_overlay_permission_autostart, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(v -> {
                editor.putBoolean("isAutoStartIn", true);
                editor.apply();
                Intent intent = new Intent();
                intent.setClassName("com.transsion.phonemaster",
                        "com.cyin.himgr.widget.activity.MainActivity");
                startActivity(intent);
                alertDialog.dismiss();
            });
            denyButton.setOnClickListener(v -> {
                alertDialog.dismiss();
                if (!checkOverLayPermission()) {
                    openPermissionDialog();
                }
            });
            alertDialog.show();
        }
    }


    private void enableProtectesApps() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        SharedPreferences pref = getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        isSettinsIn = pref.getBoolean("isSettingsIn", false);
        if (!isSettinsIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_overlay_permission_call, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(v -> {
                editor.putBoolean("isSettingsIn", true);
                editor.apply();
                Intent intent = new Intent();
                intent.setClassName("com.transsion.phonemaster",
                        "com.cyin.himgr.widget.activity.MainSettingGpActivity");
                startActivity(intent);
                alertDialog.dismiss();
            });
            denyButton.setOnClickListener(v -> {
                alertDialog.dismiss();
                if (!checkOverLayPermission()) {
                    openPermissionDialog();
                }

            });
            alertDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        AppUtils.resetFilter();
        removePing();
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showProfileDialog() {

        profileDialog = new DialogProfile();
        profileDialog.setContext(MainActivity.this);

        profileDialog.setCallBack((object, bio) -> {
            if (NetworkReceiver.isConnected()) {
                signUpRequest = (SignInRequest) object;
                //showProfileImageDialog(signUpRequest);

            } else {
                AppUtils.showSnack(MainActivity.this, findViewById(R.id.parentLay), false);
            }
        });
        profileDialog.setCancelable(false);
        profileDialog.show(getSupportFragmentManager(), TAG);
    }

    private void signUp(SignInRequest request, InputStream imageStream) {

        Timber.d("onResponse: signup %s", App.getGsonPrettyInstance().toJson(request));

      //  Call<SignInResponse> call = apiInterface.callSignUp(request);
        Call<SignInResponse> call = apiInterface.signup(mParams);
        call.enqueue(new Callback<SignInResponse>() {
            @Override
            public void onResponse(@NonNull Call<SignInResponse> call, @NonNull Response<SignInResponse> response) {
                SignInResponse signin = response.body();
                Timber.d("onResponse: signup %s", App.getGsonPrettyInstance().toJson(response.body()));
                if (signin.getStatus() != null && signin.getStatus().equals(Constants.TAG_TRUE)) {

                    GetSet.setUserId(signin.getUserId());
                    GetSet.setLoginId(signin.getLoginId());
                    GetSet.setName(signin.getName());
                    GetSet.setUserName(signin.getUserName());
                    GetSet.setDob(signin.getDob());
                    GetSet.setAge(signin.getAge());
                    GetSet.setGender(signin.getGender());
                    GetSet.setAuthToken(signin.getAuthToken());
                    GetSet.setFollowersCount("0");
                    GetSet.setFollowingCount("0");
                    GetSet.setGems(0F);
                    GetSet.setGifts(0L);
                    GetSet.setPremiumMember(Constants.TAG_FALSE);
                    GetSet.setPremiumExpiry("");
                    GetSet.setPrivacyAge(signin.getPrivacyAge());
                    GetSet.setPrivacyContactMe(signin.getPrivacyContactMe());
                    GetSet.setFollowNotification(signin.getFollowNotification());
                    GetSet.setChatNotification(signin.getChatNotification());
                    GetSet.setShowNotification(signin.getShowNotification());

                    /*GetSet.setPostCommand(signin.getCommentPrivacy());
                    GetSet.setSendMessage(profile.getMessagePrivacy());*/

                    SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                    SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                    SharedPref.putString(SharedPref.NAME, GetSet.getName());
                    SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                    SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                    SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                    SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                    SharedPref.putString(SharedPref.AUTH_TOKEN, GetSet.getAuthToken());
                    SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                    SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                    SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                    SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                    SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                    SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                    SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                    SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                    SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                    SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                    SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                    SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                    apiInterface = ApiClient.getClient().create(ApiInterface.class);

                    if (!TextUtils.isEmpty(SharedPref.getString(SharedPref.REFERAL_CODE, ""))) {
                        //updateGems(SharedPref.getString(SharedPref.REFERAL_CODE, ""));
                        updateReferral(SharedPref.getString(SharedPref.REFERAL_CODE, ""));
                    }

                    AppUtils.callerList.clear();
                    AppUtils.callerList.add(GetSet.getUserId());

                    if (imageStream == null) {
                        addDeviceID();
                    } else {
                        try {
                            uploadImage(AppUtils.getBytes(imageStream));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    profileDialog.hideLoading();
                    App.makeToast(signin.getMessage());
                }
            }

            @Override
            public void onFailure(Call<SignInResponse> call, Throwable t) {
                Timber.d("onError: signup %s", t);
                call.cancel();
                profileDialog.hideLoading();
            }
        });
    }

    public void getStreamDetails(String streamName) {
        if (NetworkReceiver.isConnected()) {
            Call<StreamDetails> call3 = apiInterface.getStreamDetails(GetSet.getUserId(), streamName);
            call3.enqueue(new Callback<StreamDetails>() {
                @Override
                public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            StreamDetails streamDetails = response.body();
                            if (streamDetails.getStreamBlocked() == 1) {
                                App.makeToast(getString(R.string.broadcast_deactivated_by_admin));
                            } else {
                                if (streamDetails.getType().equals(Constants.TAG_LIVE)) {
                                    appUtils.pauseExternalAudio();
                                   /* Intent i = new Intent(getApplicationContext(), SubscribeActivity.class);
                                    Bundle arguments = new Bundle();
                                    arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, streamDetails);
                                    i.putExtra(Constants.TAG_FROM, StreamConstants.TAG_SUBSCRIBE);
                                    i.putExtra("parent","");
                                    i.putExtras(arguments);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(i);*/
                                    Intent intent = new Intent(getApplicationContext(), LiveStreamActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                                    intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                                    intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                                    intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                                    intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                                    startActivity(intent);
                                } else {
                                    appUtils.pauseExternalAudio();
                                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                                    intent.putExtra(StreamConstants.TAG_STREAM_DATA, streamDetails);
                                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                                    intent.putExtra("parent","");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(intent);
                                }
                            }
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                        }
                    } else {
                        finish();
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<StreamDetails> call, Throwable t) {
                    call.cancel();
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
            finish();
        }
    }

    private void addDeviceID() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        @SuppressLint("HardwareIds") final String deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());
        /*request.setOneSignalId(oneSignalId);*/
        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

                if (GetSet.getUserId() != null) {
                    AppWebSocket.mInstance = null;
                    AppWebSocket.getInstance(MainActivity.this);
                }
                AdminMessageResponse.MessageData messageData = new AdminMessageResponse().new MessageData();
                messageData.setMsgFrom(Constants.TAG_ADMIN);
                messageData.setMsgId("0");
                messageData.setMsgType(Constants.TAG_TEXT);
                messageData.setMsgData(AdminData.welcomeMessage);
                String msgAt = "" + (System.currentTimeMillis() / 1000);
                String createdAt = AppUtils.getCurrentUTCTime(getApplicationContext());
                messageData.setMsgAt(msgAt);
                messageData.setCreateaAt(createdAt);
                dbHelper.addAdminMessage(messageData);
                messageData.setMsgAt(createdAt);
                messageData.setCreateaAt(msgAt);
                int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
                dbHelper.addAdminRecentMessage(messageData, unseenCount);

                if (homeFragment != null) {
                    /*RecentStreamEvent streamEvent = new RecentStreamEvent();
                    streamEvent.setUserId(GetSet.getUserId());
                    EventBus.getDefault().post(streamEvent);*/
                }
                if (searchFragment != null) {
                    GetSearchEvent searchEvent = new GetSearchEvent();
                    searchEvent.setUserId(GetSet.getUserId());
                    EventBus.getDefault().post(searchEvent);
                }

                if (SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null) != null) {
                    try {
                        Logging.e(TAG, "onResponse:fbImage " + SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null));
                        InputStream imageStream = new URL(SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null)).openStream();
                        uploadImage(AppUtils.getBytes(imageStream));
                    } catch (Exception e) {
                        Logging.e(TAG, "onResponse: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {

                    //profileDialog.hideLoading();
                    imageDialog.hideLoading();
                    getUserProfile(GetSet.getUserId());
                    showInterest();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void updateGems(String referalCode) {
        Log.e(TAG, "updateGems: " + referalCode);
        Call<ReferFriendResponse> call = apiInterface.updateReferal(referalCode);
        call.enqueue(new Callback<ReferFriendResponse>() {
            @Override
            public void onResponse(Call<ReferFriendResponse> call, Response<ReferFriendResponse> response) {
                if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    SharedPref.putString(SharedPref.REFERAL_CODE, "");
                }
            }

            @Override
            public void onFailure(Call<ReferFriendResponse> call, Throwable t) {

            }
        });
    }

    private void updateReferral(String referalCode) {
        Log.e(TAG, "updateGems: " + referalCode);
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", GetSet.getUserId());
        params.put("referral_code", referalCode);

        Call<HashMap<String, String>> call = apiInterface.updateReferral(params);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, String>> call,
                                   @NonNull Response<HashMap<String, String>> response) {
                Log.d(TAG, "Referral: " + new Gson().toJson(response.body()));
               /* if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                    SharedPref.putString(SharedPref.REFERAL_CODE, "");
                }*/
            }

            @Override
            public void onFailure(@NonNull Call<HashMap<String, String>> call, @NonNull Throwable t) {
                Log.d(TAG, "Referral: " + t);
                call.cancel();
            }
        });
    }

    private void showProfileImageDialog(/*SignInRequest signUpRequest*/) {
        imageDialog = new DialogProfileImage();
        imageDialog.setContext(MainActivity.this);

        imageDialog.setCallBack((object, bio) -> {
            InputStream imageStream = (InputStream) object;
            imageDialog.showLoading();
           // signUpRequest.setBio(bio); // Added bio here
            //signUp(signUpRequest, imageStream);
            mParams.put("bio", bio);
            signUp(signUpRequest, imageStream);

        });

        imageDialog.setCancelable(false);
        imageDialog.show(getSupportFragmentManager(), TAG);
        /*profileDialog.dismiss();*/
    }

    private void uploadImage(byte[] imageBytes) {

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    Timber.d("onResponse: %s", "image uploaded");
                    Timber.d("data username onResponse: %s", data.get(Constants.TAG_USER_IMAGE));
                    GetSet.setUserImage(data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, data.get(Constants.TAG_USER_IMAGE));

                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }

                    if (profileDialog != null) {
                        profileDialog.hideLoading();
                        //  profileDialog.dismiss();
                    }

                    getUserProfile(GetSet.getUserId());
                    showInterest();

                } else if (data.get(Constants.TAG_STATUS) != null && Objects.equals(data.get(Constants.TAG_STATUS), Constants.TAG_REJECTED)) {
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }
                    if (profileDialog != null) {
                        profileDialog.dismiss();
                    }
                    App.makeToast(data.get(Constants.TAG_MESSAGE));
                } else {
                    App.makeToast(data.get(Constants.TAG_MESSAGE));
                    if (imageDialog != null) {
                        imageDialog.hideLoading();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, Throwable t) {
                if (imageDialog != null) {
                    imageDialog.hideLoading();
                }
                call.cancel();
            }
        });
    }

    private void showGemsDialog() {
        gemsDialog = new DialogFreeGems();
        gemsDialog.setContext(MainActivity.this);
        gemsDialog.setCallBack((o, trim) -> gemsDialog.dismiss());
        gemsDialog.setCancelable(false);
        gemsDialog.show(getSupportFragmentManager(), TAG);
    }

    private void showInterest() {

        interestDialog = new DialogInterest();
        interestDialog.setContext(MainActivity.this);

        interestDialog.setCallBack((o, trim) -> {


            Intent intent = getIntent();
            finish();
            startActivity(intent);

            //profileDialog.dismiss();
            imageDialog.dismiss();
            interestDialog.dismiss();

/*            if (trim.equals("skip")) {

            } else {

                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }*/

        });
        interestDialog.setCancelable(false);
        interestDialog.show(getSupportFragmentManager(), TAG);
    }


    private void getUserProfile(String userId) {

        initPermissionDialog();

        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(userId);
                request.setProfileId(userId);
                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            GetSet.setUserId(profile.getUserId());
                            GetSet.setName(profile.getName());
                            /*GetSet.setUserName(profile.getUserName());*/
                            GetSet.setDob(profile.getDob());

                            GetSet.setGender(profile.getGender());
                            GetSet.setUserImage(profile.getUserImage());
                            /*GetSet.setLocation(profile.getLocation());*/
                            /*GetSet.setLoginId(profile.getLoginId());*/
                            /*GetSet.setPrivacyAge(profile.getPrivacyAge());
                            GetSet.setPrivacyContactMe(profile.getPrivacyContactMe());*/

                            GetSet.setPrivacyContactMe(profile.getBlockedByMe());

                            GetSet.setAge(String.valueOf(profile.getAge()));
                            GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                            GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                            GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                            //GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                            GetSet.setGems(profile.getPurChasedVotes() != null ? Float.parseFloat(profile.getPurChasedVotes()) : 0F);

                            GetSet.setShowNotification(String.valueOf(profile.getShowNotification()));
                            GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                            GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                            GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));

                            GetSet.setPremiumMember(profile.getPremiumMember());
                            GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());

                            GetSet.setReferalLink(profile.getReferalLink());
                            GetSet.setCreatedAt(profile.getCreatedAt());
                            GetSet.setBio(profile.getBio());


                            // Livza
                            GetSet.setBio(profile.getBio());
                            GetSet.setLikes(profile.getLikes());
                            GetSet.setPostCommand(profile.getCommentPrivacy());
                            GetSet.setSendMessage(profile.getMessagePrivacy());
                            SharedPref.putBoolean(SharedPref.IS_LOGGED, true);
                            SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                            /*SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());*/
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
                            SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                            SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                            SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                            SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                            SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                            SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                            SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                            SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                            SharedPref.putString(SharedPref.REFERAL_LINK, GetSet.getReferalLink());
                            SharedPref.putString(SharedPref.CREATED_AT, GetSet.getCreatedAt());
                            SharedPref.putString(SharedPref.BIO, GetSet.getBio());


                            SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                            SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());


                            if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                                AppUtils.filterLocation = new ArrayList<>();
                            }

                            /*Message Event*/
                            if (chatFragment != null) {
                                EventBus.getDefault().postSticky(GetSet.getUserId());
                            }

                            /*New Streams Event*/
                            if (homeFragment != null) {
                                /*NewStreamEvent event = new NewStreamEvent();
                                event.setHasNewStreams(profile.hasUnReadBroadcasts());
                                EventBus.getDefault().postSticky(event);*/
                            }

                            /*Profile Update Event*/
                            if (profileFragment != null) {
                                GetProfileEvent profileEvent = new GetProfileEvent();
                                profileEvent.setProfileResponse(profile);
                                EventBus.getDefault().postSticky(profileEvent);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {

                    }
                });
            }
        } else {
//            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onBackPressed() {
        //boolean consumed = tellFragments();
        // Timber.d("onBack: consumed %s", consumed);
        //if (!consumed) {
        if (doubleBackToExit) {
            overridePendingTransition(0, 0);
            finishAffinity();
        }

        if (viewPager != null && viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, false);
        } else if (homeFragment != null && homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 0) {
            if (homeFragment.homeForYou != null && homeFragment.homeForYou.viewPager.getCurrentItem() == 1) {
                homeFragment.homeForYou.viewPager.setCurrentItem(0);
            } else if (homeFragment.homeForYou != null && homeFragment.homeForYou.viewPager.getCurrentItem() == 0) {

                this.doubleBackToExit = true;
                App.makeToast(getString(R.string.back_button_exit_description));
                new Handler().postDelayed(() -> doubleBackToExit = false, 1500);
            }
        } else if (homeFragment != null && homeFragment.homeFragViewPager != null && homeFragment.homeFragViewPager.getCurrentItem() == 1) {
            if (homeFragment.homeFollowing != null && homeFragment.homeFollowing.viewPager.getCurrentItem() == 1) {
                homeFragment.homeFollowing.viewPager.setCurrentItem(0);
            } else if (homeFragment.homeFollowing != null && homeFragment.homeFollowing.viewPager.getCurrentItem() == 0) {

                this.doubleBackToExit = true;
                App.makeToast(getString(R.string.back_button_exit_description));
                new Handler().postDelayed(() -> doubleBackToExit = false, 1500);
            }
        }
    }

    private boolean tellFragments() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        Timber.d("onBack: total fragments %d %s", fragments.size(), fragments);
        for (Fragment f : fragments) {
            if (f instanceof BaseFragment) {
                Timber.d("onBack: telling %s", f);
                if (((BaseFragment) f).onBackPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    /*@Override
    public void onBackPressed() {

        if (doubleBackToExit) {
            overridePendingTransition(0, 0);
            System.exit(0);
        }

        if (viewPager != null && viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
        } else {
            this.doubleBackToExit = true;
            App.makeToast(getString(R.string.back_button_exit_description));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExit = false;
                }
            }, 2000);
        }
    }*/

    private void getGooglePlayCurrency() {
        if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
            for (MembershipPackages membershipPackages : AdminData.membershipList) {
                skuList.add(membershipPackages.getSubsTitle());
            }
        } else {
            skuList.add(SharedPref.getString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU));
        }
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                    if (skuDetailsList != null) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            String price = skuDetails.getPrice();

                                            Timber.d("initPrimeView: price=> %s", price);

                                            String validity = skuDetails.getSubscriptionPeriod();
                                            if (SharedPref.getString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU).equals(sku)) {
                                                SharedPref.putString(SharedPref.IN_APP_PRICE, price);
                                                SharedPref.putString(SharedPref.IN_APP_VALIDITY, validity);
                                                GetSet.setGetPrimeContent(price);
                                                break;
                                            }
                                        }
                                    }
                                    if (GetSet.getUserId() != null && billingResult != null) {
                                        querySubscriptions(billingResult);
                                    }
                                }
                            });
                }

            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult
                                           billingResult, @Nullable List<Purchase> purchases) {
        if (purchases != null && purchases.size() > 0) {
            Log.i(TAG, "onPurchasesUpdated: " + purchases.size());
            Log.i(TAG, "onPurchasesUpdated: " + purchases.get(purchases.size() - 1).getSku());
            /* Purchases size should be 1. */
            billingClient.endConnection();
            RenewalRequest request = new RenewalRequest();
            request.setUserId(GetSet.getUserId());
            request.setPackageId(purchases.get(purchases.size() - 1).getSku());
            request.setRenewalTime(GetSet.getPremiumExpiry());
            Call<HashMap<String, String>> call = apiInterface.verifyPayment(request);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    HashMap<String, String> hashMap = response.body();
                    if (hashMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        getUserProfile(GetSet.getUserId());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

                }
            });
        }
    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    AppDefaultResponse defaultData = response.body();
                    if (defaultData.getStatus().equals(Constants.TAG_TRUE)) {
                        AdminData.resetData();
                        AdminData.freeGems = defaultData.getFreeGems();
                        AdminData.giftList = defaultData.getGifts();
                        AdminData.giftsDetails = defaultData.getGiftsDetails();
                        AdminData.primeDetails = defaultData.getPrimeDetails();
                        AdminData.reportList = defaultData.getReports();
                        /*Add first item as Select all location filter*/
                        /*AdminData.locationList = new ArrayList<>();
                        AdminData.locationList.add(getString(R.string.select_all));
                        AdminData.locationList.addAll(defaultData.getLocations());*/
                        AdminData.membershipList = defaultData.getMembershipPackages();
                        AdminData.filterGems = defaultData.getFilterGems();
                        AdminData.filterOptions = defaultData.getFilterOptions();
                        AdminData.inviteCredits = defaultData.getInviteCredits();
                        AdminData.showAds = defaultData.getShowAds();
                        AdminData.showVideoAd = defaultData.getVideoAds();
//                        AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                        AdminData.googleAdsId = getString(R.string.banner_ad_id);
                        AdminData.contactEmail = defaultData.getContactEmail();
                        AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                        AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                        AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                        AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                        AdminData.videoCallsGems = defaultData.getVideoCalls();
                        /*AdminData.streamDetails = defaultData.getStreamConnectionInfo();*/


                        AdminData.max_sound_duration = defaultData.getMaxSoundDuration();
                        GetSet.setMax_sound_duration(defaultData.getMaxSoundDuration());

                        Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(defaultData));

                        /*SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                        SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                        SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                        SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());*/
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public void querySubscriptions(BillingResult billingResult) {

        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
        if (billingClient == null ||
                purchasesResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            return;
        }
        onPurchasesUpdated(billingResult, purchasesResult.getPurchasesList());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (checkPermissions(permissions, MainActivity.this)) {
//                new SplashActivity.GetAppDefaultTask().execute();
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, MainActivity.this)) {
                    Log.e(TAG, "onRequestPermissionsResult: :::::if" );
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, 100);
                } else {
                    Log.e(TAG, "onRequestPermissionsResult: :::::else" );
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        }else if (requestCode == Constants.CAMERA_REQUEST_CODE && grantResults.length > 0) {

            boolean isGranted=true;

            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }

            if (isGranted) {
                Intent newIntent = new Intent(MainActivity.this, DeeparActivity.class);
                startActivityForResult(newIntent, Constants.MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE);
                animateZoom(MainActivity.this);
            }else{
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) ) {
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        App.makeToast(getString(R.string.camera_microphone_permission_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);

                    }
                }else{
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO)&&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        Log.e(TAG, "onRequestPermissionsResult: :::::4");
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        Log.e(TAG, "onRequestPermissionsResult: :::::5");
                        App.makeToast(getString(R.string.camera_microphone_storage_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }
            }
        }
    }

    private boolean checkPermissions(String[] permissionList, MainActivity activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, MainActivity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (shouldShowRequestPermissionRationale( permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }

}
