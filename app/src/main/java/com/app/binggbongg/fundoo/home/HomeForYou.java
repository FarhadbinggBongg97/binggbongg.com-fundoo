package com.app.binggbongg.fundoo.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.home.eventbus.homeForYouSwipePrevent;
import com.app.binggbongg.helper.callback.ProfileImageClickListener;
import com.app.binggbongg.helper.callback.ScreenOffsetListener;
import com.app.binggbongg.model.GetSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class HomeForYou extends Fragment {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.viewpager_for_you)
    public CusViewPager viewPager;

    ScreenOffsetListener screenOffsetListener;
    int previousPosition = 0;

    HomeForYouFragmentAdapter pagerAdapter;

    private ForYouVideoFragment mForYouVideoFragment;
    private ForYouProfileFragment mForYouProfileFragment;


    public HomeForYou() {
    }

    public static HomeForYou newInstance(ScreenOffsetListener mScreenOffsetListener) {
        HomeForYou fragment = new HomeForYou();
        fragment.screenOffsetListener = mScreenOffsetListener;
        return fragment;
    }

    // Profile Image Click Callback from ForYouVideoFragment
    private final ProfileImageClickListener mProfileImageClickListener = isClicked -> {
        if (isClicked) viewPager.setCurrentItem(1, true);
        else viewPager.setCurrentItem(0, true);

    };


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
    public void onResume() {
        super.onResume();
        Timber.i("resume called");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.foryou_home, container, false);

        ButterKnife.bind(this, Objects.requireNonNull(rootView));
        initViews();
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true  /*enabled by default*/) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Timber.d("handleOnBackPressed:");

            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }


    private void initViews() {

        pagerAdapter = new HomeForYouFragmentAdapter(getChildFragmentManager(), mProfileImageClickListener);
        viewPager.setAdapter(pagerAdapter);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                previousPosition = position;

                if (position == 1) {
                    screenOffsetListener.pageSwiped("gone");
                    mForYouVideoFragment.exoplayerRecyclerViewForYou.setPlayControl(false);
                } else {
                    screenOffsetListener.pageSwiped("visible");
                    mForYouVideoFragment.exoplayerRecyclerViewForYou.setPlayControl(true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Timber.d("initViews: %s", "user_id=> " + GetSet.getUserId() + "token=> " + GetSet.getAuthToken());
    }


    public class HomeForYouFragmentAdapter extends FragmentPagerAdapter {

        ProfileImageClickListener profileImageClickListener;

        public HomeForYouFragmentAdapter(FragmentManager fm, ProfileImageClickListener mForYouProfileFragment) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.profileImageClickListener = mForYouProfileFragment;
        }

        @Override
        public @NotNull
        Fragment getItem(int position) {
            switch (position) {
                case 0: {

                    mForYouVideoFragment = ForYouVideoFragment.newInstance(profileImageClickListener);
                    return mForYouVideoFragment;
                }
                case 1: {

                    mForYouProfileFragment = ForYouProfileFragment.newInstance(profileImageClickListener);
                    return mForYouProfileFragment;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }


    }


    public ForYouVideoFragment getForYouVideoFragment() {
        return mForYouVideoFragment;
    }

    //public ForYouProfileFragment getForYouProfileFragment() { return mForYouProfileFragment; }


}
