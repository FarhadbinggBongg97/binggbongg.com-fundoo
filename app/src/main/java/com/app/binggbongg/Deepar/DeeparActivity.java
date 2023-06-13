package com.app.binggbongg.Deepar;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.CameraActivity;
import com.app.binggbongg.fundoo.MainActivity;
import com.app.binggbongg.fundoo.Utility;
import com.app.binggbongg.fundoo.Video.PostVideoActivity;
import com.app.binggbongg.fundoo.Video.UploadSoundActivity;
import com.app.binggbongg.helper.CountDownAnimation;
import com.app.binggbongg.helper.OkayCancelCallback;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.model.FilterDetailsModel;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.PermissionsUtils;
import com.app.binggbongg.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ai.deepar.ar.ARErrorType;
import ai.deepar.ar.AREventListener;
import ai.deepar.ar.CameraResolutionPreset;
import ai.deepar.ar.DeepAR;
import ai.deepar.ar.DeepARImageFormat;
import es.dmoral.toasty.Toasty;
import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class DeeparActivity extends BaseFragmentActivity implements SurfaceHolder.Callback, AREventListener, filterClickCallBack,CountDownAnimation.CountDownListener {
    public static final String TAG = "DeeparActivity";
    public static int SELECT_VIDEO = 101;
    private boolean toggleClick = false;
    private LinearLayout BeforeVideoRecordingLayout;
    private ShapeableImageView flashIcon;
    String getRecordTime = "0", ChangeRecordTime = "0";
    public int i = 0;
    Boolean isFlash = false, isRecord = false;
    private CountDownTimer countDownTimer;
    private ProgressBar progress_bar;
    File audioFilePath, videoFilePath;
    private boolean mPermissionsGranted = false;
    MediaPlayer ring;
    private String[] mRequiredPermissions = new String[]{
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };

    private final String[] mRequiredPermissions12 = new String[]{
            CAMERA,
            RECORD_AUDIO
    };
    Bitmap icon;
    private CountDownAnimation countDownAnimation;
    private Boolean isCountDownRunning = false;
    TextView textView,gallaryLay,soundLay;
    String sound_id = "", video_id = "";
    GlFilter setfilter;
    String getSound_id, getSound_title, getSound_url, getSound_isfav, getSound_cover, gethashTag;

    ShapeableImageView cameraPageCloseButton;
    private BottomSheetDialog reShootDialog;
    AsyncTask<String, Void, String> songDownload;
    ProgressDialog pdLoading;
    // Default camera lens value, change to CameraSelector.LENS_FACING_BACK to initialize with back camera
    private int defaultLensFacing = CameraSelector.LENS_FACING_FRONT;
    private ARSurfaceProvider surfaceProvider = null;
    private int lensFacing = defaultLensFacing;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ByteBuffer[] buffers;
    private int currentBuffer = 0;
    private static final int NUMBER_OF_BUFFERS = 2;
    private static boolean useExternalCameraTexture = true;

    TextView filterName, goLiveBtn;

    filterClickCallBack clickCallBack;

    private RecyclerView filterRecyclerview;
    LinearLayoutManager layoutManager;

    ApiInterface apiInterface;
    private DeepAR deepAR;

    private int currentMask = 0;
    private int currentEffect = 0;
    private int currentFilter = 0;

    private int screenOrientation;

    ArrayList<String> effects, effect_name;
    int effectsThumb;

    private boolean recording = false;
    private boolean currentSwitchRecording = false;

    private int width = 0;
    private int height = 0;

    private File videoFileName;
    private List<FilterDetailsModel.Result> Deeparfilterlist = new ArrayList<>();
    private DeeparAdapter deeparAdapter;
    private RelativeLayout cameraOptions, recordLayout, filterRecLay, videoRecordingLayout;
    boolean cameraPermission = false, audioPermission = false, storagePermission = false;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<String[]> cameraPermissionResult;
    public static String[] recordPermissions = new String[]{
            CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};
    public static String[] recordPermissions12 = new String[]{
            CAMERA, RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deepar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recordPermissions = recordPermissions12;
            mRequiredPermissions = mRequiredPermissions12;
        }else if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.O){
            useExternalCameraTexture=false;
        }


        checkRecordPermissions();
    }




    private void checkRecordPermissions() {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeeparActivity.this, recordPermissions,
                        Constants.CAMERA_REQUEST_CODE);
            } else
                initialize();
        }else{
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(DeeparActivity.this, recordPermissions,
                        Constants.CAMERA_REQUEST_CODE);
            } else
                initialize();
        }


    }

    private String[] checkGalleryPermissions() {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            return new String[] {READ_MEDIA_VIDEO};
           /* if (ActivityCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(DeeparActivity.this,new String[] {READ_MEDIA_VIDEO},
                        100);
            } else
                initialize();*/
        }else{
            return new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE };
          /*  if (ActivityCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(DeeparActivity.this, new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE },
                        100);
            } else
                initialize();*/
        }


    }

   /* public void requestAudioPermission() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            return  ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO},100) ;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100) ;
        }
        else {
            ActivityCompat.requestPermissions(this, PermissionsUtils.AUDIO_RECORD_PERMISSION, 100);
        }
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA_REQUEST_CODE && grantResults.length > 0) {

            boolean isGranted=true;

            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                }
            }

            if (isGranted) {
                initialize();
            }else{
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) ) {
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        App.makeToast(getString(R.string.camera_storage_error));
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
                        App.makeToast(getString(R.string.camera_storage_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }
            }
        }else  if (requestCode == 100) {
            if (checkPermissions(permissions, DeeparActivity.this)) {

                if (!recording) {
                    // if (checkFilePath(audioFilePath)) {
                    if (StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_MIX_VIDEO + ".mp4").exists())
                        deleteDirectory(StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_MIX_VIDEO + ".mp4"));

                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO);
                }

//                new SplashActivity.GetAppDefaultTask().execute();
                //  ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, DeeparActivity.this)) {
                    Log.e(TAG, "onRequestPermissionsResult: :::::if" );
                    ActivityCompat.requestPermissions(DeeparActivity.this, permissions, 100);
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
        }else if ( requestCode== hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE) {
            if (!isPermissionsGranted(permissions)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                        requestPermissions(permissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                    } else {
                        Toast.makeText(getApplicationContext(), "Enable Camera and MicroPhone permissions", Toast.LENGTH_SHORT).show();
//                            openPermissionDialog(permissions);
                        //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermissions(permissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                    } else {
                        Toast.makeText(getApplicationContext(), "Enable Camera, MicroPhone and Storage permissions", Toast.LENGTH_SHORT).show();
//                            openPermissionDialog(permissions);
                        //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" +getPackageName()));
                        startActivity(i);
                    }
                }
            } else {
                intentToPublishActivity();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recordPermissions = recordPermissions12;
        }else if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.O){
            useExternalCameraTexture=false;
        }

        checkRecordPermissions();*/
       /* if(deepAR == null){
            initializeDeepAR();
        }*/
    }

    private void initialize() {
        Log.e(TAG, "initialize::::::check " );
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        clickCallBack = (filterClickCallBack) this;
//        getFilters();
        releaseDeepar();
        initializeDeepAR();
        initalizeViews();
        initializeFilters();
        setupCamera();
       // requestAudioPermission();

        deeparAdapter = new DeeparAdapter(Deeparfilterlist, DeeparActivity.this, clickCallBack);
        filterRecyclerview.setAdapter(deeparAdapter);
//        filterRecyclerview.addItemDecoration(new ItemDecoration(0, 0));

        deeparAdapter.notifyDataSetChanged();
    }

    private void getFilters() {
        Deeparfilterlist.clear();

        Call<FilterDetailsModel> call = apiInterface.getFillters();

        call.enqueue(new Callback<FilterDetailsModel>() {
            @Override
            public void onResponse(Call<FilterDetailsModel> call, Response<FilterDetailsModel> response) {
                Log.e(TAG, "onResponse: :::::::::::" + new Gson().toJson(response.body()));
                if (response.body() != null) {
                    if (response.body().getStatus().equals("true")) {
                        Deeparfilterlist.addAll(response.body().getResult());
                    } else {
                        initializeFilters();
                    }
                } else {
                    initializeFilters();
                }


            }

            @Override
            public void onFailure(Call<FilterDetailsModel> call, Throwable t) {
                initializeFilters();
                Log.e(TAG, "onFailure: :::::::::::Error on Deepar response:", t);
            }
        });
    }

    private void initializeFilters() {

        Deeparfilterlist = new ArrayList<>();

        effects = new ArrayList<>();
        effects.add("none");
        effects.add("viking_helmet.deepar");
        effects.add("MakeupLook.deepar");
        effects.add("Split_View_Look.deepar");
        effects.add("Emotions_Exaggerator.deepar");
        effects.add("Stallone.deepar");
        effects.add("flower_face.deepar");
        effects.add("galaxy_background.deepar");
        effects.add("Humanoid.deepar");
        effects.add("Neon_Devil_Horns.deepar");
        effects.add("Ping_Pong.deepar");
        effects.add("Pixel_Hearts.deepar");
        effects.add("Snail.deepar");
        effects.add("Hope.deepar");
        effects.add("Vendetta_Mask.deepar");
        effects.add("Fire_Effect.deepar");
        effects.add("burning_effect.deepar");
        effects.add("Elephant_Trunk.deepar");

        effect_name = new ArrayList<>();
        effect_name.add("none");
        effect_name.add("Viking helmet");
        effect_name.add("Makeup Look");
        effect_name.add("Split View Look");
        effect_name.add("Emotions Exaggerator");
        effect_name.add("Stallone");
        effect_name.add("flower face");
        effect_name.add("galaxy background");
        effect_name.add("Humanoid");
        effect_name.add("Neon Devil Horns");
        effect_name.add("Ping Pong");
        effect_name.add("Pixel Hearts");
        effect_name.add("Snail");
        effect_name.add("Hope");
        effect_name.add("Vendetta Mask");
        effect_name.add("Fire_Effect");
        effect_name.add("burning effect");
        effect_name.add("Elephant Trunk");

        int[] effect = {
                R.drawable.none,
                R.drawable.viking,
                R.drawable.makeup,
                R.drawable.split,
                R.drawable.emotion,
             /*   R.drawable.meter,*/
                R.drawable.stallone,
                R.drawable.flower_face,
                R.drawable.galaxy,
                R.drawable.humanoid,
                R.drawable.diffuse,
                R.drawable.pingbong,
                R.drawable.heart_effect,
                R.drawable.snail,
                R.drawable.hope,
                R.drawable.vendatta,
                R.drawable.fire_effect,
                R.drawable.burning_effect,
                R.drawable.trunk_multiply
        };


        for (int i = 0; i < effects.size(); i++) {
            FilterDetailsModel.Result result = new FilterDetailsModel.Result(
                    String.valueOf(i),
                    effect_name.get(i).toString(),
                    effects.get(i).toString(),
                    effect[i]
            );
            Deeparfilterlist.add(i, result);
        }

    }


    private void initCountDownAnimation() {
        countDownAnimation = new CountDownAnimation(textView, 5);
        countDownAnimation.setCountDownListener(this);
    }

    @Override
    public void onCountDownEnd(CountDownAnimation animation) {
        isCountDownRunning = false;
        if (!recording) {
            if (checkFilePath(audioFilePath)) {
                recordVideo();
            } else {
                Toasty.warning(this, R.string.warning_sound, Toasty.LENGTH_SHORT).show();
            }
        } else {
        }
    }
    private Boolean checkFilePath(File filepath) {
        if (filepath != null) return filepath.exists();
        return false;
    }

    private void initalizeViews() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        pdLoading = new ProgressDialog(DeeparActivity.this, R.style.CameraAlertDialog);

        // Previous files delete.
       // StorageUtils.getInstance(this).clearTempCachee();

        // Audio File Path
        audioFilePath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);
        // Video File Path
        videoFilePath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_VIDEO_TRIM + Utility.VIDEO_FORMAT);
        // Get Sound Files
        Bundle extras = getIntent().getBundleExtra(Constants.TAG_SOUND_DATA);
        // Get HashTag
        gethashTag = getIntent().getStringExtra(Constants.TAG_SELECT_HASH_TAG);

        if (extras != null) {
            sound_id = getSound_id = extras.getString(Constants.TAG_SOUND_ID);
            getSound_title = extras.getString(Constants.TAG_SOUND_TITLE);
            getSound_url = extras.getString(Constants.TAG_SOUND_URL);
            getSound_isfav = extras.getString(Constants.TAG_SOUND_IS_FAV);
            getSound_cover = extras.getString(Constants.TAG_SOUND_COVER);
            ChangeRecordTime = extras.getString(Constants.TAG_SOUND_DURATION);
            ChangeRecordTime = AppUtils.convertToSeconds(ChangeRecordTime); // Convert to seconds
            songDownload = new SongDownload().execute(getSound_url, String.valueOf(audioFilePath), getSound_id);
        }

        soundLay = findViewById(R.id.soundLay);
        gallaryLay = findViewById(R.id.gallaryLay);
        TextView timerLay = findViewById(R.id.timerLay);
        LinearLayout flashLay = findViewById(R.id.flashLay);
        flashIcon = findViewById(R.id.flashIcon);
        progress_bar = findViewById(R.id.progress_bar);
        cameraOptions = findViewById(R.id.cameraOptions);
        BeforeVideoRecordingLayout = findViewById(R.id.BeforeVideoRecordingLayout);
        recordLayout = findViewById(R.id.recordLayout);
        filterRecLay = findViewById(R.id.filterRecLay);
        ImageView filterClose = findViewById(R.id.filterClose);
        TextView filterLay = findViewById(R.id.filterLay);
        goLiveBtn = findViewById(R.id.tv_liveBtn);

        textView = findViewById(R.id.textView);
        videoRecordingLayout = findViewById(R.id.videoRecordingLayout);
        ShapeableImageView tickRecord = findViewById(R.id.tickRecord);
        ShapeableImageView stopRecordIcon = findViewById(R.id.stopRecordIcon);
        cameraPageCloseButton = findViewById(R.id.cameraPageCloseButton);

        cameraOptions.setVisibility(View.VISIBLE);
        soundLay.setVisibility(View.VISIBLE);
        gallaryLay.setVisibility(View.VISIBLE);
        videoRecordingLayout.setVisibility(View.GONE);
        recordLayout.setVisibility(View.VISIBLE);
        goLiveBtn.setVisibility(View.VISIBLE);
        
        ImageButton previousMask = findViewById(R.id.previousMask);
        ImageButton nextMask = findViewById(R.id.nextMask);
        filterRecyclerview = findViewById(R.id.deepRecRV);
        filterName=findViewById(R.id.filter);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filterRecyclerview.setLayoutManager(new ScaleLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        SurfaceView arView = findViewById(R.id.surface);

        arView.getHolder().addCallback(this);

        // Surface might already be initialized, so we force the call to onSurfaceChanged
        arView.setVisibility(View.GONE);
        arView.setVisibility(View.VISIBLE);
        
        
        initCountDownAnimation();

        stopRecordIcon.setOnClickListener(v -> {
            App.preventMultipleClick(stopRecordIcon);
            stopRecording();
        });

        tickRecord.setOnClickListener(v -> {
            App.preventMultipleClick(tickRecord);
            stopRecording();
        });

        goLiveBtn.setOnClickListener(v -> {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mPermissionsGranted = checkPermissions( mRequiredPermissions,DeeparActivity.this);
                    if (!mPermissionsGranted) {
                        ActivityCompat.requestPermissions(DeeparActivity.this, mRequiredPermissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                    } else {
                        mPermissionsGranted = true;
                        intentToPublishActivity();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mPermissionsGranted = checkPermissions( mRequiredPermissions,DeeparActivity.this);
                    if (!mPermissionsGranted) {
                        ActivityCompat.requestPermissions(DeeparActivity.this, mRequiredPermissions, hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.STREAM_REQUEST_CODE);
                    } else {
                        mPermissionsGranted = true;
                        intentToPublishActivity();
                    }
                } else {
                    mPermissionsGranted = true;
                    intentToPublishActivity();
                }
      /*      Intent intent = new Intent(getApplicationContext(), LiveStreamActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
            intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
            intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
            intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
            startActivity(intent);
            finish();*/
        });

        soundLay.setOnClickListener(v -> {
            App.preventMultipleClick(soundLay);
            Intent intent = new Intent(this, UploadSoundActivity.class);
            intent.putExtra(Constants.TAG_SOUND_ID, sound_id);
            startActivityForResult(intent, Constants.CAMERA_SOUND);
        });

        filterLay.setOnClickListener(v -> {
            App.preventMultipleClick(filterLay);
            cameraOptions.setVisibility(View.GONE);
            recordLayout.setVisibility(View.GONE);
            filterRecLay.setVisibility(View.VISIBLE);
        });
        filterClose.setOnClickListener(v -> {
            filterRecLay.setVisibility(View.GONE);
            cameraOptions.setVisibility(View.VISIBLE);
            recordLayout.setVisibility(View.VISIBLE);
        });

        gallaryLay.setOnClickListener(v -> {
            App.preventMultipleClick(gallaryLay);
            isRecord = true;
            if(checkPermissions(checkGalleryPermissions(),DeeparActivity.this)){
                //   if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") || BuildConfig.DEBUG) {
                if (!recording) {
                    // if (checkFilePath(audioFilePath)) {
                    if (StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_MIX_VIDEO + ".mp4").exists())
                        deleteDirectory(StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_MIX_VIDEO + ".mp4"));

                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO);
                    /*} else {
                        Toasty.warning(this, R.string.warning_sound, Toasty.LENGTH_SHORT).show();
                    }*/
                }
           /* } else {
                Toasty.error(this, R.string.prime_alert, Toasty.LENGTH_SHORT).show();
            }*/

            }else{
                ActivityCompat.requestPermissions(DeeparActivity.this, checkGalleryPermissions(), 100);
//                requestAudioPermission();
                //initializeDeepAR();
            }
        });

