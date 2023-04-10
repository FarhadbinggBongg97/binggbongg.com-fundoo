package com.app.binggbongg.fundoo;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.io.ByteStreams;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.gson.Gson;
import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.R;
import com.app.binggbongg.apprtc.util.AppRTCUtils;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.external.CustomTypefaceSpan;
import com.app.binggbongg.external.EndlessRecyclerOnScrollListener;
import com.app.binggbongg.external.ImagePicker;
import com.app.binggbongg.external.ProgressWheel;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.helper.AppWebSocket;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.livedata.MessageLiveModel;
import com.app.binggbongg.model.AdminMessageResponse;
import com.app.binggbongg.model.ChatResponse;
import com.app.binggbongg.model.GetSet;

import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.FileUtil;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;
import com.app.binggbongg.utils.Util;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


@SuppressLint("NonConstantResourceId")
public class ChatActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents, AdapterView.OnItemClickListener /*ReplyChipAdapter.ClickListener*/ {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnVideoCall)
    ImageView btnVideoCall;
    //Audio Call Addon

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnGallery)
    ImageView btnGallery;
    @BindView(R.id.btnCamera)
    ImageView btnCamera;
    @BindView(R.id.attachLay)
    LinearLayout attachLay;
    @BindView(R.id.btnAdd)
    ImageView btnAdd;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    @BindView(R.id.btnSend)
    TextView btnSend;
    @BindView(R.id.edtLay)
    RelativeLayout edtLay;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btnMenu)
    ImageView btnMenu;
    public String partnerId = "";
    @BindView(R.id.txtTyping)
    TextView txtTyping;
    @BindView(R.id.btnContactUs)
    Button btnContactUs;
    @BindView(R.id.profileLayout)
    LinearLayout profileLayout;
    //Gif Addon

    //chat translate addon

    //Smart Replay

    //Voice Message Addon

    private Call<StreamDetails> streamApiCall;
    private ApiInterface apiInterface;
    private String partnerImage = "";
    private String partnerName = "";
    private DBHelper dbHelper;
    private MessageLiveModel liveModel;
    int currentPage = 0, limit = 20;
    private ChatAdapter adapter;
    public String chatId;
    LinearLayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    StorageUtils storageUtils;
    Handler handler = new Handler();
    Runnable runnable;
    private String blockStatus = Constants.TAG_FALSE, shareLink="";
    Handler onlineHandler = new Handler();
    int delay = 5000; //milliseconds
    private PopupMenu popupMenu;
    PopupWindow popupWindow;
    boolean btnSendShow = false;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    public static boolean isAdminChat = false, isBackPressed = false;
    public static boolean socketmessages = false;
    private List<ChatResponse> chatList = new ArrayList<>();
    private DialogLoading dialogLoading;
    private AppUtils appUtils;
    MediaRecorder mediaRecorder;
    String recordVoicePath = null, socketDuration, languageCode = "";
    //   private Context context;
    private SlidrInterface sliderInterface;
    private MediaPlayer player = new MediaPlayer();
    int playingPosition = -1;
    private SeekBar seekBar;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    int recordAudioPermission;
    //smart reply


    /* Gif and Emoji */

    GiphyDialogFragment giphyDialogFragment;
    GPHSettings settings = new GPHSettings();

    /*Chat Translation*/
    boolean from = false;
    int firstcall=0;

    private TextView txtAudioTime;
    private Handler seekHandler = new Handler();
    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            if (player.isPlaying()) {
                long currentDuration = player.getCurrentPosition();
                if (txtAudioTime != null)
                    txtAudioTime.setText(milliSecondsToTimer(currentDuration));
                int mediaPos_new = player.getCurrentPosition();
                int mediaMax_new = player.getDuration();
                seekBar.setMax(mediaMax_new);
                seekBar.setProgress(mediaPos_new);
                seekHandler.postDelayed(this, 100);
            }
        }
    };

    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_PROFILE_LIVE);
                jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
                Logging.i(TAG, "checkOnline: " + jsonObject);
                AppWebSocket.getInstance(ChatActivity.this).send(jsonObject.toString());
            } catch (JSONException e) {
                Logging.e(TAG, "checkOnline error: " + e.getMessage());
                e.printStackTrace();
            }
            onlineHandler.postDelayed(onlineRunnable, delay);
        }
    };
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        //this.context = getApplicationContext();
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);


        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(ChatActivity.this);

        /*Chat Translation*/


        /* Emoji and Gif in chat */


        // Set up recycler view for smart replies
        LinearLayoutManager chipManager = new LinearLayoutManager(this);
        chipManager.setOrientation(RecyclerView.HORIZONTAL);

        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            isAdminChat = getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_ADMIN);
        }

        if(!TextUtils.isEmpty("share_link")){
            shareLink = getIntent().getStringExtra("share_link");
            edtMessage.setText(shareLink);
            btnSend.setVisibility(View.VISIBLE);
        }

        partnerName = getIntent().getStringExtra(Constants.TAG_PARTNER_NAME);
        dbHelper = DBHelper.getInstance(this);
        storageUtils = StorageUtils.getInstance(this);

        liveModel = new ViewModelProvider(ChatActivity.this).get(MessageLiveModel.class);
        if (!isAdminChat) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(1);
            }
            partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
            partnerImage = getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE);
            chatId = GetSet.getUserId() + partnerId;
            blockStatus = dbHelper.getBlockStatus(chatId);
            dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "0");
            if (getIntent().hasExtra(Constants.TAG_BLOCKED_BY_ME)) {
                blockStatus = getIntent().getStringExtra(Constants.TAG_BLOCKED_BY_ME);
            }
            Log.d(TAG, "UserBlockStatus" + blockStatus);
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(0);
            }
            chatId = GetSet.getUserId();
            dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "0");
        }

        initView();


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
    }

    //smart reply


    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        sliderInterface = Slidr.attach(this, config);
        Slidr.attach(this, config);

        initLoadingDialog();
        txtTitle.setText(partnerName);
        adapter = new ChatAdapter(ChatActivity.this);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        if (isAdminChat) {
            btnContactUs.setVisibility(View.VISIBLE);
            txtSubTitle.setVisibility(View.GONE);
            txtTyping.setVisibility(View.GONE);
            edtMessage.setVisibility(View.GONE);
            edtLay.setVisibility(View.GONE);
            btnVideoCall.setVisibility(View.GONE);
           //audio call addon set listener
            btnMenu.setVisibility(View.GONE);
           //chat translate addon
            Log.d(TAG, "initView:firstcall ");
            getMessageList(currentPage = 0);
            getAdminMessages();
        } else {
            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                txtSubTitle.setVisibility(View.GONE);
            } else {
                txtSubTitle.setVisibility(View.VISIBLE);
            }
            profileLayout.setOnClickListener(view -> {
                Intent profile = new Intent(getApplicationContext(), OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, partnerId);
                profile.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                profile.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profile);
            });
            getMessageList(currentPage = 0);
        }

        edtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edtMessage, InputMethodManager.SHOW_FORCED);
                hideAttachment();
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                if (charSequence.length() > 0)
                    setTyping(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                runnable = new Runnable() {
                    public void run() {
                        setTyping(false);
                    }
                };
                handler.postDelayed(runnable, 1000);

                if (editable.length() > 0 && !btnSendShow) {
                    btnSendShow = true;
                    slideOutAnim();


                } else if (editable.length() == 0) {
                    btnSendShow = false;
                    slideInAnim();


                }
            }
        });

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getMessageList(currentPage = page);
            }
        };
        endlessRecyclerOnScrollListener.resetState();
        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
        recyclerView.scrollToPosition(0);

        if (!isAdminChat) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dbHelper.updateMessageReadStatus(chatId, GetSet.getUserId());
                    if (blockStatus != null && blockStatus.equals(Constants.TAG_FALSE)) {
                        sendReadStatus();
                    }
                }
            }, 2000);
        }
    }

    private void slideOutAnim() {
        //btnRecord.setVisibility(View.GONE);
        btnSend.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnSend.setRotation(180);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        } else {
            btnSend.setRotation(0);
            btnSend.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        }

    }

    private void slideInAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                -50,
                0,
                0);
        animate.setDuration(200);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnSend.setVisibility(View.GONE);
                //btnRecord.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnSend.startAnimation(animate);
    }

    private void getMessageList(int offset) {
        if (isAdminChat) {
            liveModel.getAdminMessages(this, chatId, limit, offset * limit)
                    .observe(this, new Observer<List<ChatResponse>>() {
                        @Override
                        public void onChanged(List<ChatResponse> chatResponses) {
                            adapter.setData(chatResponses);
                        }
                    });

        } else {
            liveModel.getMessages(this, chatId, limit, offset * limit).observe(this, new Observer<List<ChatResponse>>() {
                @Override
                public void onChanged(List<ChatResponse> chatResponses) {

                    Timber.i("chatresponse %s", new Gson().toJson(chatResponses));
                    adapter.setData(chatResponses);
                }
            });
        }
    }

    private void getAdminMessages() {
        if (NetworkReceiver.isConnected()) {
            long lastTime = 0L;
            ChatResponse lastMessage;
            lastMessage = dbHelper.getLastAdminMessage();
            if (lastMessage != null && lastMessage.getCreatedAt() != null) {
                lastTime = Long.parseLong(lastMessage.getCreatedAt());
            }
            Call<AdminMessageResponse> call = apiInterface.getAdminMessages(GetSet.getUserId(), Constants.TAG_ANDROID, GetSet.getCreatedAt(), lastTime);
            Log.d(TAG, "getAdminMessages: "+GetSet.getUserId()+Constants.TAG_ANDROID+ GetSet.getCreatedAt()+ "     " +lastTime);
            call.enqueue(new Callback<AdminMessageResponse>() {
                @Override
                public void onResponse(Call<AdminMessageResponse> call, Response<AdminMessageResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        ;

                        for (AdminMessageResponse.MessageData messageData : response.body().getMessageData()) {
                            dbHelper.addAdminMessage(messageData);
                            dbHelper.addAdminRecentMessage(messageData, 0);
                            ChatResponse chatResponse = new ChatResponse();
                            chatResponse.setChatId(GetSet.getUserId());
                            chatResponse.setReceiverId(GetSet.getUserId());
                            chatResponse.setMessageType(messageData.getMsgType());
                            chatResponse.setMessageEnd(Constants.TAG_RECEIVE);
                            chatResponse.setMessage(messageData.getMsgData());
                            chatResponse.setMessageId(messageData.getMsgId());
                            chatResponse.setChatTime(messageData.getCreateaAt());
                            chatResponse.setCreatedAt(messageData.getMsgAt());
                            chatResponse.setChatType(Constants.TAG_ADMIN);
                            liveModel.addAdminMessage(chatResponse);
                        }
                    }
                }

                @Override
                public void onFailure(Call<AdminMessageResponse> call, Throwable t) {

                }
            });
        } else {
            getMessageList(currentPage = 0);
        }
    }

    private void setTyping(boolean isTyping) {
        if (blockStatus != null && !blockStatus.equals(Constants.TAG_TRUE)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_USER_TYPING);
                jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
                jsonObject.put(Constants.TAG_TYPING_STATUS, isTyping ? Constants.TAG_TYPING : Constants.TAG_UNTYPING);
                Logging.i(TAG, "setTyping: " + jsonObject);
                AppWebSocket.getInstance(this).send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendReadStatus() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_UPDATE_READ);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
            Log.i(TAG, "sendReadStatus: " + jsonObject.toString());
            AppWebSocket.getInstance(this).send(jsonObject.toString());
        } catch (JSONException e) {
            Log.i(TAG, "sendReadStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
        if (isConnected) {
            AppWebSocket.getInstance(this).setWebSocketClient(null);
            AppWebSocket.setNullInstance();
            AppWebSocket.getInstance(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUtils.resumeExternalAudio();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        AppWebSocket.setCallEvents(this);
        blockStatus = dbHelper.getBlockStatus(chatId);
        Log.i(TAG, "onResume: " + blockStatus);
        checkOnline();
    }

    @Override
    protected void onStop() {
        super.onStop();


//        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        try {
            AppWebSocket.setCallEvents(null);
        } catch (Exception e) {
            Logging.e(TAG, "onDestroy: " + e.getMessage());
        }
        onlineHandler.removeCallbacks(onlineRunnable);
        AppUtils.hideKeyboard(ChatActivity.this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkReceiver();
    }

    private void checkOnline() {
        if (blockStatus != null && !blockStatus.equals(Constants.TAG_TRUE)) {
            onlineHandler.post(onlineRunnable);
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String userId) {
        if (partnerId.equals(userId)) {
            getProfile(partnerId);
        }
    }*/

    /* Emoji and Gif in Chat */


    @OnClick({R.id.btnBack, R.id.btnMenu, R.id.btnVideoCall, R.id.btnGallery, R.id.btnCamera,
            R.id.btnAdd,R.id.btnSend, R.id.btnContactUs})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                App.preventMultipleClick(btnBack);
                hideKeyboard(ChatActivity.this);
                onBackPressed();
                break;
            case R.id.btnVideoCall:
                App.preventMultipleClick(btnVideoCall);
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_call_description));
                } else {
                    if (GetSet.getGems() >= AdminData.videoCallsGems) {
                        if (NetworkReceiver.isConnected()) {
                            AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                            Intent callIntent = appRTCUtils.connectToRoom(partnerId, Constants.TAG_SEND, Constants.TAG_VIDEO);
                            callIntent.putExtra(Constants.TAG_USER_NAME, partnerName);
                            callIntent.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                            callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(callIntent);
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    } else {
                        App.makeToast(getString(R.string.not_enough_gems));
                    }
                }
                break;
           //Audio call Addon

            case R.id.btnMenu:
                App.preventMultipleClick(btnMenu);
                openMenu(view);
                break;
            case R.id.btnGallery:
                App.preventMultipleClick(btnGallery);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                            Constants.STORAGE_REQUEST_CODE);
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    App.preventMultipleClick(btnGallery);
//                    ImagePicker.pickImage(ChatActivity.this);
                    pickImage();
                }
                break;
            case R.id.btnCamera:
                App.preventMultipleClick(btnCamera);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                            Constants.STORAGE_REQUEST_CODE);
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    App.preventMultipleClick(btnCamera);
//                    ImagePicker.pickImage(ChatActivity.this);
                    pickImage();
                }
                break;
            case R.id.btnAdd:
                App.preventMultipleClick(btnAdd);
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                      if ( ActivityCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED||
                            ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{ READ_MEDIA_IMAGES,CAMERA},
                                Constants.STORAGE_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ChatActivity.this);
                          pickImage();
                      }
                }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    if ( ActivityCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                            ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{ READ_EXTERNAL_STORAGE,CAMERA},
                                Constants.STORAGE_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ChatActivity.this);
                        pickImage();
                    }
                }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                                Constants.STORAGE_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ChatActivity.this);
                        pickImage();
                    }
                }
                else {
//                    ImagePicker.pickImage(ChatActivity.this);
                    pickImage();
                }
                break;
            //Gif Addon

            case R.id.btnSend:
                App.preventMultipleClick(btnSend);
                if (TextUtils.isEmpty("" + edtMessage.getText().toString().trim())) {
                    edtMessage.setError(getString(R.string.enter_your_message));
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else if (!NetworkReceiver.isConnected()) {
                    App.makeToast(getString(R.string.no_internet_connection));
                } else {
                    //sendChat(Constants.TAG_TEXT, "", "", "");
                    String textsend = edtMessage.getText().toString();
                    sendChat(Constants.TAG_TEXT, "", "", "", textsend);
                    edtMessage.setText("");
                }
                break;
            case R.id.btnContactUs:
                App.preventMultipleClick(btnContactUs);
                if (NetworkReceiver.isConnected()) {
                    sendEmail();
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
                break;
           //Chat Translate Addon
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoFile = image;
        return image;
    }

    private Uri createImageUri() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        ContentValues vals = new ContentValues();
        vals.put(MediaStore.MediaColumns.TITLE, imageFileName);
        vals.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, vals);
    }


    private void pickImage() {

        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(this, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (captureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Timber.e(ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCurrentPhotoUri = FileProvider.getUriForFile(this,
                            getString(R.string.file_provider_authority),
                            photoFile);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                    startActivityForResult(captureIntent, REQUEST_CODE_PICK_IMAGE);
                }
            }
            pickerOptionsSheet.dismiss();
        });
        layoutGallery.setOnClickListener(v -> {
            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
            pickIntent.setType("image/jpeg");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }
            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        if (streamApiCall != null) {
            streamApiCall.cancel();
        }
        super.onBackPressed();
        isAdminChat=false;
    }


    private void sendEmail() {
        try {

            String reportContent = "\n\n" + "DEVICE OS VERSION CODE: " + Build.VERSION.SDK_INT + "\n" +
                    "DEVICE VERSION CODE NAME: " + Build.VERSION.CODENAME + "\n" +
                    "DEVICE NAME: " + AppUtils.getDeviceName() + "\n" +
                    "VERSION CODE: " + BuildConfig.VERSION_CODE + "\n" +
                    "VERSION NAME: " + BuildConfig.VERSION_NAME + "\n" +
                    "PACKAGE NAME: " + BuildConfig.APPLICATION_ID + "\n" +
                    "BUILD TYPE: " + BuildConfig.BUILD_TYPE;

            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"" + AdminData.contactEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContent);
            try {
                //start email intent
                startActivity(Intent.createChooser(emailIntent, "Email"));
            } catch (Exception e) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                App.makeToast(e.getMessage());
            }
        } catch (Exception e) {
            Logging.e(TAG, "sendEmail: " + e.getMessage());
        }
    }

    private void openMenu(View view) {
        popupMenu = new PopupMenu(ChatActivity.this, btnMenu, R.style.PopupMenuBackground);
        popupMenu.getMenuInflater().inflate(R.menu.chat_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.START);
        if (blockStatus.equals(Constants.TAG_TRUE))
            popupMenu.getMenu().getItem(1).setTitle(getString(R.string.unblock));
        else popupMenu.getMenu().getItem(1).setTitle(getString(R.string.block));

        Typeface typeface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem menuItem = popupMenu.getMenu().getItem(i);
            SpannableString mNewTitle = new SpannableString(menuItem.getTitle());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mNewTitle.setSpan(new TypefaceSpan(typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                mNewTitle.setSpan(new CustomTypefaceSpan("", typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            menuItem.setTitle(mNewTitle);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(getString(R.string.clear_chat))) {
                    openClearChatDialog();
                } else if (item.getTitle().toString().equals(getString(R.string.block))) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                        json.put(Constants.TAG_RECEIVER_ID, partnerId);
                        json.put(Constants.TAG_BLOCKED, true);
                        Logging.i(TAG, "blockUser: " + json);
                        AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
                        blockUnBlockUser(partnerId);
                        if (dbHelper.isChatIdExists(chatId)) {
                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                            dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                            dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                        } else {
                            dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, Constants.TAG_TRUE);
                            if (dbHelper.isChatIdExists(chatId)) {
                                dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                            }
                        }
                        EventBus.getDefault().postSticky(partnerId);
                        blockStatus = Constants.TAG_TRUE;
                        onlineHandler.removeCallbacks(onlineRunnable);
                        txtSubTitle.setVisibility(View.GONE);
                        txtTyping.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    popupMenu.dismiss();
                    App.makeToast(getString(R.string.user_blocked_successfully));
                } else if (item.getTitle().toString().equals(getString(R.string.unblock))) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                        json.put(Constants.TAG_RECEIVER_ID, partnerId);
                        json.put(Constants.TAG_BLOCKED, false);
                        Logging.i(TAG, "UnBlockUser: " + json);
                        AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
                        blockUnBlockUser(partnerId);
                        if (dbHelper.isChatIdExists(chatId)) {
                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                        } else {
                            dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, Constants.TAG_FALSE);
                        }
                        EventBus.getDefault().postSticky(partnerId);
                        blockStatus = Constants.TAG_FALSE;
                        txtSubTitle.setVisibility(View.VISIBLE);
                        checkOnline();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    popupMenu.dismiss();
                    App.makeToast(getString(R.string.unblocked_successfully));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private void openClearChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setMessage(getString(R.string.really_want_clear));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                hideKeyboard(ChatActivity.this);
                dbHelper.deleteMessages(chatId);
                if (MessageLiveModel.msgLiveData != null) {
                    MessageLiveModel.msgLiveData.setValue(new ArrayList<>());
                }
                AppUtils.hideKeyboard(ChatActivity.this);
                popupMenu.dismiss();
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

    private void blockUnBlockUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, partnerId);
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
//                            Log.i(TAG, "onResponse: " + responseMap);
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            try {
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                Bitmap bitmap = ImagePicker.getImageFromResult(getApplicationContext(), requestCode, resultCode, data);
                /*File uploadedFile = storageUtils.saveToSDCard(bitmap, Constants.TAG_SENT, fileName);
                if (uploadedFile != null || BuildConfig.DEBUG) {
                    String thumbnail = storageUtils.saveThumbNail(bitmap, uploadedFile.getName());
                    InputStream imageStream = ImagePicker.getInputStreamFromResult(getApplicationContext(), requestCode, resultCode, data);
                    @SuppressLint("StaticFieldLeak")
                    ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                                uploadChatImage(AppUtils.getBytes(imageStream), fileName, thumbnail, jsonObject);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(uploadedFile.getPath());
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                boolean isCamera = (data == null
                        || data.getData() == null);
                Uri imageUri = null;
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                if (isCamera) {     /** CAMERA **/
                    imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, mCurrentPhotoUri), Constants.TAG_SENT, fileName);
                    deleteFileForUri(mCurrentPhotoUri);
                    mCurrentPhotoUri = null;
                    Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                } else {            /** ALBUM **/
                    String mimeType = getContentResolver().getType(data.getData());
                    if (mimeType != null && mimeType.startsWith("image")) {
                        imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, data.getData()), Constants.TAG_SENT, fileName);
                        Timber.d("Picked: %s", imageUri);
                    } else {
                        Toasty.info(this, "Videos are not supported yet").show();
                        return;
                    }
                }

                if (imageUri != null) {
                    String thumbnail = storageUtils.saveThumbNail(FileUtil.decodeBitmap(this, imageUri), fileName);
                    try {
                        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "r");
                        byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                        JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                        uploadChatImage(imageBytes, fileName, thumbnail, jsonObject);
                    } catch (Exception e) {
                        Timber.e(e);
                        Toasty.error(this, R.string.something_went_wrong).show();
                    }
                    /**
                     * The image is working fine without a compression.
                     */
                    /*Runnable compressionTask = new ImageCompressionTask(this, imageUri,
                            outUri -> {
                                try {
                                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(outUri, "r");
                                    byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                                    JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                                    uploadChatImage(imageBytes, fileName, thumbnail, jsonObject);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toasty.error(this, R.string.something_went_wrong).show();
                                }
                            });
                    mExecutorService.execute(compressionTask);*/

                    /*@SuppressLint("StaticFieldLeak")
                    ImageCompression imageComimageCompressionpression = new ImageCompression(ChatActivity.this) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                                uploadChatImage(AppUtils.getBytes(imageStream), fileName, thumbnail, jsonObject);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(uploadedFile.getPath());*/
                }
            }
        }
       //chat Translate
    }

    private void deleteFileForUri(Uri uri) {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (contentResolver.delete(uri, null, null) > 0) {
                    Timber.i("Save to gallery: deleted %s", uri.getPath());
                } else {
                    Timber.i("Save to gallery: failed deleting %s", uri.getPath());
                }
            } else {
                /* For Android 11 (R) and above, use this snippet */
                        /*PendingIntent trashPendingIntent = MediaStore.createTrashRequest(contentResolver, Collections.singleton(mTempSavedFileUri), true);
                        try {
                            startIntentSenderForResult(trashPendingIntent.getIntentSender(), 120, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Timber.e("Save to gallery: failed deleting %s", mTempSavedFileUri.getPath());
                        }*/
            }
        }
    }

    private void uploadChatImage(byte[] imageBytes, String fileName, String thumbnail, JSONObject jsonObject) {

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_IMAGE, "image.jpg", requestFile);
        RequestBody userId = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadChatImage(body, userId);

        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.d(TAG, "onResponse: " + data);
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    liveModel.updateImageUpload(jsonObject);
                    sendImageChat(jsonObject, data.get(Constants.TAG_USER_IMAGE), fileName);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

