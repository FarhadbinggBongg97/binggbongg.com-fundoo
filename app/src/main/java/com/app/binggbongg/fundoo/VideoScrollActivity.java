package com.app.binggbongg.fundoo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.Toast;

import com.app.binggbongg.R;
import com.app.binggbongg.external.CusViewpager;
import com.app.binggbongg.fundoo.home.CusViewPager;
import com.app.binggbongg.fundoo.home.ForYouProfileFragment;
import com.app.binggbongg.fundoo.home.eventbus.homeForYouSwipePrevent;
import com.app.binggbongg.helper.callback.ProfileImageClickListener;
import com.app.binggbongg.model.GetSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class VideoScrollActivity extends BaseActivity {

    CusViewpager viewPager;
    private VideoScrollFragment videoScrollFragment;
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_scroll);
        setData();

        bundle = getIntent().getBundleExtra("data");
        Timber.d("getVideos " + bundle);

    }

    private final ProfileImageClickListener mProfileImageClickListener = isClicked -> {
        if (isClicked) viewPager.setCurrentItem(1, true);
        else viewPager.setCurrentItem(0, true);

    };

    private void setData() {
        viewPager = findViewById(R.id.video_viewPager);
        //viewPager.disableScroll(true);

        VideoFragmentAdapter fragmentAdapter = new VideoFragmentAdapter(getSupportFragmentManager(), mProfileImageClickListener);
        viewPager.setAdapter(fragmentAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // previousPosition = position;

                if (position == 1) {
                    // mScreenOffsetListener.pageSwiped("gone");
                    videoScrollFragment.exoplayerRecyclerViewForYou.setPlayControl(false);
                } else {
                    //mScreenOffsetListener.pageSwiped("visible");
                    videoScrollFragment.exoplayerRecyclerViewForYou.setPlayControl(true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Timber.d("initViews: %s", "user_id=> " + GetSet.getUserId() + "token=> " + GetSet.getAuthToken());

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(homeForYouSwipePrevent event) {

        viewPager.disableScroll(event.swipe);

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Override
    public void onBackPressed() {
        if (viewPager != null) {
            if (viewPager.getCurrentItem() == 1) {
                viewPager.setCurrentItem(0);
            } else if (viewPager.getCurrentItem() == 0) {
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("resume called");
    }

    public class VideoFragmentAdapter extends FragmentPagerAdapter {

        ProfileImageClickListener profileImageClickListener;

        public VideoFragmentAdapter(FragmentManager fm, ProfileImageClickListener mForYouProfileFragment) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.profileImageClickListener = mForYouProfileFragment;
        }

        @Override
        public @NotNull
        Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    videoScrollFragment = VideoScrollFragment.newInstance(profileImageClickListener);
                    videoScrollFragment.setData(bundle);
                    return videoScrollFragment;
                }
                case 1: {
                    return ForYouProfileFragment.newInstance(profileImageClickListener);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }
}