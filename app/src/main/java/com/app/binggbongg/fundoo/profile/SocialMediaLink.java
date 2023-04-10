package com.app.binggbongg.fundoo.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.BaseActivity;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.SocialMediaLinks;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialMediaLink extends BaseActivity implements View.OnClickListener {

    private static final String TAG = SocialMediaLink.class.getSimpleName();
    EditText fbLink, twitterLink, whatsAppLink, tiktokLink, snapchatLink, youtubeLink, pinterest,
            instagram, linkedIn;
    MaterialButton save;
    ApiInterface apiInterface;
    ImageView btnBack;
    TextView title;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media_link);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
        setData();


    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    private void initView() {
        fbLink = findViewById(R.id.et_facebook);
        twitterLink = findViewById(R.id.et_twitter);
        tiktokLink = findViewById(R.id.et_tiktok);
        whatsAppLink = findViewById(R.id.et_whatsapp);
        snapchatLink = findViewById(R.id.et_snapchat);
        youtubeLink = findViewById(R.id.et_youtube);
        pinterest = findViewById(R.id.et_pinterest);
        instagram = findViewById(R.id.et_instagram);
        linkedIn = findViewById(R.id.et_linkedIn);
        btnBack =findViewById(R.id.btnBack);
        title = findViewById(R.id.txtTitle);
        save = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        save.setOnClickListener(this);
    }

    public void setData(){
        btnBack.setOnClickListener(v -> onBackPressed());
        title.setText(getResources().getString(R.string.social_media));

        fbLink.setText(GetSet.getFbLink());
        twitterLink.setText(GetSet.getTwitterLink());
        linkedIn.setText(GetSet.getLinkedInLink());
        tiktokLink.setText(GetSet.getTiktokLink());
        whatsAppLink.setText(GetSet.getWhatsappLink());
        instagram.setText(GetSet.getInstaLink());
        snapchatLink.setText(GetSet.getSnapChatLink());
        youtubeLink.setText(GetSet.getYoutubeLink());
        pinterest.setText(GetSet.getPinterestLink());

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSave:
                updateSocialMediaLink();
                break;
        }

    }

    private void updateSocialMediaLink() {
        showLoading();
        HashMap<String, String> params = new HashMap<>();
        params.put("fb", fbLink.getText().toString());
        params.put("user_id", GetSet.getUserId());
        params.put("youtube_link",youtubeLink.getText().toString().trim());
        params.put("linkedin", linkedIn.getText().toString().trim());
        params.put("instagram", instagram.getText().toString().trim());
        params.put("snapchat", snapchatLink.getText().toString().trim());
        params.put("whatsapp", whatsAppLink.getText().toString().trim());
        params.put("twitter", twitterLink.getText().toString().trim());
        params.put("tiktok", tiktokLink.getText().toString().trim());
        params.put("pinterest", pinterest.getText().toString().trim());

        Log.d(TAG, "SocialMedia: Params" + params);
        Call<SocialMediaLinks> call = apiInterface.getSocialMediaLink(params);
        call.enqueue(new Callback<SocialMediaLinks>() {
            @Override
            public void onResponse(@NonNull Call<SocialMediaLinks> call, @NonNull Response<SocialMediaLinks> response) {
                Log.d(TAG, "SocialMediaLink: response" + new Gson().toJson(response.body()));
                if(response.body()!=null&& response.body().getStatus().equals(Constants.TAG_TRUE)){
                    hideLoading();
                    SocialMediaLinks links = response.body();
                    GetSet.setFbLink(links.getResult().getFbLink());
                    GetSet.setInstaLink(links.getResult().getInstaLink());
                    GetSet.setLinkedInLink(links.getResult().getLinkedInLink());
                    GetSet.setPinterestLink(links.getResult().getPinterestLink());
                    GetSet.setTiktokLink(links.getResult().getTiktokLink());
                    GetSet.setTwitterLink(links.getResult().getTwitterLink());
                    GetSet.setWhatsappLink(links.getResult().getWhatsAppLink());
                    GetSet.setSnapChatLink(links.getResult().getSnapChatLink());
                    GetSet.setYoutubeLink(links.getResult().getYouTubeLink());
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SocialMediaLinks> call, @NonNull Throwable t) {
                Log.d(TAG, "SocialMediaLink: onFailure" + t);
                call.cancel();
                hideLoading();
            }
        });

    }

   private void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }
}