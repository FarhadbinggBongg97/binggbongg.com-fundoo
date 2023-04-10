package hitasoft.serviceteam.livestreamingaddon;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("NonConstantResourceId")
public class LinkViewActivity extends AppCompatActivity {

    private static final String TAG = "AccountPrivacy";

    ImageView btnBack;

    TextView txtTitle;

    WebView webView;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_view);

        btnBack= findViewById(R.id.btnBack);
        txtTitle= findViewById(R.id.txtTitle);
        webView= findViewById(R.id.webView);
        progressBar= findViewById(R.id.progressBar);

        btnBack.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        btnBack.setOnClickListener(v -> onBackPressed());

        initWebView();
     if(getIntent().hasExtra("link_url")){
            setTitle(" ");
            webView.loadUrl(getIntent().getStringExtra("link_url"));
        }else {
         Toast.makeText(this, "Oops! Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
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


    public void setTitle(String title) {
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(title);
        txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
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

    public void onViewClicked() {
        finish();
    }
}
