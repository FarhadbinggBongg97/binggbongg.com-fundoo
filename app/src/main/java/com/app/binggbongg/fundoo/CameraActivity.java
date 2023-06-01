package com.app.binggbongg.fundoo;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.Deepar.DeeparActivity;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.Video.FilterType;
import com.app.binggbongg.fundoo.Video.ImageFilterType;
import com.app.binggbongg.fundoo.Video.PostVideoActivity;
import com.app.binggbongg.fundoo.Video.UploadSoundActivity;
import com.app.binggbongg.helper.CountDownAnimation;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.daasuu.gpuv.camerarecorder.CameraRecordListener;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder;
import com.daasuu.gpuv.camerarecorder.LensFacing;
import com.daasuu.gpuv.egl.filter.GlBrightnessFilter;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import timber.log.Timber;

public class CameraActivity extends BaseFragmentActivity implements CountDownAnimation.CountDownListener {

    private static final String TAG = CameraActivity.class.getSimpleName();
    public static int SELECT_VIDEO = 101;
    private boolean toggleClick = false;

    GLSurfaceView sampleGLView;
    public GPUCameraRecorder gpuCameraRecorder;

    FrameLayout frameLayout;
    private LinearLayout soundLay, gallaryLay, BeforeVideoRecordingLayout;
    private ShapeableImageView flashIcon;
    private RelativeLayout cameraOptions, recordLayout, filterRecLay, videoRecordingLayout;
    String getRecordTime, ChangeRecordTime = "";
    public int i = 0;
    protected LensFacing lensFacing = LensFacing.BACK;
    Boolean isFlash = false, isRecord = false;
    /*protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    protected int videoHeight = 720;*/
    private CountDownTimer countDownTimer;
    private ProgressBar progress_bar;
    RecyclerView filterRecyclerview,DeepRecyclerview;
    FilterAdapter filterAdapter;
    private int isSelectedPosition = -1;
    File audioFilePath, videoFilePath;
    MediaPlayer ring;
    GPUImageView imageView2;
    Bitmap icon;

    private CountDownAnimation countDownAnimation;
    private Boolean isCountDownRunning = false;
    TextView textView;

    String sound_id = "", video_id = "";
    GlFilter setfilter;
    String getSound_id, getSound_title, getSound_url, getSound_isfav, getSound_cover, gethashTag;

    ShapeableImageView cameraPageCloseButton;
    private BottomSheetDialog reShootDialog;

    AsyncTask<String, Void, String> songDownload;
    ProgressDialog pdLoading;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        pdLoading = new ProgressDialog(CameraActivity.this, R.style.CameraAlertDialog);

        // Previous files delete.
        StorageUtils.getInstance(this).clearTempCachee();

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

        apiInterface= ApiClient.getClient().create(ApiInterface.class);
        imageView2 = new GPUImageView(this);

        getDeepFilters();
        initView();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {

        frameLayout = findViewById(R.id.wrap_view);
        soundLay = findViewById(R.id.soundLay);
        gallaryLay = findViewById(R.id.gallaryLay);
        LinearLayout flipLay = findViewById(R.id.flipLay);
        LinearLayout filterLay = findViewById(R.id.filterLay);
        LinearLayout timerLay = findViewById(R.id.timerLay);
        LinearLayout flashLay = findViewById(R.id.flashLay);
        LinearLayout deeparLay=findViewById(R.id.deeparLay);
        flashIcon = findViewById(R.id.flashIcon);
        ShapeableImageView recordIcon = findViewById(R.id.recordIcon);
        progress_bar = findViewById(R.id.progress_bar);
        cameraOptions = findViewById(R.id.cameraOptions);
        filterRecyclerview = findViewById(R.id.filterRecyclerview);
        recordLayout = findViewById(R.id.recordLayout);
        BeforeVideoRecordingLayout = findViewById(R.id.BeforeVideoRecordingLayout);
        filterRecLay = findViewById(R.id.filterRecLay);
        ImageView filterClose = findViewById(R.id.filterClose);
        textView = findViewById(R.id.textView);
        videoRecordingLayout = findViewById(R.id.videoRecordingLayout);
        ShapeableImageView tickRecord = findViewById(R.id.tickRecord);
        ShapeableImageView stopRecordIcon = findViewById(R.id.stopRecordIcon);
        cameraPageCloseButton = findViewById(R.id.cameraPageCloseButton);
        icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.filter_sample_image);
        filterRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterAdapter = new FilterAdapter(this);
        filterRecyclerview.setAdapter(filterAdapter);
        cameraOptions.setVisibility(View.VISIBLE);
        soundLay.setVisibility(View.VISIBLE);
        gallaryLay.setVisibility(View.VISIBLE);
        videoRecordingLayout.setVisibility(View.GONE);
        recordLayout.setVisibility(View.VISIBLE);
        DeepRecyclerview=findViewById(R.id.deepRecRV);

