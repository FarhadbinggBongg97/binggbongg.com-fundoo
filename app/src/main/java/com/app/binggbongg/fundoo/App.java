package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.stetho.Stetho;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.logging.FileTree;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import timber.log.Timber;


public class App extends android.app.Application implements LifecycleObserver {


    private static final String TAG = App.class.getSimpleName();

    private static App instance;
    private static Activity mCurrentActivity = null;
    private Locale locale;
    public static boolean onAppForegrounded = false, isAppOpened = false;
    public static RequestOptions profileImageRequest;
    private StorageUtils storageUtils;
    private HttpProxyCacheServer proxy;

    private static long mLastClicked = 0;
    public static void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    private static Gson sGson;


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        storageUtils = StorageUtils.getInstance(this);
        EventBus.getDefault().removeAllStickyEvents();
        SharedPref.initPref(instance);
        Stetho.initializeWithDefaults(this);
        isAppOpened = false;

        profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .dontAnimate()
                .circleCrop();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AppUtils.resetFilter();
        if (SharedPref.getBoolean(SharedPref.IS_LOGGED, false)) {
            AppWebSocket.getInstance(this);
            GetSet.setLogged(true);
            GetSet.setUserId(SharedPref.getString(SharedPref.USER_ID, null));
            GetSet.setLoginId(SharedPref.getString(SharedPref.LOGIN_ID, null));
            GetSet.setLoginType(SharedPref.getString(SharedPref.LOGIN_TYPE, null));
            GetSet.setAuthToken(SharedPref.getString(SharedPref.AUTH_TOKEN, null));
            GetSet.setName(SharedPref.getString(SharedPref.NAME, null));
            GetSet.setUserName(SharedPref.getString(SharedPref.USER_NAME, null));
            GetSet.setUserImage(SharedPref.getString(SharedPref.USER_IMAGE, null));
            GetSet.setAge(SharedPref.getString(SharedPref.AGE, null));
            GetSet.setDob(SharedPref.getString(SharedPref.DOB, null));
            GetSet.setGender(SharedPref.getString(SharedPref.GENDER, null));
            /*GetSet.setLocation(SharedPref.getString(SharedPref.LOCATION, null));*/

            GetSet.setGems(SharedPref.getFloat(SharedPref.GEMS, 0F));
            GetSet.setGifts(SharedPref.getLong(SharedPref.GIFTS, 0L));
            GetSet.setVideos(SharedPref.getLong(SharedPref.VIDEOS, 0L));
            GetSet.setVideosHistory(SharedPref.getLong(SharedPref.VIDEOS_HISTORY, 0L));
            GetSet.setFollowersCount(SharedPref.getString(SharedPref.FOLLOWERS_COUNT, "0"));
            GetSet.setFollowingCount(SharedPref.getString(SharedPref.FOLLOWINGS_COUNT, "0"));
            GetSet.setFriendsCount(SharedPref.getInt(SharedPref.FRIENDS_COUNT, 0));
            GetSet.setInterestsCount(SharedPref.getInt(SharedPref.INTEREST_COUNT, 0));
            GetSet.setUnlocksLeft(SharedPref.getInt(SharedPref.UNLOCKS_LEFT, 0));
            GetSet.setPremiumMember(SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, Constants.TAG_FALSE));
            GetSet.setPremiumExpiry(SharedPref.getString(SharedPref.PREMIUM_EXPIRY, Constants.TAG_FALSE));
            GetSet.setPrivacyAge(SharedPref.getString(SharedPref.PRIVACY_AGE, Constants.TAG_FALSE));
            GetSet.setPrivacyContactMe(SharedPref.getString(SharedPref.PRIVACY_CONTACT_ME, Constants.TAG_FALSE));
            GetSet.setShowNotification(SharedPref.getString(SharedPref.SHOW_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setChatNotification(SharedPref.getString(SharedPref.CHAT_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setFollowNotification(SharedPref.getString(SharedPref.FOLLOW_NOTIFICATION, Constants.TAG_FALSE));
            GetSet.setInterestNotification(SharedPref.getBoolean(SharedPref.INTEREST_NOTIFICATION, false));
            GetSet.setGiftEarnings(SharedPref.getString(SharedPref.GIFT_EARNINGS, "0"));
            GetSet.setReferalLink(SharedPref.getString(SharedPref.REFERAL_LINK, ""));
            GetSet.setCreatedAt(SharedPref.getString(SharedPref.CREATED_AT, ""));

            GetSet.setPostCommand(SharedPref.getString(SharedPref.POST_COMMAND, "Everyone"));
            GetSet.setSendMessage(SharedPref.getString(SharedPref.SEND_MESSAGE, "Everyone"));

            if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                GetSet.setOncePurchased(true);
            } else {
                GetSet.setOncePurchased(SharedPref.getBoolean(SharedPref.ONCE_PAID, false));
            }
        }

        Timber.plant(new Timber.DebugTree(), new FileTree(this));
    }


    @SuppressLint("ResourceAsColor")
    public static void dialog(Context ctx, String title, String content, int color_green) {

        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        MaterialTextView alertTitle = (MaterialTextView) dialog.findViewById(R.id.alert_title);
        MaterialTextView alertMsg = (MaterialTextView) dialog.findViewById(R.id.alert_msg);
        MaterialButton alertOk = (MaterialButton) dialog.findViewById(R.id.alert_button);

        alertMsg.setTextColor(color_green);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        } else dialog.dismiss();


    }

    public static Gson getGsonPrettyInstance() {
        if (sGson == null) {
            sGson = new GsonBuilder().setLenient().setPrettyPrinting().create();
        }
        return sGson;
    }


    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static synchronized App getInstance() {
        return instance;
    }

