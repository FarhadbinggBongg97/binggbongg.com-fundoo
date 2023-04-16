package com.app.binggbongg.fundoo.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.home.eventbus.FollowingSwipePrevent;
import com.app.binggbongg.helper.callback.ProfileImageClickListener;
import com.app.binggbongg.helper.callback.ScreenOffsetListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class HomeFollowing extends Fragment {


    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.viewpager_following)
    public CusViewPager viewPager;

    ScreenOffsetListener screenOffsetListener;

    int previousPosition = 0;
    FollowingFragmentAdapter pagerAdapter;

    private FollowingVideoFragment followingVideoFragment;
    private FollowingProfileFragment followingProfileFragment;

    public HomeFollowing() {
    }

    public static HomeFollowing newInstance(ScreenOffsetListener mScreenOffsetListener) {

        HomeFollowing fragment = new HomeFollowing();

        fragment.screenOffsetListener = mScreenOffsetListener;
        return fragment;
    }

    // Profile Image Click Callback from ForYouVideoFragment
    private final ProfileImageClickListener mProfileImageClickListener = isClicked -> {
        if (isClicked) viewPager.setCurrentItem(1, true);
        else viewPager.setCurrentItem(0, true);
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingSwipePrevent event) {

        Timber.d("onMessageEvent: %s", event.swipe);
        viewPager.disableScroll(event.swipe);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                R.layout.following_homepage, container, false);
        Toast.makeText(getActivity(), "ForYouProfileFragment", Toast.LENGTH_SHORT).show();

        ButterKnife.bind(this, Objects.requireNonNull(rootView));

        initView(rootView);


        return rootView;
    }

    public void setCallBack(ScreenOffsetListener screenOffsetListener) {
        this.screenOffsetListener = screenOffsetListener;
    }


    private void initView(ViewGroup rootView) {

        //pagerAdapter = new FollowingFragmentAdapter(requireActivity().getSupportFragmentManager(), mProfileImageClickListener);
        pagerAdapter = new FollowingFragmentAdapter(getChildFragmentManager(), mProfileImageClickListener);
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
                    if (followingVideoFragment.followingExoplayerRecyclerView != null)
                    followingVideoFragment.followingExoplayerRecyclerView.setPlayControl(false);
                } else {
                    screenOffsetListener.pageSwiped("visible");
                    if (followingVideoFragment.followingExoplayerRecyclerView != null)
                    followingVideoFragment.followingExoplayerRecyclerView.setPlayControl(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    /*@Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);

        Timber.d("onAttach: %s", childFragment);
    }*/

    public class FollowingFragmentAdapter extends FragmentPagerAdapter {

        ProfileImageClickListener profileImageClickListener;

        public FollowingFragmentAdapter(FragmentManager fm, ProfileImageClickListener mForYouProfileFragment) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.profileImageClickListener = mForYouProfileFragment;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                default: {

                    followingVideoFragment = FollowingVideoFragment.newInstance(profileImageClickListener);
                    return followingVideoFragment;

                }
                case 1: {

                    followingProfileFragment = FollowingProfileFragment.newInstance(profileImageClickListener);
                    return followingProfileFragment;
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public FollowingVideoFragment getFollowingVideoFragment() {
        return followingVideoFragment;
    }

}