//        flashLay.setOnClickListener(v -> {
//
//            if (arView != null) {
//                arView
//                deepAR.switchFlashMode();
//                gpuCameraRecorder.changeAutoFocus();
//                if (!isFlash) {
//                    isFlash = true;
//                    flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.flashon));
//                } else {
//                    isFlash = false;
//                    flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.flashoff));
//                }
//            } else
//                Toasty.info(DeeparActivity.this, R.string.flash_not_support, Toasty.LENGTH_SHORT).show();
//        });


        timerLay.setOnClickListener(v -> {
            App.preventMultipleClick(timerLay);
            if (!isCountDownRunning) {
                if (checkFilePath(audioFilePath)) {
                    cameraOptions.setVisibility(View.GONE);
                    recordLayout.setVisibility(View.GONE);
                    startCountDownAnimation();
                } else {
                    Toasty.warning(this, R.string.warning_sound, Toasty.LENGTH_SHORT).show();
                }
            }
        });

//
//        filterRecyclerview.setOnCenterItemChangedListener(new StickyRecyclerView.OnCenterItemChangedListener() {
//            @Override
//            public void onCenterItemChanged(int centerPosition) {
//
//                Log.e(TAG, "onCenterItemChanged: ::::::::::::::"+centerPosition );
//                filterName.setText(effects.get(centerPosition).replace(".deepar",""));
//                deepAR.switchEffect("effect", getFilterPath(effects.get(centerPosition)));
//            }
//        });
//


        final ShapeableImageView screenshotBtn = findViewById(R.id.recordButton);
