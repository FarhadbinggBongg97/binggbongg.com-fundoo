package com.app.binggbongg.fundoo;

import static com.app.binggbongg.fundoo.App.getCurrentActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.fragment.app.FragmentActivity;

import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import timber.log.Timber;


public abstract class BaseFragmentActivity extends FragmentActivity implements NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = BaseFragmentActivity.class.getSimpleName();
    NetworkReceiver networkReceiver;
    private int displayHeight;
    private int displayWidth;
    boolean previousState = NetworkReceiver.isConnected();

    private final Thread.UncaughtExceptionHandler handleAppCrash =

            (thread, e) -> {
                final Writer result = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(result);
                e.printStackTrace(printWriter);
                printWriter.close();
                StackTraceElement[] arr = e.getStackTrace();
                StringBuilder report = new StringBuilder(e.toString() + "\n\n");
                report.append("--------- Stack trace ---------\n\n");
                for (int i = 0; i < arr.length; i++) {
                    report.append("    ").append(arr[i].toString()).append("\n");
                }
                report.append("-------------------------------\n\n");

                report.append("--------- Cause ---------\n\n");
                Throwable cause = e.getCause();
                if (cause != null) {
                    report.append(cause.toString()).append("\n\n");
                    arr = cause.getStackTrace();
                    for (StackTraceElement stackTraceElement : arr) {
                        report.append("    ").append(stackTraceElement.toString()).append("\n");
                    }
                }
                report.append("-------------------------------\n\n");
                sendEmail(report.toString());
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.anim_slide_left_in, R.anim.anim_stay);
       /* overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);*/

        SharedPref.initPref(this);
        networkReceiver = new NetworkReceiver();
        networkReceiver.setConnectivityListener(this);
        Thread.setDefaultUncaughtExceptionHandler(handleAppCrash);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
    }

    private void sendEmail(String crash) {
        try {

            String reportContetnt = "\n\n" + "DEVICE OS VERSION CODE: " + Build.VERSION.SDK_INT + "\n" +
                    "DEVICE VERSION CODE NAME: " + Build.VERSION.CODENAME + "\n" +
                    "DEVICE NAME: " + AppUtils.getDeviceName() + "\n" +
                    "VERSION CODE: " + BuildConfig.VERSION_CODE + "\n" +
                    "VERSION NAME: " + BuildConfig.VERSION_NAME + "\n" +
                    "PACKAGE NAME: " + BuildConfig.APPLICATION_ID + "\n" +
                    "BUILD TYPE: " + BuildConfig.BUILD_TYPE + "\n\n\n" +
                    crash;

            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"crashlog@hitasoft.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report");
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContetnt);
            try {
                //start email intent
                startActivity(Intent.createChooser(emailIntent, "Email"));
            } catch (Exception e) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                e.printStackTrace();
            }
        } catch (Exception e) {
            Timber.d("sendEmail: %s", e.getMessage());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.setCurrentActivity(this);
    }

    @Override
    protected void onStop() {
        clearReferences();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    public void registerNetworkReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);
    }

    public void unregisterNetworkReceiver() {
        try {
            if (networkReceiver != null) {
                unregisterReceiver(networkReceiver);
            }
        } catch (Exception e) {
            Logging.e(TAG, "unregisterNetworkReceiver: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
    //    super.onBackPressed();
        /*overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);*/
        //setResult(Constants.MUSIC_REQUEST_CODE);
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_rtl_right_out);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected && GetSet.isIsLogged()) {
            AppWebSocket.getInstance(this);
//            if (Constants.RANDOU_ENABLED)
//                RandouWebSocket.getInstance(this);
        } else {
            if (AppWebSocket.getInstance(this) != null)
                AppWebSocket.getInstance(this).disconnect();
            AppWebSocket.mInstance = null;
//            if (Constants.RANDOU_ENABLED)
//                RandouWebSocket.mInstance = null;
        }
        if (previousState != isConnected) {
            previousState = isConnected;
            onNetworkChanged(isConnected);
        }
    }

    public abstract void onNetworkChanged(boolean isConnected);

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            super.setRequestedOrientation(requestedOrientation);
        } catch (IllegalStateException e) {
            // Only fullscreen activities can request orientation
            Logging.e(TAG, "setRequestedOrientation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearReferences() {
        Activity currActivity = App.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            App.setCurrentActivity(null);
    }

}
