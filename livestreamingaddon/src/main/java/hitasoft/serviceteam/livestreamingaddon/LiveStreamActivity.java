package hitasoft.serviceteam.livestreamingaddon;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import hitasoft.serviceteam.livestreamingaddon.external.helper.LocaleManager;
import hitasoft.serviceteam.livestreamingaddon.external.helper.NetworkReceiver;
import hitasoft.serviceteam.livestreamingaddon.external.helper.Utils;
import hitasoft.serviceteam.livestreamingaddon.external.model.LiveStreamRequest;
import hitasoft.serviceteam.livestreamingaddon.external.model.LiveStreamResponse;
import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiClient;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiInterface;
import hitasoft.serviceteam.livestreamingaddon.external.utils.Constants;
import hitasoft.serviceteam.livestreamingaddon.external.utils.GetSet;
import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.UserVideoActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveStreamActivity extends AppCompatActivity {

    public static final String TAG = "LiveStreamActivity";

    ImageView btnBack, btnSettings;
    private FloatingActionButton btnAddStream;
    MaterialButton btnWatchLive;
    MaterialButton btnRecordedLive;
    ViewPager viewpager;
    TextView txtTitle;
    ViewPagerAdapter adapter;
    private SlidrInterface slidrInterface;
    private String getstreamUrl, from = "";
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
    private boolean mPermissionsGranted = false;


    public boolean isPermissionsGranted(String[] permissions) {
        boolean isGranted = false;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            } else {
                isGranted = true;
            }
        }
        return isGranted;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {

            case Constants.STREAM_REQUEST_CODE: {
                if (!isPermissionsGranted(permissions)) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO)) {
                            requestPermissions(permissions, Constants.STREAM_REQUEST_CODE);
                        } else {
                            Toast.makeText(this, "Enable Camera and MicroPhone permissions", Toast.LENGTH_SHORT).show();
//                            openPermissionDialog(permissions);
                            //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
                            startActivity(i);
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, Constants.STREAM_REQUEST_CODE);
                        } else {
                            Toast.makeText(this, "Enable Camera, MicroPhone and Storage permissions", Toast.LENGTH_SHORT).show();
//                            openPermissionDialog(permissions);
                            //makeToast(getString(R.string.camera_microphone_storage_permission_error));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
                            startActivity(i);
                        }
                    }
                } else {
                    MoveToPublishActivity();
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mRequiredPermissions = mRequiredPermissions12;
        }
//        if (!mPermissionsGranted) return;

        btnBack = findViewById(R.id.btnBack);
        btnWatchLive = findViewById(R.id.btnWatchLive);
        btnRecordedLive = findViewById(R.id.btnRecordedLive);
        viewpager = findViewById(R.id.viewpager);
        txtTitle = findViewById(R.id.txtTitle);
        btnSettings = findViewById(R.id.btnSettings);
        btnAddStream = findViewById(R.id.btnAddStream);


        btnBack.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);

        /*if(GetSet.getImageUrl()!=null){
            Glide.with(this).load(GetSet.getImageUrl()).into(btnSettings);
        }else{
            btnSettings.setImageResource(R.drawable.ic_account);
        }
*/


        Glide.with(this)
                .load(GetSet.getImageUrl())
                .error(R.drawable.ic_account)
                .into(btnSettings);


        GetSet.setUserId(getIntent().getStringExtra(StreamConstants.TAG_USER_ID));
        GetSet.setUserName(getIntent().getStringExtra(StreamConstants.TAG_USER_NAME));
        GetSet.setToken(getIntent().getStringExtra(StreamConstants.TAG_TOKEN));
        GetSet.setImageUrl(getIntent().getStringExtra(StreamConstants.TAG_USER_IMAGE));
        getstreamUrl = getIntent().getStringExtra(StreamConstants.TAG_STREAM_BASE_URL);
        GetSet.setStream_base_url(getstreamUrl);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        txtTitle.setText(R.string.live_streams);

        setUpViewpager();

        if (!TextUtils.isEmpty(getIntent().getStringExtra("from"))) {
            from = getIntent().getStringExtra("from");
            if (from.equals("recordlive")) {
                viewpager.setCurrentItem(1);
            }
        } else {
            viewpager.setCurrentItem(0);
        }


        btnBack.setOnClickListener(v -> {
            finish();
//            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
        });

        btnSettings.setOnClickListener(v -> {
            Intent myVideos = new Intent(LiveStreamActivity.this, UserVideoActivity.class);
            myVideos.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(myVideos);
        });

        btnAddStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mPermissionsGranted = hasPermissions(getApplicationContext(), mRequiredPermissions);
                    if (!mPermissionsGranted) {
                        ActivityCompat.requestPermissions(LiveStreamActivity.this, mRequiredPermissions, Constants.STREAM_REQUEST_CODE);
                    } else {
                        mPermissionsGranted = true;
                        MoveToPublishActivity();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mPermissionsGranted = hasPermissions(getApplicationContext(), mRequiredPermissions);
                    if (!mPermissionsGranted) {
                        ActivityCompat.requestPermissions(LiveStreamActivity.this, mRequiredPermissions, Constants.STREAM_REQUEST_CODE);
                    } else {
                        mPermissionsGranted = true;
                        MoveToPublishActivity();
                    }
                } else {
                    mPermissionsGranted = true;
                    MoveToPublishActivity();
                }
                // return if the user hasn't granted the app the necessary permissions

            }
        });

    }

    private void MoveToPublishActivity() {

        Intent stream = new Intent(LiveStreamActivity.this, PublishActivity.class);
        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
        stream.putExtra(Constants.TAG_USER_NAME, GetSet.getUserName());
        stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
        stream.putExtra(Constants.TAG_TOKEN, GetSet.getToken());
        stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStream_base_url());
        startActivity(stream);
    }

    private void setUpViewpager() {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new WatchLiveFragment("watchLive"));
        adapter.addFragment(new WatchLiveFragment("RecordLive"));
        viewpager.setAdapter(adapter);

        viewpager.setPageTransformer(true, new FlipHorizontalPageTransformer());

        btnWatchLive.setTextColor(getResources().getColor(R.color.colorWhite));
        btnRecordedLive.setTextColor(getResources().getColor(R.color.colorSecondaryText));

        btnWatchLive.setOnClickListener(v -> viewpager.setCurrentItem(0, true));
        btnRecordedLive.setOnClickListener(v -> viewpager.setCurrentItem(1, true));

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {


                switch (position) {
                    case 0:
                    default:
                        btnWatchLive.setTextColor(getResources().getColor(R.color.colorWhite));
                        btnRecordedLive.setTextColor(getResources().getColor(R.color.colorSecondaryText));
//                        enableSliding(false);
                        break;
                    case 1:
                        btnWatchLive.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                        btnRecordedLive.setTextColor(getResources().getColor(R.color.colorWhite));
//                        enableSliding(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


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
        private final List<String> mFrom = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public @NotNull
        Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }


    public interface SlidrInterface {

        void lock();

        void unlock();
    }

    public class FlipHorizontalPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float pos) {
            final float rotation = 180f * pos;

            page.setAlpha(rotation > 90f || rotation < -90f ? 0 : 1);
            page.setPivotX(page.getWidth() * 0.5f);
            page.setPivotY(page.getHeight() * 0.5f);
            page.setRotationY(rotation);
        }
    }
}