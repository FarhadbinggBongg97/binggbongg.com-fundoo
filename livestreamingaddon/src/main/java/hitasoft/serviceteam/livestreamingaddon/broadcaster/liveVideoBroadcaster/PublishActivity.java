package hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import hitasoft.serviceteam.livestreamingaddon.PlayerActivity;
import hitasoft.serviceteam.livestreamingaddon.R;
import hitasoft.serviceteam.livestreamingaddon.broadcaster.ILiveVideoBroadcaster;
import hitasoft.serviceteam.livestreamingaddon.broadcaster.LiveVideoBroadcaster;
import hitasoft.serviceteam.livestreamingaddon.external.avloading.AVLoadingIndicatorView2;
import hitasoft.serviceteam.livestreamingaddon.external.avloading.BallBeatIndicator;
import hitasoft.serviceteam.livestreamingaddon.external.heartlayout.HeartLayout;
import hitasoft.serviceteam.livestreamingaddon.external.helper.CustomLayoutManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.LocaleManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.NetworkReceiver;
import hitasoft.serviceteam.livestreamingaddon.external.helper.SocketIO;
import hitasoft.serviceteam.livestreamingaddon.external.helper.Utils;
import hitasoft.serviceteam.livestreamingaddon.external.model.StartStreamResponse;
import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiClient;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiInterface;
import hitasoft.serviceteam.livestreamingaddon.external.utils.AppUtils;
import hitasoft.serviceteam.livestreamingaddon.external.utils.Constants;
import hitasoft.serviceteam.livestreamingaddon.external.utils.GetSet;
import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.SubscribeActivity;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PublishActivity extends AppCompatActivity implements SocketIO.SocketEvents,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, View.OnClickListener {

    private static final String TAG = PublishActivity.class.getSimpleName();

    ImageView btnClose;
    TextView publisherNameTV, txtTime, lftVoteCount;
    private GLSurfaceView mGLView;
    private ConstraintLayout publishLay;
    private EditText edtTitle, edtLinkVideo;
    private RelativeLayout tipsLay;
    private LinearLayout loadingTipsLay;
    private TextView txtTips;
    private ImageView btnCancelTips, btnCancel;
    private Button btnGoLive;
    private BottomSheetDialog bottomCameraDialog;
    private LinearLayout frontCameraLay;
    private LinearLayout backCameraLay;
    private RelativeLayout bottomLay;
    private RelativeLayout commentsLay;
    private RecyclerView rvComments;
    private HeartLayout heartLay;
    private LinearLayout viewersLay;
    private RelativeLayout viewLay;
    private ImageView iconView;
    private AppCompatTextView txtViewCount;
    private RecyclerView rvViewers;
    private LinearLayout stopLay;
    private Button btnStopBroadCast;
    private RoundedImageView btnDetail;
    private RelativeLayout loadingLay;
    private ImageView loadingImage;
    private LinearLayout initializeLay;
    private AVLoadingIndicatorView2 avBallIndicator;
    private AppCompatTextView txtLiveCount;
    private RelativeLayout liveCountLay;
    private FrameLayout liveTxtLay;
    private View transparentCover;
    RelativeLayout bottomDurationLay;
    AppCompatTextView bottomStreamTitle;
    RoundedImageView publisherImage;
    RoundedImageView publisherColorImage;
    AppCompatTextView txtPublisherName;
    LinearLayout bottomFirstLay, bottomDeleteLay, bottomReportLay, chatHideLay, bottomDetailsLay,
            bottomUserLay, bottomTopLay, bottomViewersLay, bottomGiftsLay;
    AppCompatTextView bottomViewerCount, txtBottomDuration, bottomGiftCount;
    RecyclerView bottomRecyclerView, bottomGiftView;
    private BottomSheetDialog bottomDialog, bottomCommentsDialog, bottomGiftsDialog, bottomHashTagsDialog;
    private View bottomSheet;

    private static final long FADE_DURATION = 1000;
    private Intent mLiveVideoBroadcasterServiceIntent;
    private ILiveVideoBroadcaster mLiveVideoBroadcaster;
    private Animation fadeOut, shake;
    // Gestures are used to toggle the focus modes
    protected GestureDetectorCompat mAutoFocusDetector = null;
    private boolean mIsRecording = false, isPublished = false;
    private ApiInterface apiInterface;
    private String title, streamName, getVideoId, streamToken = null, attachedLink;
    private final Handler handler = new Handler();
    private int resultCode = RESULT_CANCELED;

    ArrayList<StreamDetails.LiveViewers> viewersList = new ArrayList<>();
    ArrayList<HashMap<String, String>> commentList = new ArrayList<HashMap<String, String>>();
    ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
    private ViewerListViewAdapter viewerListAdapter;
    private BottomListAdapter bottomListAdapter;
    private CommentsAdapter commentsAdapter;
    private GiftsAdapter giftsAdapter;
    private String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };

    private final String[] mRequiredPermissions12 = new String[]{
            CAMERA,
            RECORD_AUDIO
    };
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private SocketIO socketIO;
    private Socket mSocket;
    private int displayHeight, displayWidth;
    private final boolean isStreamStarted = false;
    private final Handler serviceHandler = new Handler();
    private final Runnable bindServiceRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "run: bindServiceRunnable");
                checkServiceIsConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private GestureDetector gestureScanner;
    Utils appUtils;
    //    private DatabaseHandler dbHelper;
    private static final long SECONDS_INTERVAL = 1000;
    private static final long SCREEN_UPLOAD_INTERVAL = 10000;
    private boolean isCameraOpened = false;
    private int lensFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SharedPreferences pref;
    private Context context;
    ProgressBar progress_bar;
    private final Runnable imageUpload = new Runnable() {
        @Override
        public void run() {
            try {
                takeScreenShot();
                handler.postDelayed(imageUpload, SCREEN_UPLOAD_INTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "onConnect: " + "EVENT_CONNECTED");
        }
    };

    private long startTime;
    private Timer timer = new Timer();

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startTime += SECONDS_INTERVAL;

                    Log.e(TAG, "run: ::::::::::::::::::" + startTime);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(Constants.TAG_TYPE, StreamConstants.TAG_DURATION);
                        jsonObject.put(StreamConstants.TAG_TIME, appUtils.getCommentsDuration(startTime));
                        jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
                        if (mSocket != null && mSocket.connected()) {
                            mSocket.emit(StreamConstants.TAG_SEND_MESSAGE, jsonObject);
                        }
                        Log.d(TAG, "TimerData" + jsonObject);
                        txtTime.setText(appUtils.getCommentsDuration(startTime));
                    } catch (JSONException e) {
                        Log.e(TAG, "timerTask: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    private final Emitter.Listener _msgReceived = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, "msgReceived: Lisener" + data);
                    String type = data.optString(Constants.TAG_TYPE);
                    switch (type) {
                        case StreamConstants.TAG_STREAM_JOINED:
                            if (data.optString(StreamConstants.TAG_STREAM_NAME).equals(streamName)) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                                map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                                map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                                map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                                map.put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE));
                                addComment(map);
                                getStreamInfo(streamName);
                            }
                            break;
                        case Constants.TAG_TEXT: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_TYPE, Constants.TAG_TEXT);
                            addComment(map);
                            break;
                        }
                        case StreamConstants.TAG_LIKED: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(Constants.TAG_MESSAGE, data.optString(Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_TYPE, Constants.TAG_MESSAGE);
                            try {
                                String userId = data.optString(Constants.TAG_USER_ID);
                                String likeColor = data.optString(StreamConstants.TAG_LIKE_COLOR);
                                heartLay.addHeart(Color.parseColor(likeColor));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case Constants.TAG_GIFT: {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID));
                            map.put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME));
                            map.put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE));
                            map.put(StreamConstants.TAG_STREAM_NAME, data.optString(StreamConstants.TAG_STREAM_NAME));
                            map.put(Constants.TAG_GIFT_TITLE, data.optString(Constants.TAG_GIFT_TITLE));
                            map.put(Constants.TAG_GIFT_ICON, data.optString(Constants.TAG_GIFT_ICON));
                            map.put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE));
                            addComment(map);
                            lftVoteCount.setText(data.optString("lifetime_vote_count") != null ?
                                    data.optString("lifetime_vote_count") : "0");
                            getStreamInfo(streamName);
                            break;
                        }
                    }
                }
            });
        }
    };
    private final Emitter.Listener _subscriberLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(TAG, "_subscriberLeft: " + data);
                    getStreamInfo(data.optString(StreamConstants.TAG_STREAM_NAME));
                }
            });
        }
    };
    private final Emitter.Listener _streamInfo = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamInfo: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    StreamDetails tempDetails = gson.fromJson("" + data, StreamDetails.class);
                    txtViewCount.setText(tempDetails.getWatchCount() != null ? tempDetails.getWatchCount() : "");
                    lftVoteCount.setText(tempDetails.getLftVoteCount() != null ? tempDetails.getLftVoteCount() : "0");
                    viewersList.clear();
                    viewersList.addAll(tempDetails.getLiveViewers());
                    viewerListAdapter.notifyDataSetChanged();

                    giftsList.clear();
                    giftsList.addAll(tempDetails.getGiftList());
                    bottomGiftCount.setText("" + giftsList.size());
                    giftsAdapter.notifyDataSetChanged();
                    viewerListAdapter.notifyDataSetChanged();
                    setBottomDialogUI(tempDetails);
                }
            });
        }
    };
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
            if (mLiveVideoBroadcaster == null) {
                mLiveVideoBroadcaster = binder.getService();
                mLiveVideoBroadcaster.init(PublishActivity.this, mGLView);
                mLiveVideoBroadcaster.setAdaptiveStreaming(true);
            }
            mLiveVideoBroadcaster.openCamera(lensFacing);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCameraOpened = true;
                }
            }, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLiveVideoBroadcaster = null;
        }
    };
    private boolean isBroadCastStopped = false;
    private final Emitter.Listener _streamBlocked = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "_streamBlocked: " + data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PublishActivity.this, getString(R.string.your_broadcast_deactivated_by_admin), Toast.LENGTH_SHORT).show();
                    triggerStopRecording();
                }
            });
        }
    };
    private String getstreamUrl;

    private void setBottomDialogUI(StreamDetails details) {
        bottomListAdapter.notifyDataSetChanged();
        bottomStreamTitle.setText(details.getTitle());
        bottomViewerCount.setText(details.getWatchCount());
        txtLiveCount.setText(details.getWatchCount());
    }

    private void addComment(HashMap<String, String> map) {
        commentList.add(map);
        commentsAdapter.notifyItemInserted(commentList.size() - 1);
        rvComments.scrollToPosition(commentList.size() - 1);
    }

    private void publishStream() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket != null && mSocket.connected()) {
                Log.i(TAG, "publishStream: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_PUBLISH_STREAM, jsonObject);
            }

        } catch (JSONException e) {
            Log.e(TAG, "publishStream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopStreamSocket() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_TYPE, "streamend");
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket != null && mSocket.connected()) {
                Log.i(TAG, "stopStreamSocket: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_STOP_STREAM, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "stopStreamSocket error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*Socket: stopstream
    {
        "type": "streamend",
            "user_id": "602bd7ad7e9169fb60edff0d",
            "user_name": "RaJ",
            "user_image": "https:\/\/fundoo.appkodes.in\/public\/img\/accounts\/profile_image-1613486047703",
            "stream_name": "Video_sT69vGvNynpX",
    }*/

    private void getStreamInfo(String streamName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(StreamConstants.TAG_STREAM_NAME, streamName);
            if (mSocket.connected()) {
                Log.i(TAG, "getStreamInfo: " + jsonObject);
                mSocket.emit(StreamConstants.TAG_GET_STREAM_INFO, jsonObject);
            }
        } catch (JSONException e) {
            Log.e(TAG, "getStreamInfo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean mPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        overridePendingTransition(0, 0);
        context = this;
        Intent intent = getIntent();

        GetSet.setUserId(getIntent().getStringExtra(StreamConstants.TAG_USER_ID));
        GetSet.setUserName(getIntent().getStringExtra(StreamConstants.TAG_USER_NAME));
        GetSet.setToken(getIntent().getStringExtra(StreamConstants.TAG_TOKEN));
        GetSet.setImageUrl(getIntent().getStringExtra(StreamConstants.TAG_USER_IMAGE));
        getstreamUrl = getIntent().getStringExtra(StreamConstants.TAG_STREAM_BASE_URL);
        GetSet.setStream_base_url(getstreamUrl);

        Log.e(TAG, "onCreate: ::::::::::" + getstreamUrl);

        title = intent.getStringExtra(StreamConstants.TAG_TITLE);
        streamName = intent.getStringExtra(StreamConstants.TAG_STREAM_NAME);
        streamToken = intent.getStringExtra(StreamConstants.TAG_STREAM_TOKEN);
        lensFacing = intent.getIntExtra(StreamConstants.TAG_LENS_FACING, lensFacing);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out_fast);
        shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        //binding on resume not to having leaked service connection
        mLiveVideoBroadcasterServiceIntent = new Intent(this, LiveVideoBroadcaster.class);
        //this makes service do its job until done
        startService(mLiveVideoBroadcasterServiceIntent);
        setContentView(R.layout.activity_publish);

        pref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Constants.isInStream = true;
        appUtils = new Utils(this);
//        dbHelper = DatabaseHandler.getInstance(this);
        gestureScanner = new GestureDetector(this, this);

        findViews();
        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL activity.
        if (mGLView != null) {
            mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mRequiredPermissions = mRequiredPermissions12;
        }

        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

        // return if the user hasn't granted the app the necessary permissions
        if (!mPermissionsGranted) return;

        initView();
        initSocket();
//        checkServiceIsConnected();
    }

    /*@Override
    public void onNetworkChange(boolean isConnected) {
        if (!isConnected) {
            finish();
        }
    }*/

    private void checkServiceIsConnected() {
        if (isCameraOpened) {
            Log.d(TAG, "checkServiceIsConnected: amera");
            startPublish();
        } else {
            Log.d(TAG, "checkServiceIsConnected: else");
            serviceHandler.postDelayed(bindServiceRunnable, 1000);
        }
    }

    public void findViews() {

        Log.e(TAG, "findViews: :::::::::");
        btnClose = findViewById(R.id.btnClose);
        publisherNameTV = findViewById(R.id.publisherNameTV);
        txtTime = findViewById(R.id.txtTime);
        mGLView = findViewById(R.id.cameraPreview_surfaceView);
        publishLay = findViewById(R.id.publishLay);
        edtTitle = findViewById(R.id.edtTitle);
        edtLinkVideo = findViewById(R.id.edtLinkVideo);
        loadingTipsLay = findViewById(R.id.loadingTipsLay);
        tipsLay = findViewById(R.id.tipsLay);
        txtTips = findViewById(R.id.txtTips);
        btnCancelTips = findViewById(R.id.btnCancelTips);
        btnCancel = findViewById(R.id.btnCancel);

        btnGoLive = findViewById(R.id.btnGoLive);
        bottomLay = findViewById(R.id.bottomLay);
        commentsLay = findViewById(R.id.commentsLay);
        rvComments = findViewById(R.id.rv_comments);
        heartLay = findViewById(R.id.heartLay);
        viewersLay = findViewById(R.id.viewersLay);
        viewLay = findViewById(R.id.viewLay);
        iconView = findViewById(R.id.iconView);
        txtViewCount = findViewById(R.id.txtViewCount);
        rvViewers = findViewById(R.id.rv_viewers);
        stopLay = findViewById(R.id.stopLay);
        btnStopBroadCast = findViewById(R.id.btnStopBroadCast);
        btnDetail = findViewById(R.id.btnDetail);
        loadingLay = findViewById(R.id.loadingLay);
        loadingImage = findViewById(R.id.loadingImage);
        initializeLay = findViewById(R.id.initializeLay);
        avBallIndicator = findViewById(R.id.avBallIndicator);
        transparentCover = findViewById(R.id.transparent_cover);
        progress_bar = findViewById(R.id.progress_bar);
        lftVoteCount = findViewById(R.id.tv_lftVoteCount);

        avBallIndicator.setIndicator(new BallBeatIndicator());
        btnStopBroadCast.setOnClickListener(this);
        btnDetail.setOnClickListener(this);
        btnGoLive.setOnClickListener(this);
        tipsLay.setOnClickListener(this);
        viewLay.setOnClickListener(this);
        btnCancelTips.setOnClickListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                System.exit(0);
            }
        });
    }


    public void initView() {
        // Initialize the UI controls
        if (LocaleManager.isRTL()) {
            viewLay.setBackground(getResources().getDrawable(R.drawable.rounded_curve_rtl_bg));
        } else {
            viewLay.setBackground(getResources().getDrawable(R.drawable.rounded_curve_bg));
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.viewersLay);
        params.height = (int) (displayHeight * 0.4);
        commentsLay.setLayoutParams(params);
        // setLoadingLay(VISIBLE);
        Glide.with(this)
                .load(GetSet.getImageUrl())
                .placeholder(R.drawable.profile_square)
                .error(R.drawable.profile_square)
                .thumbnail(0.1f)
                .into(loadingImage);

        publisherNameTV.setText(GetSet.getUserName() + "");

        btnClose.setOnClickListener(v -> {
            //closeConfirmDialog(getString(R.string.stop_publish_description));
            handler.removeCallbacks(imageUpload);
            isPublished = false;
            triggerStopRecording();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvViewers.setLayoutManager(linearLayoutManager);
        viewerListAdapter = new ViewerListViewAdapter(this, viewersList);
        rvViewers.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvViewers.setAdapter(viewerListAdapter);
        rvViewers.setHasFixedSize(true);
        viewerListAdapter.notifyDataSetChanged();

        commentsAdapter = new CommentsAdapter(this, commentList);
        CustomLayoutManager commentLayoutManager = new CustomLayoutManager(this);
        commentLayoutManager.setScrollEnabled(false);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.setAdapter(commentsAdapter);
        commentsAdapter.notifyDataSetChanged();

        transparentCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureScanner.onTouchEvent(event);
            }
        });
        initBottomDetailsDialog();
        initBottomViewersDialog();
        initBottomGiftDialog();
    }

    private void initBottomDetailsDialog() {
        bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_details, null);
        bottomDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomDialog.setContentView(bottomSheet);
        bottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        bottomFirstLay = bottomSheet.findViewById(R.id.bottomFirstLay);
        bottomDeleteLay = bottomSheet.findViewById(R.id.bottomDeleteLay);
        liveCountLay = bottomSheet.findViewById(R.id.liveCountLay);
        liveTxtLay = bottomSheet.findViewById(R.id.liveTxtLay);
        txtLiveCount = bottomSheet.findViewById(R.id.txtLiveCount);
        bottomStreamTitle = bottomSheet.findViewById(R.id.bottomStreamTitle);
        bottomUserLay = bottomSheet.findViewById(R.id.bottomUserLay);
        publisherImage = bottomSheet.findViewById(R.id.publisherImage);
        publisherColorImage = bottomSheet.findViewById(R.id.publisherColorImage);
        txtPublisherName = bottomSheet.findViewById(R.id.txtPublisherName);
        bottomDetailsLay = bottomSheet.findViewById(R.id.bottomDetailsLay);
        chatHideLay = bottomSheet.findViewById(R.id.chatHideLay);
        bottomReportLay = bottomSheet.findViewById(R.id.bottomReportLay);

        if (LocaleManager.isRTL()) {
            liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg_rtl));
            liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg_rtl));
        } else {
            liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg));
            liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg));
        }

        bottomStreamTitle.setText(title);
        bottomDeleteLay.setVisibility(GONE);
        bottomReportLay.setVisibility(GONE);
        chatHideLay.setVisibility(VISIBLE);
        bottomUserLay.setVisibility(GONE);

        bottomDetailsLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing()) {
                    bottomCommentsDialog.show();
                }
            }
        });

        chatHideLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (bottomGiftsDialog != null && !bottomGiftsDialog.isShowing()) {
                    bottomGiftsDialog.show();
                }
            }
        });

        bottomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void initBottomViewersDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_viewers, null);
        bottomCommentsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomCommentsDialog.setContentView(bottomSheet);

        bottomViewersLay = bottomSheet.findViewById(R.id.bottomViewersLay);
        bottomViewerCount = bottomSheet.findViewById(R.id.bottomViewerCount);
        bottomRecyclerView = bottomSheet.findViewById(R.id.bottomRecyclerView);
        bottomTopLay = bottomSheet.findViewById(R.id.bottomTopLay);
        txtBottomDuration = bottomSheet.findViewById(R.id.txtBottomDuration);
        bottomDurationLay = bottomSheet.findViewById(R.id.bottomDurationLay);

        bottomDurationLay.setVisibility(GONE);

        bottomListAdapter = new BottomListAdapter(this, viewersList);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomRecyclerView.setAdapter(bottomListAdapter);
        bottomRecyclerView.setHasFixedSize(true);
        bottomListAdapter.notifyDataSetChanged();

        bottomCommentsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomCommentsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void initBottomGiftDialog() {
        View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_gifts, null);
        bottomGiftsDialog = new BottomSheetDialog(this, R.style.Bottom_StreamDialog); // Style here
        bottomGiftsDialog.setContentView(bottomSheet);

        bottomGiftsLay = bottomSheet.findViewById(R.id.bottomGiftsLay);
        bottomGiftCount = bottomSheet.findViewById(R.id.bottomGiftCount);
        bottomGiftView = bottomSheet.findViewById(R.id.bottomGiftView);

        giftsAdapter = new GiftsAdapter(this, giftsList);
        bottomGiftView.setLayoutManager(new LinearLayoutManager(this));
        bottomGiftView.setAdapter(giftsAdapter);
        bottomGiftView.setHasFixedSize(true);
        giftsAdapter.notifyDataSetChanged();

        bottomGiftsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });

        bottomGiftsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
    }

    private void initSocket() {
        socketIO = new SocketIO(PublishActivity.this);
        mSocket = socketIO.getInstance();
        socketIO.setSocketEvents(PublishActivity.this);

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        mSocket.on(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
        mSocket.on(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
        mSocket.on(StreamConstants.TAG_STREAM_INFO, _streamInfo);
        mSocket.on(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        mSocket.connect();
    }

    private void startPublish() {
        if (!mIsRecording) {
            if (mLiveVideoBroadcaster != null) {
                if (!mLiveVideoBroadcaster.isConnected()) {
                    String builder = pref.getString(StreamConstants.TAG_STREAM_BASE_URL, StreamConstants.RTMP_URL)
                            + streamName + "?" + StreamConstants.TAG_TOKEN + "=" + (streamToken != null ? streamToken : "");
                    Log.d(TAG, "startPublish builder: " + builder);
                    Log.d(TAG, "streamName=> " + streamName);

                    new PublishStreamTask().execute(builder);
                } else {
                    Toast.makeText(PublishActivity.this, getString(R.string.streaming_not_finished), Toast.LENGTH_SHORT).show();
                    //makeToast(getString(R.string.streaming_not_finished));
                    finish();
                }
            } else {
                Toast.makeText(PublishActivity.this, getString(R.string.oopps_shouldnt_happen), Toast.LENGTH_SHORT).show();
                //makeToast(getString(R.string.oopps_shouldnt_happen));
                finish();
            }
        } else {
            triggerStopRecording();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class PublishStreamTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPreExecute() {
            setLoadingLay(VISIBLE);
        }


        @Override
        protected Boolean doInBackground(String... url) {
            Log.i(TAG, "doInBackground: " + url[0]);
            return mLiveVideoBroadcaster.startBroadcasting(url[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.i(TAG, "onPostExecute: " + result);
            mIsRecording = result;
            if (result) {
                isPublished = true; // Changed - Once stream live after that back button pressed
                serviceHandler.removeCallbacks(bindServiceRunnable);
                setLoadingLay(GONE);
                resultCode = RESULT_OK;
                initStream();
            } else {
                //makeToast(getString(R.string.something_wrong));
                Toast.makeText(PublishActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                triggerStopRecording();
            }
        }
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void triggerStopRecording() {
        if (mIsRecording) {
            Log.d(TAG, "triggerStopRecording: ");
            mLiveVideoBroadcaster.stopBroadcasting();
            stopTimer();
            stopStream();
            serviceHandler.removeCallbacks(bindServiceRunnable);
        }

    }

    private void setLoadingLay(final int visiblity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (visiblity == VISIBLE) {
                    avBallIndicator.show();
                } else {
                    avBallIndicator.hide();
                }
                loadingLay.setVisibility(visiblity);
                initializeLay.setVisibility(visiblity);
            }
        });
    }

    private void setPublishLayVisible(final int visiblity) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (visiblity == VISIBLE) {
                    publishLay.setVisibility(visiblity);
                    bottomLay.setVisibility(GONE);
                } else {
                    publishLay.setVisibility(visiblity);
                    bottomLay.setVisibility(VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //this lets activity bind
        bindService(mLiveVideoBroadcasterServiceIntent, mConnection, 0);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);

    }

    private void initStream() {
        startStream();
        publishStream();
        startTime = 0;
        if (timer != null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, 0, SECONDS_INTERVAL);
        }
    }

    @Override
    public void onBackPressed() {
        if (isPublished) {
            // closeConfirmDialog(getString(R.string.stop_publish_description));
            handler.removeCallbacks(imageUpload);
            isPublished = false;
            triggerStopRecording();
        } else {
            finish();
        }
    }

    /**
     * Android Activity lifecycle methods
     */
    @Override
    protected void onResume() {
        super.onResume();
        // If running on Android 6 (Marshmallow) or above, check to see if the necessary permissions
        // have been granted
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mRequiredPermissions = mRequiredPermissions12;
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;*/

        if (socketIO != null) {
            socketIO.setSocketEvents(PublishActivity.this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLiveVideoBroadcaster != null) {
            mLiveVideoBroadcaster.pause();
        }
        if (socketIO != null) {
            socketIO.setSocketEvents(null);
        }
        finish();
        unbindService(mConnection);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLiveVideoBroadcaster.setDisplayOrientation();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        handler.removeCallbacks(imageUpload);
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
            mSocket.off(StreamConstants.TAG_MESSAGE_RECEIVED, _msgReceived);
            mSocket.off(StreamConstants.TAG_SUBSCRIBER_LEFT, _subscriberLeft);
            mSocket.off(StreamConstants.TAG_STREAM_INFO, _streamInfo);
            mSocket.off(StreamConstants.TAG_STREAM_BLOCKED, _streamBlocked);
        }
        Constants.isInStream = false;
    }

    private boolean getIsRecording() {
        return mIsRecording || loadingLay.getVisibility() == VISIBLE;
    }

    /**
     * Click handler for the switch camera button
     */
    public void onSwitchCamera() {
        if (mLiveVideoBroadcaster != null) {
            mLiveVideoBroadcaster.changeCamera();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAutoFocusDetector != null)
            mAutoFocusDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    public void startStream() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put(StreamConstants.TAG_PUBLISHER_ID, GetSet.getUserId());
            map.put(StreamConstants.TAG_TITLE, title);
            map.put(StreamConstants.TAG_LINK_URL, attachedLink);
            map.put(StreamConstants.TAG_NAME, streamName);
            map.put(StreamConstants.TAG_RECORDING, "" + true);

            Log.i(TAG, "startStream: " + new Gson().toJson(map));
            Call<StartStreamResponse> call3 = apiInterface.startStream(GetSet.getToken(), map);
            call3.enqueue(new Callback<StartStreamResponse>() {
                @Override
                public void onResponse(Call<StartStreamResponse> call, Response<StartStreamResponse> response) {
                    if (response.isSuccessful()) {
                        StartStreamResponse streamResponse = response.body();
                        if (streamResponse.getStatus().equals(Constants.TAG_TRUE)) {
                            hideProgress();
                            handler.post(imageUpload);
                        } else {
                            Toast.makeText(PublishActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                            //makeToast(getString(R.string.something_wrong));
                            triggerStopRecording();
                        }
                    }
                }

                @Override
                public void onFailure(Call<StartStreamResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    //makeToast(getString(R.string.something_wrong));
                    Toast.makeText(PublishActivity.this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
                    call.cancel();
                    triggerStopRecording();
                }
            });
        }
    }

    public void stopStream() {
        if (NetworkReceiver.isConnected() && streamName != null) {
            if (!isBroadCastStopped) {
                progress_bar.setVisibility(VISIBLE);
                isBroadCastStopped = true;
                /*btnStopBroadCast.setOnClickListener(null);*/
                Map<String, String> map = new HashMap<>();
                map.put(StreamConstants.TAG_PUBLISHER_ID, GetSet.getUserId());
                map.put(Constants.TAG_NAME, streamName);

                Log.i(TAG, "stopStreamapion Params: " + map);

                Call<Map<String, String>> call3 = apiInterface.stopStream(GetSet.getToken(), map);
                call3.enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        Log.i(TAG, "stopStreamapionResponse: " + response);
                        hideProgress();
                        bottomLay.setVisibility(View.GONE);
                        setPublishLayVisible(VISIBLE);
                        if (lensFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            onSwitchCamera();
                        }
                        stopStreamSocket();
                        progress_bar.setVisibility(GONE);
                        Toast.makeText(PublishActivity.this, getString(R.string.your_stream_end_description), Toast.LENGTH_SHORT).show();
                        mIsRecording = false;
                        isBroadCastStopped = false;
                        //makeToast(getString(R.string.your_stream_end_description));
                    }

                    @Override
                    public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                        Log.i(TAG, "stopStreamapionResponse: " + t.getMessage());
                        progress_bar.setVisibility(GONE);
                        setPublishLayVisible(VISIBLE);
                    }
                });
            }
        } else {
            setPublishLayVisible(VISIBLE);
        }
    }

    public void takeScreenShot() {
        mGLView.queueEvent(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {

                EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();



                /*EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
                Bitmap bitmap = createBitmapFromGLSurface(0, 0, mGLView.getWidth(), mGLView.getHeight(), gl);
                File file = StorageManager.getInstance(PublishActivity.this).
                        saveToCacheDir(bitmap, "Temp_".concat("" + System.currentTimeMillis()).concat(".jpg"));
                if (file != null) {
                    byte[] bytes = new byte[0];
                    try {
                        bytes = FileUtils.readFileToByteArray(file);
                        uploadImage(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/


                final Bitmap bitmap = Bitmap.createBitmap(mGLView.getWidth(), mGLView.getHeight(),
                        Bitmap.Config.ARGB_8888);

                final HandlerThread handlerThread = new HandlerThread("PixelCopier");
                handlerThread.start();

                PixelCopy.request(mGLView, bitmap, (copyResult) -> {
                    if (copyResult == PixelCopy.SUCCESS) {
                        Log.e(TAG, bitmap.toString());

                        saveToCacheDir(bitmap, "Temp_".concat("" + System.currentTimeMillis()).concat(".jpg"));
                    } else {
                        Toast toast = Toast.makeText(PublishActivity.this,
                                "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    handlerThread.quitSafely();
                }, new Handler(handlerThread.getLooper()));

                Log.i(TAG, "takeScreenshotrun: " + gl);


                saveToCacheDir(bitmap, "Temp_".concat("" + System.currentTimeMillis()).concat(".jpg"));
                if (bitmap != null) {
                    uploadImage(bitmap);
                }
            }
        });
    }


    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDir: " + mDataDir);
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
            Log.i(TAG, "saveToCacheDire: " + mDataDir);
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;
        Log.i(TAG, "saveToCacheDirec: " + file);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    // from other answer in this question
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl) {

        int[] bitmapBuffer = new int[w * h];
        int[] bitmapSource = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e(TAG, "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(StreamConstants.TAG_STREAM_IMAGE, streamName + ".jpeg", requestFile);
        RequestBody publisherId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        RequestBody videoId = RequestBody.create(getVideoId, MediaType.parse("multipart/form-data"));
        RequestBody streamNameBody = RequestBody.create(streamName, MediaType.parse("multipart/form-data"));
        Log.i(TAG, "uploadImages: " + bitmap);
        Call<Map<String, String>> call3 = apiInterface.uploadStreamImage(GetSet.getToken(), body, publisherId, videoId, streamNameBody);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                Log.i(TAG, "uploadImagesonResponse: " + response);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                call.cancel();
            }
        });
    }

/*    public void uploadImage(byte[] imageBytes) {
        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(StreamConstants.TAG_STREAM_IMAGE, streamName + ".jpeg", requestFile);
        RequestBody publisherId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        RequestBody streamNameBody = RequestBody.create(streamName, MediaType.parse("multipart/form-data"));

        Call<Map<String, String>> call3 = apiInterface.uploadStreamImage(GetSet.getToken(), body, publisherId, streamNameBody);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Log.i(TAG, "uploadImageRes: " + response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "uploadImage: " + t.getMessage());
            }
        });
    }*/

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnStopBroadCast) {
            // closeConfirmDialog(getString(R.string.stop_publish_description));
            handler.removeCallbacks(imageUpload);
            isPublished = false;
            triggerStopRecording();
        } else if (id == R.id.btnDetail) {
            if (bottomDialog != null && !bottomDialog.isShowing())
                bottomDialog.show();
        } else if (id == R.id.viewLay) {
            if (bottomCommentsDialog != null && !bottomCommentsDialog.isShowing())
                bottomCommentsDialog.show();
        } else if (id == R.id.btnCancelTips) {
            tipsLay.setVisibility(GONE);
            tipsLay.startAnimation(fadeOut);
        } else if (id == R.id.btnGoLive) {
            if (TextUtils.isEmpty(edtTitle.getText().toString().trim())) {
                //  tipsLay.setVisibility(VISIBLE);
                // tipsLay.startAnimation(shake);
                Toast.makeText(context, "Enter the description or name for this live", Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.isEmpty(edtLinkVideo.getText()) &&
                    !Patterns.WEB_URL.matcher(edtLinkVideo.getText().toString()).matches()
                /*!URLUtil.isValidUrl(edtLinkVideo.getText().toString())*/) {
                Toast.makeText(context, "Enter valid url", Toast.LENGTH_SHORT).show();
            } else if (!checkStreamPermissions()) {
                requestStreamPermissions();
            } else if (!NetworkReceiver.isConnected()) {
                Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                //makeToast(context.getString(R.string.no_internet_connection));
            } else {
                hideSoftKeyboard(this);
                pauseExternalAudio(this);
                btnGoLive.setEnabled(false);
                openBottomCameraDialog();
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openBottomCameraDialog() {
        if (bottomCameraDialog == null) {
            View bottomSheet = getLayoutInflater().inflate(R.layout.dialog_bottom_camera, null);
            bottomCameraDialog = new BottomSheetDialog(this, R.style.Bottom_WhiteDialog); // Style here
            bottomCameraDialog.setContentView(bottomSheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            frontCameraLay = bottomSheet.findViewById(R.id.frontCameraLay);
            backCameraLay = bottomSheet.findViewById(R.id.backCameraLay);

            frontCameraLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "onClick: :::::::::::::front");
                    frontCameraLay.setEnabled(false);
                    pauseExternalAudio(PublishActivity.this);
                    getStreamName(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
            });

            backCameraLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG, "onClick: :::::::::::::back");
                    backCameraLay.setEnabled(false);
                    pauseExternalAudio(PublishActivity.this);
                    getStreamName(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            });

            bottomCameraDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                    View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(true);
                    frontCameraLay.setEnabled(true);
                    backCameraLay.setEnabled(true);
                    btnGoLive.setEnabled(false);
                }
            });

            bottomCameraDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                    View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                    BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                    BottomSheetBehavior.from(bottomSheet).setHideable(true);
                    frontCameraLay.setEnabled(true);
                    backCameraLay.setEnabled(true);
                    btnGoLive.setEnabled(true);
                }
            });
        }

        if (!bottomCameraDialog.isShowing()) {
            bottomCameraDialog.show();
        }
    }

    public void pauseExternalAudio(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            Constants.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    private void getStreamName(final int mLensFacing) {

        if (NetworkReceiver.isConnected()) {
//            unBindCamera();
            btnGoLive.setEnabled(false);
            showProgress();
            Log.e(TAG, "getStreamName: :::::::::::::::" + GetSet.getUserId());
            Call<HashMap<String, String>> call = apiInterface.getStreamName(GetSet.getToken(), GetSet.getUserId());
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> map = response.body();
                        Log.i(TAG, "getStreamNameRes: " + map);
                        if (map.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            title = edtTitle.getText().toString().trim();
                            attachedLink = edtLinkVideo.getText().toString().trim();

                            getVideoId = map.get("video_id");
                            streamName = map.get(StreamConstants.TAG_STREAM_NAME);
                            streamToken = map.get(StreamConstants.TAG_STREAM_TOKEN);
                            setPublishLayVisible(GONE);
                            setLoadingLay(VISIBLE);
                            if (lensFacing != mLensFacing) {
                                onSwitchCamera();
                            }
                            lensFacing = mLensFacing;
                            startPublish();
                            edtLinkVideo.getText().clear();
                            edtTitle.getText().clear();
                            btnGoLive.setEnabled(true);
                            bottomCameraDialog.dismiss();
                            if (bottomStreamTitle != null)
                                bottomStreamTitle.setText(title); // Added QA Review
                        } else {
                            Toast.makeText(PublishActivity.this, "" + map.get(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        btnGoLive.setEnabled(true);
                        //initCameraView();
                    }
                    hideProgress();
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    t.printStackTrace();
                    Log.e(TAG, "onFailure: :::::::::::::", t);
                    hideProgress();
                    btnGoLive.setEnabled(true);
//                    initCameraView();
                }
            });
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(context, CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStreamPermissions() {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
             return ContextCompat.checkSelfPermission(context, CAMERA)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED /*&&
                ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED*/;
        }else{
            return ContextCompat.checkSelfPermission(context, CAMERA)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
        }

    }

    private void requestStreamPermissions() {
     /*   if (ActivityCompat.checkSelfPermission(context,
                CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {*/
            ActivityCompat.requestPermissions(this, mRequiredPermissions, Constants.STREAM_REQUEST_CODE);
        /*}*/
    }

    public boolean isPermissionsGranted(String[] permissions) {
        boolean isGranted = false;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            } else {
                isGranted = true;
            }
        }
        return isGranted;
    }

    public void showProgress() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    public void hideProgress() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
        // TODO Auto-generated method stub
        float sensitivity = 50;
        // Get swipe delta value in x axis.
        float deltaX = e1.getX() - e2.getX();

        // Get swipe delta value in y axis.
        float deltaY = e1.getY() - e2.getY();
        // Get absolute value.
        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);
        // Minimal x and y axis swipe distance.
        int MIN_SWIPE_DISTANCE_X = 100;
        int MIN_SWIPE_DISTANCE_Y = 100;

        // Maximal x and y axis swipe distance.
        int MAX_SWIPE_DISTANCE_X = 1000;
        int MAX_SWIPE_DISTANCE_Y = 1000;

        Log.i(TAG, "onFling: ");
        if ((deltaYAbs >= MIN_SWIPE_DISTANCE_Y) && (deltaYAbs <= MAX_SWIPE_DISTANCE_Y)) {
            if (deltaY > 0) {
                if (bottomLay.getVisibility() != VISIBLE)
                    slideUpAnim();
            } else {
                if (bottomLay.getVisibility() == VISIBLE)
                    slideDownAnim();
            }
        }

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        Log.i(TAG, "onDoubleTapEvent: " + motionEvent.getAction());
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            onSwitchCamera();
        }
        return false;
    }

    private void slideUpAnim() {
        bottomLay.setVisibility(VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                bottomLay.getHeight(),
                0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        bottomLay.startAnimation(animate);
    }

    private void slideDownAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                bottomLay.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomLay.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bottomLay.startAnimation(animate);
    }


    public class GiftsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<StreamDetails.Gifts> giftsList = new ArrayList<>();
        Context context;
        Random rnd;

        public GiftsAdapter(Context context, ArrayList<StreamDetails.Gifts> giftsList) {
            this.giftsList = giftsList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gifts_received, parent, false);
            return new GiftViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                StreamDetails.Gifts gift = giftsList.get(position);
                Glide.with(context)
                        .load(/*Constants.IMAGE_URL +*/ gift.getGiftIcon())
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }

                Glide.with(context)
                        .load(/*Constants.IMAGE_URL +*/ gift.getUserImage())
                        .error(R.drawable.ic_account)
                        .placeholder(R.drawable.ic_account)
                        .into(holder.userImage);

                int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
                holder.colorImage.setBackgroundColor(color);
                String userName = "@" + gift.getUserName() + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + gift.getGiftTitle() + "\"";
                String strGift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + strGift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);
            }
        }

        @Override
        public int getItemCount() {
            return giftsList.size();
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage, colorImage;
            FrameLayout profileLay;
            AppCompatTextView txtGiftName;
            ImageView giftImage;
            ConstraintLayout itemLay;

            public GiftViewHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                colorImage = view.findViewById(R.id.colorImage);
                profileLay = view.findViewById(R.id.profileLay);
                itemLay = view.findViewById(R.id.itemLay);
                giftImage = view.findViewById(R.id.giftImage);
                txtGiftName = view.findViewById(R.id.txtGiftName);

                itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       /* if (giftsList.get(getAdapterPosition()).userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(context, MyProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        } else {
                            Intent profile = new Intent(context, OthersProfileActivity.class);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            profile.putExtra(Constants.TAG_PARTNER_ID, giftsList.get(getAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_PARTNER_NAME, giftsList.get(getAdapterPosition()).getUserName());
                            profile.putExtra(Constants.TAG_PARTNER_IMAGE, giftsList.get(getAdapterPosition()).getUserImage());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_SEARCH);
                            startActivity(profile);
                        }*/
                    }
                });
            }
        }
    }

    public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public final int VIEW_TYPE_JOIN = 0;
        public final int VIEW_TYPE_TEXT = 1;
        public final int VIEW_TYPE_GIFT = 2;
        ArrayList<HashMap<String, String>> commentList;
        Context context;

        public CommentsAdapter(Context context, ArrayList<HashMap<String, String>> commentList) {
            this.commentList = commentList;
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (commentList.get(position) != null) {
                String type = "" + commentList.get(position).get(Constants.TAG_TYPE);
                switch (type) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_TEXT;
                    case StreamConstants.TAG_STREAM_JOINED:
                        return VIEW_TYPE_JOIN;
                    case Constants.TAG_GIFT:
                        return VIEW_TYPE_GIFT;
                }
            }
            return VIEW_TYPE_TEXT;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_JOIN) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_join, parent, false);
                return new JoiningViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_TEXT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_GIFT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_gifts, parent, false);
                return new GiftViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof MyViewHolder) {
                final MyViewHolder holder = (MyViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                holder.txtMessage.setText(map.get(Constants.TAG_MESSAGE));
                holder.txtUserName.setText("@" + map.get(Constants.TAG_USER_NAME));
                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        holder.itemLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(holder.itemLay, holder.getAdapterPosition());
                        holder.itemLay.setVisibility(GONE);

                    }
                }.start();

            } else if (viewHolder instanceof JoiningViewHolder) {
                final JoiningViewHolder joinViewHolder = (JoiningViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);

                joinViewHolder.txtJoined.setText("@" + map.get(Constants.TAG_USER_NAME) + " Joined");
                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        joinViewHolder.itemLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(joinViewHolder.itemLay, joinViewHolder.getAdapterPosition());
                        joinViewHolder.itemLay.setVisibility(GONE);

                    }
                }.start();
            } else if (viewHolder instanceof GiftViewHolder) {
                final GiftViewHolder holder = (GiftViewHolder) viewHolder;
                final HashMap<String, String> map = commentList.get(position);
                Glide.with(context)
                        .load(/*Constants.GIFT_IMAGE_URL +*/ map.get(Constants.TAG_GIFT_ICON))
                        .error(R.drawable.gift)
                        .placeholder(R.drawable.gift)
                        .into(holder.giftImage);
                if (LocaleManager.isRTL()) {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, 20));
                } else {
                    holder.giftImage.setTranslationX(AppUtils.dpToPx(context, -20));
                }

                String userName = "@" + map.get(Constants.TAG_USER_NAME) + " " + getString(R.string.sent) + "\n";
                String giftTitle = "\"" + map.get(Constants.TAG_GIFT_TITLE) + "\"";
                String gift = " " + getString(R.string.gift);
                Spannable spannable = new SpannableString(userName + giftTitle + gift);
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), userName.length(), (userName + giftTitle).length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtGiftName.setText(spannable, TextView.BufferType.SPANNABLE);

                new CountDownTimer(5000, 1000) { //(timer_duration, timer_interval)
                    @Override
                    public void onTick(long millisUntilFinished) {
                        holder.giftLay.setVisibility(VISIBLE);
                        //runs every second (1000 ms)
                    }

                    @Override
                    public void onFinish() {
                        //Do your operations when timer is finised
                        setFadeAnimation(holder.giftLay, holder.getAdapterPosition());
                        holder.giftLay.setVisibility(GONE);

                    }
                }.start();
            }
        }

        public class JoiningViewHolder extends RecyclerView.ViewHolder {
            TextView txtJoined;
            RelativeLayout itemLay;

            public JoiningViewHolder(View view) {
                super(view);
                txtJoined = view.findViewById(R.id.txtJoined);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }

        public class GiftViewHolder extends RecyclerView.ViewHolder {
            TextView txtGiftName;
            ImageView giftImage;
            RelativeLayout giftLay;

            public GiftViewHolder(View view) {
                super(view);
                txtGiftName = view.findViewById(R.id.txtGiftName);
                giftImage = view.findViewById(R.id.giftImage);
                giftLay = view.findViewById(R.id.giftLay);

            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView txtUserName;
            TextView txtMessage;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                txtUserName = view.findViewById(R.id.txtUserName);
                txtMessage = view.findViewById(R.id.txtMessage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }
    }

    private void setFadeAnimation(final View view, final int position) {
        /*sets animation from complete opaque state(1.0f) to complete transparent state(0.0f)*/
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    public class ViewerListViewAdapter extends RecyclerView.Adapter<ViewerListViewAdapter.MyViewHolder> {

        ArrayList<StreamDetails.LiveViewers> viewersList;
        Context context;
        Random rnd;

        public ViewerListViewAdapter(Context context, ArrayList<StreamDetails.LiveViewers> viewersList) {
            this.viewersList = viewersList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_viewers, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final StreamDetails.LiveViewers viewer = viewersList.get(position);
            int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            Glide.with(context).load(viewer.getUserImage())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .error(R.drawable.profile_square)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.userImage);
            holder.colorImage.setBackgroundColor(color);
            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return viewersList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            ImageView colorImage;
            RelativeLayout itemLay;

            public MyViewHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                colorImage = view.findViewById(R.id.colorImage);
                itemLay = view.findViewById(R.id.itemLay);
            }
        }
    }

    public class BottomListAdapter extends RecyclerView.Adapter<BottomListAdapter.MyViewHolder> {

        ArrayList<StreamDetails.LiveViewers> viewerList;
        Context context;
        Random rnd;

        public BottomListAdapter(Context context, ArrayList<StreamDetails.LiveViewers> viewerList) {
            this.viewerList = viewerList;
            this.context = context;
            rnd = new Random();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_viewer, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final StreamDetails.LiveViewers viewer = viewerList.get(position);
            int color = Color.argb(60, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
            Glide.with(context).load("" + viewer.getUserImage())
                    .apply(RequestOptions.circleCropTransform())
                    .thumbnail(0.5f)
                    .error(R.drawable.profile_square)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.userImage);
            holder.colorImage.setBackgroundColor(color);

            holder.txtUserName.setText(viewer.getUserName());
            holder.mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return viewerList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            RoundedImageView colorImage;
            TextView txtUserName;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);
                mainLay = view.findViewById(R.id.mainLay);
                userImage = view.findViewById(R.id.userImage);
                txtUserName = view.findViewById(R.id.txtUserName);
                colorImage = view.findViewById(R.id.colorImage);
            }
        }
    }

    private void closeConfirmDialog(String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PublishActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.removeCallbacks(imageUpload);
                isPublished = false;
                triggerStopRecording();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
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

    //
// Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
// the results of the permissions request
//
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (mLiveVideoBroadcaster.isPermissionGranted()) {
                    mLiveVideoBroadcaster.openCamera(lensFacing);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    RECORD_AUDIO)) {
                        mLiveVideoBroadcaster.requestPermission();
                    } else {
                        new AlertDialog.Builder(PublishActivity.this)
                                .setTitle(R.string.permission)
                                .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                            startActivity(intent);

                                        } catch (ActivityNotFoundException e) {
                                            //e.printStackTrace();

                                            //Open the generic Apps page:
                                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);

                                        }
                                    }
                                })
                                .show();
                    }
                }
            }
            break;
            case Constants.CAMERA_REQUEST_CODE: {
                if (!isPermissionsGranted(permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            openPermissionDialog(permissions);
                        //makeToast(getString(R.string.camera_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
                        startActivity(i);
                    }
                }
            }
            break;
            case Constants.STREAM_REQUEST_CODE: {
                if (!isPermissionsGranted(permissions)) {

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) ) {
                        requestPermissions(permissions, Constants.STREAM_REQUEST_CODE);
                    } else {
//                            openPermissionDialog(permissions);
                        //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
                        startActivity(i);
                    }
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.STREAM_REQUEST_CODE);
                        } else {
//                            openPermissionDialog(permissions);
                            //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
                            startActivity(i);
                        }
                    }
                } else {
                    btnGoLive.performClick();
                }
            }
        }
    }

    //    Utility method to check the status of a permissions request for an array of permission identifiers
    private static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }
}