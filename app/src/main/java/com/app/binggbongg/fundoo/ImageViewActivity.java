package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.R;
import com.app.binggbongg.external.ImagePicker;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.OnSwipeTouchListener;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.FileUtil;
import com.app.binggbongg.utils.SharedPref;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@SuppressLint("NonConstantResourceId")
public class ImageViewActivity extends BaseFragmentActivity {
    private static final String TAG = ImageViewActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    String image = "";

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.lottieImage)
    LottieAnimationView lottieImage;
    @BindView(R.id.shimmerLayout)
    ShimmerFrameLayout shimmerLayout;
    @BindView(R.id.bottomLay)
    LinearLayout bottomLay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    ApiInterface apiInterface;
    int resultCode = 0;
    @BindView(R.id.btnClose)
    ImageButton btnClose;
    @BindView(R.id.parentLay)
    FrameLayout parentLay;
    @BindView(R.id.txtChangePhoto)
    TextView txtChangePhoto;
    private RequestOptions profileImageRequest;
    StorageUtils storageUtils;
    String from;

    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //     overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);

        setContentView(R.layout.activity_image_view);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        image = getIntent().getStringExtra(Constants.TAG_USER_IMAGE);
        storageUtils = StorageUtils.getInstance(this);
        /*profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .dontAnimate();*/

        Log.d(TAG, "ImageViewActivity: "+image);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        if (from.equals(Constants.TAG_PROFILE)) {
            shimmerLayout.startShimmer();
            initListeners();
        } else {
            bottomLay.setVisibility(View.GONE);
        }

        if (from.equals(Constants.TAG_SENT) ||
                from.equals(Constants.TAG_RECEIVED)) {
            if (from.equals(Constants.TAG_SENT)){
                Log.d(TAG, "onCreate: +++");
                image= String.valueOf(storageUtils.getImageUri(Constants.TAG_SENT, image));
            }/*else if (from.equals(Constants.TAG_RECEIVED)){
                Log.d(TAG, "onCreate: __");
                //image= String.valueOf(storageUtils.getImageUri(Constants.TAG_SENT, image));
                image= String.valueOf(storageUtils.getImageUri(Constants.TAG_RECEIVED,image));
            }*/
            Glide.with(getApplicationContext())
                    .load(image)
                    .error(R.drawable.default_profile_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    .into(imageView);

        } else {
            Glide.with(getApplicationContext())
                    /*.load(  image)*/
                    .load(image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    // .apply(profileImageRequest)
                    .into(imageView);
        }


   /*     if (from.equals(Constants.TAG_SENT) ||
                from.equals(Constants.TAG_RECEIVED)) {
            Glide.with(getApplicationContext())
                    .load(image)
                    .error(R.drawable.default_profile_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    .into(imageView);

        } else {
            Glide.with(getApplicationContext())
                    *//*.load(  image)*//*
                    .load(image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    // .apply(profileImageRequest)
                    .into(imageView);
        }*/

        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.TOP)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        parentLay.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeUp() {

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{CAMERA},
                                Constants.CAMERA_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ImageViewActivity.this);
                        pickImage();
                    }
                }else{
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{CAMERA,
                                        WRITE_EXTERNAL_STORAGE},
                                Constants.CAMERA_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ImageViewActivity.this);
                        pickImage();
                    }
                }


            }
        });
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
                    currentPhotoFile = FileUtil.createImageFile(this);
                    photoFile = currentPhotoFile;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            try {
                InputStream imageStream = ImagePicker.getInputStreamFromResult(this, requestCode, resultCode, data);
                uploadImage(AppUtils.getBytes(imageStream));
                String filePath = ImagePicker.getImagePathFromResult(getApplicationContext(), requestCode, resultCode, data);
                Glide.with(getApplicationContext())
                        .load(filePath)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                imageView.setImageDrawable(resource);
                                return false;
                            }
                        })
                        //  .apply(profileImageRequest)
                        .into(imageView);
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
                    imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this,
                            mCurrentPhotoUri), Constants.TAG_TEMP, fileName);

                    //deleteFileForUri(mCurrentPhotoUri);
                    mCurrentPhotoUri = null;
                    Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                } else {            /** ALBUM **/
                    String mimeType = getContentResolver().getType(data.getData());
                    if (mimeType != null && mimeType.startsWith("image")) {

                        imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this,
                                data.getData()), Constants.TAG_TEMP, fileName);

                        Timber.d("Picked: %s", imageUri);
                    } else {
                        Toasty.info(this
                                , "Videos are not supported yet").show();
                        return;
                    }
                }

                if (imageUri != null) {
                    try {
                        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "r");

                        InputStream imageStream = new FileInputStream(pfd.getFileDescriptor());
                        uploadImage(AppUtils.getBytes(imageStream));
                        Glide.with(imageView)
                                .load(imageUri)
                                .into(imageView);

                    } catch (Exception e) {
                        Timber.e(e);
                        Toasty.error(this, R.string.something_went_wrong).show();
                    }
                    /**
                     * The image is working fine without a compression.
                     */
                    /*Runnable compressionTask = new ImageCompressionTask(getContext()
                    , imageUri,
                            outUri -> {
                                try {
                                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(outUri, "r");
                                    byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                                    JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                                    uploadChatImage(imageBytes, fileName, thumbnail, jsonObject);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toasty.error(getContext()
                                    , R.string.something_went_wrong).show();
                                }
                            });
                    mExecutorService.execute(compressionTask);*/

                    /*@SuppressLint("StaticFieldLeak")
                    ImageCompression imageComimageCompressionpression = new ImageCompression(ChatActivity.getContext()
                    ) {
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
    }

    private void uploadImage(byte[] imageBytes) {
        lottieImage.setVisibility(View.GONE);
        txtChangePhoto.setText(R.string.uploading);
        showLoading();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                Log.d(TAG, "uploadImage:Response" + new Gson().toJson(response.body()));
                Map<String, String> data = response.body();
                hideLoading();
                resultCode = -1;
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    GetSet.setUserImage(Constants.IMAGE_URL + data.get(Constants.TAG_USER_IMAGE));

                    Timber.d("getUserImage: %s", GetSet.getUserImage());
                    Timber.d("getUserImage data: %s", data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                    txtChangePhoto.setText(R.string.profile_image_updated);
                    new Handler().postDelayed(() -> onBackPressed(), 500);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.d(TAG, "uploadImage:Failure" + t.getMessage());
                call.cancel();
                hideLoading();
                txtChangePhoto.setText(R.string.swipe_change_photo);
                lottieImage.setVisibility(View.VISIBLE);
            }
        });
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
//                    ImagePicker.pickImage(this, "Select your image:");
                    pickImage();
                } else {
                   if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                           if (shouldShowRequestPermissionRationale(CAMERA) ) {
                               requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                           } else {
                               App.makeToast(getString(R.string.camera_storage_error));
                               Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                               startActivity(i);

                           }
                   }else{
                           if (shouldShowRequestPermissionRationale(CAMERA) &&
                                   shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
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

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
        // overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }


    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @OnClick(R.id.btnClose)
    public void onViewClicked() {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }
}