//        screenshotBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deepAR.takeScreenshot();
//            }
//        });

        TextView switchCamera = findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = false;
                lensFacing = lensFacing == CameraSelector.LENS_FACING_FRONT ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
                //unbind immediately to avoid mirrored frame.
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setupCamera();
            }
        });

//        ImageButton openActivity = findViewById(R.id.openActivity);
//        openActivity.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent myIntent = new Intent(MainActivity.this, BasicActivity.class);
//                MainActivity.this.startActivity(myIntent);
//            }
//
//
//        });


//        final TextView screenShotModeButton = findViewById(R.id.screenshotModeButton);
//        final TextView recordModeBtn = findViewById(R.id.recordModeButton);

//        recordModeBtn.getBackground().setAlpha(0x00);
//        screenShotModeButton.getBackground().setAlpha(0xA0);

//        screenShotModeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(currentSwitchRecording) {
//                    if(recording) {
//                        Toast.makeText(getApplicationContext(), "Cannot switch to screenshots while recording!", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    recordModeBtn.getBackground().setAlpha(0x00);
//                    screenShotModeButton.getBackground().setAlpha(0xA0);
//                    screenshotBtn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            deepAR.takeScreenshot();
//                        }
//                    });
//
//                    currentSwitchRecording = !currentSwitchRecording;
//                }
//            }
//        });


