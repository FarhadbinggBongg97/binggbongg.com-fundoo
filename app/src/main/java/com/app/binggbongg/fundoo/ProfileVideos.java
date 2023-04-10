package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.viewpageranimation.FlipHorizontalPageTransformer;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressLint("NonConstantResourceId")
public class ProfileVideos extends BaseActivity {

    private static final String TAG = ProfileVideos.class.getSimpleName();
    String from, profileId;

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnMyVideos)
    MaterialButton btnMyVideos;

    @BindView(R.id.btnLikedVideos)
    MaterialButton btnLikedVideos;

    @BindView(R.id.viewpager)
    ViewPager viewpager;

    @BindView(R.id.txtTitle)
    TextView txtTitle;

    ViewPagerAdapter adapter;
    private SlidrInterface slidrInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_videos);

        animation();

        ButterKnife.bind(this);

        from = getIntent().getStringExtra(Constants.TAG_FROM);
        profileId = getIntent().getStringExtra(Constants.TAG_PROFILE_ID);

        Log.e(TAG, "onCreate: :::::::::::::::::"+from );

        if (from.equals("own_profile")) {
            btnMyVideos.setText(R.string.my_videos);
        }
        
        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        txtTitle.setText(R.string.videos);

        setUpViewpager();


        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
        });

    }

    private void setUpViewpager() {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MyVideosFragment(profileId, getString(R.string.videos), from), getString(R.string.videos), from);
        adapter.addFragment(new LikedVideosFragment(profileId, getString(R.string.liked_videos), from), getString(R.string.liked_videos), from);
        viewpager.setAdapter(adapter);

        viewpager.setPageTransformer(true, new FlipHorizontalPageTransformer());

        btnMyVideos.setTextColor(getResources().getColor(R.color.colorWhite));
        btnLikedVideos.setTextColor(getResources().getColor(R.color.colorSecondaryText));

        btnMyVideos.setOnClickListener(v -> viewpager.setCurrentItem(0, true));
        btnLikedVideos.setOnClickListener(v -> viewpager.setCurrentItem(1, true));

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {


                switch (position) {
                    case 0:
                    default:
                        btnMyVideos.setTextColor(getResources().getColor(R.color.colorWhite));
                        btnLikedVideos.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                        enableSliding(true);
                        break;
                    case 1:
                        btnMyVideos.setTextColor(getResources().getColor(R.color.colorSecondaryText));
                        btnLikedVideos.setTextColor(getResources().getColor(R.color.colorWhite));
                        enableSliding(false);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        slidrInterface = Slidr.attach(this, config);


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
        public @NotNull Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title, String from) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mFrom.add(from);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();

        /*Intent intent= getIntent();
        intent.putExtra("profile_id", profileId);
        setResult(Constants.HOME_FOR_PROFILE_FANS_FOLLOWING, getIntent());
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);*/
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }
}