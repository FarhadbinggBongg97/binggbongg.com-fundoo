package com.app.binggbongg.fundoo.Video;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.fundoo.MainActivity;
import com.app.binggbongg.fundoo.Utility;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.ProgressRequestBody;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.PostHashTagRes;
import com.app.binggbongg.model.VideoPrivacyModel;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.FileUtil;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hendraanggrian.appcompat.widget.SocialArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class PostVideoActivity extends BaseFragmentActivity {

    private static final String TAG = PostVideoActivity.class.getSimpleName();
    private SlidrInterface slidrInterface;
    private ImageView uploadingImagePreview;
    private TextView txtTitle, video_view_result, txtpercentage, categoryItem ;
    private TextView tvSavedFilePath, tvCatItem;
    private ShapeableImageView imagePreview;
    private SwitchMaterial save_switch;
    private SocialAutoCompleteTextView inputDescriptionTxt;
    private RelativeLayout parentLay;
    private EditText videoLink;
    private Boolean isUploading = false;


    private String getVideoId, getSoundId, gettype, getHashTag;
    LinearLayoutManager linearLayoutManager;

    private final List<VideoPrivacyModel> privacyData = new ArrayList<>();
    private ApiInterface apiInterface;
    private RecyclerView hashTagRec;

    private BottomSheetDialog reShootDialog, uploadingDialog;
    private ProgressBar uploadingProgress;

    private Bitmap myBitmap;

    List<PostHashTagRes> hashTagList = new ArrayList<>();
    String tagcomment = "", tagged_user_name = "", tagged_user_id = "", contestId="";
    ContestCategoryDialog contestCategoryDialog;


    ArrayAdapter<PostHashTagRes.Result> hashTagAdapter;

    String postViewMode = "public";

    File outputVideoPath;
    ProgressDialog pdLoading;

    private Uri mTempSavedFileUri;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);


        //animation();

        outputVideoPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_MIX_VIDEO + Utility.VIDEO_FORMAT);
        Timber.d("outputVideoPath=> %s", outputVideoPath);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        privacyData.add(0, new VideoPrivacyModel("Every one", true));
        privacyData.add(1, new VideoPrivacyModel("fans", false));
        privacyData.add(2, new VideoPrivacyModel("Only me", false));

        getVideoId = getIntent().getStringExtra(Constants.TAG_VIDEO_ID);
        getSoundId = getIntent().getStringExtra(Constants.TAG_SOUND_ID);
        gettype = getIntent().getStringExtra(Constants.TAG_TYPE);
        getHashTag = getIntent().getStringExtra(Constants.TAG_SELECT_HASH_TAG);

        txtTitle = findViewById(R.id.txtTitle);
        imagePreview = findViewById(R.id.imagePreview);
        save_switch = findViewById(R.id.save_switch);
        inputDescriptionTxt = findViewById(R.id.inputDescriptionTxt);
        video_view_result = findViewById(R.id.video_view_result);
        hashTagRec = findViewById(R.id.hashTagRecLayout);
        tvSavedFilePath = findViewById(R.id.tv_savedFilePath);
        parentLay = findViewById(R.id.parentLay);
        categoryItem = findViewById(R.id.tv_cat_item);
        videoLink = findViewById(R.id.et_videoLink);


        if (getHashTag != null) {
            tagged_user_name = getHashTag;
            inputDescriptionTxt.setText("#" + tagged_user_name);
        }


        hashTagAdapter = new HashTagArrayAdapter(PostVideoActivity.this, R.layout.item_post_hashtag, R.id.hashTagName);

        inputDescriptionTxt.setHashtagAdapter(hashTagAdapter);
        inputDescriptionTxt.setThreshold(0);


        pdLoading = new ProgressDialog(PostVideoActivity.this, R.style.CameraAlertDialog);
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
        //videoMixFun();


        new Handler().postDelayed(() -> {
            File file = StorageUtils.getInstance(PostVideoActivity.this).getTempFile(PostVideoActivity.this, Constants.TAG_VIDEO_TRIM + ".mp4");
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            StorageUtils.getInstance(PostVideoActivity.this).saveVideoThumbNail(bitmap, Constants.TAG_VIDEO_THUMBNAIL + Utility.IMAGE_FORMAT);

            if (StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_VIDEO_THUMBNAIL + Utility.IMAGE_FORMAT).exists()) {
                myBitmap = BitmapFactory.decodeFile(StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_VIDEO_THUMBNAIL + Utility.IMAGE_FORMAT).getAbsolutePath());
                imagePreview.setImageBitmap(myBitmap);
            }

            new downloadFile().execute(String.valueOf(outputVideoPath));

        }, 500);



        /*if (getVideoId == null) getStreamName();*/


        txtTitle.setText(getResources().getString(R.string.post));

        imagePreview.setOnClickListener(v -> {
            startActivity(new Intent(this, VideoPreviewActivity.class));
            overridePendingTransition(R.anim.anim_zoom_out, R.anim.anim_zoom_in);
        });

        save_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 4);
                valueAnimator.setRepeatCount(Animation.INFINITE);
                valueAnimator.setDuration(1000L);
                valueAnimator.addUpdateListener(animation -> {
                    // noinspection SetTextI18n
                    tvSavedFilePath.setText("Saving" + getDots((Integer) valueAnimator.getAnimatedValue(), ""));
                });
                valueAnimator.start();

                String outFileName = new SimpleDateFormat("'Post_'yyyy-MM-dd-HH-mm'.mp4'").format(new Date());
                String outFileDir = "/Binggbongg/Saved Videos";

                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                Uri galleryUri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outFileName);
                    contentValues.put(MediaStore.MediaColumns.TITLE, outFileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + outFileDir);
                    contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                    contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

                    Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    galleryUri = resolver.insert(collection, contentValues);
                } else {
                    // for API 28 and below
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                            + outFileDir;
                    File targetDir = new File(directory);
                    if (!targetDir.exists()) {
                        if (targetDir.mkdirs()) {
                            Timber.i("Directory created: %s", targetDir.getAbsolutePath());
                        }
                    }
                    File createdVideo = new File(directory, outFileName);
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outFileName);
                    contentValues.put(MediaStore.MediaColumns.TITLE, outFileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                    contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                    contentValues.put(MediaStore.Video.Media.DATA, createdVideo.getAbsolutePath());

                    galleryUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                }

                mTempSavedFileUri = galleryUri;
                try {
                    ParcelFileDescriptor pfd = resolver.openFileDescriptor(galleryUri, "w");
                    boolean saved = FileUtil.copyFile(new FileInputStream(outputVideoPath), new FileOutputStream(pfd.getFileDescriptor()));
                    tvSavedFilePath.setVisibility(View.VISIBLE);
                    tvSavedFilePath.setText(getString(R.string.saved_to_gallery_filename, Environment.DIRECTORY_DCIM + outFileDir + "/" + outFileName));
                    Toasty.success(this, R.string.video_saved_to_gallery).show();
                    Timber.i("Save to gallery: %s", galleryUri.getPath());
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    Timber.e("Save to gallery: failed %s", e.getMessage());
                    Toasty.error(this, R.string.cannot_save_to_gallery).show();
                    save_switch.setChecked(false);
                }
                valueAnimator.cancel();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear();
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(galleryUri, contentValues, null, null);
                }
            } else {
                tvSavedFilePath.setText(null);
                tvSavedFilePath.setVisibility(View.GONE);

                if (mTempSavedFileUri != null) {
                    ContentResolver contentResolver = getContentResolver();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        if (contentResolver.delete(mTempSavedFileUri, null, null) > 0) {
                            Timber.i("Save to gallery: deleted %s", mTempSavedFileUri.getPath());
                            mTempSavedFileUri = null;
                        } else {
                            Timber.i("Save to gallery: failed deleting %s", mTempSavedFileUri.getPath());
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
        });

        findViewById(R.id.btnPost).setOnClickListener(v -> {
            App.preventMultipleClick(v);
            Timber.i("getVideoId %s", getVideoId);
            if (NetworkReceiver.isConnected()) {
                if (getVideoId != null && !getVideoId.equals("")) {
                    if (inputDescriptionTxt.getText().toString().isEmpty()) {
                        inputDescriptionTxt.setError(getResources().getString(R.string.txt_warning));
                    } else if(categoryItem.getText().toString().isEmpty()||categoryItem.getText().toString().equals("Select")){
                        categoryItem.setError("Please choose the contest category");
                        Snackbar.make(findViewById(R.id.parentLay),"Please choose the contest category",Snackbar.LENGTH_SHORT)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showCategoryDialog();
                                    }
                                })
                                .show();
                    } else {
                        if(!TextUtils.isEmpty(videoLink.getText().toString()) &&
                                !Patterns.WEB_URL.matcher(videoLink.getText().toString()).matches()
                                /*!URLUtil.isValidUrl(videoLink.getText().toString())*/){
                            Toast.makeText(this, "Enter the valid link", Toast.LENGTH_SHORT).show();
                        }else{
                            upload(inputDescriptionTxt.getText().toString());
                        }
                    }
                } else {
                    getStreamName("upload");
                }
            } else {
                Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            }

        });

        findViewById(R.id.who_can).setOnClickListener(v -> {

            Intent intent = new Intent(this, DynamicPrivacyActivity.class);
            intent.putExtra("data", (Serializable) privacyData);
            intent.putExtra(Constants.TAG_TITLE, getResources().getString(R.string.who_can_view));
            startActivityForResult(intent, Constants.VIDEO_PRIVACY);
            overridePendingTransition(R.anim.anim_zoom_out, R.anim.anim_zoom_in);

        });


        findViewById(R.id.btnBack).setOnClickListener(v -> {
            openReshootDialog();
        });


        TextWatcher textWatcher = new TextWatcher() {

            boolean valid = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String text = s.toString();
                //beforeTextChanged = text;
                if (count == 0 && text.isEmpty()) {
                  /*  liveUser.clear();
                    commentUserAdapter.notifyDataSetChanged();
                    userRecylerView.setVisibility(View.GONE);*/
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String abc = s.toString();

                final int index = abc.lastIndexOf("#");
                if (index >= 0) {

                }

                Timber.d("onTextChanged:");

            }

            @Override
            public void afterTextChanged(Editable s) {
                String txt = s.toString();

                final int index = txt.lastIndexOf("#");
                final int lastIndex;

                if (txt.contains("#") && !txt.endsWith(" "))
                    if (index >= 0) {
                        String query = txt.substring(index + 1);
                        lastIndex = query.lastIndexOf(" ");
                        if (lastIndex == -1) {
                            Timber.d("afterTextChanged split: %s", query);
                            //getHashTag(query);
                        } else {
                            //  hashTagRec.setVisibility(View.GONE);
                        }
                    }
            }


        };


        inputDescriptionTxt.addTextChangedListener(textWatcher);

        inputDescriptionTxt.setHashtagTextChangedListener((view, text) -> {

            Timber.d("setHashtagTextChangedListener: %s", text);

            getHashTag(String.valueOf(text));

        });

        inputDescriptionTxt.setOnHashtagClickListener((view, text) -> {

            Timber.d("setOnHashtagClickListener: %s", text);

        });

        findViewById(R.id.lay_category).setOnClickListener(v -> showCategoryDialog());

    }

    private void showCategoryDialog() {

        contestCategoryDialog = new ContestCategoryDialog();
        contestCategoryDialog.update(categoryItem.getText().toString());
        contestCategoryDialog.setContext(PostVideoActivity.this);

        contestCategoryDialog.setCallBack((object, bio) -> {
            Bundle bundle = (Bundle) object;
            if(!TextUtils.isEmpty(bundle.getString("contest_name"))){
                categoryItem.setError(null);
                categoryItem.setText(bundle.getString("contest_name"));
                contestId = bundle.getString("contest_id");
            }
            contestCategoryDialog.dismiss();
        });
        contestCategoryDialog.setCancelable(true);
        contestCategoryDialog.show(getSupportFragmentManager(), TAG);
    }

    private void getStreamName(String uploading) {

        if (NetworkReceiver.isConnected()) {
            Call<HashMap<String, String>> call = apiInterface.getStreamName(GetSet.getUserId());
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<HashMap<String, String>> call, @NotNull Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> map = response.body();
                        Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(map));
                        if (Objects.requireNonNull(Objects.requireNonNull(map).get(Constants.TAG_STATUS)).equals(Constants.TAG_TRUE)) {
                            getVideoId = map.get("video_id");

                            // Sometimes bitmap null so implement manuval delay

                            if (uploading.equalsIgnoreCase("upload")) {
                                if (inputDescriptionTxt.getText().toString().isEmpty()) {
                                    inputDescriptionTxt.setError(getResources().getString(R.string.txt_warning));
                                } else {
                                    upload(inputDescriptionTxt.getText().toString());
                                }
                            } else {
                                //upload(inputDescriptionTxt.getText().toString());
                                pdLoading.dismiss();
                            }


                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            pdLoading.dismiss();
        }
    }

    /*public void copyFile(File source, File destination) throws IOException {
        try {
            FileUtils.copy(new FileInputStream(source), new FileOutputStream(destination));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*private void copyFile(String inputPath, String inputFileName, String outputPath) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + "/" + inputFileName);
            out = new FileOutputStream(outputPath + "/" + inputFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)

            Timber.d("copyFile: %s", outputPath + "/" + inputFileName);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                Timber.d("ex: %s", e.getMessage());
            }
        }
    }*/


    private String serialialize(List<String> hashtags) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hashtags.size(); i++) {
            builder.append(hashtags.get(i));
            if (i < hashtags.size() - 1) builder.append(",");
        }
        return builder.toString();
    }


    public void getHashTag(String tag) {
        if (NetworkReceiver.isConnected()) {

            tag = tag.replaceAll("#", "");

            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("type", Constants.TAG_SEARCH);
            map.put("search_key", tag);

            Timber.d("getHashTag: %s", App.getGsonPrettyInstance().toJson(map));
            Call<PostHashTagRes> call3 = apiInterface.getPostHashTags(map);

            call3.enqueue(new Callback<PostHashTagRes>() {
                @Override
                public void onResponse(@NotNull Call<PostHashTagRes> call, @NotNull Response<PostHashTagRes> response) {
                    try {
                        PostHashTagRes data = response.body();
                        if (data.getStatus().equals(Constants.TAG_TRUE)) {

                            Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(data.getResult()));

                            if (data.getStatus().contains(Constants.TAG_TRUE)) {
                                // hashTagRec.setVisibility(View.VISIBLE);
                                /*hashTagList.clear();
                                hashTagList.addAll(data.getResult());
                                hashTagAdapter.notifyDataSetChanged();*/
                                //arrayAdapter.notifyDataSetChanged();

                                //  ArrayAdapter<PostHashTagRes.Result> hashtagAdapter = new HashtagArrayAdapter(PostVideoActivity.this);

                                hashTagAdapter.clear();

                                hashTagAdapter.addAll(data.getResult());
                                hashTagAdapter.notifyDataSetChanged();

                            }

                            //HashTagResponse data = response.body();
                            /* if (data.getStatus().equals("true")) {
                             *//*nullLay.setVisibility(View.GONE);
                                hashTagList.addAll(data.getResult().getVideos());*//*
                            }*/

                            /*if (hashTagList.size() == 0) {
                                nullText.setText(R.string.no_hashtags);
                                nullLay.setVisibility(View.VISIBLE);
                            }

                            relatedDataAdapter.notifyDataSetChanged();

                            if (mSwipeRefreshLayout.isRefreshing()) {
                                relatedDataAdapter.showLoading(false);
                                swipeRefresh(false);
                                isLoading = true;
                            } else {
                                relatedDataAdapter.showLoading(false);
                                relatedDataAdapter.notifyDataSetChanged();
                            }*/
                        } else {
                            // hashTagRec.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Timber.d("onResponse:error %s", e.getMessage());
                        /*hashTagList.clear();
                        recyclerView.stopScroll();
                        relatedDataAdapter.notifyDataSetChanged();*/
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<PostHashTagRes> call, Throwable t) {
                    /*if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        relatedDataAdapter.showLoading(false);
                    } else {
                        if (hashTagList.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }*/
                    call.cancel();
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
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
            reShootDialog.dismiss();
            setResult(RESULT_OK);
            finish();
        });

        reShoot_sheetView.findViewById(R.id.cancel).setOnClickListener(v -> reShootDialog.dismiss());

        reShoot_sheetView.findViewById(R.id.exit).setOnClickListener(v -> {
            startActivity(new Intent(PostVideoActivity.this, MainActivity.class));
            finish();
           /* reShootDialog.dismiss();
            setResult(RESULT_CANCELED);
            finish();*/
        });

        if (reShootDialog != null) reShootDialog.show();
    }


    private void upload(String des) {


        if (NetworkReceiver.isConnected()) {

            File file = StorageUtils.getInstance(getBaseContext()).getTempFile(getBaseContext(), Constants.TAG_MIX_VIDEO + Utility.VIDEO_FORMAT);

            if (file == null) {
                return;
            }

            View reShoot_sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_uploading_progress, null);
            uploadingDialog = new BottomSheetDialog(this, R.style.reshootBottomDialog); // Style here
            uploadingDialog.getBehavior().setHideable(false);
            uploadingDialog.setContentView(reShoot_sheetView);

            txtpercentage = reShoot_sheetView.findViewById(R.id.percentage);
            uploadingProgress = reShoot_sheetView.findViewById(R.id.progress_bar);
            uploadingImagePreview = reShoot_sheetView.findViewById(R.id.uploadingImagePreview);
            uploadingImagePreview.setImageBitmap(myBitmap);
            uploadingProgress.setProgress(0);
            uploadingProgress.setMax(100);
            txtpercentage.setText("0");


            uploadingDialog.show();

            uploadingDialog.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            });

            ProgressRequestBody.UploadCallbacks uploadCallbacks =
                    new ProgressRequestBody.UploadCallbacks() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onProgressUpdate(int percentage) {
                            isUploading = true;
                            uploadingProgress.setProgress(percentage + 1);
                            txtpercentage.setText((percentage + 1) + " %");

                            Timber.d("onProgressUpdate: %s", percentage);
                        }

                        @Override
                        public void onError(String message) {
                            Timber.d("onError: %s", message);
                        }

                        @Override
                        public void onFinish() {


                        }
                    };

            if (video_view_result.getText().toString().equals("Every one")) postViewMode = "public";
            else if (video_view_result.getText().toString().equals("fans")) postViewMode = "fans";
            else if (video_view_result.getText().toString().equals("Only me"))
                postViewMode = "private";


            ProgressRequestBody fileBody = new ProgressRequestBody(file, "video.mp4",
                    uploadCallbacks);

            MultipartBody.Part filePart =
                    MultipartBody.Part.createFormData("videodata", Constants.TAG_MIX_VIDEO + ".mp4", fileBody);

            RequestBody description = RequestBody.create(MediaType.parse("text/plain"),
                    des);

            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"),
                    GetSet.getUserId());

            RequestBody video_id = RequestBody.create(MediaType.parse("text/plain"),
                    getVideoId);

            RequestBody devicetype = RequestBody.create(MediaType.parse("text/plain"),
                    String.valueOf("1"));

            RequestBody type = RequestBody.create(MediaType.parse("text/plain"),
                    gettype);

            RequestBody mode = RequestBody.create(MediaType.parse("text/plain"),
                    postViewMode);

            RequestBody sound_id = RequestBody.create(MediaType.parse("text/plain"),
                    getSoundId);

            RequestBody contesttId = RequestBody.create(MediaType.parse("text/plain"),
                    contestId);

            RequestBody contestName = RequestBody.create(MediaType.parse("text/plain"),
                    categoryItem.getText().toString());

            RequestBody contestStatus = RequestBody.create(MediaType.parse("text/plain"),
                    "true");

            RequestBody videoLinkk = RequestBody.create(MediaType.parse("text/plain"),
                    videoLink.getText().toString());


            Call<Map<String, String>> call;
            if (!serialialize(inputDescriptionTxt.getHashtags()).trim().equals("")) {
                RequestBody hashtags = RequestBody.create(MediaType.parse("text/plain"), serialialize(inputDescriptionTxt.getHashtags()));
                call = apiInterface.postVideo(filePart, description, userId, video_id, devicetype,
                        type, mode, sound_id, hashtags, contesttId, contestName, videoLinkk, contestStatus);
                Timber.d("hashtags");
            } else {
                call = apiInterface.postVideoWithoutHashTag(filePart, description, userId, video_id,
                        devicetype, type, mode, sound_id, contesttId, contestName, videoLinkk, contestStatus);
                Timber.d("without hashtags");
            }

            Timber.d("description: %s ", des);
            Timber.d("user_id %s ", GetSet.getUserId());
            Timber.d("video_id: %s ", getVideoId);
            Timber.d("devicetype: %s ", "1");
            Timber.d("type: %s ", gettype);
            Timber.d("mode: %s ", postViewMode);
            Timber.d("sound_id: %s ", getSoundId);
            Timber.d("hashtags: %s", inputDescriptionTxt.getHashtags());
            Timber.d("contest_category_id: %s", contestId);
            Timber.d("contest_name: %s", categoryItem.getText().toString());

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {


                    Timber.d("onResponse:status  %s", response.body());

                    Map<String, String> getRes = response.body();

                    if (getRes.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        uploadCoverPhoto();
                    } else
                        Toasty.warning(PostVideoActivity.this, getResources().getString(R.string.something_went_wrong));
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    Log.d("PostvideoActivity", "onFailure: " + t.getMessage());
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void uploadCoverPhoto() {

        if (NetworkReceiver.isConnected()) {
            File file = StorageUtils.getInstance(getBaseContext()).getTempFile(getBaseContext(), Constants.TAG_VIDEO_THUMBNAIL + Utility.IMAGE_FORMAT);

            if (file == null) {
                return;
            }

            RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("stream_image", "video_thumbnail" + ".jpeg", requestFile);
            RequestBody videoId = RequestBody.create(getVideoId, MediaType.parse("multipart/form-data"));

            Timber.d("Postvideo: Videoid: %s", videoId);
            Timber.d("Postvideo: body: %s", body);

            Call<Map<String, String>> call = apiInterface.uploadStream(body, videoId);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    Timber.d("onResponse:coverPhoto  %s", response.body());

                    Map<String, String> getRes = response.body();
                    if (getRes.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        startActivity(new Intent(PostVideoActivity.this, MainActivity.class));
                        finish();
                        uploadingDialog.dismiss();
                    }

                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {

                    Timber.d("onResponse: %s", t.getMessage());

                }
            });

        } else {
            uploadingDialog.dismiss();
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }

    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
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
        /*super.onBackPressed();*/
        openReshootDialog();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

        Timber.i("onNetworkChanged isConnected %s", isConnected);
        Timber.i("onNetworkChanged isUploading %s", isUploading);

        if (!isConnected && !isUploading) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
            isUploading = false;
        } else if (!isConnected && isUploading) {
            Toasty.error(this, getResources().getString(R.string.internet_disturb), Toasty.LENGTH_SHORT).show();
            uploadingDialog.dismiss();
            isUploading = false;
        } else {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.VIDEO_PRIVACY) {

            if (resultCode != RESULT_CANCELED) {

                privacyData.clear();

                String getData = Objects.requireNonNull(data).getStringExtra("privacyName");

                video_view_result.setText(getData);

                switch (getData) {
                    case "Every one":
                        privacyData.add(0, new VideoPrivacyModel("Every one", true));
                        privacyData.add(1, new VideoPrivacyModel("fans", false));
                        privacyData.add(2, new VideoPrivacyModel("Only me", false));
                        break;
                    case "fans":
                        privacyData.add(0, new VideoPrivacyModel("Every one", false));
                        privacyData.add(1, new VideoPrivacyModel("fans", true));
                        privacyData.add(2, new VideoPrivacyModel("Only me", false));
                        break;
                    case "Only me":
                        privacyData.add(0, new VideoPrivacyModel("Every one", false));
                        privacyData.add(1, new VideoPrivacyModel("fans", false));
                        privacyData.add(2, new VideoPrivacyModel("Only me", true));
                        break;
                }


            }

        } else if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Timber.d("onActivityResult: saveDoc %s", data.getData());
            }
        }
    }


    /*private void videoMixFun() {

        if (outputVideoPath.exists()) {
            int rc = FFmpeg.execute("-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/videorecord.mp4 " +
                    "-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiotrim.mp3 " +
                    "-shortest -threads 0 " +
                    "-preset ultrafast " +
                    "-map 0:v:0 -map 1:a:0 " +
                    "-c:a aac -c:v libx264 " +
                    //  "-vf scale=480:-2,setsar=1 " +
                    "-pixel_format yuv420p " +
                    "-y " + outputVideoPath);

            if (rc == RETURN_CODE_SUCCESS) {
                // long duration = System.currentTimeMillis() - startTime;
                // Timber.d("completed successfully, time: \" %s", duration);
                //FFmpeg.cancel();
                // getStreamName();
                pdLoading.dismiss();
            } else if (rc == RETURN_CODE_CANCEL) {
                Timber.d("RETURN_CODE_CANCEL ");
            } else {
                Timber.d("Command execution failed with rc=%d", rc);
            }
        } else {
            int rc = FFmpeg.execute("-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/videorecord.mp4 " +
                    "-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiotrim.mp3 " +
                    "-shortest -threads 0 " +
                    "-preset ultrafast " +
                    "-map 0:v:0 -map 1:a:0 " +
                    "-c:a aac -c:v libx264 " +
                    /*"-c:a aac -c:v libx264  " +*/
    // "-vf scale=480:-2,setsar=1 " +
    // "-pixel_format yuv420p " +
    //*"/storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiomixvideo.mp4");*//*
    /*outFile.getAbsolutePath() + "/out.mp4");*/
                    /*"-y " + outputVideoPath);
                    outputVideoPath);

            if (rc == RETURN_CODE_SUCCESS) {

                pdLoading.dismiss();
            } else if (rc == RETURN_CODE_CANCEL) {
                Timber.d("RETURN_CODE_CANCEL ");
            } else {
                Timber.d("Command execution failed with rc=%d", rc);
            }
        }
    }*/





    /*public class HashtagArrayAdapter<T extends Hashtagable> extends SocialArrayAdapter<T> {
        private final int countPluralRes;

        public HashtagArrayAdapter(@NonNull Context context) {
            this(context, com.hendraanggrian.appcompat.socialview.commons.R.plurals.posts);
        }

        public HashtagArrayAdapter(@NonNull Context context, @PluralsRes int countPluralRes) {
            super(context, com.hendraanggrian.appcompat.socialview.commons.R.layout.socialview_layout_hashtag, com.hendraanggrian.appcompat.socialview.commons.R.id.socialview_hashtag);
            this.countPluralRes = countPluralRes;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(com.hendraanggrian.appcompat.socialview.commons.R.layout.socialview_layout_hashtag, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final T item = getItem(position);
            if (item != null) {
                holder.hashtagView.setText(item.getId());

                if (item.getCount() > 0) {
                    holder.countView.setVisibility(View.VISIBLE);
                    final int count = item.getCount();
                    holder.countView.setText(getContext().getResources().getQuantityString(countPluralRes, count, count));
                } else {
                    holder.countView.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

        private  class ViewHolder {
            private final TextView hashtagView;
            private final TextView countView;

            ViewHolder(View itemView) {
                hashtagView = itemView.findViewById(com.hendraanggrian.appcompat.socialview.commons.R.id.socialview_hashtag);
                countView = itemView.findViewById(com.hendraanggrian.appcompat.socialview.commons.R.id.socialview_hashtag_count);
            }
        }
    }*/


    public static class HashTagArrayAdapter extends SocialArrayAdapter<PostHashTagRes.Result> {

        public HashTagArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return super.getView(position, convertView, parent);

            // return new MyViewHolder(LayoutInflater.from(parent).inflate(R.layout.item_post_hashtag, parent, false));
        }

        @NonNull
        @Override
        public CharSequence convertToString(PostHashTagRes.Result object) {

            return object.getName();
        }

        @Override
        public void add(@Nullable PostHashTagRes.Result object) {
            Timber.d("add: %s", object);
            super.add(object);
        }

        @Override
        public void addAll(@NonNull Collection<? extends PostHashTagRes.Result> collection) {
            super.addAll(collection);
            Timber.d("addAll: %s", collection);
        }

        @Override
        public void remove(@Nullable PostHashTagRes.Result object) {
            super.remove(object);
        }

        @Override
        public void clear() {
            super.clear();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return super.getFilter();
        }


    }


    private class downloadFile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {


            if (outputVideoPath.exists()) {
                Timber.d("outputVideoPath.exists() %s", "exits");
                if(!TextUtils.isEmpty(getSoundId)){
                    int rc = FFmpeg.execute("-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/videorecord.mp4 " +
                            "-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiotrim.mp3 " +
                            "-shortest -threads 0 " +
                            /*"-preset ultrafast " +*/
                            "-map 0:v:0 -map 1:a:0 " +
                            "-vcodec copy " +
                            "-b:v 2M -r 24 " +
                            /*"-vf scale=720:-2,setsar=1 " +*/
                            /*"-c:a aac -c:v libx264 " +*/
                            //  "-vf scale=480:-2,setsar=1 " +
                            /*"-pixel_format yuv420p " +*/
                            "-y " + outputVideoPath);

                    if (rc == RETURN_CODE_SUCCESS) {
                        Timber.d("RETURN_CODE_SUCESS ");
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Timber.d("RETURN_CODE_CANCEL ");
                    } else {
                        Timber.d("Command execution failed with rc=%d", rc);
                    }

                }else{
                    int rc = FFmpeg.execute("-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/videorecord.mp4 " +
                            "-shortest -threads 0 " + // If audio file greater than video file, cut the audio
                            /*"-preset ultrafast " +*/
                            "-vcodec copy " +
                            "-b:v 2M -r 24 " + // Bit Rate and Frame Rate
                            // "-vf scale=480:-2,setsar=1 " +
                            /*"-pixel_format yuv420p " +*/
                            //*"/storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiomixvideo.mp4");*//*
                            /*outFile.getAbsolutePath() + "/out.mp4");*/
                            /*"-y " + outputVideoPath);*/
                            outputVideoPath);
                    if (rc == RETURN_CODE_SUCCESS) {
                        Timber.d("RETURN_CODE_SUCESS ");
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Timber.d("RETURN_CODE_CANCEL ");
                    } else {
                        Timber.d("Command execution failed with rc=%d", rc);
                    }

                }
            } else {

                Timber.d("outputVideoPath.exists() %s", "else");

                if(!TextUtils.isEmpty(getSoundId)){
                    int rc = FFmpeg.execute("-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/videorecord.mp4 " +
                            "-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiotrim.mp3 " +
                            "-shortest -threads 0 " + // If audio file greater than video file, cut the audio
                            /*"-preset ultrafast " +*/
                            "-map 0:v:0 -map 1:a:0 " +
                            "-vcodec copy " +
                            "-b:v 2M -r 24 " + // Bit Rate and Frame Rate
                            // "-vf scale=480:-2,setsar=1 " +
                            /*"-pixel_format yuv420p " +*/
                            //*"/storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiomixvideo.mp4");*//*
                            /*outFile.getAbsolutePath() + "/out.mp4");*/
                            /*"-y " + outputVideoPath);*/
                            outputVideoPath);

                    if (rc == RETURN_CODE_SUCCESS) {
                        Timber.d("RETURN_CODE_SUCESS ");
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Timber.d("RETURN_CODE_CANCEL ");
                    } else {
                        Timber.d("Command execution failed with rc=%d", rc);
                    }
                }else{
                    int rc = FFmpeg.execute("-i /storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/videorecord.mp4 " +
                            "-shortest -threads 0 " + // If audio file greater than video file, cut the audio
                            /*"-preset ultrafast " +*/
                            "-vcodec copy " +
                            "-b:v 2M -r 24 " + // Bit Rate and Frame Rate
                            // "-vf scale=480:-2,setsar=1 " +
                            /*"-pixel_format yuv420p " +*/
                            //*"/storage/emulated/0/Android/data/com.app.binggbongg/files/Temp/audiomixvideo.mp4");*//*
                            /*outFile.getAbsolutePath() + "/out.mp4");*/
                            /*"-y " + outputVideoPath);*/
                            outputVideoPath);
                    if (rc == RETURN_CODE_SUCCESS) {
                        Timber.d("RETURN_CODE_SUCESS ");
                    } else if (rc == RETURN_CODE_CANCEL) {
                        Timber.d("RETURN_CODE_CANCEL ");
                    } else {
                        Timber.d("Command execution failed with rc=%d", rc);
                    }

                }
            }
            return strings[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            pdLoading.setMessage(getResources().getString(R.string.almost_done));
            getStreamName("");
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

    @Override
    protected void onResume() {
        super.onResume();

  /*      if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }*/
    }
}