//
//        recordModeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {

        if (!currentSwitchRecording) {

//                    recordModeBtn.getBackground().setAlpha(0xA0);
//                    screenShotModeButton.getBackground().setAlpha(0x00);
            screenshotBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.preventMultipleClick(screenshotBtn);

                    if (StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_MIX_VIDEO + ".mp4").exists())
                        deleteDirectory(StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_MIX_VIDEO + ".mp4"));
                    findViewById(R.id.stopRecordIcon).setEnabled(false);
                    findViewById(R.id.tickRecord).setEnabled(false);
                    findViewById(R.id.stopRecordIcon).setAlpha(0.3f);
                    findViewById(R.id.tickRecord).setAlpha(0.3f);
                    recordVideo();

                }
            });

        }
//            }
//        });


        cameraPageCloseButton.setOnClickListener(v -> backPressed());


    }
    public void deleteDirectory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isDirectory()) {
                            deleteDirectory(files[i]);
                        } else {
                            files[i].delete();
                        }
                    }
                }
            }
            file.delete();
        }
    }

    private void openReshootDialog() {

        View reShoot_sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_reshoot, null);
        reShootDialog = new BottomSheetDialog(this, R.style.reshootBottomDialog); // Style here
        reShootDialog.setContentView(reShoot_sheetView);
        reShootDialog.getBehavior().setHideable(false);

        TextView textView = reShoot_sheetView.findViewById(R.id.title);
        textView.setText(R.string.alert_close_this_video);

        reShoot_sheetView.findViewById(R.id.reshoot).setOnClickListener(v -> {

            if (reShootDialog != null && reShootDialog.isShowing()) {
                if (deepAR != null) deepAR.stopVideoRecording();
                recording = false;
                timerCancel();
            }
        });

        reShoot_sheetView.findViewById(R.id.cancel).setOnClickListener(v -> reShootDialog.dismiss());
        reShoot_sheetView.findViewById(R.id.exit).setOnClickListener(v -> finish());

        reShootDialog.show();
    }


    private void backPressed() {
        if (!recording) {
            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
            finish();
        } else openReshootDialog();
    }

    private void startCountDownAnimation() {
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        countDownAnimation.setAnimation(animationSet);
        countDownAnimation.setStartCount(5);
        countDownAnimation.start();
        isCountDownRunning = true;
    }


    private void timerCancel() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            progress_bar.setProgress(0);
            progress_bar.invalidate();
        }
    }

    private void timerStart() {
        i = 0;
        getRecordTime = GetSet.getMax_sound_duration()!=null? GetSet.getMax_sound_duration():"30";
        if(!getRecordTime.equals("0") && !ChangeRecordTime.equals("0")){
            if (Integer.parseInt(getRecordTime) <= Integer.parseInt(ChangeRecordTime))
                getRecordTime = GetSet.getMax_sound_duration() != null ? GetSet.getMax_sound_duration() : "30";
            else if (getRecordTime != null && Integer.parseInt(getRecordTime) >= Integer.parseInt(ChangeRecordTime))
                getRecordTime = ChangeRecordTime;
        } else{
            getRecordTime = GetSet.getMax_sound_duration()!=null? GetSet.getMax_sound_duration():"30";
        }

        long conSec = Long.parseLong(getRecordTime) * 1000;
        progress_bar.setMax(Integer.parseInt(getRecordTime));
        Timber.d("timerStart: getRec max %s", Integer.parseInt(getRecordTime));
        countDownTimer = new CountDownTimer(conSec, 1000) {
            public void onTick(long millisUntilFinished) {
                i++;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    progress_bar.setProgress((int) (i * Integer.parseInt(getRecordTime) / (conSec / 1000)), true);
                else
                    progress_bar.setProgress((int) (i * Integer.parseInt(getRecordTime) / (conSec / 1000)));

                if (i == 3) {
                    findViewById(R.id.stopRecordIcon).setAlpha(1f);
                    findViewById(R.id.tickRecord).setAlpha(1f);
                    findViewById(R.id.stopRecordIcon).setEnabled(true);
                    findViewById(R.id.tickRecord).setEnabled(true);
                }
            }

            public void onFinish() {
               stopRecording();
            }
        }.start();
    }

    private void stopRecording(){
        try {
            if (recording) {
                if (ring != null && ring.isPlaying()) ring.stop();
                timerCancel();
                deepAR.stopVideoRecording();
                Toast.makeText(getApplicationContext(), "Recording " + videoFileName.getName() + " saved.", Toast.LENGTH_LONG).show();
                recording = !recording;
            }
        } catch (Exception e) {
            Timber.i("Exception %s", e.getMessage());
        }

    }

    private void recordVideo() {

            videoFileName = (StorageUtils.getInstance(DeeparActivity.this).getTempFile(DeeparActivity.this, Constants.TAG_VIDEO_TRIM + ".mp4"));
//                                videoFileName = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "video_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".mp4");
            deepAR.startVideoRecording(videoFileName.toString(), width / 2, height / 2);
            Toast.makeText(getApplicationContext(), "Recording started.", Toast.LENGTH_SHORT).show();
            isRecord = false;
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    private void initializeDeepAR() {

        Log.e(TAG, "initializeDeepAR::::::check " );
        deepAR = new DeepAR(this);
        deepAR.setLicenseKey(getString(R.string.deepar_key));
        deepAR.initialize(this, this);
        setupCamera();
    }

    private void setupCamera() {
        Log.e(TAG, "initializeDeepAR::::::check " );
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if(cameraProviderFuture!=null){
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        bindImageAnalysis(cameraProvider);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        CameraResolutionPreset cameraResolutionPreset = CameraResolutionPreset.P1920x1080;
        int width;
        int height;
        int orientation = getScreenOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            width = cameraResolutionPreset.getWidth();
            height = cameraResolutionPreset.getHeight();
        } else {
            width = cameraResolutionPreset.getHeight();
            height = cameraResolutionPreset.getWidth();
        }

        Size cameraResolution = new Size(width, height);
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        if (useExternalCameraTexture) {
            Preview preview = new Preview.Builder()
                    .setTargetResolution(cameraResolution)
                    .build();

            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
            if (surfaceProvider == null && deepAR!=null /*&& !isRecord*/) {
                surfaceProvider = new ARSurfaceProvider(this, deepAR);
            }
            preview.setSurfaceProvider(surfaceProvider);
            surfaceProvider.setMirror(lensFacing == CameraSelector.LENS_FACING_FRONT);
        } else {
            buffers = new ByteBuffer[NUMBER_OF_BUFFERS];
            for (int i = 0; i < NUMBER_OF_BUFFERS; i++) {
                buffers[i] = ByteBuffer.allocateDirect(width * height * 3);
                buffers[i].order(ByteOrder.nativeOrder());
                buffers[i].position(0);
            }
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setTargetResolution(cameraResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageAnalyzer);
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis);
        }
    }

    private ImageAnalysis.Analyzer imageAnalyzer = new ImageAnalysis.Analyzer() {
        @Override
        public void analyze(@NonNull ImageProxy image) {
            byte[] byteData;
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byteData = new byte[ySize + uSize + vSize];

            //U and V are swapped
            yBuffer.get(byteData, 0, ySize);
            vBuffer.get(byteData, ySize, vSize);
            uBuffer.get(byteData, ySize + vSize, uSize);

            buffers[currentBuffer].put(byteData);
            buffers[currentBuffer].position(0);
            if (deepAR != null) {
                deepAR.receiveFrame(buffers[currentBuffer],
                        image.getWidth(), image.getHeight(),
                        image.getImageInfo().getRotationDegrees(),
                        lensFacing == CameraSelector.LENS_FACING_FRONT,
                        DeepARImageFormat.YUV_420_888,
                        image.getPlanes()[1].getPixelStride()
                );
            }
            currentBuffer = (currentBuffer + 1) % NUMBER_OF_BUFFERS;
            image.close();
        }
    };


    private String getFilterPath(String filterName) {
        if (filterName.equals("none")) {
            return null;
        }
        return "file:///android_asset/" + filterName;
    }

    private void gotoNext() {
        currentEffect = (currentEffect + 1) % effects.size();
        deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
    }

    private void gotoPrevious() {
        currentEffect = (currentEffect - 1 + effects.size()) % effects.size();
        deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void releaseDeepar(){
        recording = false;
        currentSwitchRecording = false;
        ProcessCameraProvider cameraProvider = null;
        try {
            if(cameraProviderFuture!=null){
                cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (surfaceProvider != null) {
            surfaceProvider.stop();
            surfaceProvider = null;
        }
        if(deepAR!=null) deepAR.release();
        deepAR = null;

        if (surfaceProvider != null) {
            surfaceProvider.stop();
        }
        if (deepAR == null) {
            return;
        }
        deepAR.setAREventListener(null);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
      releaseDeepar();

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If we are using on screen rendering we have to set surface view where DeepAR will render
        deepAR.setRenderSurface(holder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (deepAR != null) {
            deepAR.setRenderSurface(null, 0, 0);
        }
    }

    @Override
    public void screenshotTaken(Bitmap bitmap) {
        CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
        try {
            File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image_" + now + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(DeeparActivity.this, new String[]{imageFile.toString()}, null, null);
            Toast.makeText(DeeparActivity.this, "Screenshot " + imageFile.getName() + " saved.", Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void videoRecordingStarted() {

        recording = !recording;
        cameraOptions.setVisibility(View.GONE);
        soundLay.setVisibility(View.GONE);
        gallaryLay.setVisibility(View.GONE);
        goLiveBtn.setVisibility(View.GONE);
        if (audioFilePath!=null){
            ring = MediaPlayer.create(this, Uri.fromFile(audioFilePath));
            if (ring!=null && !ring.isPlaying())ring.start();
        }

        timerStart();
        recordLayout.setVisibility(View.VISIBLE);
        BeforeVideoRecordingLayout.setVisibility(View.GONE);
        videoRecordingLayout.setVisibility(View.VISIBLE);

        currentSwitchRecording = !currentSwitchRecording;
        Log.e(TAG, "videoRecordingStarted: ::::::::::::::::");

    }

    @Override
    public void videoRecordingFinished() {

        try {

            videoRecordingLayout.setVisibility(View.GONE);
            recordLayout.setVisibility(View.VISIBLE);
            BeforeVideoRecordingLayout.setVisibility(View.VISIBLE);
            cameraOptions.setVisibility(View.VISIBLE);
            soundLay.setVisibility(View.VISIBLE);
            gallaryLay.setVisibility(View.VISIBLE);
            goLiveBtn.setVisibility(View.VISIBLE);

            progress_bar.setProgress(0);

            if (reShootDialog != null && reShootDialog.isShowing()) {

                if (ring != null && ring.isPlaying()) ring.stop();
                timerCancel();
                reShootDialog.dismiss();

            } else {

                Timber.i("onRecordComplete()");
                gotoPostVideoPage("camera");
            }
        } catch (Exception e) {
            Timber.i("view exception %s", e.getMessage());
        }

//        Intent intent = new Intent();
//        intent.setData(FileProvider.getUriForFile(DeeparActivity.this,
//                BuildConfig.APPLICATION_ID + ".fileprovider", videoFileName));
//        setResult(Constants.DEEPAR_REQUEST_CODE, intent);
//        finish();
        Log.e(TAG, "videoRecordingFinished: ::::::::::::::::::" + FileProvider.getUriForFile(DeeparActivity.this,
                BuildConfig.APPLICATION_ID + ".fileprovider", videoFileName));
    }

    @Override
    public void videoRecordingFailed() {
        Log.e(TAG, "videoRecordingFailed: ::::::::::");
    }

    @Override
    public void videoRecordingPrepared() {

    }

    @Override
    public void shutdownFinished() {

    }

    @Override
    public void initialized() {
        // Restore effect state after deepar release
        if(!recording){
            Log.d(TAG, "recording" + recording);
        }else{
            deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
        }
    }

    @Override
    public void faceVisibilityChanged(boolean b) {

    }

    @Override
    public void imageVisibilityChanged(String s, boolean b) {

    }

    @Override
    public void frameAvailable(Image image) {

    }

    @Override
    public void error(ARErrorType arErrorType, String s) {
        Log.e(TAG, "error: :::::::::::::::"+s+":::"+arErrorType );
    }


    @Override
    public void effectSwitched(String s) {

    }

    @Override
    public void onFilterClick(int position, FilterDetailsModel.Result result) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                filterRecyclerview.smoothScrollToPosition(position);
            }
        }, 200);

        currentEffect=position;
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(filterRecyclerview);
        deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
        deeparAdapter.notifyDataSetChanged();
        filterName.setText(result.getFillterName());

        Log.e(TAG, "onFilterClick: :::::::::::::;" + position + ":::::::::" + new Gson().toJson(result));
    }


    public class DeeparAdapter extends RecyclerView.Adapter<DeeparAdapter.DeeparVH> {

        List<FilterDetailsModel.Result> filterList;
        Context context;
        filterClickCallBack callBack;
        private int selectedPostition = 0;

        public DeeparAdapter(List<FilterDetailsModel.Result> filterList, Context context, filterClickCallBack callBack) {
            this.filterList = filterList;
            this.context = context;
            this.callBack = callBack;
        }

        @NonNull
        @Override
        public DeeparVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_ar_filter, parent, false);
            return new DeeparVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeeparVH holder, @SuppressLint("RecyclerView") int position) {
            FilterDetailsModel.Result model = filterList.get(position);

            if(selectedPostition==position){
                holder.thumb.setStrokeWidth(5);
                holder.thumb.setStrokeColor(context.getColor(R.color.white));
            }else{
                holder.thumb.setStrokeWidth(0);
            }

            Glide.with(context)
                    .load(model.getImageUrl())
                    .error(R.drawable.magic_wand)
                    .into(holder.filterThumbnail);

//            holder.filterName.setText(model.getFillterName().replace(".deepar", ""));
//
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(selectedPostition!=position){
                        holder.thumb.setStrokeWidth(5);
                        holder.thumb.setStrokeColor(context.getColor(R.color.white));
                        callBack.onFilterClick(position, model);
                        selectedPostition =position;
                    }else{
                        holder.thumb.setStrokeWidth(0);
                    }
                    notifyDataSetChanged();

                }
            });

        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        public class DeeparVH extends RecyclerView.ViewHolder {

            MaterialCardView thumb;
            ImageView filterThumbnail;
            TextView filterName;

            public DeeparVH(@NonNull View itemView) {
                super(itemView);
                thumb = itemView.findViewById(R.id.thumb);
                filterName = itemView.findViewById(R.id.filterNameTV);
                filterThumbnail = itemView.findViewById(R.id.filterIV);
            }
        }
    }

    private class SongDownload extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL u = new URL(strings[0]);
                URLConnection conn = u.openConnection();
                int contentLength = conn.getContentLength();

                DataInputStream stream = new DataInputStream(u.openStream());

                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();

                DataOutputStream fos = new DataOutputStream(new FileOutputStream(strings[1]));
                fos.write(buffer);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Timber.d("FileNotFoundException: %s", e.getMessage());
                return null;
            } catch (IOException e) {
                Timber.d("IOException: %s", e.getMessage());
                return null;
            }
            return strings[1];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            /*AlertDialog.*/

            ValueAnimator processingAnimator = ValueAnimator.ofInt(0, 4);
            processingAnimator.setRepeatCount(Animation.INFINITE);
            processingAnimator.setDuration(1000L);
            processingAnimator.addUpdateListener(animation -> {
                // noinspection SetTextI18n
                pdLoading.setMessage("\tPlease wait" + getDots((Integer) processingAnimator.getAnimatedValue(), ""));
            });
            pdLoading.setCancelable(false);
            pdLoading.setOnShowListener(dialog -> processingAnimator.start());
            pdLoading.setOnDismissListener(dialog -> processingAnimator.cancel());
            pdLoading.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Timber.d("onPostExecute: result %s", result);

                Toasty.success(DeeparActivity.this, getResources().getString(R.string.selected_song_sucess), Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
            }
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

    public class ScaleLayoutManager extends LinearLayoutManager {

        private final float mShrinkAmount = 0.15f;
        private final float mShrinkDistance = 0.9f;

        public ScaleLayoutManager(Context context) {
            super(context);
        }

        public ScaleLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller=new CenterSmoothScroller(
                    recyclerView.getContext()
            );
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        @Override
        public void onLayoutCompleted(RecyclerView.State state) {
            super.onLayoutCompleted(state);
//            scaleChild();
        }

        @Override
        public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int orientation = getOrientation();
            if (orientation == HORIZONTAL) {
//                scaleChild();
                return super.scrollHorizontallyBy(dx, recycler, state);
            } else {
                return 0;
            }
        }

        private void scaleChild() {
            float midPoint = getWidth() / 2.f;
            float d1 = mShrinkDistance * midPoint;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                float childMidPoint = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f;
                float d = Math.min(d1, Math.abs(midPoint - childMidPoint));
                float scale = 1.05f - mShrinkAmount * d / d1;
                child.setScaleY(scale);
                child.setScaleX(scale);
            }
        }


    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart+(boxEnd-boxStart)/2)-(viewStart+(viewEnd-viewStart)/2) ;   }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_VIDEO) {
            if (data != null) {

                Log.e(TAG, "onActivityResult::::::::::::: " + data.getData());
                Log.d(TAG, "Maximum sound duration" + GetSet.getMax_sound_duration());
                TrimVideo.activity(String.valueOf(data.getData()))
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setMinToMax(Long.parseLong("5"), Long.parseLong(GetSet.getMax_sound_duration()))
                        .start(this);
            }
        } else if (requestCode == Constants.CAMERA_SOUND) {
            if (data != null) {
                sound_id = data.getStringExtra(Constants.TAG_SOUND_ID);
                ChangeRecordTime = AppUtils.convertToSeconds(data.getStringExtra(Constants.TAG_SOUND_DURATION));
                Timber.i("ChangeRecordTime %s", ChangeRecordTime);
            }
        } else if (resultCode == Constants.VIDEO_TRIMMER_CODE) {
            gotoPostVideoPage("gallery");
        } else if (requestCode == Constants.CAMERA_POST_PAGE) {
            Timber.i("reShoot calling %s", "reShoot "+resultCode);
//            if(resultCode==RESULT_OK) {

                reShoot();
          /*  }else{
                releaseDeepar();
                finish();
            }*/
        }else if(resultCode==Constants.DEEPAR_REQUEST_CODE){
            gotoPostVideoPage("camera");
        }
    }

    private void reShoot() {
        try {
            progress_bar.setProgress(0);
            videoRecordingLayout.setVisibility(View.GONE);
            recordLayout.setVisibility(View.VISIBLE);
            BeforeVideoRecordingLayout.setVisibility(View.VISIBLE);
            cameraOptions.setVisibility(View.VISIBLE);
            soundLay.setVisibility(View.VISIBLE);
            gallaryLay.setVisibility(View.VISIBLE);
            goLiveBtn.setVisibility(View.VISIBLE);
            checkRecordPermissions();
        } catch (Exception e) {
            Timber.i("view exception %s", e.getMessage());
        }
    }


    private void gotoPostVideoPage(String uploadVideoType) {

        if (reShootDialog != null) reShootDialog.dismiss();
        Intent intent = new Intent(DeeparActivity.this, PostVideoActivity.class);
        intent.putExtra(Constants.TAG_SOUND_ID, sound_id);
        intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
        intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
        intent.putExtra(Constants.TAG_TYPE, uploadVideoType);
        startActivityForResult(intent, Constants.CAMERA_POST_PAGE);
        releaseDeepar();
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }


    private boolean checkPermissions(String[] permissionList, DeeparActivity activity) {
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

    private boolean shouldShowRationale(String[] permissions, DeeparActivity activity) {
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
    private void intentToPublishActivity() {
        Intent stream = new Intent(DeeparActivity.this, PublishActivity.class);
        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
        stream.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
        stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
        stream.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
        stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
        startActivity(stream);
        finish();
    }

    public boolean isPermissionsGranted(String[] permissions) {
        boolean isGranted = false;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(DeeparActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            } else {
                isGranted = true;
            }
        }
        return isGranted;
    }
}
