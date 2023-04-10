package com.app.binggbongg.fundoo;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission_group.CAMERA;
import static com.app.binggbongg.apprtc.util.AppRTCUtils.CAPTURE_PERMISSION_REQUEST_CODE;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.app.binggbongg.R;
import com.app.binggbongg.apprtc.AppRTCAudioManager;
import com.app.binggbongg.apprtc.AppRTCClient;
import com.app.binggbongg.apprtc.DirectRTCClient;
import com.app.binggbongg.apprtc.PeerConnectionClient;
import com.app.binggbongg.apprtc.UnhandledExceptionHandler;
import com.app.binggbongg.apprtc.WebSocketRTCClient;
import com.app.binggbongg.apprtc.util.AppRTCUtils;
import com.app.binggbongg.external.RandomString;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.Gift;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class VideoCallActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents, PeerConnectionClient.PeerConnectionEvents,
        CallControlsFragment.OnCallEvents, AppRTCClient.SignalingEvents {

    public static final String LOG_TAG_CALL = "Fundoo.Call.Msg";

    private static final String TAG = VideoCallActivity.class.getSimpleName();
    public static boolean isInCall = false;

    @BindView(R.id.fullscreen_video_view)
    SurfaceViewRenderer fullscreenRenderer;
    @BindView(R.id.pip_video_view)
    SurfaceViewRenderer pipRenderer;
    @BindView(R.id.stickerImageBig)
    LottieAnimationView stickerImageBig;
    @BindView(R.id.txtGiftName)
    TextView txtGiftName;
    @BindView(R.id.giftImageBig)
    ImageView giftImageBig;
    @BindView(R.id.giftDisplayLay)
    LinearLayout giftDisplayLay;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.incomingImage)
    RoundedImageView incomingImage;
    @BindView(R.id.txtIncomingName)
    TextView txtIncomingName;
    @BindView(R.id.calltime)
    TextView txtcalltime;
    @BindView(R.id.incomingLay)
    RelativeLayout incomingLay;
    @BindView(R.id.outGoingImage)
    RoundedImageView outGoingImage;
    @BindView(R.id.txtOutGoingName)
    TextView txtOutGoingName;
    @BindView(R.id.outGoingLay)
    RelativeLayout outGoingLay;
    @BindView(R.id.btnAcceptCall)
    ImageButton btnAcceptCall;
    @BindView(R.id.btnDeclineCall)
    ImageButton btnDeclineCall;
    @BindView(R.id.declineCall)
    ImageView declinecall;
    @BindView(R.id.attenCall)
    ImageView attencall;
    @BindView(R.id.callAcceptLay)
    LinearLayout callAcceptLay;
    @BindView(R.id.txtMessage)
    TextView txtMessage;
    @BindView(R.id.txtUserAway)
    TextView txtUserAway;
    @BindView(R.id.fullscreenBlurView)
    ImageView fullScreenBlurView;
    @BindView(R.id.pipBlurView)
    ImageView pipBlurView;

    private Intent intent;
    private Uri roomUri;
    private Boolean isMutualFollow = true;

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    @Nullable
    private VideoFileRenderer videoFileRenderer;
    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable
    private PeerConnectionClient peerConnectionClient;
    @Nullable
    private AppRTCClient appRtcClient;
    @Nullable
    private AppRTCClient.SignalingParameters signalingParameters;
    @Nullable
    private AppRTCAudioManager audioManager;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private boolean commandLineRun;
    private boolean activityRunning;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    Vibrator vibrator;
    Ringtone ringtone;
    CountDownTimer countDownTimer, callEmitTimer;
    Timer callTimer = new Timer();
    Animation downAnimation, upAnimation;
    ToneGenerator toneGenerator;
    public boolean isCallPause = false, isScreenOn, isPeerReceived;
    public boolean isCallAttend = false, isCallConnected = false;
    private boolean connected;
    private boolean isError;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs;
    private boolean micEnabled = true;
    private boolean screencaptureEnabled;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;
    public String chatType = "", roomId = "", name = "", imgUrl = "", from = "",
            toastText = "", userName = "", userImage = "", platform = "";
    public static String userId = "", receiverId = "";
    private ApiInterface apiInterface;
    private boolean isFrontCamera = true, isBlurred = false;
    public boolean isSender = false;
    private Bitmap blurredBitMap = null;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private static final String TAG_CPU_WAKELOCK = "MyApp::MyWakelockTag";
    // Controls
    CallControlsFragment controlsFragment = null;
    int displayHeight, displayWidth;
    public static boolean speaker = false;

    public String[] permissions = AppRTCUtils.MANDATORY_PERMISSIONS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Timber.tag(LOG_TAG_CALL).d("Starting video call activity");

        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_video_call);
        ButterKnife.bind(this);
        isInCall = true;
        Constants.isInVideoCall = true;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        connected = false;
        signalingParameters = null;
        intent = getIntent();
        chatType = intent.getExtras().getString(Constants.TAG_CHAT_TYPE);
        from = intent.getExtras().getString(Constants.TAG_FROM);
        userId = intent.getExtras().getString(Constants.TAG_USER_ID);
        userName = intent.getExtras().getString(Constants.TAG_USER_NAME);
        userImage = intent.getExtras().getString(Constants.TAG_USER_IMAGE);
        receiverId = intent.getExtras().getString(Constants.TAG_RECEIVER_ID);
        platform = intent.getExtras().getString(Constants.TAG_PLATFORM);
        roomUri = intent.getData();
        isSender = from.equals(Constants.TAG_SEND);
        AppWebSocket.setCallEvents(this);
        initPowerOptions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions=AppRTCUtils.MANDATORY_PERMISSIONSFORANDROID12;
        }

        initView();

        if (roomUri == null) {
            com.app.binggbongg.utils.Logging.e(TAG, "Didn't timer get any URL in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (from.equals(Constants.TAG_RECEIVE) && platform.equals("ios") && chatType.equals(Constants.TAG_VIDEO)) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.TAG_USER_ID, userId);
                jsonObject.put(Constants.TAG_CALL_TYPE, Constants.TAG_PLATFORM);
                jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CALL);
                jsonObject.put(Constants.TAG_PLATFORM, "android");
//                createCall(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (from.equals(Constants.TAG_RECEIVE) && platform.equals("ios") && chatType.equals(Constants.TAG_AUDIO)) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.TAG_USER_ID, userId);
                jsonObject.put(Constants.TAG_CALL_TYPE, Constants.TAG_PLATFORM);
                jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CALL);
                jsonObject.put(Constants.TAG_PLATFORM, "android");
