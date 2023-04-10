package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.binggbongg.Deepar.DeeparActivity;
import com.google.android.material.button.MaterialButton;
import com.app.binggbongg.R;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class HashTagActivity extends BaseFragmentActivity {

    @BindView(R.id.btnBack)
    ImageView btnBack;

    @BindView(R.id.btnEdit)
    ImageView btnEdit;

    @BindView(R.id.hashTagName)
    TextView hashTagName;

    @BindView(R.id.hashTagCount)
    TextView hashTagCount;

    @BindView(R.id.hashTagImage)
    ImageView hashTagImage;

    @BindView(R.id.btnUseHashTag)
    MaterialButton btnUseHashTag;

    @BindView(R.id.btnRelatedVideos)
    MaterialButton btnRelatedVideos;

    private String hashTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);
        ButterKnife.bind(this);

        animation();

        btnBack.setColorFilter(getResources().getColor(R.color.colorWhite));
        btnEdit.setVisibility(View.GONE);

        hashTag = getIntent().getStringExtra(Constants.TAG_SELECT_HASH_TAG);

        setDataToView();


    }

    private void setDataToView() {


        btnRelatedVideos.setOnClickListener(v -> {

            Intent intent = new Intent(this, RelatedHashTagVideoActivity.class);
            intent.putExtra(Constants.TAG_TITLE, hashTag);
            startActivity(intent);

        });

        btnUseHashTag.setOnClickListener(v -> {

            Intent intent = new Intent(this, DeeparActivity.class);
            intent.putExtra(Constants.TAG_SELECT_HASH_TAG, hashTag);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);


        });

        btnBack.setOnClickListener(v -> {
            Timber.i("setDataToView clicked");
            onBackPressed();
        });

        hashTagName.setText("#" + hashTag);

    }

    private void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }


}



