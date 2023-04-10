package com.app.binggbongg.fundoo.Video;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.R;
import com.app.binggbongg.audiotrimmer.ActivityEditor;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.Utility;
import com.app.binggbongg.helper.FragmentObserver;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.ProgressRequestBody;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.helper.callback.CompositeOnNetworkChangedListener;
import com.app.binggbongg.helper.viewpageranimation.FlipHorizontalPageTransformer;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class UploadSoundActivity extends BaseFragmentActivity {

    private static final String TAG = UploadSoundActivity.class.getSimpleName();
    public static String searchQuery;

    ViewPager viewPager;
    ImageView btnBack;
    EditText edtSearch;
    CoordinatorLayout contentLay;
    ViewPagerAdapter adapter;
    private SlidrInterface slidrInterface;

    MaterialButton btnFav, btnDiscover;
    MaterialButton uploadButton;
    private String getSoundId = "", isUerPrime = "";
    private ApiInterface apiInterface;
    File outputPath;
    Bitmap videoThumnail;
    ProgressDialog pdLoading;

    String from ="";
    DiscoverSoundFragment soundFragment;

    CompositeOnNetworkChangedListener compositeOnNetworkChangedListener = new CompositeOnNetworkChangedListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackground(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
        setContentView(R.layout.activity_upload_sound);

        overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        isUerPrime = SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false");


        Timber.i("isUerPrime %s", isUerPrime);

        if(getIntent().getStringExtra("from") != null){
            from = getIntent().getStringExtra("from");
            DiscoverSoundFragment.setData(from);
        }

        getSoundId = getIntent().getStringExtra(Constants.TAG_SOUND_ID);
        Timber.d("onCreate: %s", getSoundId);

        animation();

        searchQuery = "";

        viewPager = findViewById(R.id.fragment_home_viewpager);
        contentLay = findViewById(R.id.contentLay);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFav = findViewById(R.id.btnFav);
        btnDiscover = findViewById(R.id.btnDiscover);

        uploadButton = findViewById(R.id.uplaodButton);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(1);

        btnDiscover.setOnClickListener(view -> {
            enableSliding(true);
            viewPager.setCurrentItem(0);
        });
        btnFav.setOnClickListener(view -> {
            enableSliding(false);
            viewPager.setCurrentItem(1);
        });

        uploadButton.setOnClickListener(v -> {

            //if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") || BuildConfig.DEBUG) {
            if (NetworkReceiver.isConnected()) {
                startActivityForResult(new Intent(UploadSoundActivity.this, ActivityEditor.class), Constants.OPEN_AUDIO_FILE_REQUEST_CODE);
            } else {
                Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            }
           /* } else {
                Toasty.error(this, R.string.prime_alert, Toasty.LENGTH_SHORT).show();
            }*/

        });

        btnBack.setOnClickListener(v -> backPressed());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String txt = editable.toString();

                Timber.d("afterTextChanged: %s", txt);

                try {
                    searchQuery = edtSearch.getText().toString().trim();
                    updateFragments();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        viewPager.setCurrentItem(0);
        btnDiscover.setTextColor(getResources().getColor(R.color.colorWhite));
        btnFav.setTextColor(getResources().getColor(R.color.colorSecondaryText));
    }


    public void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        slidrInterface = Slidr.attach(this, config);
    }


    @Override
    public void onBackPressed() {
        backPressed();
    }

    public void backPressed() {
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {

        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
        Timber.i("onNetworkChanged %s", isConnected);

        compositeOnNetworkChangedListener.onNetworkChanged(isConnected);

    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DiscoverSoundFragment(getSoundId), getString(R.string.broadcast));
        adapter.addFragment(new DiscoverFavSoundFragment(getSoundId), getString(R.string.people));

        viewPager.setPageTransformer(true, new FlipHorizontalPageTransformer());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                    default:
                        uploadButton.setVisibility(View.VISIBLE);
                        btnDiscover.setTextColor(getResources().getColor(R.color.colorWhite));
                        btnFav.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                        edtSearch.setHint(getString(R.string.search_sounds));
                        enableSliding(true);
                        break;
                    case 1:
                        uploadButton.setVisibility(View.GONE);
                        btnDiscover.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                        btnFav.setTextColor(getResources().getColor(R.color.colorWhite));
                        edtSearch.setHint(getString(R.string.search_fav));
                        enableSliding(false);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateFragments() {
        adapter.updateFragments();
    }

    public void enableSliding(boolean enable) {
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private Observable mObservers = new FragmentObserver();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragmentList.get(position) instanceof Observer)
                mObservers.addObserver((Observer) mFragmentList.get(position));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }
    }


    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.OPEN_AUDIO_FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                pdLoading = new ProgressDialog(UploadSoundActivity.this, R.style.CameraAlertDialog);
                pdLoading.setCancelable(false);
                ValueAnimator processingAnimator = ValueAnimator.ofInt(0, 4);
                processingAnimator.setRepeatCount(Animation.INFINITE);
                processingAnimator.setDuration(1000L);
                processingAnimator.addUpdateListener(animation -> {
                    // noinspection SetTextI18n
                    pdLoading.setMessage("Processing" + getDots((Integer) processingAnimator.getAnimatedValue(), ""));
                });
                processingAnimator.start();
                pdLoading.setMessage(getResources().getString(R.string.processing));
                pdLoading.setOnShowListener(dialog -> processingAnimator.start());
                pdLoading.setOnDismissListener(dialog -> processingAnimator.cancel());
                pdLoading.show();

                /*val warningSnackBar = Snacky.builder()
                        .setActivty(this)
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.editor_toast_color))
                        .setText(this.getString(R.string.Edit_Done_Toast))
                        .setDuration(Snacky.LENGTH_LONG);

                warningSnackBar.success().show();*/

                Log.e(TAG, "onActivityResult: :::::::::::::::::"+data );

                if (data != null) {
                    outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);
                  /*  if(from.equals("main")){

                    }else{
                        outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_CHANGE + Utility.AUDIO_FORMAT);
                    }*/

                    Timber.i("song title %s", data.getStringExtra(Constants.TAG_SONG_TITLE));
                    Timber.i("song duration %s", data.getStringExtra(Constants.TAG_SONG_DURATION));
                    Timber.i("TAG_SONG_ALBUM %s", data.getStringExtra(Constants.TAG_SONG_ALBUM));

                    if (data.getStringExtra(Constants.TAG_SONG_ALBUM) != null)
                        videoThumnail = getAlbumImage(data.getStringExtra(Constants.TAG_SONG_ALBUM));

                  //  File outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);


                  /*  File outputPath;
                    if(from.equals("main")){
                        outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);
                    }else{
                        outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_CHANGE + Utility.AUDIO_FORMAT);
                    }*/

                    ProgressRequestBody.UploadCallbacks uploadCallbacks =
                            new ProgressRequestBody.UploadCallbacks() {
                                @Override
                                public void onProgressUpdate(int percentage) {
                                    Log.e(TAG, "onProgressUpdate: ::::::::::::::::::"+percentage );
                                }

                                @Override
                                public void onError(String message) {
                                    Timber.d("onError: %s", message);
                                }

                                @Override
                                public void onFinish() {


                                }
                            };


                    Random r = new Random();


                    ProgressRequestBody fileBody = new ProgressRequestBody(outputPath, "audio.mp3",
                            uploadCallbacks);

                    MultipartBody.Part filePart =
                            MultipartBody.Part.createFormData("sound_data", data.getStringExtra(Constants.TAG_SONG_TITLE) + ".mp3", fileBody);

                    RequestBody userId = RequestBody.create(MediaType.parse("text/plain"),
                            GetSet.getUserId());
                    RequestBody token = RequestBody.create(MediaType.parse("text/plain"),
                            GetSet.getAuthToken());

                    RequestBody title = RequestBody.create(MediaType.parse("text/plain"),
                            data.getStringExtra(Constants.TAG_SONG_TITLE) + +Math.abs(r.nextInt()));

                    RequestBody duration = RequestBody.create(MediaType.parse("text/plain"),
                            data.getStringExtra(Constants.TAG_SONG_DURATION));


                    Call<Map<String, String>> call = apiInterface.uploadAudio(filePart, userId, token, title, duration);

                    call.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

                            Timber.d("onResponse upload:status  %s", response.body());

                            Map<String, String> getRes = response.body();

                            if (getRes.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                                //  App.makeToast("Songs uploaded");

                                if (videoThumnail != null) {
                                    pdLoading.setMessage(getResources().getString(R.string.almost_done));

                                    uploadImage(getRes.get("sound_id"), videoThumnail, data.getStringExtra(Constants.TAG_SONG_DURATION));
                                } else {
                                    pdLoading.dismiss();
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.TAG_SOUND_ID, getRes.get("sound_id"));
                                    intent.putExtra(Constants.TAG_SOUND_DURATION, data.getStringExtra(Constants.TAG_SONG_DURATION));
                                    setResult(Constants.CAMERA_SOUND, intent);
                                    finish();

                                }

                            /*if (!getTitle.equals("notitle"))
                                uploadImage(getRes.get("sound_id"));
                            else {

                                Intent intent = new Intent();
                                intent.putExtra(Constants.TAG_SOUND_ID, getRes.get("sound_id"));
                                setResult(Constants.TRIMMER_REQUEST_CODE);
                                finish();
                            }*/


                            } else {
                                //  mProgressDialog.dismiss();
                                App.makeToast("Songs already uploaded");
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {

                            Timber.d("onFailure:upload %s", t.getMessage());

                        }
                    });
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                //  Toast.makeText(UploadSoundActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

    public void uploadImage(String getSoundId, Bitmap videoThumnail, String getSoundDuration) {


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        videoThumnail.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_COVER_IMAGE, "cover_image" + ".jpeg", requestFile);
        RequestBody publisherId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        RequestBody soundId = RequestBody.create(getSoundId, MediaType.parse("multipart/form-data"));

        Call<Map<String, String>> call3 = apiInterface.uploadAlbumImage(body, publisherId, soundId);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                Timber.d("onResponse:upload %s", response.body());

                pdLoading.dismiss();
                Intent intent = new Intent();
                intent.putExtra(Constants.TAG_SOUND_ID, getSoundId);
                intent.putExtra(Constants.TAG_SOUND_DURATION, getSoundDuration);
                setResult(Constants.CAMERA_SOUND, intent);
                finish();

            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                Timber.d("onResponse:upload %s", t.getMessage());
                call.cancel();
            }
        });


    }


    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, Uri.parse(path));
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }
}