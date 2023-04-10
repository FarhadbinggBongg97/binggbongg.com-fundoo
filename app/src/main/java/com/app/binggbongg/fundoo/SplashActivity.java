package com.app.binggbongg.fundoo;


import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.binggbongg.model.FilterDetailsModel;
import com.facebook.share.Share;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.helper.callback.FingerPrintCallBack;
import com.app.binggbongg.model.AddDeviceRequest;
import com.app.binggbongg.model.AppDefaultResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.DeviceTokenPref;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SplashActivity extends BaseFragmentActivity /*implements OSSubscriptionObserver*/ {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private ApiInterface apiInterface;
    DBHelper dbHelper;
    ImageView splashImage;
    private BiometricPrompt.PromptInfo promptInfo;
    private ExecutorService executor;
    private BiometricPrompt biometricPrompt;
    private AppUpdateManager appUpdateManager;
    InstallStateUpdatedListener listener;
    private boolean isMandatoryUpdate = true;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    Cipher defaultCipher;
    SecretKey secretKey;
    Cipher cipherNotInvalidated;
    private AppUtils appUtils;
    private boolean hasStream = false;
    private String streamName = null;
    private static final String KEY_NAME = "hitasoft";
    private char[] KEY_PASSWORD = KEY_NAME.toCharArray();
    String oneSignalId = null;

    public static String[] mandaryPermissions12= new String[]{READ_PHONE_STATE,BLUETOOTH_CONNECT};
    public static String[] mandaryPermissions13= new String[]{READ_PHONE_STATE,BLUETOOTH_CONNECT,POST_NOTIFICATIONS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);*/

        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_splash);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        //OneSignal.addSubscriptionObserver(this);


       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(SplashActivity.this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(SplashActivity.this, READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(SplashActivity.this, BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{POST_NOTIFICATIONS,READ_PHONE_STATE,BLUETOOTH_CONNECT}, 100);
            } else {
                Log.e(TAG, "onCreate: ::::::else1" );
                new GetAppDefaultTask().execute();
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(SplashActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(SplashActivity.this, BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{READ_PHONE_STATE,BLUETOOTH_CONNECT}, 100);
            } else {
                Log.e(TAG, "onCreate: ::::::else2" );
                new GetAppDefaultTask().execute();
            }
        }else {
            Log.e(TAG, "onCreate: ::::::else2" );
            new GetAppDefaultTask().execute();
        }
*/


        /*if (!checkMandatoryPermissions()) {
            requestMandatoryPermissions();
        }else {
            new GetAppDefaultTask().execute();
        }*/

        new GetAppDefaultTask().execute();
        App.isAppOpened = false;
        appUtils = new AppUtils(this);
   /*     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(SplashActivity.this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
            }
        }*/
        try {
            StorageUtils storageUtils = StorageUtils.getInstance(this);
            storageUtils.deleteCacheDir();
        } catch (Exception e) {
            Timber.e("onCreate: " + e.getMessage());
        }

        executor = Executors.newSingleThreadExecutor();
        appUtils.hideKeyboard(this);
        initView();
        checkIsFromLink();


        //printHashKey(SplashActivity.this);
        // start service for observing intents
//        startService(new Intent(this, LockscreenService.class));
//        printHashKey();
//        checkUser();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
        splashImage = findViewById(R.id.splashImage);
        AppUtils.callerList = new ArrayList<>();
        dbHelper = DBHelper.getInstance(this);
        splashImage.setImageDrawable(getDrawable(R.mipmap.ic_splash));
    }


    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("TAG", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            // Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            //Log.e(TAG, "printHashKey()", e);
        }
    }


    private void checkForUpdate() {
        int appUpdateType = isMandatoryUpdate ? AppUpdateType.IMMEDIATE : AppUpdateType.FLEXIBLE;
        int requestCode = isMandatoryUpdate ? Constants.REQUEST_APP_UPDATE_IMMEDIATE : Constants.REQUEST_APP_UPDATE_FLEXIBLE;
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        listener = state -> {

            // (Optional) Provide a download progress bar.
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                long bytesDownloaded = state.bytesDownloaded();
                long totalBytesToDownload = state.totalBytesToDownload();
                // Implement progress bar.

            } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                // When status updates are no longer needed, unregister the listener.
                appUpdateManager.completeUpdate();
                appUpdateManager.unregisterListener(listener);
                popupSnackbarForCompleteUpdate();
            }
        };

        // Returns an intent object that you use to check for an update
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateType == AppUpdateType.IMMEDIATE && appUpdateInfo.isUpdateTypeAllowed(appUpdateType)) {
                    // Request the update.
                    startAppUpdateImmediate(appUpdateInfo);
                } else if (appUpdateType == AppUpdateType.FLEXIBLE && appUpdateInfo.isUpdateTypeAllowed(appUpdateType)) {
                    // Request the update.
                    appUpdateManager.registerListener(listener);
                    startAppUpdateFlexible(appUpdateInfo);
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                checkUser();
            } else {
                checkUser();
            }
        }).addOnFailureListener(e -> {

                    checkUser();
                }
        );
    }

    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    Constants.REQUEST_APP_UPDATE_IMMEDIATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    Constants.REQUEST_APP_UPDATE_FLEXIBLE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        if (appUpdateManager != null) {
            appUpdateManager
                    .getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> {
                                //FLEXIBLE:
                                // If the update is downloaded but not installed,
                                // notify the user to complete the update.
                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                    popupSnackbarForCompleteUpdate();
                                }

                                //IMMEDIATE:
                                if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    // If an in-app update is already running, resume the update.
                                    startAppUpdateImmediate(appUpdateInfo);
                                }
                            }).addOnFailureListener(new com.google.android.play.core.tasks.OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    checkUser();
                }
            });
        }

    }

    private void checkUser() {
        App.isAppOpened = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkUserIsLoggedIn();
                    }
                }, 0);
            }
        });
    }

    private void checkUserIsLoggedIn() {


        Timber.i("splashTest getUserId %s", GetSet.getUserId());
        Timber.i("splashTest SharedPref.getBoolean %s", SharedPref.getBoolean("isLogged", false));

        if (SharedPref.getBoolean("isLogged", false)) {

            Timber.i("splashTest %s", "if enter");

            if (!AppUtils.callerList.contains(GetSet.getUserId())) {
                AppUtils.callerList.add(GetSet.getUserId());
            }
            dbHelper.updateChatsOnline();
            dbHelper.updateChatsTyping();
            GetSet.setLogged(true);
            GetSet.setUserId(SharedPref.getString(SharedPref.USER_ID, null));
            GetSet.setLoginId(SharedPref.getString(SharedPref.LOGIN_ID, null));
            GetSet.setAuthToken(SharedPref.getString(SharedPref.AUTH_TOKEN, null));
            GetSet.setName(SharedPref.getString(SharedPref.NAME, null));
            GetSet.setUserImage(SharedPref.getString(SharedPref.USER_IMAGE, null));
            GetSet.setAge(SharedPref.getString(SharedPref.AGE, null));
            GetSet.setDob(SharedPref.getString(SharedPref.DOB, null));
            GetSet.setGender(SharedPref.getString(SharedPref.GENDER, null));
            /*GetSet.setLocation(SharedPref.getString(SharedPref.LOCATION, null));*/
            GetSet.setGems(SharedPref.getFloat(SharedPref.GEMS, 0));
            GetSet.setGifts(SharedPref.getLong(SharedPref.GIFTS, 0));
            GetSet.setVideos(SharedPref.getLong(SharedPref.VIDEOS, 0));
            GetSet.setVideosHistory(SharedPref.getLong(SharedPref.VIDEOS_HISTORY, 0));
            GetSet.setFollowersCount(SharedPref.getString(SharedPref.FOLLOWERS_COUNT, null));
            GetSet.setFollowingCount(SharedPref.getString(SharedPref.FOLLOWINGS_COUNT, null));
            GetSet.setInterestsCount(SharedPref.getInt(SharedPref.INTEREST_COUNT, 0));
            GetSet.setFriendsCount(SharedPref.getInt(SharedPref.FRIENDS_COUNT, 0));
            GetSet.setUnlocksLeft(SharedPref.getInt(SharedPref.UNLOCKS_LEFT, 0));
            hitasoft.serviceteam.livestreamingaddon.external.utils.GetSet.setImageUrl(GetSet.getUserImage());
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


            /*if (Constants.RANDOU_ENABLED) {
             *//*Intent intent = new Intent(getApplicationContext(), com.hitasoft.app.livza.randou.MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);*//*
            } else {



            }*/

            Timber.d("splashTest: MainActivity enter");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

            AppWebSocket.mInstance = null;
            AppWebSocket.getInstance(SplashActivity.this);


        } else {
            Timber.d("splashTest: LoginActivity enter");
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        /*SplashActivity.this.finish();*/
    }

    public void checkFingerPrintEnabled() {
        if (SharedPref.getBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false) && appUtils.checkIsDeviceEnabled(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                secretKey = getSecretKey();
                defaultCipher = getCipher();
                generateSecretKey();
                if (defaultCipher != null && secretKey != null) {
                    try {
                        defaultCipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            }

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    generateSecretKey();
//                    createKey(Constants.DEFAULT_KEY_NAME, true);
//                    createKey(Constants.KEY_NAME_NOT_INVALIDATED, false);
//                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    openBioMetricDialog();
//                    openFingerPrintDialog();
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
                    checkUser();
                }
            }
        } else {
            SharedPref.putBoolean(SharedPref.IS_FINGERPRINT_LOCKED, false);
            checkForUpdate();
        }
    }

    private void checkIsFromLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        Log.i(TAG, "onSuccess: " + deepLink);
                        if (deepLink != null) {
                            if (deepLink.getQueryParameter("referal_code") != null) {
                                String referalCode = deepLink.getQueryParameter("referal_code");
                                SharedPref.putString(SharedPref.REFERAL_CODE, referalCode);
                            } else if (deepLink.getQueryParameter("stream_name") != null) {
                                streamName = deepLink.getQueryParameter("stream_name");
                                hasStream = true;
                            }
                        } else {
                            SharedPref.putString(SharedPref.REFERAL_CODE, "");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logging.e(TAG, "onSuccess: " + e.getMessage());
            }
        });
    }

    private void openBioMetricDialog() {
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "onAuthenticationError: " + errorCode);
                Log.e(TAG, "onAuthenticationError: " + errString);
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
                } else {
                    finishAndRemoveTask();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.e(TAG, "onAuthenticationSucceeded: " + new Gson().toJson(result));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkUserIsLoggedIn();
                    }
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.e(TAG, "onAuthenticationFailed: ");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.security))
                .setSubtitle("")
                .setDescription(getString(R.string.touch_fingerprint_description))
                // Cannot call setNegativeButtonText() and
                // setDeviceCredentialAllowed() at the same time.
                .setNegativeButtonText(getString(R.string.cancel))