    public static void makeToast(String message) {
        Toast.makeText(instance.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void makeCustomToast(String message) {

        /*Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        LayoutInflater li = (LayoutInflater) instance.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
        View toastView = li.inflate(R.layout.toast_hint_layout, null);
        TextView text = (TextView) toastView.findViewById(R.id.hint_text_tv);
        text.setText(message);
        text.setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toastView.setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        toastView.setBackgroundColor(Color.BLACK);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();*/

        Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        toast.getView()
                .setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message))
                .setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toast.getView().setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(instance.getResources().getColor(R.color.colorWhite));
        toast.show();



      /*  Toast toast = Toast.makeText(instance, message, Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(Color.parseColor("#F6AE2D"));*/
    /*    toast.getView()
                .setBackgroundTintList(ColorStateList.valueOf(instance.getResources().getColor(R.color.colorBlack)));
        ((TextView) toast.getView().findViewById(android.R.id.message))
                .setTextColor(instance.getResources().getColor(R.color.colorWhite));
*/        //toast.show();
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        fix();
    }

    public static void fix() {
        try {
            Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
            Method method = clazz.getSuperclass().getDeclaredMethod("stop");
            method.setAccessible(true);

            Field field = clazz.getDeclaredField("INSTANCE");
            field.setAccessible(true);

            method.invoke(field.get(null));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    public static String fmtFloat(float d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%s", d);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        onAppForegrounded = true;
        if (!(App.getCurrentActivity() instanceof SplashActivity)) {
            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(instance);
        }

        /*if (App.getCurrentActivity() instanceof RandouCallActivity) {
            RandouWebSocket.mInstance = null;
            RandouWebSocket.getInstance(instance);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (RandouCallActivity.isInCall && !VideoCallActivity.isInCall) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                            jsonObject.put(Constants.TAG_PARTNER_ID, RandouCallActivity.partnerId);
                            jsonObject.put(Constants.TAG_MSG_TYPE, Constants.ONLINE);
                            Log.i(TAG, "onAppForegrounded: " + jsonObject);
                            RandouWebSocket.getInstance(instance).send(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 1000);
        } else*/
        if ((App.getCurrentActivity() instanceof VideoCallActivity)) {
            if (NetworkReceiver.isConnected()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (VideoCallActivity.isInCall && AppWebSocket.getInstance(instance) != null) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                                jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                jsonObject.put(Constants.TAG_RECEIVER_ID, VideoCallActivity.receiverId.equals(GetSet.getUserId())
                                        ? VideoCallActivity.userId : VideoCallActivity.receiverId);
                                jsonObject.put(Constants.TAG_MSG_TYPE, Constants.ONLINE);
                                Log.i(TAG, "onAppForegrounded: " + jsonObject);
                                AppWebSocket.getInstance(instance).send(jsonObject.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 1000);
            }
        } else {
           /* if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false)) {
                DialogAppLock dialogAppLock = new DialogAppLock();
                dialogAppLock.setCallBack(new FingerPrintCallBack() {
                    @Override
                    public void onPurchased(boolean withFingerprint, @Nullable FingerprintManager.CryptoObject cryptoObject) {

                    }

                    @Override
                    public void onError(String errString) {

                    }
                });
                dialogAppLock.setCancelable(false);
                dialogAppLock.show(get());
            }*/
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        onAppForegrounded = false;
        if (GetSet.isIsLogged() && AppWebSocket.getInstance(this) != null) {
            if (VideoCallActivity.isInCall) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_RECEIVER_ID, VideoCallActivity.receiverId.equals(GetSet.getUserId())
                            ? VideoCallActivity.userId : VideoCallActivity.receiverId);
                    jsonObject.put(Constants.TAG_MSG_TYPE, Constants.OFFLINE);
                    Log.i(TAG, "onAppBackgrounded: " + jsonObject);
                    AppWebSocket.getInstance(this).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            AppWebSocket.getInstance(instance).disconnect();
        }

        /*if (GetSet.isIsLogged() && Constants.RANDOU_ENABLED && RandouWebSocket.getInstance(instance) != null) {
            if (RandouCallActivity.isInCall) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_TYPE, Constants.TAG_NOTIFY_USER);
                    jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_PARTNER_ID, RandouCallActivity.partnerId);
                    jsonObject.put(Constants.TAG_MSG_TYPE, Constants.OFFLINE);
                    Log.i(TAG, "onAppBackgrounded: " + jsonObject);
                    RandouWebSocket.getInstance(instance).send(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onAppDestroy() {

    }

    public static boolean isOnAppForegrounded() {
        return onAppForegrounded;
    }

    public void changeLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        String lang = SharedPref
                .getString(Constants.TAG_LANGUAGE_CODE, Constants.DEFAULT_LANGUAGE_CODE);
        if (!(config.locale.getLanguage().equals(lang))) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            Logging.i(TAG, "changeLocale: " + lang);
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }

    // TODO: 26/10/21 @VishnuKumar
    public static boolean isPreventMultipleClick() {
        if (SystemClock.elapsedRealtime() - mLastClicked < 1000) {
            return true;
        }
        mLastClicked = SystemClock.elapsedRealtime();
        return false;
    }

    public static void preventMultipleClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 1000);
    }

    public static RequestOptions getProfileImageRequest() {
        return profileImageRequest;
    }

    public static int getScreenOrientation(Activity context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int orientation;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        orientation = context.getResources().getConfiguration().orientation;
        return orientation;
    }

    public static void hideKeyboardFragment(Context context, View view) {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        /*imm.hideSoftInputFromWindow(context.getWindow().getDecorView().getWindowToken(), 0);*/
        imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
    }

    public static void hideKeyboardActivity(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

}
