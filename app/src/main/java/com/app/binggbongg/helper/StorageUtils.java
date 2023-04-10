package com.app.binggbongg.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.app.binggbongg.R;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.FileUtil;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.apachecommons.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class StorageUtils {
    private static final String TAG = StorageUtils.class.getSimpleName();
    private static StorageUtils mInstance;
    private final Context context;
    private String appDirectory;
    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";
    public static final String VIDEO_CACHE_DIR = "video-cache";
    public static final String TEMP_CACHE_DIR = "Temp";

    public StorageUtils(Context context) {
        this.context = context;
        appDirectory = "/" + context.getString(R.string.app_name);

    }

    public static StorageUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageUtils(context);
        }
        return mInstance;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
        fileName = getFileName(fileName);

        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        Uri outUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + path);
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

            /*if (Objects.equals(from, Constants.TAG_SENT) && !containsNoMedia(Environment.DIRECTORY_PICTURES + path)) {
                makeNoMedia(Environment.DIRECTORY_PICTURES + path);
            }*/

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            outUri = resolver.insert(collection, contentValues);

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

                ParcelFileDescriptor pfd = resolver.openFileDescriptor(outUri, "w");
                boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));

                Timber.i("Save: %s", outUri.getPath());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Timber.e("Save: failed %s", e.getMessage());
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(outUri, contentValues, null, null);
            }
            return outUri;
        } else {
            // for API 28 and below (in progress)
           /* final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
            File targetDir = new File(directory);

            File folder = new File(targetDir, path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) {
                    if (folder.mkdirs()) {
                        Timber.i("Directory created: %s", targetDir.getAbsolutePath());
                    }
                    File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                    try {
                        if (!noMediaFile.exists()) {
                            noMediaFile.createNewFile();
                        }
                    } catch (IOException e) {
                        Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                folder.mkdirs();
            }

            File createdMedia = new File(folder.getAbsoluteFile(), fileName);
            if (createdMedia.exists()) createdMedia.delete();*/

            File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) folder.mkdirs();
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                folder.mkdirs();
            }

            File file = new File(folder.getAbsoluteFile(), fileName);
            if (file.exists())
                file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                refreshGallery(file);
                return createImageUri(from, file.getAbsolutePath());
            } catch (Exception e) {
                Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Logging.i(TAG, "Finished scanning " + file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveThumbNail(Bitmap bitmap, String filename) {
        String stored = "";
        String path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return file.getPath();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    public boolean saveToGallery(Bitmap bitmap, @NonNull String name, String from) throws IOException {
        boolean saved;
        OutputStream fos;
        File image = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.TITLE, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +
                    File.separator + context.getString(R.string.app_name) + File.separator);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir;
            if (from != null && from.equals("QRCode")) {
                imagesDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString() + File.separator + context.getString(R.string.app_name) +
                        File.separator + context.getString(R.string.my_qr_code);
            } else {
                imagesDir = Environment.getExternalStorageDirectory().toString() + File.separator
                        + getChildDir("");
            }
            File file = new File(imagesDir);
            boolean isDir = false;
            if (!file.exists()) {
                isDir = file.mkdirs();
            }
            image = new File(imagesDir, name);
            if (!image.exists()) {
                image.createNewFile();
            }
            fos = new FileOutputStream(image);
        }
        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        if (saved && image != null) {
            refreshGallery(image);
        }
        return saved;
    }

    public boolean checkIfImageExists(String from, String imageName) {
        return getImageUri(from, imageName) != null;
    }

    /*public boolean checkIfImageExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file != null && file.exists();
    }*/

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getPath(@NonNull String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = File.separator + context.getString(R.string.app_name) + File.separator + context.getString(R.string.app_name) + "Images" + File.separator + "Sent" + File.separator;

                break;
            case "profile":
                path = File.separator + context.getString(R.string.app_name) + File.separator + context.getString(R.string.app_name) + "Images" + File.separator + "Profile" + File.separator;
                break;
            case Constants.TAG_THUMBNAIL:
                path = File.separator + context.getString(R.string.app_name) + File.separator + context.getString(R.string.app_name) + "Images" + File.separator + ".thumbnails" + File.separator;
                break;
            case "temp":
                path = File.separator + context.getString(R.string.app_name) + File.separator + context.getString(R.string.app_name) + "Images" + File.separator + "temp" + File.separator;
                break;
            case Constants.TAG_AUDIO_SENT:
                //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                path = "/" + "Android" + "/" + "data/" + "com.app.binggbongg/"+"files/"+"_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                path = "/" + "Android" + "/" + "data/" + "com.app.binggbongg/"+"files/"+"_Audio/";
                break;
            default:
                path = File.separator + context.getString(R.string.app_name) + File.separator + context.getString(R.string.app_name) + "Images" + File.separator;
                break;
        }
        if(from.equals("audio_sent")||from.equals("audio_receive")) {
            File sdcard = Environment.getExternalStorageDirectory();
            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile();
                }
            } catch (IOException e) {
                Logging.e(TAG, "getPath: " + e.getMessage());
                e.printStackTrace();
            }
            return folder.getPath();
        }
        return path;
    }

    public Uri createImageUri(String from, String filePath) {
        Uri imageUri = getImageUri(from, getFileName(filePath));
        if (imageUri == null) {
            // create a new uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            } else {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();

                final String fileName = getFileName(filePath);
                final String path = getPath(from);
                Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.DATA, filePath);

                imageUri = resolver.insert(collection, values);
            }
        }
        return imageUri;
    }

    public Uri getImageUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri imageUri = null;
        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String selection;
        final String[] selectionArgs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " +
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            selectionArgs = new String[]{fileName, Environment.DIRECTORY_PICTURES + path};
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + path;
            selection = MediaStore.MediaColumns.DATA + "=?";
            selectionArgs = new String[]{path + fileName};
        }
        try (Cursor c = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (c.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID)));
                Timber.d("Check file: exists %s %s", fileName, imageUri);
            } else {
                Timber.d("Check file: not found %s", (Object) selectionArgs);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return imageUri;
    }

    public File getImage(String from, String imageName) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case Constants.TAG_SENT:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case Constants.TAG_THUMBNAIL:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                case Constants.TAG_AUDIO_SENT:
                    //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name)+ "_Audio/Sent/";
                    path = "/" + "Android" + "/" + "data/" + "com.app.binggbongg/"+"files/"+"_Audio/Sent/";
                    break;
                case Constants.TAG_AUDIO_RECEIVE:
                    //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name)+ "_Audio/";
                    path = "/" + "Android" + "/" + "data/" + "com.app.binggbongg/"+"files/"+"_Audio/";
                    break;
                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                    break;
            }

                mediaImage = new File(myDir.getPath() + path + FilenameUtils.getName(imageName));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getFileSavePath(String from) {
        return new File(getRootDir(context) + getChildDir(from));
    }
    public boolean checkIfFileExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file != null && file.exists();
    }

    public File getRootDir(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, null)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