//                .setDeviceCredentialAllowed(true)
                .setConfirmationRequired(false)
                .build();

        if (defaultCipher != null) {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void generateSecretKey() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec keyGenParameterSpec = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(false)
                        .setUserAuthenticationValidityDurationSeconds(120)
                        .build();
                mKeyGenerator.init(keyGenParameterSpec);
                mKeyGenerator.generateKey();
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private SecretKey getSecretKey() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            // Before the keystore can be accessed, it must be loaded.
            mKeyStore.load(null);
            return ((SecretKey) mKeyStore.getKey(KEY_NAME, KEY_PASSWORD));
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException |
                NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Cipher getCipher() {
        try {
            return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openFingerPrintDialog() {
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
        if (fingerprintManager != null && !fingerprintManager.hasEnrolledFingerprints()) {
            checkUser();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                initCipher(defaultCipher, Constants.DEFAULT_KEY_NAME);
            }
            DialogFingerPrint dialogFingerPrint = new DialogFingerPrint();
            dialogFingerPrint.setContext(this);
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
                    Timber.i("openFingerPrintDialog=> %s", errorMsg);

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
                    checkUserIsLoggedIn();
                }
            });
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            App.makeToast("Failed to encrypt the data with the generated key. " + "Retry the purchase");
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

       /* getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);*/
      /*  if (!checkMandatoryPermissions()) {
            requestMandatoryPermissions();
        }else {
            new GetAppDefaultTask().execute();
        }*/
        checkNewAppVersionState();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterInstallStateUpdListener();
    }

    /* Displays the snackbar notification and call to action. */
    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.splashLay),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.colorPrimary));
        snackbar.show();
        unregisterInstallStateUpdListener();
    }


    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(@NonNull Call<AppDefaultResponse> call, @NonNull Response<AppDefaultResponse> response) {
                    Timber.d("Splash: onResponse:%s ", App.getGsonPrettyInstance().toJson(response.body()));

                    Log.e(TAG, "onResponse: default:::::::::::::::;;"+new Gson().toJson(response.body()));
                    if (response.isSuccessful()) {
                        Timber.d("onResponse:%s ", App.getGsonPrettyInstance().toJson(response.body()));
                        AppDefaultResponse defaultData = response.body();
                        if (("" + defaultData.getStatus()).equals(Constants.TAG_TRUE)) {
                            AdminData.resetData();
                            AdminData.freeGems = defaultData.getFreeGems();
                            AdminData.giftList = defaultData.getGifts();
                            AdminData.giftsDetails = defaultData.getGiftsDetails();
                            AdminData.primeDetails = defaultData.getPrimeDetails();
                            AdminData.reportList = defaultData.getReports();
                            /*Add first item as Select all location filter*/
/*                            AdminData.locationList = new ArrayList<>();
                            AdminData.locationList.add(getString(R.string.select_all));
                            AdminData.locationList.addAll(defaultData.getLocations());*/
                            AdminData.membershipList = defaultData.getMembershipPackages();
                            AdminData.filterGems = defaultData.getFilterGems();
                            AdminData.filterOptions = defaultData.getFilterOptions();
                            AdminData.inviteCredits = defaultData.getInviteCredits();

                            AdminData.max_sound_duration = defaultData.getMaxSoundDuration();
                            GetSet.setMax_sound_duration(defaultData.getMaxSoundDuration());

                            Timber.d("onResponse:%s ", defaultData);

//                            AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                            AdminData.googleAdsId = getString(R.string.banner_ad_id);
                            AdminData.showVideoAd = defaultData.getVideoAds();
                            AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                            AdminData.contactEmail = defaultData.getContactEmail();


                            AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                            //AdminData.showMoneyConversion = "0";

                            AdminData.videoCallsGems = defaultData.getVideoCalls();
                            if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
                                SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, AdminData.membershipList.get(0).getSubsTitle());
                            } else {
                                SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU);
                            }
                            AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                            AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                            AdminData.streamDetails = defaultData.getStreamConnectionInfo();
                            AdminData.streamDetails = defaultData.getStreamConnectionInfo();

                            Timber.d("onResponse: streamURL %s", defaultData.getStreamConnectionInfo().getStreamBaseUrl());

                            GetSet.setStreamBaseUrl(defaultData.getStreamConnectionInfo().getStreamBaseUrl());

                           /* SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                            SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                            SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                            SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());*/
                        }
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AppDefaultResponse> call, @NonNull Throwable t) {
                    Timber.d("Splash: onFailure:%s ", t.getMessage());
                    App.makeToast(getString(R.string.something_went_wrong));
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

    private void addDeviceID() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

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
                if(response.body()!=null && response.body().get("status").equals("false")){
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Logging.d(TAG, "printHashKey: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Logging.e(TAG, "printHashKey" + e.getMessage());
        }
    }

   /* @Override
    public void onOSSubscriptionChanged(OSSubscriptionStateChanges osSubscriptionStateChanges) {
        if (!osSubscriptionStateChanges.getFrom().isSubscribed() &&
                osSubscriptionStateChanges.getTo().isSubscribed()) {
            // get player ID
            if (osSubscriptionStateChanges.getTo().getUserId() != null) {
                oneSignalId = osSubscriptionStateChanges.getTo().getUserId();
            }
        }
    }*/

    @SuppressLint("StaticFieldLeak")
    private class GetAppDefaultTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getAppDefaultData();
            if (SharedPref.getBoolean("isLogged", false)) {
                addDeviceID();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            checkFingerPrintEnabled();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.DEVICE_LOCK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                checkForUpdate();
            } else {
                App.makeToast(getString(R.string.unable_to_verify));
                finish();
            }
        } else if (requestCode == Constants.REQUEST_APP_UPDATE_IMMEDIATE || requestCode == Constants.REQUEST_APP_UPDATE_FLEXIBLE) {
            if (resultCode != RESULT_OK) {
                Logging.d(TAG, "Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
                unregisterInstallStateUpdListener();
                if (requestCode == Constants.REQUEST_APP_UPDATE_FLEXIBLE && resultCode == RESULT_CANCELED) {
                    checkUserIsLoggedIn();
                }
            }
        }
    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && listener != null)
            appUpdateManager.unregisterListener(listener);
    }


    /*private void requestMandatoryPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(this, mandaryPermissions13, 100);
        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            ActivityCompat.requestPermissions(this, mandaryPermissions12, 100);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{ WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    private boolean checkMandatoryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }else   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                   *//* && ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED*//*;
        } else return true;
    }*/


   /* @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (checkPermissions(permissions, SplashActivity.this)) {
                new GetAppDefaultTask().execute();
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, SplashActivity.this)) {
                    ActivityCompat.requestPermissions(SplashActivity.this, permissions, 100);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        }
    }

    private boolean checkPermissions(String[] permissionList, SplashActivity activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, SplashActivity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }
*/



}
