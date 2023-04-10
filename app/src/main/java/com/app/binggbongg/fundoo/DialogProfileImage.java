package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.binggbongg.BuildConfig;
import com.app.binggbongg.R;
import com.app.binggbongg.external.ImagePicker;
import com.app.binggbongg.helper.StorageUtils;
import com.app.binggbongg.helper.callback.OnOkClickListener;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.FileUtil;
import com.app.binggbongg.utils.SharedPref;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static com.app.binggbongg.fundoo.App.hideKeyboardFragment;
import static com.app.binggbongg.thread.ImageCompressionTask.calculateInSampleSize;

@SuppressLint("NonConstantResourceId")
public class DialogProfileImage extends DialogFragment {

    public static final String TAG=DialogProfileImage.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    @BindView(R.id.txtTitle)
    TextView txtTitle;

    @BindView(R.id.profileImage)
    ImageView profileImage;

    @BindView(R.id.uploadImage)
    ImageView uploadImage;

    @BindView(R.id.btnFinish)
    Button btnFinish;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.headerLay)
    RelativeLayout headerLay;


    @BindView(R.id.filledTextField)
    EditText filledTextField;


    private OnOkClickListener callBack;
    private Context context;
    InputStream imageStream;

    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    StorageUtils storageUtils;
    public  static  boolean usecamera=false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_profile_image, container, false);
        ButterKnife.bind(this, itemView);

        progressBar.setIndeterminate(true);

        storageUtils = StorageUtils.getInstance(getContext());

        if (SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null) != null) {
            Glide.with(context)
                    .load(SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null))
                    .apply(RequestOptions.circleCropTransform())
                    .apply(App.getProfileImageRequest())
                    .into(profileImage);
        }

        filledTextField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) filledTextField.setHint("");
        });

        return itemView;

    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }


    @OnClick({R.id.profileLayout, R.id.btnFinish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.profileLayout:
                Log.d(TAG, "onViewClicked: 2");
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                    Log.d(TAG, "onViewClicked: 1");
                    if (ActivityCompat.checkSelfPermission(requireContext(),
                            CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onViewClicked: 4");
                        requestPermissions(new String[]{CAMERA},
                                Constants.CAMERA_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ImageViewActivity.this);
                        pickImage();
                    }
                }else{
                    Log.d(TAG, "onViewClicked: 3");
                    if (ActivityCompat.checkSelfPermission(requireContext(),
                            CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(requireContext(),
                                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions( new String[]{CAMERA,
                                        WRITE_EXTERNAL_STORAGE},
                                Constants.CAMERA_REQUEST_CODE);
                    } else {
//                    ImagePicker.pickImage(ImageViewActivity.this);
                        pickImage();
                    }
                }
               /* if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                        CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getContext(),
                                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{CAMERA,
                                    WRITE_EXTERNAL_STORAGE},
                            Constants.CAMERA_REQUEST_CODE);
                } else {
                    //ImagePicker.pickImage(this, "Select your image:");

                    pickImage();
                }*/
                break;
            case R.id.btnFinish:
                hideKeyboardFragment(Objects.requireNonNull(getContext()), Objects.requireNonNull(getView()));
                App.preventMultipleClick(btnFinish);
                callBack.onOkClicked(imageStream, filledTextField.getText().toString().trim());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 234) {
            try {
                uploadImage.setVisibility(View.GONE);
                profileImage.setVisibility(View.VISIBLE);

                imageStream = ImagePicker.getInputStreamFromResult(context, requestCode, resultCode, data);
                String filePath = ImagePicker.getImagePathFromResult(context, requestCode, resultCode, data);

                GetSet.setUserImage(filePath);
                Glide.with(context)
                        .load(filePath)
                        .apply(App.getProfileImageRequest())
                        .into(profileImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
           boolean isCamera = (data == null
                        || data.getData() == null);
           usecamera=(data == null
                   || data.getData() == null);
                Uri imageUri = null;
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                if (isCamera) {     /** CAMERA **/
                    imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(requireContext(),
                            mCurrentPhotoUri), Constants.TAG_TEMP, fileName);

                    //deleteFileForUri(mCurrentPhotoUri);
                    mCurrentPhotoUri = null;
                    Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                } else {            /** ALBUM **/
                    String mimeType = requireContext().getContentResolver().getType(data.getData());
                    if (mimeType != null && mimeType.startsWith("image")) {

                        imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(getContext(),
                                data.getData()), Constants.TAG_TEMP, fileName);

                        Timber.d("Picked: %s", imageUri);
                    } else {
                        Toasty.info(requireContext()
                                , "Videos are not supported yet").show();
                        return;
                    }
                }

                if (imageUri != null) {
                    try {
                        ParcelFileDescriptor pfd = requireContext().getContentResolver().openFileDescriptor(imageUri, "r");
                        imageStream = new FileInputStream(pfd.getFileDescriptor());
                        Bitmap bmp = FileUtil.decodeBitmap(requireContext(), imageUri);
                        if (bmp != null) {
                            uploadImage.setVisibility(View.GONE);
                            profileImage.setVisibility(View.VISIBLE);
                            if(Build.BRAND.equals("samsung")&&isCamera&&Build.VERSION.SDK_INT >= 23)
                            {
                                profileImage.setImageBitmap(rotateImage(bmp, 270));
                            }
                            else
                                {
                                profileImage.setImageBitmap(handleSamplingAndRotationBitmap(getContext(), imageUri));
                            }
                        }

                        /*Glide.with(profileImage)
                                .asBitmap()
                                .load(FileUtil.decodeBitmap(requireContext(), imageUri))
                                .into(profileImage);
*/

                        // byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                        //JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                        //uploadChatImage(imageBytes, fileName, thumbnail, jsonObject);


                    } catch (Exception e) {
                        Timber.e(e);
                        Toasty.error(requireContext()
                                , R.string.something_went_wrong).show();
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

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 500;
        int MAX_WIDTH = 500;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void pickImage() {

        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, Objects.requireNonNull(getView()).findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(requireContext(), R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (captureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    currentPhotoFile = FileUtil.createImageFile(requireContext());
                    photoFile = currentPhotoFile;
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Timber.e(ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCurrentPhotoUri = FileProvider.getUriForFile(requireContext(),
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

            if (chooserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }
            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (permissionGranted) {
                    Log.e(TAG, "onRequestPermissionsResult: :::::1");
//                    ImagePicker.pickImage(this, "Select your image:");
                    pickImage();
                } else {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                        if (shouldShowRequestPermissionRationale(CAMERA) ) {
                            Log.e(TAG, "onRequestPermissionsResult: :::::2");
                            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                        } else {
                            Log.e(TAG, "onRequestPermissionsResult: :::::3");
                            App.makeToast(getString(R.string.camera_storage_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivity(i);

                        }
                    }else{
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
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
                break;
        }
    }



    public void setContext(Context context) {
        this.context = context;
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnFinish.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnFinish.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
