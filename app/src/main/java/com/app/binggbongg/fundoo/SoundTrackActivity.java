package com.app.binggbongg.fundoo;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.binggbongg.Deepar.DeeparActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.home.eventbus.FollowingVideoFav;
import com.app.binggbongg.fundoo.home.eventbus.ForYouVideoFav;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class SoundTrackActivity extends BaseFragmentActivity {

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnEdit)
    ImageView btnEdit;

    @BindView(R.id.soundTrackName)
    TextView soundTrackName;

    @BindView(R.id.hashTagCount)
    TextView hashTagCount;

    @BindView(R.id.hashTagImage)
    ImageView hashTagImage;

    @BindView(R.id.backImageView)
    ImageView backImageView;

    @BindView(R.id.playControl)
    ImageView playControl;

    @BindView(R.id.btnAddToFav)
    MaterialButton btnAddToFav;

    @BindView(R.id.btnUseHashTag)
    MaterialButton btnUseHashTag;

    @BindView(R.id.btnRelatedVideos)
    MaterialButton btnRelatedVideos;

    @BindView(R.id.timeEnd)
    TextView timeEnd;

    @BindView(R.id.timeStart)
    TextView timeStart;

    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;

    @BindView(R.id.circuleProgress)
    ProgressBar circuleProgress;


    MediaPlayer mediaPlayer;

    ObjectAnimator anim;

    PlayState PlayPause = PlayState.PLAY;

    public enum PlayState {PLAY, PAUSE}

    private ApiInterface apiInterface;

    Boolean networkConnect;

    private String sound_id, sound_title, sound_url, sound_isfav, sound_cover, sound_duraion;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_track);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        ButterKnife.bind(this);


        animation();

        Bundle extras = getIntent().getBundleExtra(Constants.TAG_SOUND_DATA);
        sound_id = extras.getString(Constants.TAG_SOUND_ID);
        sound_title = extras.getString(Constants.TAG_SOUND_TITLE);
        sound_url = extras.getString(Constants.TAG_SOUND_URL);
        sound_isfav = extras.getString(Constants.TAG_SOUND_IS_FAV);
        sound_cover = extras.getString(Constants.TAG_SOUND_COVER);
        sound_duraion = extras.getString(Constants.TAG_SOUND_DURATION);


        if (sound_duraion.equalsIgnoreCase("0:60")) timeEnd.setText("1:00");
        else timeEnd.setText(sound_duraion);
        timeStart.setText("0:00");

        Glide.with(this)
                .load(sound_cover)

                .transform(new BlurTransformation())
                .into(backImageView);

        btnBack.setColorFilter(getResources().getColor(R.color.colorWhite));
        btnEdit.setVisibility(View.GONE);

        setDataToView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (anim != null && anim.isRunning()) anim.pause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            PlayPause = PlayState.PAUSE;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
            networkConnect = false;

        } else {
            playControl.setVisibility(View.VISIBLE);
            playControl.setAlpha(1f);
            networkConnect = true;
        }

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        networkConnect = isConnected;
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }
    }


    private void favApi() {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, sound_id);
            map.put(Constants.TAG_FAV_TYPE, "sound");

            Timber.d("setFav params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();

                        Timber.d("setFav onResponse: %s", new Gson().toJson(response.body()));

                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                            if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_UNN_FAV)) {
                                btnAddToFav.setText(R.string.addafav2);
                                EventBus.getDefault().post(new ForYouVideoFav(sound_id, "Sound", false));
                                EventBus.getDefault().post(new FollowingVideoFav(sound_id, "Sound", false));

                            } else if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_SOUND_FAV)) {
                                btnAddToFav.setText(R.string.unFav);
                                EventBus.getDefault().post(new ForYouVideoFav(sound_id, "Sound", true));
                                EventBus.getDefault().post(new FollowingVideoFav(sound_id, "Sound", true));
                            }

                        } else
                            App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void setDataToView() {


        soundTrackName.setText(sound_title);

        if (sound_isfav != null) {

            Timber.d("sound_isfav: %s", sound_isfav);

            // True means already fav
            if (sound_isfav.equals("true")) {
                btnAddToFav.setText(getResources().getString(R.string.unFav));
            } else {
                btnAddToFav.setText(getResources().getString(R.string.addafav2));
            }
        }

        btnBack.setOnClickListener(v -> {
            BackPressed();
        });

        btnAddToFav.setOnClickListener(v -> {
            App.preventMultipleClick(btnAddToFav);
            favApi();
        });

        btnRelatedVideos.setOnClickListener(v -> {
            App.preventMultipleClick(btnRelatedVideos);
            Intent intent = new Intent(this, RelatedSoundActivity.class);
            intent.putExtra(Constants.TAG_TITLE, getResources().getString(R.string.related_videos));
            intent.putExtra(Constants.TAG_SOUND_ID, sound_id);
            intent.putExtra(Constants.TAG_COVER_IMAGE, sound_cover);
            startActivity(intent);

        });

        btnUseHashTag.setOnClickListener(v -> {
            if (networkConnect) {
                App.preventMultipleClick(btnUseHashTag);
                Intent intent = new Intent(this, DeeparActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_SOUND_ID, sound_id);
                bundle.putString(Constants.TAG_SOUND_TITLE, sound_title);
                bundle.putString(Constants.TAG_SOUND_URL, sound_url);
                bundle.putString(Constants.TAG_SOUND_IS_FAV, sound_isfav);
                bundle.putString(Constants.TAG_SOUND_COVER, sound_cover);
                bundle.putString(Constants.TAG_SOUND_DURATION, sound_duraion);
                intent.putExtra(Constants.TAG_SOUND_DATA, bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
            } else {
                Toasty.error(this, getResources().getString(R.string.internet_disturb), Toasty.LENGTH_SHORT).show();
            }

        });

        hashTagImage.setOnClickListener(v -> {

            if (networkConnect) {

                if (mediaPlayer != null) {

                    if (PlayPause.equals(PlayState.PLAY)) {
                        mediaPlayer.pause();
                        PlayPause = PlayState.PAUSE;
                        if (anim.isRunning()) anim.pause();

                        playControl.setImageResource(R.drawable.video_play);

                        playControl.animate().cancel();

                        playControl.setAlpha(1f);

                    } else {

                        mediaPlayer.start();
                        PlayPause = PlayState.PLAY;
                        if (anim.isRunning()) anim.resume();
                        else anim.start();

                        playControl.setImageResource(R.drawable.video_pause);

                        playControl.animate().cancel();
                        playControl.setAlpha(1f);
                        playControl.animate().alpha(0f)
                                .setDuration(500).setStartDelay(100);

                    }
                } else {
                    circuleProgress.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> {
                        mediaPlayer = MediaPlayer.create(SoundTrackActivity.this, Uri.parse(sound_url));
                        mediaPlayer.setOnPreparedListener(mp -> {
                            mediaPlayer.start();
                            anim.start();
                            circuleProgress.setVisibility(View.GONE);
                        });

                        StrartbarUpdate();
                        playControl.setAlpha(1f);
                        playControl.animate().alpha(0f)
                                .setDuration(500).setStartDelay(100);

                        mediaPlayer.setOnCompletionListener(mp -> {
                            PlayPause = PlayState.PAUSE;
                            anim.cancel();
                        });
                    }, 500);
                }
            } else {
                Toasty.error(this, getResources().getString(R.string.internet_disturb), Toasty.LENGTH_SHORT).show();
            }

        });

        anim = ObjectAnimator.ofFloat(hashTagImage, View.ROTATION, 0f, 360f)
                .setDuration(100000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setInterpolator(new LinearInterpolator());

        Glide.with(this)
                .load(sound_cover)
                .into(hashTagImage);


    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

    private void BackPressed() {
        if (handler != null) handler.removeCallbacks(r);
        setResult(Constants.SINGLE_ACTIVITY);
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        BackPressed();
    }

    public String ShowTime(int time) {
        time /= 1000;
        int minute = time / 60;
        int second = time % 60;
        minute %= 60;

        return String.format("%d:%02d", minute, second);
    }

    Handler handler = new Handler();

    public void StrartbarUpdate() {
        Timber.d("MP: Duration %dms", getDurationMillis(sound_duraion));
        progress_bar.setMax(getDurationMillis(sound_duraion));
        handler.post(r);
    }

    private int getDurationMillis(@NotNull String duration) {
        if (duration.split(":").length > 0) {
            String[] time = duration.split(":");
            try {
                int min = Integer.parseInt(time[0]);
                int secs = Integer.parseInt(time[1]);
                return ((min * 60) + secs) * 1000;
            } catch (NumberFormatException e) {
                Timber.e(e);
            }
        }
        return -1;
    }

    Runnable r = new Runnable() {

        @Override
        public void run() {

            int CurrentPosition = mediaPlayer.getCurrentPosition();
            timeStart.setText(String.valueOf(ShowTime(CurrentPosition)));
            progress_bar.setProgress(CurrentPosition);
            handler.postDelayed(r, 500);
        }
    };


}