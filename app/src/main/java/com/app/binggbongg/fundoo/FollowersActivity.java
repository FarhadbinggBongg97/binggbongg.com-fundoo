package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint("NonConstantResourceId")
public class FollowersActivity extends BaseFragmentActivity {

    private static final String TAG = FollowersActivity.class.getSimpleName();

    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.shimmerLayout)
    ShimmerFrameLayout shimmerLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    String partnerId;
    FollowingsFragment followingsFragment;
    FollowersFragment followersFragment;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    SlidrInterface slidrInterface;
    @BindView(R.id.adView)
    AdView adView;
    public static boolean hasInterestChange, hasFriendChange;
    public String from ="", shareLink="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);
        partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        slidrInterface = Slidr.attach(this, config);
        shimmerLayout.startShimmer();
        initView();
        if(getIntent().getStringExtra("from")!=null){
            from = getIntent().getStringExtra("from");
            shareLink = getIntent().getStringExtra("share_link");
        }

    }

    private void initView() {
        setToolBarTitle(0);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setToolBarTitle(position);
                if (position == 0) {
                    enableSliding(true);
                } else {
                    enableSliding(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if ((getIntent().getStringExtra(Constants.TAG_ID) != null) &&
                (getIntent().getStringExtra(Constants.TAG_ID).equals(Constants.TAG_FOLLOWINGS))) {
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(0);
        }

        if (GetSet.getPremiumMember()!=null && GetSet.getPremiumMember().equals(Constants.TAG_FALSE))


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

    }

    private void setToolBarTitle(int position) {
        if (position == 0) {
            if (shimmerLayout.isShimmerStarted()) {
                shimmerLayout.stopShimmer();
            }
            shimmerLayout.startShimmer();
            txtTitle.setText(getString(R.string.fans));
            txtSubTitle.setText("");
            if (partnerId.equals(GetSet.getUserId())) {
                txtSubTitle.setText(R.string.swipe_to_see_followings);
            } else {
                txtSubTitle.setText(R.string.swipe_to_see_other_followings);
            }
            txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        } else {
            if (shimmerLayout.isShimmerStarted()) {
                shimmerLayout.stopShimmer();
            }
            shimmerLayout.startShimmer();
            txtTitle.setText(getString(R.string.followings));
            txtSubTitle.setText("");
            if (partnerId.equals(GetSet.getUserId())) {
                txtSubTitle.setText(R.string.swipe_to_see_followers);
            } else {
                txtSubTitle.setText(R.string.swipe_to_see_other_followers);
            }
            txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 2;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                followersFragment = new FollowersFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                bundle.putString("from", from);
                bundle.putString("share_link", shareLink);
                followersFragment.setArguments(bundle);
                return followersFragment;
            } else {
                followingsFragment = new FollowingsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                bundle.putString("from", from);
                bundle.putString("share_link", shareLink);
                followingsFragment.setArguments(bundle);
                return followingsFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
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
    public void onNetworkConnectionChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void enableSliding(boolean enable) {
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }

}
