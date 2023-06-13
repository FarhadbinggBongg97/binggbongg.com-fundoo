package com.app.binggbongg.fundoo.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.binggbongg.fundoo.MainActivity;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.PublishActivity;
import timber.log.Timber;

import static com.app.binggbongg.R2.id.bottom_navigation;


public class HomeFragment extends Fragment implements ForYouVideoFragment.onHideBottomBarEventListener, View.OnClickListener {

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
    TextView tvForYou,tvRecordedLive;
    private Spinner spinner,spinnerBanner;
    String[] arrayForSpinner = {"Following", "Followers", "Live Streamers","Go Live","Category leaders and Winners"};
    String[] arrayBanner = {"Facebook", "Instagram", "Twitter","Website","Youtube"};

    boolean scroll=false;
    boolean discover=false;
    ViewFlipper viewFlipper,viewFlipperScroll;
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
        ButterKnife.bind(this, requireActivity());

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
        tvRecordedLive = rootView.findViewById(R.id.btnRecordedLive);
        homeFragViewPager = rootView.findViewById(R.id.fragment_home_viewpager);

        homeFragViewPager.setPageTransformer(false, new ZoomInTransformer());
        TextView btnScrollBanner=rootView.findViewById(R.id.btnScrollBanner);
        TextView btnDiscover=rootView.findViewById(R.id.btnDiscover);

        TextView tvFollowing=rootView.findViewById(R.id.tv_following_dis);
        TextView tvFollowers=rootView.findViewById(R.id.tv_follower_dis);
        TextView tvLiveStreamers=rootView.findViewById(R.id.tv_live_stream_dis);
        TextView tvGoLive=rootView.findViewById(R.id.tv_go_live_dis);
        TextView tvCategory=rootView.findViewById(R.id.tv_category_dis);

        tvFollowing.setOnClickListener(this);
        tvFollowers.setOnClickListener(this);
        tvLiveStreamers.setOnClickListener(this);
        tvGoLive.setOnClickListener(this);
        tvCategory.setOnClickListener(this);

        viewFlipper = rootView.findViewById(R.id.viewFlipper);
        viewFlipperScroll = rootView.findViewById(R.id.viewFlipperScroll);

        setViewFlipper();
        btnScrollBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scroll){
                    scroll=false;
                    viewFlipperScroll.setVisibility(View.GONE);
                }else {
                    scroll=true;
                    viewFlipperScroll.setVisibility(View.VISIBLE);
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (discover){
                    discover=false;
                    viewFlipper.setVisibility(View.GONE);
                }else {
                    discover=true;
                    viewFlipper.setVisibility(View.VISIBLE);
                }
            }
        });

        spinner = rootView.findViewById(R.id.spinner);
        spinnerBanner = rootView.findViewById(R.id.spinnerBanner);
        spinner.setAdapter(new CustomSpinnerAdapter(getActivity(), R.layout.spinner_row, arrayForSpinner, getActivity().getString(R.string.discover)));
        spinnerBanner.setAdapter(new CustomSpinnerAdapter(getActivity(), R.layout.spinner_row, arrayBanner, getActivity().getString(R.string.scroll_banner)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection here
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (selectedItem){
                    case "Following":
                        if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                            homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                            homeFragViewPager.setCurrentItem(1);
                            homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
                        }
                        break;
                    case "Followers":
                        if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                            homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
                            homeFragViewPager.setCurrentItem(0);
                            homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
                        }
                        break;
                    case "Live Streamers":
                        Intent stream = new Intent(context, PublishActivity.class);
                        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                        stream.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                        stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                        stream.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                        stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                        startActivity(stream);
                        break;
                    case "Go Live":
                        try {
                            ((MainActivity)getActivity()).checkRecordPermissions();
                        } catch (Exception e) {
                            Timber.i("Error onClickAddStory %s", e.getMessage());
                        }
                        break;
                    case "Category leaders and Winners":
//                       ProfileFragment profileFragment = new ProfileFragment();
//                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                        transaction.replace(R.id.fragment_home_viewpager, profileFragment);
//                        transaction.addToBackStack(null);
//                        transaction.commit();
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerBanner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection here
                String selectedItem = parent.getItemAtPosition(position).toString();
                switch (selectedItem){
                    case "Facebook":
                        break;
                    case "Instagram":

                        break;
                    case "Twitter":
                       break;
                    case "Website":

                        break;
                    case "Youtube":

                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        tvForYou.setOnClickListener(view -> {

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
                        tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    }
                } else if (previousPosition==1){
                    if (positionOffset > 1) {

                    } else {

                        tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                        tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    }
                }
                else {
                    if (positionOffset > 1) {

                    } else {

                        tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                        tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    tvForYou.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                } else if (position == 1) {

                    tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    tvRecordedLive.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                }else if (position==2){
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_following_dis:
                if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                    homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                    homeFragViewPager.setCurrentItem(1);
                    homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
                }
                break;
            case R.id.tv_follower_dis:
                if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                    homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
                    homeFragViewPager.setCurrentItem(0);
                    homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
                }
                break;
            case R.id.tv_live_stream_dis:
                Intent stream = new Intent(context, PublishActivity.class);
                stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                stream.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                stream.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                stream.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                stream.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                stream.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                stream.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                startActivity(stream);
                break;
            case R.id.tv_go_live_dis:
                try {
                    ((MainActivity)getActivity()).checkRecordPermissions();
                } catch (Exception e) {
                    Timber.i("Error onClickAddStory %s", e.getMessage());
                }
                break;
            case R.id.tv_category_dis:
                break;
        }
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


    public class CustomSpinnerAdapter extends ArrayAdapter<String>{

        Context context;
        String[] objects;
        String firstElement;
        boolean isFirstTime;

        public CustomSpinnerAdapter(Context context, int textViewResourceId, String[] objects, String defaultText) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.objects = objects;
            this.isFirstTime = true;
            setDefaultText(defaultText);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if(isFirstTime) {
                objects[0] = firstElement;
                isFirstTime = false;
            }
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            notifyDataSetChanged();
            return getCustomView(position, convertView, parent);
        }

        public void setDefaultText(String defaultText) {
            this.firstElement = objects[0];
            objects[0] = defaultText;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.spinner_row, parent, false);
            TextView label = (TextView) row.findViewById(R.id.btnRecent);
            label.setText(objects[position]);

            return row;
        }

    }
    public void setViewFlipper()
    {
        // -------View Flipper
        //1 - Create the animations, we will load the animations from
        //    the already made animations in the android.R.anim folder
        //    custom animations can be added into to res/anim folder
        Animation anim_in = AnimationUtils.loadAnimation(context,R.anim.anim_rtl_right_in);
        Animation anim_out = AnimationUtils.loadAnimation(context,R.anim.anim_slide_left_out);

        //3 - Assisgn the animations
        viewFlipper.setInAnimation(anim_in);
        viewFlipper.setOutAnimation(anim_out);

        viewFlipperScroll.setInAnimation(anim_in);
        viewFlipperScroll.setOutAnimation(anim_out);

        //4 - Set the flipping interval, that is the time between flips
        viewFlipper.setFlipInterval(2000);
        viewFlipperScroll.setFlipInterval(2000);

        //5 - Start FLipping - optional here, can also be
        //    called on click for click to flip
        viewFlipper.startFlipping();
        viewFlipperScroll.startFlipping();
    }

}