////// Addon voice message ///////////

/*    private void uploadAudioChat(byte[] audioBytes, String fileName, JSONObject jsonObject){
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), audioBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_AUDIO, fileName, requestFile);
        RequestBody userId = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadChatImage(body, userId);

        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.d(TAG, "onResponse: "+data);
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    liveModel.updateImageUpload(jsonObject);
                    sendAudioChat(jsonObject, data.get(Constants.TAG_USER_IMAGE), fileName,data.get("audio_duration"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }*/

    private void uploadAudio(byte[] audioBytes, String fileName, JSONObject jsonObject) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), audioBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_IMAGE, "audio.mp3", requestFile);
        RequestBody userId = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadAudio(body, userId);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> data = response.body();
                    if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        liveModel.updateImageUpload(jsonObject);
                        sendFileChat(jsonObject, data.get(Constants.TAG_USER_IMAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }


    private void sendChat(String type, String chatImage, String filePath, String thumbnail, String txt) {
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            switch (type) {
                case Constants.TAG_TEXT:
                    //msg = edtMessage.getText().toString();
                    msg = txt;
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;
              //Gif Addon

                case Constants.TAG_AUDIO:
                    msg = getFileName(filePath);
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE_END, Constants.TAG_SEND);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());

            /*Add receiver info in DB*/
            json.put(Constants.TAG_USER_ID, partnerId);
            json.put(Constants.TAG_USER_NAME, partnerName);
            json.put(Constants.TAG_USER_IMAGE, partnerImage);
            /*Add original message to DB*/
            json.put(Constants.TAG_MESSAGE, msg);
            json.put(Constants.TAG_THUMBNAIL, thumbnail);
            json.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_SEND);
            json.put(Constants.TAG_PROGRESS, "");
            liveModel.addMessage(getApplicationContext(), json);
            liveModel.addRecentChat(json);
            recyclerView.smoothScrollToPosition(0);
            edtMessage.setText("");
        } catch (JSONException e) {
            Logging.e(TAG, "sendChat: " + e.getMessage());
        }
    }

    private void sendImageChat(JSONObject jsonObject, String chatImage, String fileName) {
        try {
            String utcTime = jsonObject.getString(Constants.TAG_CHAT_TIME);
            String messageId = jsonObject.getString(Constants.TAG_MSG_ID);
            String msg = "";
            String type = jsonObject.getString(Constants.TAG_MSG_TYPE);
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;
              //Gif Addon
                case Constants.TAG_AUDIO:
                    msg = fileName;
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendImageChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());
            dbHelper.updateMessage(messageId, Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "sendImageChat: " + e.getMessage());
        }
    }

    //////Addon voice message ///////////

    private void sendFileChat(JSONObject jsonObject, String file) {
        try {
            String utcTime = jsonObject.getString(Constants.TAG_CHAT_TIME);
            String messageId = jsonObject.getString(Constants.TAG_MSG_ID);
            String msg = "";
            String type = jsonObject.getString(Constants.TAG_MSG_TYPE);
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = file;
                    break;
              //Gif Addon
                case Constants.TAG_AUDIO:
                    msg = file;
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_CHAT_TIME, utcTime);

            if (type.equals("audio"))
                json.put(Constants.TAG_SOUND_DURATION, Float.valueOf(socketDuration));

            Logging.i(TAG, "sendFileChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());
            dbHelper.updateMessage(messageId, Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "sendFileChat: " + e.getMessage());
        }
    }

    private JSONObject updateDB(String type, String chatImage, String filePath,
                                String thumbnail, String progress) {
        JSONObject json = new JSONObject();
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;
                //Gif Addon
                case Constants.TAG_AUDIO:
                    msg = getFileName(filePath);
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE_END, Constants.TAG_SEND);
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            json.put(Constants.TAG_USER_ID, partnerId);
            json.put(Constants.TAG_USER_NAME, partnerName);
            json.put(Constants.TAG_USER_IMAGE, partnerImage);
            /*Add original message to DB*/
            json.put(Constants.TAG_MESSAGE, msg);
            json.put(Constants.TAG_THUMBNAIL, thumbnail);
            json.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_UNREAD);
            json.put(Constants.TAG_PROGRESS, progress);
            liveModel.addMessage(getApplicationContext(), json);
            liveModel.addRecentChat(json);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "updateDB: " + e.getMessage());
        }
        return json;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    private void showAttachment() {
        // Prepare the View for the animation
        attachLay.setVisibility(View.VISIBLE);
        // Start the animation
        attachLay.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(100)
                .setListener(null);
    }

    private void hideAttachment() {
        attachLay.animate()
                .translationX(-100)
                .setDuration(100)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        attachLay.setVisibility(View.GONE);
                    }
                });
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
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initView:secondcall ");
                socketmessages=true;
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(message);
                    String type = jsonObject.getString(Constants.TAG_TYPE);
                    if (!isAdminChat) {
                        firstcall=0;
                        switch (type) {
                            case Constants.TAG_RECEIVE_CHAT:
                                recyclerView.smoothScrollToPosition(0);
                                if (blockStatus != null && blockStatus.equals(Constants.TAG_FALSE)) {
                                    sendReadStatus();
                                }
                                break;
                            case Constants.TAG_lISTEN_TYPING:
                                if (jsonObject.getString(Constants.TAG_RECEIVER_ID).equals(GetSet.getUserId())) {
                                    if (blockStatus.equals(Constants.TAG_FALSE)) {
                                        if (jsonObject.getString(Constants.TAG_TYPING_STATUS).equals(Constants.TAG_TYPING)) {
                                            txtSubTitle.setVisibility(View.GONE);
                                            txtTyping.setVisibility(View.VISIBLE);
                                        } else {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    txtTyping.setVisibility(View.GONE);
                                                    txtSubTitle.setVisibility(View.VISIBLE);
                                                }
                                            }, 1000);
                                        }
                                    } else {
                                        txtTyping.setVisibility(View.GONE);
                                    }
                                }
                                break;
                            case Constants.TAG_PROFILE_STATUS:
                                if (jsonObject.optString(Constants.TAG_ONLINE_STATUS).equals(Constants.TAG_TRUE)) {

                                    /*chat translation*/
                                    from = true;

                                    if (txtTyping.getVisibility() != View.VISIBLE) {
                                        txtSubTitle.setVisibility(View.VISIBLE);
                                        txtSubTitle.setText(getString(R.string.online));
                                    } else {
                                        txtSubTitle.setVisibility(View.GONE);
                                    }
                                } else if (jsonObject.getString(Constants.TAG_ONLINE_STATUS).equals(Constants.TAG_FALSE)) {
                                    txtSubTitle.setVisibility(View.GONE);
                                } else {
                                    if (txtTyping.getVisibility() != View.VISIBLE) {
                                        txtSubTitle.setVisibility(View.VISIBLE);
                                        txtSubTitle.setText(getString(R.string.last_seen_at) + " " + AppUtils.getRecentDate(getApplicationContext(),
                                                AppUtils.getTimeFromUTC(getApplicationContext(), jsonObject.optString(Constants.TAG_ONLINE_STATUS))).toLowerCase());
                                    }
                                }
                                break;
                            case Constants.TAG_RECEIVE_READ_STATUS:
                                liveModel.changeSentStatus();
                                break;
                            case Constants.TAG_OFFLINE_READ_STATUS:
                                liveModel.changeSentStatus();
                                break;
                        }
                    }
                    else
                    {
                    if(firstcall>0) {
                            getAdminMessages();
                    }
                    else
                    {
                        firstcall++;
                    }
                    }

                } catch (
                        JSONException e) {
                    Log.e(TAG, "onWebSocketMessage: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.STORAGE_REQUEST_CODE:
                if (checkPermissions(permissions)) {
//                    ImagePicker.pickImage(this, "Select your image:");
                    pickImage();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.STORAGE_REQUEST_CODE);
                        } else {
                            App.makeToast(getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
            case Constants.DOWNLOAD_REQUEST_CODE:
                if (!checkPermissions(permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.DOWNLOAD_REQUEST_CODE);
                        } else {
                            App.makeToast(getString(R.string.storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
            //Voice Message Addon
        }
        if (requestCode == Constants.VOICE_REQUEST_CODE) {
            recordAudioPermission = ContextCompat.checkSelfPermission(ChatActivity.this, RECORD_AUDIO);
            int storage = ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE);
            /*  setVoiceRecorder();*/
        }
    }

    public boolean checkPermissions(String[] permissions) {
        boolean permissionGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }
        return permissionGranted;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public class ChatAdapter extends RecyclerView.Adapter {

        public static final int VIEW_TYPE_SENT_TEXT = 0;
        public static final int VIEW_TYPE_RECEIVE_TEXT = 1;
        public static final int VIEW_TYPE_SENT_IMAGE = 2;
        public static final int VIEW_TYPE_RECEIVE_IMAGE = 3;
        public static final int VIEW_TYPE_SENT_STREAM = 4;
        public static final int VIEW_TYPE_RECEIVE_STREAM = 5;
        public static final int VIEW_TYPE_FOOTER = 7;
        public static final int VIEW_TYPE_CALL = 8;
        public static final int VIEW_TYPE_DATE = 9;
        public static final int VIEW_TYPE_SENT_AUDIO = 10;
        public static final int VIEW_TYPE_RECEIVE_AUDIO = 11;

        private final Context context;
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;
        RequestOptions requestOptions;


        public ChatAdapter(Context con) {
            context = con;
            requestOptions = new RequestOptions().error(R.drawable.rounded_square_semi_transparent)
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new BlurTransformation(15, 1));
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            /* Chat Translation*/
            if (viewType == VIEW_TYPE_RECEIVE_TEXT) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_text_bubble_receive, parent, false);
                viewHolder = new ReceiveMessageHolder(itemView);
            } else if (viewType == VIEW_TYPE_RECEIVE_IMAGE) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_image_bubble_receive, parent, false);
                viewHolder = new ReceiveImageHolder(itemView);
            } /* Gif Addon*/
            else if (viewType == VIEW_TYPE_RECEIVE_STREAM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_stream_bubble_receive, parent, false);
                viewHolder = new ReceiveStreamHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_TEXT) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_text_bubble_sent, parent, false);
                viewHolder = new SentMessageHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_image_bubble_sent, parent, false);
                viewHolder = new SentImageHolder(itemView);
            } /*Gif Addon*/
            else if (viewType == VIEW_TYPE_SENT_STREAM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_stream_bubble_sent, parent, false);
                viewHolder = new SentStreamHolder(itemView);
            } else if (viewType == VIEW_TYPE_DATE) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_date_layout, parent, false);
                viewHolder = new DateHolder(itemView);
            } else if (viewType == VIEW_TYPE_CALL) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.chat_call_layout, parent, false);
                viewHolder = new CallHolder(itemView);
            } /*Voice message Addon*/
            else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {
            if (holder instanceof ReceiveMessageHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveMessageHolder) holder).bind(chat);
            } else if (holder instanceof ReceiveImageHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveImageHolder) holder).bind(chat);
            } /*Gif Addon*/
            else if (holder instanceof ReceiveStreamHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveStreamHolder) holder).bind(chat);
            } else if (holder instanceof SentMessageHolder) {
                ChatResponse chat = chatList.get(position);
                ((SentMessageHolder) holder).bind(chat);
            } else if (holder instanceof SentImageHolder) {
                ((SentImageHolder) holder).bind(chatList.get(position));
            } /*Gif Addon*/
            else if (holder instanceof SentStreamHolder) {
                ((SentStreamHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof DateHolder) {
                ChatResponse chat = chatList.get(position);
                ((DateHolder) holder).bind(chat);
            } else if (holder instanceof CallHolder) {
                ChatResponse chat = chatList.get(position);
                ((CallHolder) holder).bind(chat);
            } /*Voice Message Addon*/
            else if (holder instanceof ChatFragment.ChatAdapter.FooterViewHolder) {
                ChatFragment.ChatAdapter.FooterViewHolder footerHolder = (ChatFragment.ChatAdapter.FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            ChatResponse chat = chatList.get(position);
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            else if (chat.getMessageType().equals(Constants.TAG_DATE)) {
                return VIEW_TYPE_DATE;
            } else if (chat.getMessageType().equals(Constants.TAG_MISSED)) {
                return VIEW_TYPE_CALL;
            } else if (chat.getChatType().equals(Constants.TAG_ADMIN) || chat.getMessageEnd().equals(Constants.TAG_RECEIVED)) {
                switch (chat.getMessageType()) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_RECEIVE_TEXT;
                    case Constants.TAG_IMAGE:
                        return VIEW_TYPE_RECEIVE_IMAGE;
                   //Gif Addon
                    case Constants.TAG_LIVE:
                    case Constants.TAG_RECORDED:
                        return VIEW_TYPE_RECEIVE_STREAM;
                    //Voice Message Addon
                    default:
                        return VIEW_TYPE_RECEIVE_TEXT;
                }
            } else {
                switch (chat.getMessageType()) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_SENT_TEXT;
                    case Constants.TAG_IMAGE:
                        return VIEW_TYPE_SENT_IMAGE;
                   //Gif Addon
                    case Constants.TAG_LIVE:
                    case Constants.TAG_RECORDED:
                        return VIEW_TYPE_SENT_STREAM;
                    //Voice Message Addon
                    default:
                        return VIEW_TYPE_SENT_TEXT;
                }
            }
        }

        @Override
        public int getItemCount() {
            int itemCount = chatList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public void setData(List<ChatResponse> newData) {
            chatList = newData;
            adapter.notifyDataSetChanged();

        }

        /*public void setData(List<ChatResponse> newData) {
            if (chatList == null) {
                chatList = newData;
                notifyItemRangeInserted(0, newData.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return chatList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newData.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        if ((chatList.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                                newData.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                            return true;
                        } else
                            return (chatList.get(oldItemPosition).getMessageId() != null && newData.get(newItemPosition).getMessageId() != null) &&
                                    (chatList.get(oldItemPosition).getMessageId().equals(newData.get(newItemPosition).getMessageId()));
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        if ((chatList.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                                newData.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                            return true;
                        } else {
                            return Objects.equals(chatList.get(oldItemPosition).getDeliveryStatus(), newData.get(newItemPosition).getDeliveryStatus());
                        }
                    }
                });
                chatList.clear();
                chatList.addAll(newData);
                result.dispatchUpdatesTo(adapter);
            }
        }*/

        public class DateHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtMsgTime)
            TextView txtMsgTime;

            public DateHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bind(ChatResponse chat) {
                txtMsgTime.setText(chat.getChatTime());
            }
        }

        public class CallHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtMsg)
            TextView txtMsg;

            public CallHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bind(ChatResponse chat) {
                txtMsg.setText(context.getString(R.string.missed_call_at) + " " +
                        AppUtils.getChatTime(context, chat.getChatTime()));
            }
        }

        public class SentMessageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            int start = 0;

            public SentMessageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                Spanned spannedText;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    spannedText = Html.fromHtml(chat.getMessage(), Html.FROM_HTML_MODE_LEGACY);
                } else {
                    spannedText = Html.fromHtml(chat.getMessage());
                }
                txtMessage.setText(spannedText);
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());

                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                userImage.setVisibility(View.GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                                .into(userImage);
                    }
                }
            }

            private ChatResponse getLastSentMessage(String s) {
                for (ChatResponse chatResponse : chatList) {
                    if (chatResponse.getMessageEnd().equals(Constants.TAG_SEND)) {
                        return chatResponse;
                    }
                }
                return null;
            }
        }

        public class SentImageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.chatImage)
            RoundedImageView chatImage;
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.btnUpload)
            ImageView btnUpload;
            @BindView(R.id.uploadProgress)
            ProgressWheel uploadProgress;
            @BindView(R.id.uploadLay)
            RelativeLayout uploadLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.itemLay)
            ConstraintLayout itemLay;

            public SentImageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));

                Log.d(TAG, "bind: chatTime" +AppUtils.getChatTime(context, chat.getChatTime()));

                if (storageUtils.checkIfImageExists(Constants.TAG_SENT, chat.getMessage())) {
                    uploadLay.setVisibility(View.GONE);
                    uploadProgress.stopSpinning();

                    Glide.with(getApplicationContext())
                            .load(storageUtils.getImageUri(Constants.TAG_SENT, chat.getMessage()))
                            .into(chatImage);


                    if (chat.getProgress().equals(Constants.TAG_COMPLETED)) {
                        uploadProgress.stopSpinning();
                        uploadLay.setVisibility(View.GONE);
                    } else {
                        btnUpload.setVisibility(View.VISIBLE);
                        uploadProgress.setVisibility(View.VISIBLE);
                    }
                } else {
                    uploadLay.setVisibility(View.GONE);
                }

                userImage.setVisibility(View.GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                                .into(userImage);
                    }
                }

                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (NetworkReceiver.isConnected()) {
                            if (storageUtils.checkIfImageExists(Constants.TAG_SENT, chat.getMessage())) {
                                File uploadedFile = storageUtils.getImage(Constants.TAG_SENT, chat.getMessage());
                                String sentPath = uploadedFile.getAbsolutePath();
                                BufferedInputStream buf;
                                try {
                                    int size = (int) uploadedFile.length();
                                    byte[] bytes = new byte[size];
                                    buf = new BufferedInputStream(new FileInputStream(uploadedFile));
                                    buf.read(bytes, 0, bytes.length);
                                    buf.close();
                                    uploadProgress.spin();
                                    uploadChatImage(bytes, sentPath, "", new JSONObject());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    }
                });

                chatImage.setOnClickListener(view -> {
                    if (storageUtils.checkIfImageExists(Constants.TAG_SENT, chat.getMessage())) {
                        Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                        imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                        imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_SENT);
                        startActivity(imageIntent);
                    }
                });

            }
        }

        /*Emoji and Gif in chat*/


        public class SentStreamHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;

            public SentStreamHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
                userImage.setVisibility(View.GONE);
                String[] link = chat.getMessage().split(Constants.TAG_HTTP);
                String message = null;
                if (link.length > 1) {
                    message = Constants.TAG_HTTP + link[1];

                    SpannableStringBuilder str = new SpannableStringBuilder(link[0] + message);
                    int startIndex = link[0].length();
                    int endIndex;
                    if (message.charAt(message.length() - 1) == '\"') {
                        endIndex = link[0].length() + message.length() - 1;
                    } else {
                        endIndex = link[0].length() + message.length();
                    }
//                    str.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    String finalMessage = message.trim();
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            if (NetworkReceiver.isConnected()) {
                                showLoadingDialog();
                                Log.i(TAG, "onClick: " + finalMessage);
                                FirebaseDynamicLinks.getInstance()
                                        .getDynamicLink(Uri.parse(finalMessage))
                                        .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                                            @Override
                                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                Uri deepLink = null;
                                                if (pendingDynamicLinkData != null) {
                                                    deepLink = pendingDynamicLinkData.getLink();
                                                    if (deepLink.getQueryParameter("stream_name") != null) {
                                                        String streamName = deepLink.getQueryParameter("stream_name");
                                                        getStreamDetails(streamName);
                                                    }
                                                }
                                                Log.i(TAG, "onSuccess: " + deepLink);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Logging.e(TAG, "onSuccess: " + e.getMessage());
                                    }
                                });
                            }
                        }
                    };
                    str.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtMessage.setText(str);
                } else {
                    message = link[0];
                    txtMessage.setText(message);
                }
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
//                txtMessage.setText(chat.getMessage());
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(View.VISIBLE);
                        Glide.with(context)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                                .into(userImage);
                    }
                }
            }
        }

        public class ReceiveMessageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.translatebtn)
            TextView translatebtn;

            public ReceiveMessageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                txtMessage.setText(chat.getMessage());
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));

                if (isAdminChat) {
                    userImage.setVisibility(View.GONE);
                    translatebtn.setVisibility(View.GONE);
                } else {
                    Glide.with(context)
                            .load(partnerImage)
                            .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                            .into(userImage);
                    if ((!SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE).equals("")
                            || !languageCode.equals("")) && !Util.isEmoji(chat.getMessage())) {
                        translatebtn.setVisibility(View.VISIBLE);
                    } else {
                        translatebtn.setVisibility(View.GONE);
                    }
                    userImage.setVisibility(View.VISIBLE);
                }

                /* Chat Translation*/
             /*   translatebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String translatedMsg = null;
                        new TranslateLanguageTasks(ChatActivity.this, chat.getMessage(), true) {

                            @Override
                            protected void onPostExecute(String translatedMsg) {
                                chat.setMessage(translatedMsg);
                                notifyItemChanged(getAdapterPosition());

                            }
                        }.execute();
                        Log.i(TAG, "translatedMsgonClick2: " + translatedMsg);
                    }
                });*/
            }
        }

        public class ReceiveImageHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.chatImage)
            RoundedImageView chatImage;
            @BindView(R.id.btnDownload)
            ImageView btnDownload;
            @BindView(R.id.downloadProgress)
            ProgressWheel downloadProgress;
            @BindView(R.id.downloadLay)
            RelativeLayout downloadLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;

            public ReceiveImageHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {

//                downloadLay.setAlpha(1f);
                downloadLay.setVisibility(View.GONE);
                if (isAdminChat) {
                    userImage.setVisibility(View.GONE);
                } else {
                    Glide.with(context)
                            .load(partnerImage)
                            .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                            .into(userImage);
                    userImage.setVisibility(View.VISIBLE);
                }

                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));


                Timber.i("chat=> %s", new Gson().toJson(chat));

                if (!storageUtils.checkIfImageExists(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION)) {
                    //  Uri file = storageUtils.getImageUri(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION);

                    /*downloadLay.setAlpha(1f);*/
                    //Timber.i("DownloadLay gone file.getPath=> %s", file.getPath());

                    Timber.i("DownloadLay visible"+chat.getMessage());
                    Timber.i("DownloadLay visible %s", getAdapterPosition());
                    /*downloadLay.setVisibility(View.VISIBLE);*/
                    // pass the request as a a parameter to thediskcache thumbnail request
                    downloadLay.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .asBitmap()
                            .load(chat.getMessage())
                            .apply(requestOptions)
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                                    storageUtils.saveToSDCard(resource, Constants.TAG_THUMBNAIL, chat.getAttachment() + Constants.IMAGE_EXTENSION);
                                    chatImage.setImageBitmap(resource);
                                    downloadLay.setVisibility(View.VISIBLE);
                                    return true;
                                }
                            }).into(chatImage);
                } else {

                    Glide.with(context)
                            .load(chat.getMessage())
                            .into(chatImage);
                    /*downloadLay.setAlpha(0f);*/
                    Timber.i("DownloadLay gone");


                }

                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{READ_MEDIA_IMAGES},
                                        Constants.DOWNLOAD_REQUEST_CODE);
                            } else {
                                Download();
                            }
                        }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{READ_EXTERNAL_STORAGE},
                                        Constants.DOWNLOAD_REQUEST_CODE);
                            } else {
                                Download();
                            }
                        }else if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.Q){
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE},
                                        Constants.DOWNLOAD_REQUEST_CODE);
                            } else {
                                Download();
                            }
                        }
                        else {
                            Download();
                        }
                    }

                    private void Download() {
                        if (NetworkReceiver.isConnected()) {
                            downloadProgress.spin();
                            DownloadImage downloadImage = new DownloadImage(chat.getMessage(), new DownloadListener() {
                                @Override
                                public void onDownloading() {

                                }

                                @Override
                                public void onDownloaded(Bitmap bitmap) {
                                    Glide.with(context)
                                            .load(chat.getMessage())
                                            .into(chatImage);
                                    downloadProgress.stopSpinning();
                                    downloadLay.setVisibility(View.GONE);
                                    //  downloadLay.setAlpha(0f);
                                }

                                @Override
                                public void onAudioDownloaded(String filePath) {

                                }
                            });
                            downloadImage.execute();
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    }
                });

                chatImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (storageUtils.checkIfImageExists(Constants.TAG_RECEIVED, chat.getMessage() + Constants.IMAGE_EXTENSION)) {
                            Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                            imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                            imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_RECEIVED);
                            startActivity(imageIntent);
                        } else {
                            btnDownload.performClick();
                        }
                    }
                });
            }
        }

        /*Emoji and Gif in chat*/


        public class ReceiveStreamHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.userImage)
            RoundedImageView userImage;
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.itemLay)
            RelativeLayout itemLay;

            public ReceiveStreamHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            public void bind(ChatResponse chat) {
                Glide.with(context)
                        .load(partnerImage)
                        .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                        .into(userImage);
                userImage.setVisibility(View.VISIBLE);
                String[] link = chat.getMessage().split(Constants.TAG_HTTP);
                String message = null;
                if (link.length > 1) {
                    message = Constants.TAG_HTTP + link[1];
                    SpannableStringBuilder str = new SpannableStringBuilder(link[0] + message);
                    int startIndex = link[0].length();
                    int endIndex;
                    if (message.charAt(message.length() - 1) == '\"') {
                        endIndex = link[0].length() + message.length() - 1;
                    } else {
                        endIndex = link[0].length() + message.length();
                    }
//                    str.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    String finalMessage = message.trim();
                    str.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            if (NetworkReceiver.isConnected()) {
                                showLoadingDialog();
                                Log.i(TAG, "onClick: " + finalMessage);
                                FirebaseDynamicLinks.getInstance()
                                        .getDynamicLink(Uri.parse(finalMessage))
                                        .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                                            @Override
                                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                Uri deepLink = null;
                                                if (pendingDynamicLinkData != null) {
                                                    deepLink = pendingDynamicLinkData.getLink();
                                                    if (deepLink.getQueryParameter("stream_name") != null) {
                                                        String streamName = deepLink.getQueryParameter("stream_name");
                                                        getStreamDetails(streamName);
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Logging.e(TAG, "onSuccess: " + e.getMessage());
                                    }
                                });
                            }
                        }
                    }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtMessage.setText(str);
                } else {
                    message = link[0];
                    txtMessage.setText(message);
                }
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
//                txtMessage.setText(chat.getMessage());
                txtChatTime.setText(AppUtils.getChatTime(context, chat.getChatTime()));
            }
        }

        // Voice Message Addon
        /*Add Sent and ReceivedVoiceHolder CLass*/

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }

    public void getStreamDetails(String streamName) {
        if (NetworkReceiver.isConnected()) {
            if (streamApiCall != null) {
                streamApiCall.cancel();
            }
            if (!isBackPressed) {
                showLoadingDialog();
                streamApiCall = apiInterface.getStreamDetails(GetSet.getUserId(), streamName);
                streamApiCall.enqueue(new Callback<StreamDetails>() {
                    @Override
                    public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                StreamDetails streamDetails = response.body();
                                if (streamDetails.getStreamBlocked() == 1) {
                                    App.makeToast(getString(R.string.broadcast_deactivated_by_admin));
                                } else {
                                    /*if (streamDetails.getType().equals(Constants.TAG_LIVE)) {
                                        appUtils.pauseExternalAudio();
                                        Intent i = new Intent(getApplicationContext(), SubscribeActivity.class);
                                        Bundle arguments = new Bundle();
                                        arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, streamDetails);
                                        i.putExtra(Constants.TAG_FROM, Constants.TAG_CHAT);
                                        i.putExtras(arguments);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(i);
                                    } else {
                                        appUtils.pauseExternalAudio();
                                        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                                        intent.putExtra(StreamConstants.TAG_STREAM_DATA, streamDetails);
                                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_CHAT);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(intent);
                                    }*/
                                }
                                hideLoadingDialog();
                            } else {
                                if (response.body().getMessage() != null && response.body().getMessage().equals("No Stream found")) {
                                    App.makeToast(getString(R.string.no_stream_found));
                                } else {
                                    App.makeToast(getString(R.string.something_went_wrong));
                                }
                                hideLoadingDialog();
                            }
                        } else {
                            App.makeToast(getString(R.string.something_went_wrong));
                            hideLoadingDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<StreamDetails> call, Throwable t) {
                        if (!call.isCanceled()) {
                            call.cancel();
                            hideLoadingDialog();
                        }
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });
            }
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
            finish();
            getParentActivityIntent();
        }
    }

    private void initLoadingDialog() {
        dialogLoading = new DialogLoading();
        dialogLoading.setContext(this);
        dialogLoading.setCancelable(false);
    }

    private void showLoadingDialog() {
        if (!dialogLoading.isAdded())
            dialogLoading.show(getSupportFragmentManager(), TAG);
    }

    private void hideLoadingDialog() {
        dialogLoading.dismissAllowingStateLoss();
    }

    class PostDiffCallback extends DiffUtil.Callback {

        private final List<ChatResponse> oldPosts, newPosts;
        private ChatResponse temp;

        public PostDiffCallback(List<ChatResponse> oldPosts, List<ChatResponse> newPosts) {
            this.oldPosts = oldPosts;
            this.newPosts = newPosts;
        }

        @Override
        public int getOldListSize() {
            return oldPosts.size();
        }

        @Override
        public int getNewListSize() {
            return newPosts.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if ((oldPosts.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                    newPosts.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                return true;
            } else {
                return oldPosts.get(oldItemPosition).getMessageId().equals(newPosts.get(newItemPosition).getMessageId());
            }
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if ((oldPosts.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                    newPosts.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                return true;
            } else {
                ChatResponse newProduct = newPosts.get(newItemPosition);
                ChatResponse oldProduct = oldPosts.get(oldItemPosition);
                return newProduct.getMessageId().equals(oldProduct.getMessageId()) &&
                        newProduct.getDeliveryStatus().equals(oldProduct.getDeliveryStatus());


            }
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            newPosts.get(newItemPosition).setDeliveryStatus(Constants.TAG_READ);
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }


    /*private ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            // custom observer methods
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            // custom observer methods
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            // custom observer methods
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            // custom observer methods
            adapter.notifyItemRangeChanged(position, count);
        }
    };*/

    @SuppressLint("StaticFieldLeak")
    private class DownloadImage extends AsyncTask<String, Integer, Bitmap> {
        String imagePath;
        private Bitmap rotatedBitmap;
        int orientation = 0;
        String fileName;
        int fileLength;
        DownloadListener downloadListener;

        public DownloadImage(String imagePath, DownloadListener downloadListener) {
            this.imagePath = imagePath;
            this.downloadListener = downloadListener;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = /*Constants.CHAT_IMAGE_URL +*/ imagePath;
            Bitmap bitmap = null;
            fileName = getFileName(imageURL);
            rotatedBitmap = null;
            try {
                try {
                    InputStream inputStream = new URL(imageURL).openStream();
                    /*ExifInterface exif = null;     //Since API Level 5
                    try {
                        exif = new ExifInterface(inputStream);
                        orientation = exif.getRotationDegrees();
                        Logging.e(TAG, " orientation " + orientation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null) {
                        return null;
                    } else {
//                        rotatedBitmap = rotateImage(bitmap, orientation);
                        rotatedBitmap = bitmap;
                        storageUtils.saveToSDCard(rotatedBitmap, Constants.TAG_RECEIVED, fileName + Constants.IMAGE_EXTENSION);
                    }

                } catch (Exception e) {
                    Logging.e("Error Message", e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return rotatedBitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.e(TAG, "onProgressUpdate: " + values[0]);
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            downloadListener.onDownloaded(result);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public interface DownloadListener {
        void onDownloading();

        void onDownloaded(Bitmap bitmap);

        void onAudioDownloaded(String filePath);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }


    //////Addon voice message ///////////


    public void resumeExternalAudio(Context context) {
        if (AppUtils.isExternalPlay) {
            AppUtils.isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public void pauseExternalAudio(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            AppUtils.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    //Voice Message Addon


    //Smart Reply


}