        initCountDownAnimation();

        GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
        glBrightnessFilter.setBrightness(0.1f);
        setfilter = glBrightnessFilter;

        stopRecordIcon.setOnClickListener(v -> {
            App.preventMultipleClick(stopRecordIcon);
            stopRecording();
        });

        tickRecord.setOnClickListener(v -> {
            App.preventMultipleClick(tickRecord);
            stopRecording();
        });

        soundLay.setOnClickListener(v -> {
            App.preventMultipleClick(soundLay);
            Intent intent = new Intent(this, UploadSoundActivity.class);
            intent.putExtra(Constants.TAG_SOUND_ID, sound_id);
            startActivityForResult(intent, Constants.CAMERA_SOUND);
        });

        gallaryLay.setOnClickListener(v -> {
            App.preventMultipleClick(gallaryLay);
            if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") || BuildConfig.DEBUG) {
                if (!isRecord) {
                    if (checkFilePath(audioFilePath)) {
                        Intent intent = new Intent();
                        intent.setType("video/*");
                        intent.setAction(Intent.ACTION_PICK);
                        startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO);
                    } else {
                        Toasty.warning(this, R.string.warning_sound, Toasty.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toasty.error(this, R.string.prime_alert, Toasty.LENGTH_SHORT).show();
            }

        });

        deeparLay.setOnClickListener(v ->{
            Intent intent = new Intent(CameraActivity.this, DeeparActivity.class);
            startActivityForResult(intent,Constants.DEEPAR_REQUEST_CODE);
        });

        flashLay.setOnClickListener(v -> {

            if (gpuCameraRecorder != null && gpuCameraRecorder.isFlashSupport()) {
                gpuCameraRecorder.switchFlashMode();
                gpuCameraRecorder.changeAutoFocus();
                if (!isFlash) {
                    isFlash = true;
                    flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.flashon));
                } else {
                    isFlash = false;
                    flashIcon.setImageDrawable(getResources().getDrawable(R.drawable.flashoff));
                }
            } else
                Toasty.info(CameraActivity.this, R.string.flash_not_support, Toasty.LENGTH_SHORT).show();
        });
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
        flipLay.setOnClickListener(v -> {
            releaseCamera();
            if (lensFacing == LensFacing.BACK) {
                lensFacing = LensFacing.FRONT;
                setfilter = FilterType.gpuVideoView(0, this);
            } else {
                lensFacing = LensFacing.BACK;
                setfilter = FilterType.gpuVideoView(0, this);

            }
            toggleClick = true;
        });
        recordIcon.setOnClickListener(v -> {
            App.preventMultipleClick(recordIcon);
            //if (checkFilePath(audioFilePath)) {

                if (StorageUtils.getInstance(CameraActivity.this).getTempFile(CameraActivity.this, Constants.TAG_MIX_VIDEO + ".mp4").exists())
                    deleteDirectory(StorageUtils.getInstance(CameraActivity.this).getTempFile(CameraActivity.this, Constants.TAG_MIX_VIDEO + ".mp4"));

                findViewById(R.id.stopRecordIcon).setEnabled(false);
                findViewById(R.id.tickRecord).setEnabled(false);
                findViewById(R.id.stopRecordIcon).setAlpha(0.3f);
                findViewById(R.id.tickRecord).setAlpha(0.3f);
                recordVideo();
          /*  } else {
                Toasty.warning(this, R.string.warning_sound, Toasty.LENGTH_SHORT).show();
            }*/
        });

        cameraPageCloseButton.setOnClickListener(v -> backPressed());
    }

    private void getDeepFilters() {

        ArrayList<String> effects;
        effects = new ArrayList<>();
        effects.add("none");
        effects.add("viking_helmet.deepar");
        effects.add("MakeupLook.deepar");
        effects.add("Split_View_Look.deepar");
        effects.add("Emotions_Exaggerator.deepar");
        effects.add("Emotion_Meter.deepar");
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



//        List<FilterDetailsModel.Result> DeepfilterList = new ArrayList<>();
//
//       Call<FilterDetailsModel> call=apiInterface.getFillters();
//
//       call.enqueue(new Callback<FilterDetailsModel>() {
//           @Override
//           public void onResponse(Call<FilterDetailsModel> call, Response<FilterDetailsModel> response) {
//               Log.e(TAG, "onResponse: ::::::::::::::::"+new Gson().toJson(response.body()) );
//               if (response.body() != null) {
//
//                   if (response.body().getStatus().equals("true")){
//                       DeepfilterList.addAll(response.body().getResult());
//                       Toast.makeText(CameraActivity.this, "Oops! ", Toast.LENGTH_SHORT).show();
//
//                   }else{
//                       Toast.makeText(CameraActivity.this, "Oops! Something went wrong!", Toast.LENGTH_SHORT).show();
//                   }
//               }
//           }
//
//           @Override
//           public void onFailure(Call<FilterDetailsModel> call, Throwable t) {
//               t.printStackTrace();
//               Log.e(TAG, "onFailure: ::::::::::::::",t );
//           }
//       });

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
                if (gpuCameraRecorder != null) gpuCameraRecorder.stop();
                isRecord = false;
                timerCancel();
            }
        });

        reShoot_sheetView.findViewById(R.id.cancel).setOnClickListener(v -> reShootDialog.dismiss());
        reShoot_sheetView.findViewById(R.id.exit).setOnClickListener(v -> finish());

        reShootDialog.show();
    }

    private void stopRecording() {

        try {
            if (ring != null && ring.isPlaying()) ring.stop();
            timerCancel();
            if (gpuCameraRecorder != null) {
                gpuCameraRecorder.stop();
            }
            isRecord = false;
        } catch (Exception e) {
            Timber.i("Exception %s", e.getMessage());
        }


    }

    private void gotoPostVideoPage(String uploadVideoType) {

        if (reShootDialog != null) reShootDialog.dismiss();
        Intent intent = new Intent(CameraActivity.this, PostVideoActivity.class);
        intent.putExtra(Constants.TAG_SOUND_ID, sound_id);
        intent.putExtra(Constants.TAG_VIDEO_ID, video_id);
        intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag);
        intent.putExtra(Constants.TAG_TYPE, uploadVideoType);
        startActivityForResult(intent, Constants.CAMERA_POST_PAGE);
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }


    private void initCountDownAnimation() {
        countDownAnimation = new CountDownAnimation(textView, 5);
        countDownAnimation.setCountDownListener(this);
    }

    @Override
    public void onCountDownEnd(CountDownAnimation animation) {
        isCountDownRunning = false;
        if (!isRecord) {
            if (checkFilePath(audioFilePath)) {
                recordVideo();
            } else {
                Toasty.warning(this, R.string.warning_sound, Toasty.LENGTH_SHORT).show();
            }
        } else {
        }
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


    private void recordVideo() {

        if (gpuCameraRecorder != null) {

            GetSet.setMax_sound_duration("30");
            getRecordTime = GetSet.getMax_sound_duration() != null ? GetSet.getMax_sound_duration() : "";

            //if (!ChangeRecordTime.equals("") && !getRecordTime.equals("")) {

               /* if (Integer.parseInt(getRecordTime) <= Integer.parseInt(ChangeRecordTime))
                    getRecordTime = GetSet.getMax_sound_duration() != null ? GetSet.getMax_sound_duration() : "";
                else if (getRecordTime != null && Integer.parseInt(getRecordTime) >= Integer.parseInt(ChangeRecordTime))
                    getRecordTime = ChangeRecordTime;*/

                Timber.i("ChangeRecordTime Helloooo %s", getRecordTime);

                videoFilePath = (StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_VIDEO_TRIM + ".mp4"));
                gpuCameraRecorder.start(String.valueOf(videoFilePath));
                cameraOptions.setVisibility(View.GONE);
                soundLay.setVisibility(View.GONE);
                gallaryLay.setVisibility(View.GONE);
                ring = MediaPlayer.create(this, Uri.fromFile(audioFilePath));
                //ring.start();
                timerStart();
                isRecord = true;
                recordLayout.setVisibility(View.VISIBLE);
                BeforeVideoRecordingLayout.setVisibility(View.GONE);
                videoRecordingLayout.setVisibility(View.VISIBLE);
            //}
        }

    }


    private Boolean checkFilePath(File filepath) {
        if (filepath != null) return filepath.exists();
        return false;
    }

    private void timerStart() {
        i = 0;
        long conSec = Long.parseLong("30") * 1000;
        progress_bar.setMax(Integer.parseInt("30"));
        Timber.d("timerStart: getRec max %s", Integer.parseInt("30"));
        countDownTimer = new CountDownTimer(conSec, 1000) {
            public void onTick(long millisUntilFinished) {
                i++;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    progress_bar.setProgress((int) (i * Integer.parseInt("30") / (conSec / 1000)), true);
                else
                    progress_bar.setProgress((int) (i * Integer.parseInt("30") / (conSec / 1000)));

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

    private void timerCancel() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            progress_bar.setProgress(0);
            progress_bar.invalidate();
        }
    }

    private void releaseCamera() {
        try {

            if (sampleGLView != null) {
                sampleGLView.onPause();
            }
            if (gpuCameraRecorder != null) {
                gpuCameraRecorder.stop();
                gpuCameraRecorder.release();
                gpuCameraRecorder = null;
            }
            if (sampleGLView != null) {
                ((FrameLayout) findViewById(R.id.wrap_view)).removeView(sampleGLView);
                sampleGLView = null;
            }
        } catch (Exception e) {
            Timber.i("exception %s", e.getMessage());
        }

    }

    private void initCameraView() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new GLSurfaceView(CameraActivity.this);
            frameLayout.addView(sampleGLView);
            frameLayout.setFitsSystemWindows(true);

            gpuCameraRecorder = new GPUCameraRecorderBuilder(CameraActivity.this, sampleGLView)
                    .cameraRecordListener(new CameraRecordListener() {
                        @Override
                        public void onGetFlashSupport(boolean flashSupport) {
                        }

                        @Override
                        public void onRecordComplete() {

                            try {

                                videoRecordingLayout.setVisibility(View.GONE);
                                recordLayout.setVisibility(View.VISIBLE);
                                BeforeVideoRecordingLayout.setVisibility(View.VISIBLE);
                                cameraOptions.setVisibility(View.VISIBLE);
                                soundLay.setVisibility(View.VISIBLE);
                                gallaryLay.setVisibility(View.VISIBLE);

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

                        }

                        @Override
                        public void onRecordStart() {
                        }

                        @Override
                        public void onError(Exception exception) {
                        }

                        @Override
                        public void onCameraThreadFinish() {
                            if (toggleClick) {
                                CameraActivity.this.runOnUiThread(() -> {
                                    initCameraView();
                                });
                            }
                            toggleClick = false;
                        }

                        @Override
                        public void onVideoFileReady() {
                        }
                    })
                    .lensFacing(lensFacing)
                    .mute(false)
                    .filter(isSelectedPosition == -1 ? setfilter : FilterType.gpuVideoView(isSelectedPosition, this))
                    .build();
        }

    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

        Timber.i("onNetworkChanged %s", isConnected);

        if (!isConnected) {
            if (songDownload != null) {
                getSound_id = "";
                songDownload.cancel(false);
                Toasty.error(Objects.requireNonNull(this), R.string.internet_disturb, Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
            }
        }


    }

    private void backPressed() {
        if (!isRecord) {
            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
            finish();
        } else openReshootDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();

        App.setCurrentActivity(this);

        overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_stay);
        checkRecordPermissions();

        if (!NetworkReceiver.isConnected()) {
            onNetworkChanged(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ring != null && ring.isPlaying()) ring.stop();
        timerCancel();
        if (gpuCameraRecorder != null) {
            gpuCameraRecorder.stop();
            gpuCameraRecorder.release();
            gpuCameraRecorder = null;
        }
        if (sampleGLView != null) {
            frameLayout.removeView(sampleGLView);
            sampleGLView = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (permissionGranted) {
                    initCameraView();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) && shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) && shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        } else {
                            App.makeToast(getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);
                        }
                    }
                }
                break;
        }
    }

    public static Bitmap drawableToBitmap(@NonNull Drawable d) {
        Bitmap bitmap;
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            if (bitmapDrawable.getBitmap() != null)
                return bitmapDrawable.getBitmap();
        }
        if (d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bitmap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_VIDEO) {
            if (data != null) {

                Log.e(TAG, "onActivityResult::::::::::::: "+data.getData());

                TrimVideo.activity(String.valueOf(data.getData()))
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setMinToMax(Long.parseLong("5"), Long.parseLong("60"))
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
            Timber.i("reShoot calling %s", "reShoot");
            reShoot();
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

        } catch (Exception e) {
            Timber.i("view exception %s", e.getMessage());
        }
    }

    private void checkRecordPermissions() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{
                            CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO},
                    Constants.CAMERA_REQUEST_CODE);
        } else
            initCameraView();
    }

    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {
        Context mContext;

        public FilterAdapter(Context context) {
            this.mContext = context;

            if (isSelectedPosition == -1) isSelectedPosition = 0;
        }

        @NonNull
        @Override
        public FilterAdapter.FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
            return new FilterAdapter.FilterViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FilterAdapter.FilterViewHolder holder, @SuppressLint("RecyclerView") int position) {

            holder.filterTitle.setText(ImageFilterType.gpuImageView().get(position).name());
            holder.setGpuImage(holder.bmp, ImageFilterType.gpuImageView(position, mContext));

            holder.itemView.setOnClickListener(v -> {
                isSelectedPosition = position;
                if (lensFacing == LensFacing.FRONT && position == 0) {
                    GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
                    glBrightnessFilter.setBrightness(0.1f);
                    gpuCameraRecorder.setFilter(glBrightnessFilter);
                } else
                    gpuCameraRecorder.setFilter(FilterType.gpuVideoView(position, CameraActivity.this));

                notifyDataSetChanged();
            });

            if (isSelectedPosition == position) holder.imageView.setStrokeWidth(2.5f);
            else holder.imageView.setStrokeWidth(0f);

        }

        @Override
        public void onViewDetachedFromWindow(@NonNull FilterAdapter.FilterViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public int getItemCount() {
            return ImageFilterType.gpuImageView().size();
        }

        public class FilterViewHolder extends RecyclerView.ViewHolder {
            TextView filterTitle;
            ShapeableImageView imageView;
            Bitmap bmp;
            private GPUImage mGpuImage;

            public FilterViewHolder(@NonNull View itemView) {
                super(itemView);
                this.bmp = drawableToBitmap(ResourcesCompat.getDrawable(getResources(), R.drawable.filter_sample_image, getTheme()));
                imageView = itemView.findViewById(R.id.imageView);
                filterTitle = itemView.findViewById(R.id.filterTitle);

            }


            public void setGpuImage(Bitmap bitmap, GPUImageFilter filter) {
                if (mGpuImage == null) {
                    mGpuImage = new GPUImage(CameraActivity.this);
                }
                mGpuImage.setFilter(filter);
                imageView.setImageBitmap(mGpuImage.getBitmapWithFilterApplied(bitmap));
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

                Toasty.success(CameraActivity.this, getResources().getString(R.string.selected_song_sucess), Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
            }
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

}
