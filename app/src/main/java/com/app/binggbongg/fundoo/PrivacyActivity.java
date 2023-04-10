package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.TermsResponse;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class PrivacyActivity extends BaseFragmentActivity {

    private static final String TAG = "AccountPrivacy";

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.parentLay)
    RelativeLayout parentLay;
    ApiInterface apiInterface;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        ButterKnife.bind(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        btnBack.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);


        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        initWebView();
        if (getIntent().hasExtra(Constants.TAG_TITLE)) {
            setTitle(getIntent().getStringExtra(Constants.TAG_TITLE));
            loadLink(getIntent().getStringExtra(Constants.TAG_DESCRIPTION));
        }else if(getIntent().hasExtra("link_url")){
            setTitle(" ");
            webView.loadUrl(getIntent().getStringExtra("link_url"));
        }else {
            getTerms();
        }

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

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

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient());
        webSetting.setJavaScriptEnabled(true);
        webView.clearCache(true);
    }

    private void getTerms() {
        Call<TermsResponse> call = apiInterface.getTerms();
        call.enqueue(new Callback<TermsResponse>() {
            @Override
            public void onResponse(@NotNull Call<TermsResponse> call, @NotNull Response<TermsResponse> response) {
                TermsResponse termsResponse = response.body();
                if (termsResponse.getStatus().equals(Constants.TAG_TRUE) && termsResponse.getTerms() != null) {
                    setTitle(termsResponse.getTerms().getHelpTitle());
                    loadLink(termsResponse.getTerms().getHelpDescrip());
                }
            }

            @Override
            public void onFailure(@NotNull Call<TermsResponse> call, @NotNull Throwable t) {

            }
        });
    }

    public void setTitle(String title) {
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(title);
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public void loadLink(String helpDescrip) {
        webView.loadData(helpDescrip, "text/html; charset=utf-8", "UTF-8");
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            proceedUrl(view, url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

    }

    private void proceedUrl(WebView view, String url) {
        if (url.startsWith("mailto:")) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
        } else if (url.startsWith("tel:")) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
        } else {
            view.loadUrl(url);
        }
    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        finish();
    }
}