//                createCall(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!checkPermissions()) {
            Log.i(TAG, "oncreatecall: " + "received");

                ActivityCompat.requestPermissions(this, permissions, AppRTCUtils.CALL_PERMISSIONS_REQUEST_CODE);

        } else {
            Log.i(TAG, "oncreatecall: " + "received");
            initRender();
        }
    }

    private void initView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(VideoCallActivity.this);
        FrameLayout.LayoutParams giftParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        giftParams.bottomMargin = (int) (displayHeight * 0.2);
        giftParams.gravity = Gravity.BOTTOM | Gravity.START;
        giftDisplayLay.setLayoutParams(giftParams);

        controlsFragment = new CallControlsFragment();
        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
                if (!isFrontCamera) {
                    fullscreenRenderer.setMirror(false);
                    pipRenderer.setMirror(false);
                } else {
                    setMirror(isSwappedFeeds);
                }
//                setSwappedBlurFeeds(isBlurred);
            }
        });

        fullscreenRenderer.setOnClickListener(listener);
        remoteSinks.add(remoteProxyRenderer);

        if (from.equals(Constants.TAG_RECEIVE)) {
            roomId = intent.getExtras().getString(Constants.TAG_ROOM_ID);

            incomingLay.setVisibility(View.VISIBLE);
            txtIncomingName.setText(userName);
            Glide.with(this)
                    .load(userImage)
                    .apply(new RequestOptions().dontAnimate().error(R.mipmap.ic_launcher))
                    .into(incomingImage);
            callAcceptLay.setVisibility(View.VISIBLE);

            if(chatType.equals(Constants.TAG_AUDIO))
            {
                btnAcceptCall.setVisibility(View.GONE);
                btnDeclineCall.setVisibility(View.GONE);
            }
            else
            {
                declinecall.setVisibility(View.GONE);
                attencall.setVisibility(View.GONE);
            }

            btnAcceptCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NetworkReceiver.isConnected()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ringtone != null) {
                                    ringtone.stop();
                                    ringtone = null;
                                }
                                if (vibrator != null) {
                                    vibrator.cancel();
                                    vibrator = null;
                                }
                                isCallAttend = true;
                                isCallConnected = true;
                                incomingLay.setVisibility(View.GONE);
                                btnAcceptCall.setVisibility(View.GONE);

                                onCallAccept();
                            }
                        });
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                    }
                }
            });

            btnDeclineCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createCall(Constants.TAG_ENDED, userId);
                    onCallHangUp();
                }
            });

            attencall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NetworkReceiver.isConnected()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ringtone != null) {
                                    ringtone.stop();
                                    ringtone = null;
                                }
                                if (vibrator != null) {
                                    vibrator.cancel();
                                    vibrator = null;
                                }
                                isCallAttend = true;
                                isCallConnected = true;
                                incomingLay.setVisibility(View.GONE);
                                attencall.setVisibility(View.GONE);

                                onCallAccept();
                            }
                        });
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                    }
                }
            });

            declinecall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createCall(Constants.TAG_ENDED, userId);
                    onCallHangUp();
                }
            });

            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (alert == null) {
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                // I can'timer see this ever being null (as always have a default notification)
                // but just incase
                if (alert == null) {
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
                }
            }
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
            switch (audio.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
                    if (!ringtone.isPlaying())
                        ringtone.play();
                    vibrator.vibrate(pattern, 0);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    vibrator.vibrate(pattern, 0);
                    break;
            }
            turnOnScreen();
        } else {
            outGoingLay.setVisibility(View.VISIBLE);
            callAcceptLay.setVisibility(View.VISIBLE);
            if(chatType.equals(Constants.TAG_AUDIO))
            {
                btnAcceptCall.setVisibility(View.GONE);
                btnDeclineCall.setVisibility(View.GONE);
            }
            else
            {
                declinecall.setVisibility(View.GONE);
                attencall.setVisibility(View.GONE);
            }

            txtOutGoingName.setText(userName);
            txtIncomingName.setText(userName);
            Glide.with(this)
                    .load(userImage)
                    .apply(new RequestOptions().dontAnimate().error(R.mipmap.ic_launcher))
                    .into(outGoingImage);
            btnAcceptCall.setVisibility(View.GONE);
            btnDeclineCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!connected && isMutualFollow) {
                        sendMessage(Constants.TAG_MISSED, receiverId);
                    }
                    createCall(Constants.TAG_ENDED, receiverId);
                    onCallHangUp();
                }
            });
            attencall.setVisibility(View.GONE);
            declinecall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!connected && isMutualFollow) {
                        sendMessage(Constants.TAG_MISSED, receiverId);
                    }
                    createCall(Constants.TAG_ENDED, receiverId);
                    onCallHangUp();
                }
            });
            toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 30000);
        }
    }

    private void setAudioOutputConfigs() {
        AudioManager am = audioManager.getAudioManager();
        AppRTCAudioManager.AudioDevice tempDevice;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (am.isWiredHeadsetOn() || am.isBluetoothScoOn() || am.isBluetoothA2dpOn()) {
                tempDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                audioManager.selectAudioDevice(tempDevice);
            } else if (chatType.equals(Constants.TAG_VIDEO)) {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        } else {
            switch (audioManager.audioOutput) {
                case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                    tempDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                    break;
                case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                    tempDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                    break;
                case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                    tempDevice = AppRTCAudioManager.AudioDevice.BLUETOOTH;
//                    audioManager.setSpeakerphoneOn(false);
                    break;
                case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
//                    audioManager.setSpeakerphoneOn(false);
                    tempDevice = AppRTCAudioManager.AudioDevice.BLUETOOTH;
                    break;
                default:
                    tempDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                    break;
            }
            Log.i(TAG, "setAudioOutputConfigs: " + tempDevice);
            audioManager.selectAudioDevice(tempDevice);
        }
    }

    private void initRender() {

        final EglBase eglBase = EglBase.create();

        // Create video renderers.
        pipRenderer.init(eglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        String saveRemoteVideoToFile = intent.getStringExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        if (saveRemoteVideoToFile != null) {
            int videoOutWidth = intent.getIntExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
            int videoOutHeight = intent.getIntExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
            try {
                videoFileRenderer = new VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.getEglBaseContext());
                remoteSinks.add(videoFileRenderer);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to open video file for output: " + saveRemoteVideoToFile, e);
            }
        }
        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */);

        // Check for mandatory permissions.
        for (String permission : permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }

        boolean loopback = intent.getBooleanExtra(AppRTCUtils.EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(AppRTCUtils.EXTRA_TRACING, false);

        int videoWidth = intent.getIntExtra(AppRTCUtils.EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(AppRTCUtils.EXTRA_VIDEO_HEIGHT, 0);

        screencaptureEnabled = intent.getBooleanExtra(AppRTCUtils.EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }

        startTimer();

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(AppRTCUtils.EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(AppRTCUtils.EXTRA_ORDERED, true),
                    intent.getIntExtra(AppRTCUtils.EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(AppRTCUtils.EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(AppRTCUtils.EXTRA_PROTOCOL),
                    intent.getBooleanExtra(AppRTCUtils.EXTRA_NEGOTIATED, false), intent.getIntExtra(AppRTCUtils.EXTRA_ID, -1));
        }
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(AppRTCUtils.EXTRA_VIDEO_CALL, false), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(AppRTCUtils.EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(AppRTCUtils.EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(AppRTCUtils.EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(AppRTCUtils.EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(AppRTCUtils.EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, true),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        intent.getBooleanExtra(AppRTCUtils.EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);
        commandLineRun = intent.getBooleanExtra(AppRTCUtils.EXTRA_CMDLINE, false);
        int runTimeMs = intent.getIntExtra(AppRTCUtils.EXTRA_RUNTIME, 0);

        Log.d(TAG, "VIDEO_FILE: '" + intent.getStringExtra(AppRTCUtils.EXTRA_VIDEO_FILE_AS_CAMERA) + "'");

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this);
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }
        // Create connection parameters.
        String urlParameters = intent.getStringExtra(AppRTCUtils.EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);
        // Send intent arguments to fragments.
        controlsFragment.setContext(this);
        controlsFragment.setControlEvent(this);
        Bundle controlBundle = new Bundle();
        controlBundle.putString(Constants.TAG_CHAT_TYPE, chatType);
        controlBundle.putString(Constants.TAG_FROM, from);
        controlsFragment.setArguments(controlBundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.call_controls_container, controlsFragment);
        ft.commit();
//        controlsFragment.setCallUI();

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(() -> disconnect(), runTimeMs);
        }

        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient(
                getApplicationContext(), eglBase, peerConnectionParameters, this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }
        peerConnectionClient.createPeerConnectionFactory(options);

        /*if (screencaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }*/
        startCall();
        Timber.tag(TAG).d("Staring call..");
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();
        // Start room connection.
        appRtcClient.connectToRoom(roomConnectionParameters);

        if (audioManager.hasWiredHeadset()) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            speaker = false;
        } else {
            if (chatType.equals(Constants.TAG_VIDEO)) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                speaker = true;
                if (from.equals(Constants.TAG_SEND)) {
                    startAudioManager();
                    setAudioOutputConfigs();

                }
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                speaker = false;
                if (from.equals(Constants.TAG_SEND)) {
                    startAudioManager();
               //     setAudioOutputConfigs();

                }
            }
        }
       /* if (from.equals(Constants.TAG_SEND)) {
                startAudioManager();
                setAudioOutputConfigs();

        }*/
    }

    public void startTimer() {
        if (isSender) {
            try {
                createCall(Constants.TAG_CREATED, receiverId);
                callEmitTimer = new CountDownTimer(30000, 1000) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (connected) {
                            cancelCallEmitTimer();
                        }
                    }

                    @Override
                    public void onFinish() {
                        Log.v(TAG, "callEmitTimer=Ended");
                        if (callEmitTimer != null) {
                            cancelCallEmitTimer();
                            stopAudioManager();
                            if (isSender && !connected && isMutualFollow) {
                                sendMessage(Constants.TAG_MISSED, receiverId);
                            }
                            createCall(Constants.TAG_ENDED, receiverId);
                            finish();
                        }
                    }
                };
                callEmitTimer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createCall(String callType, String receiverId) {
        RandomString randomString = new RandomString(10);
        String utcTime = AppUtils.getCurrentUTCTime(this);
        roomId = GetSet.getUserId() + randomString.nextString();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_CREATE_CALL);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            jsonObject.put(Constants.TAG_RECEIVER_ID, receiverId);
            jsonObject.put(Constants.TAG_CHAT_TYPE, chatType);
            jsonObject.put(Constants.TAG_CALL_TYPE, callType);
            jsonObject.put(Constants.TAG_CREATED_AT, utcTime);
            jsonObject.put(Constants.TAG_ROOM_ID, roomId);
            jsonObject.put(Constants.TAG_PLATFORM, Constants.TAG_ANDROID);
            //com.hitasoft.app.fundoo.utils.Logging.i(TAG, "createCall: " + jsonObject);
            AppWebSocket.getInstance(this).send(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            com.app.binggbongg.utils.Logging.e(TAG, "createCall: " + e.getMessage());
        }
    }

    private void sendMessage(String callType, String receiverId) {
        RandomString randomString = new RandomString(10);
        String utcTime = AppUtils.getCurrentUTCTime(this);
        long unixStamp = System.currentTimeMillis() / 1000L;
        String messageId = GetSet.getUserId() + unixStamp;
        roomId = GetSet.getUserId() + randomString.nextString();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_USER_NAME, GetSet.getName());
            jsonObject.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            jsonObject.put(Constants.TAG_RECEIVER_ID, receiverId);
            jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            jsonObject.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(getString(R.string.missed_call)));
            jsonObject.put(Constants.TAG_MSG_ID, messageId);
            jsonObject.put(Constants.TAG_MSG_TYPE, Constants.TAG_MISSED);
            jsonObject.put(Constants.TAG_CHAT_TIME, utcTime);
            jsonObject.put(Constants.TAG_ROOM_ID, roomId);
            jsonObject.put(Constants.TAG_PLATFORM, Constants.TAG_ANDROID);
            com.app.binggbongg.utils.Logging.i(TAG, "sendMessage: " + jsonObject);
            AppWebSocket.getInstance(this).send(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            com.app.binggbongg.utils.Logging.e(TAG, "sendMessage: " + e.getMessage());
        }
    }

    private void cancelCallEmitTimer() {
        if (callEmitTimer != null) {
            callEmitTimer.cancel();
            callEmitTimer = null;
        }
    }


    private boolean checkPermissions() {
        boolean isPermissionGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted = false;
                break;
            }
        }
        return isPermissionGranted;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && intent.getBooleanExtra(AppRTCUtils.EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return intent.getBooleanExtra(AppRTCUtils.EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private Handler answerHandler = new Handler();
    int delay = 1000; //milliseconds
    Runnable answerRunnable = new Runnable() {

        @Override
        public void run() {
            onCallAccept();
        }
    };

    private void onCallAccept() {
        if (signalingParameters != null && !signalingParameters.initiator && isPeerReceived) {
            /*Remove handler once call connected*/
            answerHandler.removeCallbacks(answerRunnable);
            // Create answer. Answer SDP will be sent to offering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createAnswer();
        } else {
            answerHandler.postDelayed(answerRunnable, delay);
        }
    }

    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    @TargetApi(21)
    private @Nullable
    VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCallPause = false;
        registerNetworkReceiver();
        App.setCurrentActivity(this);
        if (!NetworkReceiver.isConnected()) {
            /*toastText = getString(R.string.poor_internet_connection);
            logAndToast(toastText);
            disconnect();*/
        } else {
            if (peerConnectionClient != null && chatType.equals("video"))
                peerConnectionClient.startVideoSource();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(Constants.CALL_TAG, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCallPause) {
            if (ringtone != null) {
                ringtone.stop();
                ringtone = null;
            }
            if (vibrator != null) {
                vibrator.cancel();
                vibrator = null;
            }
        }
        isCallPause = true;
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
        // Don'timer stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        Thread.setDefaultUncaughtExceptionHandler(null);
        AppWebSocket.setCallEvents(null);
        activityRunning = false;
        isInCall = false;
        userId = "";
        try {
            releaseWakeLock();
        } catch (Exception e) {
            com.app.binggbongg.utils.Logging.e(TAG, "onDestroy: " + e.getMessage());
        }
        Constants.isInVideoCall = false;
        stopAudioManager();
        disconnect();
        if (!NetworkReceiver.isConnected()) {
            toastText = getString(R.string.poor_internet_connection);
        }
        controlsFragment = null;
        unregisterNetworkReceiver();
        finishAndRemoveTask();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        closeConfirmDialog();
    }

    public void hideControls() {
        if (controlsFragment != null) {
            controlsFragment.hideDialogs();
        }
    }


    private void initPowerOptions() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG_CPU_WAKELOCK);
        wakeLock.acquire(600000);
        isScreenOn = powerManager.isInteractive();
    }

    public void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String type = jsonObject.getString(Constants.TAG_TYPE);
            if (type.equals(Constants.TAG_LISTEN_USER)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (jsonObject.getString(Constants.TAG_MSG_TYPE).equals(Constants.ONLINE)) {
                                txtUserAway.setVisibility(View.GONE);
                            } else if (jsonObject.getString(Constants.TAG_MSG_TYPE).equals(Constants.OFFLINE)) {
                                txtUserAway.setVisibility(View.VISIBLE);
                                txtUserAway.setText(userName + " " + getString(R.string.away_from_chat));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (type.equals(Constants.TAG_CALL_REJECTED)) {
                txtMessage.setText("");
                txtUserAway.setVisibility(View.VISIBLE);
                txtUserAway.setText(R.string.mutual_followers_can_make_a_call);
                isMutualFollow = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onCallHangUp();
                    }
                }, 2000);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    public void turnOnScreen() {
        releaseWakeLock();
        if (!isScreenOn) {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true);
                setTurnScreenOn(true);

            }
            isScreenOn = true;
        }
    }

    private void startAudioManager() {
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    public void stopAudioManager() {
        if (audioManager != null) {
            /*Reset audio manager to previous state*/
            audioManager.stop();
            audioManager = null;
        }
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
    }

    private void setMirror(boolean isSwappedFeeds) {
        if (isSwappedFeeds) {
            fullscreenRenderer.setMirror(true);
            pipRenderer.setMirror(false);
        } else {
            fullscreenRenderer.setMirror(false);
            pipRenderer.setMirror(true);
        }
    }

    private void updateVideoViews(final boolean remoteVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pipRenderer != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) pipRenderer.getLayoutParams();
                    ViewGroup.MarginLayoutParams tempParams = (ViewGroup.MarginLayoutParams) pipRenderer.getLayoutParams();
                    if (remoteVisible && params != null) {
                        params.width = getDisplayMetrics().widthPixels * 30 / 100;
                        params.height = getDisplayMetrics().heightPixels * 20 / 100;
                        tempParams.width = (getDisplayMetrics().widthPixels * 30 / 100) - 10;
                        tempParams.height = (getDisplayMetrics().heightPixels * 20 / 100) - 10;
                        pipRenderer.setLayoutParams(params);
                        pipBlurView.setLayoutParams(tempParams);
                        pipRenderer.setVisibility(View.VISIBLE);
                    }
                }
            }

        });

    }

    private void startCountDown(String callType) {

        long time;

        if (callType.equals("answer")) {
            Log.v(TAG, "startCountDown=" + callType);
            long startTime = SystemClock.elapsedRealtime();
            callTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    //Function call every second
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(TAG, "onChronometerTick");
                            long hours = (((SystemClock.elapsedRealtime() - startTime) / 1000) / 60) / 60;
                            long minutes = ((SystemClock.elapsedRealtime() - startTime) / 1000) / 60;
                            long seconds = ((SystemClock.elapsedRealtime() - startTime) / 1000) % 60;
                            txtcalltime.setText(twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds));
                        }
                    });
                }
            }, 0, 1000);
        }
        else {
            if (callType.equals("waiting")) {
                time = 30000;
            } else {
                time = 15000;
            }
            countDownTimer = new CountDownTimer(time, 1000) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long millisUntilFinished) {
                    if (!NetworkReceiver.isConnected()) {
                        finish();
                    } else if (isCallConnected && isCallAttend) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                            Log.v(TAG, "countDownTimer=cancel");
                        }
                    }
                }

                @Override
                public void onFinish() {
                    Log.v(TAG, "countDownTimer=Ended");
                    if (isCallConnected && isCallAttend) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                            stopAudioManager();
                            Log.v(TAG, "countDownTimer=cancel");
                        }
                    } else {
                        finish();
                    }
                }
            };

            countDownTimer.start();
        }
    }
    private String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        } else if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private @Nullable
    VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        String videoFileAsCamera = intent.getStringExtra(AppRTCUtils.EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                return null;
            }
        } else if (screencaptureEnabled) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(VideoCallActivity.this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        return videoCapturer;
    }

    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {

        txtMessage.setVisibility(View.VISIBLE);
        signalingParameters = params;
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(
                localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                isPeerReceived = true;
                if (!from.equals(Constants.TAG_RECEIVE)) {
                    peerConnectionClient.createAnswer();
                }
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                if (toneGenerator != null) {
                    toneGenerator.stopTone();
                    toneGenerator = null;
                }
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                isPeerReceived = true;
                isCallConnected = true;
                peerConnectionClient.setRemoteDescription(sdp);
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    com.app.binggbongg.utils.Logging.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        if (peerConnectionClient == null) {
            com.app.binggbongg.utils.Logging.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
            return;
        }
        peerConnectionClient.removeRemoteIceCandidates(candidates);
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(String description) {
        reportError(description);
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isCallAttend = true;
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                txtMessage.setVisibility(View.GONE);
                if (from.equals(Constants.TAG_RECEIVE)) {
                    if (controlsFragment != null) {
                        controlsFragment.setCallUI();
                    }
                }
                connected = true;

                if (chatType.equals(Constants.TAG_AUDIO)) {
                    txtcalltime.setVisibility(View.VISIBLE);
                    startCountDown("answer");
                } else {
                    updateVideoViews(chatType.equals(Constants.TAG_VIDEO));
                }
               // updateVideoViews(chatType.equals(Constants.TAG_VIDEO));
                outGoingLay.setVisibility(View.GONE);
                if (from.equals(Constants.TAG_RECEIVE)) {
                    startAudioManager();
                    setAudioOutputConfigs();
                }
                callConnected();
            }

        });
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, AppRTCUtils.STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false /* isSwappedFeeds */);
        setMirror(false);
        if (chatType.equals(Constants.TAG_AUDIO)) {
            incomingLay.setVisibility(View.VISIBLE);
            startCountDown("answer");
        }
        if (isSender) {
            chargeCalls();
        }
    }

    private void chargeCalls() {
        Call<HashMap<String, String>> call = apiInterface.chargeCalls(GetSet.getUserId());
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {

            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && connected) {

                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    @Override
    public void onIceClosed() {

    }

    @Override
    public void onClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (platform != null && platform.equals("ios")) {
                    connected = false;
                    disconnect();
                } else {
                    connected = false;
                    disconnect();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppRTCUtils.CALL_PERMISSIONS_REQUEST_CODE) {

            boolean isPermissionEnabled = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = false;
                    break;
                } else {
                    isPermissionEnabled = true;
                }
            }

            if (!isPermissionEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i(TAG, "setAudioOutputConfigs: " + "received");
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                            shouldShowRequestPermissionRationale(WAKE_LOCK)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK}, AppRTCUtils.CALL_PERMISSIONS_REQUEST_CODE);
                    } else {
                        App.makeToast(getString(R.string.camera_microphone_permission_error));
                        disconnect();
                    }
                }
            } else {
                Log.i(TAG, "setAudioOutputConfigs2: " + "received");

                initRender();
            }
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(VideoCallActivity.this, permissions, requestCode);
    }

    public void setWaiting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelCallEmitTimer();
                startCountDown("waiting");
            }
        });
    }

    // CallFragment.OnCallEvents interface implementation.
    @Override
    public void onBlurClicked(boolean isBlurred) {
        this.isBlurred = isBlurred;
        if (isBlurred) {
            if (remoteProxyRenderer.target != null && remoteProxyRenderer.target == fullscreenRenderer) {
                fullscreenRenderer.addFrameListener(new EglRenderer.FrameListener() {
                    @Override
                    public void onFrame(Bitmap bitmap) {
                        if (bitmap != null) {
                            blurredBitMap = bitmap;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fullScreenBlurView.setVisibility(View.VISIBLE);
                                    pipBlurView.setVisibility(View.GONE);
                                    fullScreenBlurView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }
                }, 0.5f);
            } else if (remoteProxyRenderer.target != null && remoteProxyRenderer.target == pipRenderer) {
                pipRenderer.addFrameListener(new EglRenderer.FrameListener() {
                    @Override
                    public void onFrame(Bitmap bitmap) {
                        if (bitmap != null) {
                            blurredBitMap = bitmap;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fullScreenBlurView.setVisibility(View.GONE);
                                    pipBlurView.setVisibility(View.VISIBLE);
                                    pipBlurView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }
                }, 0.5f);
            }
        } else {
            fullScreenBlurView.setVisibility(View.GONE);
            pipBlurView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onCallHangUp() {
        stopAudioManager();
        if (isCallAttend) {
            toastText = getString(R.string.call_ended);
        } else {
            toastText = getString(R.string.call_cancelled);
        }
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        switchCamera();
    }

    private void switchCamera() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
            isFrontCamera = !isFrontCamera;
            if (!isFrontCamera) {
                fullscreenRenderer.setMirror(false);
                pipRenderer.setMirror(false);
            } else {
                setMirror(false);
            }
        }
    }

   /* @Override
    public boolean onToggleMic(boolean isMicEnabled) {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(isMicEnabled);
        }
        return isMicEnabled;
    }*/
   @Override
   public boolean onToggleMic(boolean isMicEnabled) {
       if (peerConnectionClient != null) {
           if(chatType.equals(Constants.TAG_AUDIO)) {
               micEnabled = !micEnabled;
           }
           peerConnectionClient.setAudioEnabled(isMicEnabled);
       }
       return isMicEnabled;
   }
    @Override
    public boolean onToggleSpeaker() {
        return setSpeaker(!speaker);
    }
    @Override
    public void onStickerSend(String stickerId) {

    }

    @Override
    public void onGiftSend(Gift gift) {

    }

    @Override
    public void onReportSend(String report) {

    }
    public boolean setSpeaker(boolean speakerOn) {
        if (speakerOn) {
            speaker = true;
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            speaker = false;
            if (audioManager.hasWiredHeadset()) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
        return speaker;
    }
    public void setSwappedBlurFeeds(boolean isBlurred) {
        this.isBlurred = isBlurred;
        if (isBlurred) {
            if (remoteProxyRenderer.target == fullscreenRenderer) {
                if (blurredBitMap != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fullScreenBlurView.setVisibility(View.VISIBLE);
                            pipBlurView.setVisibility(View.GONE);
                            fullScreenBlurView.setImageBitmap(blurredBitMap);
                        }
                    });
                }
            } else if (remoteProxyRenderer.target == pipRenderer) {
                if (blurredBitMap != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fullScreenBlurView.setVisibility(View.GONE);
                            pipBlurView.setVisibility(View.VISIBLE);
                            pipBlurView.setImageBitmap(blurredBitMap);
                        }
                    });
                }
            }
        } else {
            fullScreenBlurView.setVisibility(View.GONE);
            pipBlurView.setVisibility(View.GONE);
        }
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!connected || !controlsFragment.isAdded()) {
            return;
        }

        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction callTransaction = getSupportFragmentManager().beginTransaction();
        callTransaction.setCustomAnimations(R.anim.anim_slide_left_in, R.anim.anim_slide_right_out);
//        userTransaction.setCustomAnimations(R.anim.anim_slide_up, R.anim.anim_slide_down);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);
        if (callControlFragmentVisible) {
            callTransaction.show(controlsFragment);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    callAcceptLay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            callAcceptLay.startAnimation(slideUp);
        } else {
            callTransaction.hide(controlsFragment);
            slideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    callAcceptLay.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            callAcceptLay.startAnimation(slideDown);
        }
        callTransaction.commit();
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    public void disconnect() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(Constants.CALL_TAG, 0);
        }
        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        activityRunning = false;

        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }

        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (callTimer != null) {
            callTimer.cancel();
            callTimer = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
        cancelCallEmitTimer();
        finishAndRemoveTask();
        finish();
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return flags;
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if (!isConnected) {
            disconnect();
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void closeConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoCallActivity.this);
        builder.setMessage(getString(R.string.really_want_exit));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                disconnect();
                overridePendingTransition(0, 0);
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
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


    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(getString(R.string.okay),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect();
                                }
                            })
                    .create()
                    .show();
        }
    }
}
