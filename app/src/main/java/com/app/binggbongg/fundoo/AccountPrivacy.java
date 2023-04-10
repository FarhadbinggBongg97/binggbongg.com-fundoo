package com.app.binggbongg.fundoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.fundoo.Video.DynamicPrivacyActivity;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.model.VideoPrivacyModel;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class AccountPrivacy extends BaseFragmentActivity {


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

    @BindView(R.id.resultPostCommand)
    TextView resultPostCommand;

    @BindView(R.id.resultPostMessage)
    TextView resultPostMessage;

    @BindView(R.id.BlackLay)
    LinearLayout BlackLay;

    @BindView(R.id.postCommandLay)
    LinearLayout postCommandLay;

    @BindView(R.id.messageLay)
    LinearLayout messageLay;

    @BindView(R.id.mainLay)
    LinearLayout mainLay;

    @BindView(R.id.mProgressbar)
    ProgressBar mProgressbar;


    private ApiInterface apiInterface;

    private final List<VideoPrivacyModel> postCommandList = new ArrayList<>();
    private final List<VideoPrivacyModel> postMessageList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_privacy);

        ButterKnife.bind(this);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        txtTitle.setText(getString(R.string.privacy));

        setSettings();


        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }


        BlackLay.setOnClickListener(v -> startActivity(new Intent(AccountPrivacy.this, BlockedUsersActivity.class)));

        postCommandLay.setOnClickListener(v -> {
            Intent intent = new Intent(this, DynamicPrivacyActivity.class);
            intent.putExtra("data", (Serializable) postCommandList);
            intent.putExtra(Constants.TAG_TITLE, getResources().getString(R.string.who_can_post_comments));
            startActivityForResult(intent, Constants.SETTING_POST_COMMAND);
        });


        messageLay.setOnClickListener(v -> {
            Intent intent = new Intent(this, DynamicPrivacyActivity.class);
            intent.putExtra("data", (Serializable) postMessageList);
            intent.putExtra(Constants.TAG_TITLE, getResources().getString(R.string.who_can_send_message));
            startActivityForResult(intent, Constants.SETTING_POST_MESSAGE);
        });

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

    }


    private void updatePrivateChat(String type, String getData) {


        ProfileRequest request = new ProfileRequest();
        if (type.equals("postCommand"))
            request.setComment_privacy(getData);
        else if (type.equals("postMessage"))
            request.setMessage_privacy(getData);
        updateSettings(type, request);
    }

    private void updateSettings(String type, ProfileRequest request) {
        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());

                Timber.d("getProfile request: %s", new Gson().toJson(request));

                Call<ProfileResponse> call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        ProfileResponse profile = response.body();

                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            Timber.d("onResponse: %s", new Gson().toJson(profile));

                            if (type.equalsIgnoreCase("postCommand")) {
                                GetSet.setPostCommand(profile.getCommentPrivacy());
                                SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());

                                if (Constants.TAG_FRIENDS.equals(profile.getCommentPrivacy()))
                                    resultPostCommand.setText(getResources().getString(R.string.settings_Fans));
                                else
                                    resultPostCommand.setText(profile.getCommentPrivacy());

                            } else if (type.equalsIgnoreCase("postMessage")) {


                                GetSet.setSendMessage(profile.getMessagePrivacy());
                                SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());

                                if (Constants.TAG_FRIENDS.equals(profile.getMessagePrivacy()))
                                    resultPostMessage.setText(getResources().getString(R.string.settings_Fans));
                                else
                                    resultPostMessage.setText(profile.getMessagePrivacy());
                            }

                            mainLay.setAlpha(1.0f);
                            mProgressbar.setAlpha(0f);

                            postCommandLay.setEnabled(true);
                            BlackLay.setEnabled(true);
                            messageLay.setEnabled(true);
                            btnBack.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        call.cancel();
                    }
                });
            }
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void setSettings() {

        switch (GetSet.getPostCommand()) {
            case Constants.TAG_EVERYONE:
                postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), true));
                postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                break;
            case Constants.TAG_FRIENDS:
                postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), true));
                postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                break;
            case Constants.TAG_OFF:
                postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), true));
                break;
        }


        switch (GetSet.getSendMessage()) {
            case Constants.TAG_EVERYONE:
                postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), true));
                postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                break;
            case Constants.TAG_FRIENDS:
                postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), true));
                postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                break;
            case Constants.TAG_OFF:
                postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), true));
                break;
        }


        if (GetSet.getPostCommand().equals(Constants.TAG_FRIENDS))
            resultPostCommand.setText(getResources().getString(R.string.settings_Fans));
        else
            resultPostCommand.setText(GetSet.getPostCommand());

        if (GetSet.getSendMessage().equals(Constants.TAG_FRIENDS))
            resultPostMessage.setText(getResources().getString(R.string.settings_Fans));
        else
            resultPostMessage.setText(GetSet.getSendMessage());


        mainLay.setAlpha(1.0f);
        mProgressbar.setAlpha(0f);

        Timber.i("postCommandList %s", App.getGsonPrettyInstance().toJson(postCommandList));
        Timber.i("postSendMessageList %s", App.getGsonPrettyInstance().toJson(postMessageList));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Timber.d("requestCode: %s", requestCode);
        Timber.d("resultcode: %s", resultCode);

        if (requestCode == Constants.SETTING_POST_COMMAND) {

            if (resultCode != RESULT_CANCELED) {

                postCommandLay.setEnabled(false);
                BlackLay.setEnabled(false);
                messageLay.setEnabled(false);
                btnBack.setEnabled(false);

                mainLay.setAlpha(0.5f);
                mProgressbar.setAlpha(1.0f);

                postCommandList.clear();
                String getData = Objects.requireNonNull(data).getStringExtra("privacyName");

                if (getData.equals(getResources().getString(R.string.settings_Fans)))
                    updatePrivateChat("postCommand", Constants.TAG_FRIENDS);
                else
                    updatePrivateChat("postCommand", getData);

                if (getData.equals(getResources().getString(R.string.settings_everyone))) {
                    postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), true));
                    postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                    postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                } else if (getData.equals(getResources().getString(R.string.settings_Fans))) {
                    postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                    postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), true));
                    postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                } else if (getData.equals(getResources().getString(R.string.settings_Off))) {
                    postCommandList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                    postCommandList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                    postCommandList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), true));
                }

            }

        } else if (requestCode == Constants.SETTING_POST_MESSAGE) {

            if (resultCode != RESULT_CANCELED) {
                postCommandLay.setEnabled(false);
                BlackLay.setEnabled(false);
                messageLay.setEnabled(false);
                btnBack.setEnabled(false);

                mainLay.setAlpha(0.5f);
                mProgressbar.setAlpha(1.0f);

                String getData = Objects.requireNonNull(data).getStringExtra("privacyName");

                if (getData.equals(getResources().getString(R.string.settings_Fans)))
                    updatePrivateChat("postMessage", Constants.TAG_FRIENDS);
                else
                    updatePrivateChat("postMessage", getData);

                postMessageList.clear();
                if (getData.equals(getResources().getString(R.string.settings_everyone))) {
                    postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), true));
                    postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                    postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                } else if (getData.equals(getResources().getString(R.string.settings_Fans))) {
                    postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                    postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), true));
                    postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), false));
                } else if (getData.equals(getResources().getString(R.string.settings_Off))) {
                    postMessageList.add(0, new VideoPrivacyModel(getResources().getString(R.string.settings_everyone), false));
                    postMessageList.add(1, new VideoPrivacyModel(getResources().getString(R.string.settings_Fans), false));
                    postMessageList.add(2, new VideoPrivacyModel(getResources().getString(R.string.settings_Off), true));
                }
            }
        }
    }
}