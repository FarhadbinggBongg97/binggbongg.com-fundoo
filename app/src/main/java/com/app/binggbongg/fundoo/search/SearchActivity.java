package com.app.binggbongg.fundoo.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.helper.FragmentObserver;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.viewpageranimation.FlipHorizontalPageTransformer;
import com.app.binggbongg.model.event.GetSearchEvent;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SearchActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "SearchActivity";

    public static String searchQuery = "";
    TabLayout tabLayout;
    ViewPager viewPager;
    ImageView btnClear, btnBack;
    EditText edtSearch;
    CoordinatorLayout contentLay;
    ViewPagerAdapter adapter;
    private ApiInterface apiInterface;
    private Context context;
    private Typeface typeface;
    private String from;
    private SlidrInterface slidrInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackground(new ColorDrawable(Color.WHITE));
        setContentView(R.layout.activity_search);
        context = this;
        searchQuery = "";
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(context, R.font.font_regular);
        }
        initView();

    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .build();
        slidrInterface = Slidr.attach(this, config);


        viewPager = findViewById(R.id.fragment_home_viewpager);
        tabLayout = findViewById(R.id.tabs);
        contentLay = findViewById(R.id.contentLay);
        btnClear = findViewById(R.id.btnClear);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);


        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        setupViewPager(viewPager);
        // viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        changeTabsFont();

        btnClear.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    AppUtils.hideKeyboard(SearchActivity.this);
                    try {
                        searchQuery = edtSearch.getText().toString().trim();
                        updateFragments();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    if (btnClear.getVisibility() != View.VISIBLE) {
                        btnClear.setVisibility(View.VISIBLE);
                    }
                } else if (editable.length() == 0) {
                    btnClear.setVisibility(View.VISIBLE);
                }
            }
        });

        if (!TextUtils.isEmpty(from) && from.equals(Constants.TAG_SEARCH)) {
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(0);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    // For set custom font in tab
    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeface);
                }
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {


        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new SearchUser(), getString(R.string.search_user));
        adapter.addFragment(new SearchVideos(), getString(R.string.search_video));
        adapter.addFragment(new SearchSounds(), getString(R.string.search_sound));
        adapter.addFragment(new SearchHashTag(), getString(R.string.search_hashtag));

        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new FlipHorizontalPageTransformer());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                    default:
                        edtSearch.setHint(getString(R.string.search_for_users));
                        enableSliding(true);
                        break;
                    case 1:
                        edtSearch.setHint(getString(R.string.search_for_videos));
                        enableSliding(false);
                        break;
                    case 2:
                        edtSearch.setHint(getString(R.string.search_for_sounds));
                        enableSliding(false);
                        break;
                    case 3:
                        edtSearch.setHint(getString(R.string.search_for_hashtag));
                        enableSliding(false);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateFragments() {
        adapter.updateFragments();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnClear:
                edtSearch.getText().clear();
                searchQuery = "";
                updateFragments();
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            default:
                break;
        }
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
        private Observable mObservers = new FragmentObserver();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragmentList.get(position) instanceof Observer)
                mObservers.addObserver((Observer) mFragmentList.get(position));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLiveStreams(GetSearchEvent searchEvent) {
        updateFragments();
    }
}
