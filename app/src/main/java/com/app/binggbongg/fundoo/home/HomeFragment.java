package com.app.binggbongg.fundoo.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.callback.ScreenOffsetListener;
import com.app.binggbongg.helper.viewpageranimation.ZoomInTransformer;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.StreamListActivity;
import timber.log.Timber;

import static com.app.binggbongg.R2.id.bottom_navigation;


public class HomeFragment extends Fragment implements ForYouVideoFragment.onHideBottomBarEventListener {

    public static final String TAG = HomeFragment.class.getSimpleName();

    public static final int BOTTOM_MARGIN = 150;

    ApiInterface apiInterface;
    private Context context;

    LinearLayout tabLayout;
    public CustomViewPager homeFragViewPager;

    AppUtils appUtils;
    private int previousPosition = 0;

    public HomeFollowing homeFollowing;
    public HomeForYou homeForYou;

    boolean isShow;

    Bundle bundle;

    @Override
    public void onHideEvent(boolean s) {
        isShow=s;
    }

    @BindView(bottom_navigation)
    BottomNavigationView bottomNavigation;
    TextView tvForYou, tvFollowing,tvRecordedLive;

    private final ScreenOffsetListener mScreenOffsetListener = visible -> {

        if (visible.equals("visible")) {
            bottomNavigation.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
        } else {

            bottomNavigation.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

//        EventBus.getDefault().register(this);
    }

    public HomeFragment() {
       }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_home, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView(rootView);
        appUtils = new AppUtils(getActivity());
        ButterKnife.bind(this, Objects.requireNonNull(getActivity()));

        bundle=new Bundle();
        bundle.putBoolean("isShow",isShow);

        return rootView;
    }

    @Override
    public void setTargetFragment(@Nullable Fragment fragment, int requestCode) {
        super.setTargetFragment(fragment, requestCode);
    }

    @Override
    public void onPrimaryNavigationFragmentChanged(boolean isPrimaryNavigationFragment) {
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View rootView) {

        tabLayout = rootView.findViewById(R.id.tabLayout);
        tvForYou = rootView.findViewById(R.id.btnRecent);
        tvFollowing = rootView.findViewById(R.id.btnFollowings);
        tvRecordedLive = rootView.findViewById(R.id.btnRecordedLive);
        homeFragViewPager = rootView.findViewById(R.id.fragment_home_viewpager);

        homeFragViewPager.setPageTransformer(false, new ZoomInTransformer());

        tvForYou.setOnClickListener(view -> {

            if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
                homeFragViewPager.setCurrentItem(0);
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
            }

        });
        tvFollowing.setOnClickListener(view -> {

            if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                homeFragViewPager.setCurrentItem(1);
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
            }


        });

        tvRecordedLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent liveStreaming = new Intent(getContext(), LiveStreamActivity.class);
                liveStreaming.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                liveStreaming.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                liveStreaming.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                liveStreaming.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                liveStreaming.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                liveStreaming.putExtra("from", "recordlive");
                startActivity(liveStreaming);
            }
        });

        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getChildFragmentManager(), mScreenOffsetListener);
        homeFragViewPager.setAdapter(pagerAdapter);
        homeFragViewPager.setOffscreenPageLimit(1);

        homeFragViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


                if (previousPosition == 0) {
                    if (positionOffset > 1) {

                    } else {

                        tvForYou.setTextColor(context.getResources().getColor(R.color.colorWhite));
                        tvFollowing.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                        tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    }
                } else if (previousPosition==1){
                    if (positionOffset > 1) {

                    } else {

                        tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                        tvFollowing.setTextColor(context.getResources().getColor(R.color.colorWhite));
                        tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    }
                }
                else {
                    if (positionOffset > 1) {

                    } else {

                        tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                        tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorWhite));
                        tvFollowing.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {


                if (position == 0) {
                    tvForYou.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    tvFollowing.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                } else if (position == 1) {

                    tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    tvFollowing.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                }else if (position==2){
                    tvFollowing.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                }

                previousPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }


        });


    }


    public class HomePagerAdapter extends FragmentPagerAdapter {
        private final ScreenOffsetListener mScreenOffsetListener;


        public HomePagerAdapter(FragmentManager fm, ScreenOffsetListener screenOffsetListener) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mScreenOffsetListener = screenOffsetListener;
        }

        @Override
        public @NotNull Fragment getItem(int position) {

            Timber.d("getItem: %d", position);

            switch (position) {

                case 0:
                default: {

                    homeForYou = HomeForYou.newInstance(mScreenOffsetListener);
                    homeForYou.setArguments(bundle);
                    return homeForYou;

                }
                case 1: {

                    homeFollowing = HomeFollowing.newInstance(mScreenOffsetListener);
                    homeFollowing.setArguments(bundle);
                    return homeFollowing;

                }
            }

        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }


    @Override
    public void onResume() {

        super.onResume();
        if (getArguments() != null) {
            isShow = getArguments().getBoolean("isShow", false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bundle.putBoolean("isShow",isShow);

    }

    @Override
    public void onStop() {
        super.onStop();
        //   EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