//        Log.i(TAG, "getAppDataDir: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public String getChildDir(String from) {
        String path;
        switch (from) {
            case "sent":
                path = appDirectory + IMG_SENT_PATH;
                break;
            case "profile":
                path = appDirectory + IMG_PROFILE_PATH;
                break;
            case "banner":
                path = appDirectory + IMG_BANNER_PATH;
                break;
            default:
                path = appDirectory + IMG_HOME_PATH;
                break;
        }
        return path;
    }

    public Uri getTempFileUri(Context context) {
        final String path = getPath("temp");
        final String fileName = "Temp_" + System.currentTimeMillis() + ".jpg";

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");

            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            // for API 28 and below
            final File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + path);
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    Timber.d("Directory: created %s", folder);
                }
            }
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");

            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        return resolver.insert(collection, values);
    }

    public File getTempFile(Context context, String fileName) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, "Temp")[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    /*public Uri SaveVideoToGallery(Context context, File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, "My video title");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }*/

    public static void exportMp4ToGallery(Context context, String filePath) {

        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }

    public void  clearTempCachee() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            File mDataDir = ContextCompat.getExternalFilesDirs(context, "Temp")[0];

            if (mDataDir.listFiles() != null) {
                for (File listFile : mDataDir.listFiles()) {
                    listFile.delete();
                }
            }

        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.listFiles() != null) {
                for (File listFile : mDataDir.listFiles()) {
                    listFile.delete();
                }
            }
        }
    }


    public String saveVideoThumbNail(Bitmap bitmap, String filename) {

        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, "Temp")[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "saveVideoThumbNail: " + mDataDir);



/*
        String stored = "";

        String path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        //File file = new File(folder.getAbsoluteFile(), filename);

        File file = new File(mDataDir.getPath() + File.separator + filename);


        /*if (file.exists())
            return file.getPath();*/

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            //   stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }


    public File TrimmerPath(Context context, String fileName, String audioVideo) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, audioVideo)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    public File saveToAppDir(Bitmap bitmap, String from, String filename) {
        boolean stored = false;
        File sdcard = getRootDir(context);
        String path = getChildDir(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists())
            folder.mkdir();

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

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

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

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

    public void deleteCacheDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                            file.delete();
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                        file.delete();
                    }
                }
        }
    }

    public void clearVideoCache() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                            for (File listFile : file.listFiles()) {
                                listFile.delete();
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                        for (File listFile : file.listFiles()) {
                            listFile.delete();
                        }
                        break;
                    }
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean containsNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        final String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " +
                MediaStore.MediaColumns.TITLE + "=?";
        final String[] selectionArgs = new String[]{dir, MediaStore.MEDIA_IGNORE_FILENAME};
        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        try (Cursor c = resolver.query(collection,
                null, selection, selectionArgs,     null)) {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void makeNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.TITLE, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, dir);

        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        final Uri target = resolver.insert(collection, contentValues);

        try (ParcelFileDescriptor pfd = new ParcelFileDescriptor(resolver.openFileDescriptor(target, "w"));
             FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            outputStream.flush();
            Timber.d("Gallery: .nomedia created at " + dir);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }


    public boolean ifFolderExists(String from){
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMBNAIL:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            case Constants.TAG_AUDIO_SENT:
                //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/Sent/";
                path = "/"+ "Android" +"/" + "data/" + "com.app.binggbongg/"+"files/"+"_Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
                //path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name).replaceAll(" ", "") + "_Audio/";
                path = "/" + "Android" + "/" + "data/" + "com.app.binggbongg/"+"files/"+"_Audio/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                break;
        }



        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists()) {
            folder.mkdirs();
            return true;
        }
        return true;
    }
}
