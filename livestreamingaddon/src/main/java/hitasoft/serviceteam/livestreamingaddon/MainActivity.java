package hitasoft.serviceteam.livestreamingaddon;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import hitasoft.serviceteam.livestreamingaddon.broadcaster.liveVideoBroadcaster.*;
import hitasoft.serviceteam.livestreamingaddon.external.helper.NetworkReceiver;
import hitasoft.serviceteam.livestreamingaddon.external.utils.AdminData;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiClient;
import hitasoft.serviceteam.livestreamingaddon.external.utils.ApiInterface;
import hitasoft.serviceteam.livestreamingaddon.external.utils.Constants;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.SubscribeActivity;
import hitasoft.serviceteam.livestreamingaddon.model.AppDefaultResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    /**
     * PLEASE WRITE RTMP BASE URL of the your RTMP SERVER.
     */

//    public static final String RTMP_BASE_URL = "rtmp://media.hitasoft.in/LiveApp/";
    ApiInterface apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getAppDefaultData();
    }

    public void openVideoBroadcaster(View view) {
        Intent i = new Intent(this, PublishActivity.class);
        startActivity(i);
    }

    public void openVideoPlayer(View view) {
        Intent i = new Intent(this, SubscribeActivity.class);
        startActivity(i);
    }

    private void getAppDefaultData() {
        if (NetworkReceiver.isConnected()) {
            Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID);
            call.enqueue(new Callback<AppDefaultResponse>() {
                @Override
                public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                    AppDefaultResponse defaultData = response.body();
                    if (defaultData.getStatus().equals(Constants.TAG_TRUE)) {
                        AdminData.resetData();
                        AdminData.freeGems = defaultData.getFreeGems();
                        AdminData.giftList = defaultData.getGifts();
                        AdminData.giftsDetails = defaultData.getGiftsDetails();
                        AdminData.reportList = defaultData.getReports();
                        /*Add first item as Select all location filter*/
                        /*AdminData.locationList = new ArrayList<>();
                        AdminData.locationList.add(getString(R.string.select_all));
                        AdminData.locationList.addAll(defaultData.getLocations());*/
                        AdminData.membershipList = defaultData.getMembershipPackages();
                        AdminData.filterGems = defaultData.getFilterGems();
                        AdminData.filterOptions = defaultData.getFilterOptions();
                        AdminData.inviteCredits = defaultData.getInviteCredits();
                        AdminData.showAds = defaultData.getShowAds();
                        AdminData.showVideoAd = defaultData.getVideoAds();
//                        AdminData.googleAdsId = defaultData.getGoogleAdsClient();
                        //AdminData.googleAdsId = getString(R.string.banner_ad_id);
                        AdminData.contactEmail = defaultData.getContactEmail();
                        AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                        AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                        AdminData.videoAdsClient = defaultData.getVideoAdsClient();
                        AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                        AdminData.videoCallsGems = defaultData.getVideoCalls();
                        /*AdminData.streamDetails = defaultData.getStreamConnectionInfo();*/


                        AdminData.max_sound_duration = defaultData.getMaxSoundDuration();

                        /*SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                        SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                        SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                        SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());*/
                    }
                }

                @Override
                public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }
}
