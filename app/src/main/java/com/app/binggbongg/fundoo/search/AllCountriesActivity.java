package com.app.binggbongg.fundoo.search;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.BaseFragmentActivity;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.model.CountryResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AllCountriesActivity extends BaseFragmentActivity {

    private static final String TAG = AllCountriesActivity.class.getSimpleName();
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.countryView)
    RecyclerView countryView;
    private ApiInterface apiInterface;
    private List<GradientDrawable> countryBgList = new ArrayList<>();
    private List<CountryResponse.Result> countryList = new ArrayList<>();

    private GridLayoutManager mLayoutManager;
    private AppUtils appUtils;
    private int bgListIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_countries);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        countryBgList = appUtils.getCountryBGList();
        initView();
    }

    private void initView() {
        btnBack.setScaleX(LocaleManager.isRTL() ? -1 : 1);
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        txtTitle.setText(getString(R.string.popular_countries));
        txtTitle.setVisibility(View.VISIBLE);



    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }


    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }

}
