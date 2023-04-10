package com.app.binggbongg.helper;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.binggbongg.fundoo.HistoryActivity;
import com.app.binggbongg.fundoo.NotificationActivity;
import com.app.binggbongg.fundoo.SingleVideoActivity;
import com.app.binggbongg.fundoo.StreamsListActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.app.binggbongg.R;
import com.app.binggbongg.apprtc.util.AppRTCUtils;
import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.ChatActivity;
import com.app.binggbongg.fundoo.MainActivity;
import com.app.binggbongg.fundoo.VideoCallActivity;
import com.app.binggbongg.fundoo.profile.OthersProfileActivity;
import com.app.binggbongg.model.AddDeviceRequest;
import com.app.binggbongg.model.ChatResponse;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.app.binggbongg.utils.DeviceTokenPref;
import com.app.binggbongg.utils.Logging;
import com.app.binggbongg.utils.SharedPref;
import com.app.binggbongg.utils.Util;

import java.util.Map;
import java.util.regex.Pattern;

import hitasoft.serviceteam.livestreamingaddon.LiveStreamActivity;
import hitasoft.serviceteam.livestreamingaddon.external.utils.StreamConstants;
import hitasoft.serviceteam.livestreamingaddon.liveVideoPlayer.SubscribeActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    ApiInterface apiInterface;
    DBHelper dbHelper;
    Context mContext;
    AppUtils appUtils;

    @Override
    public void onNewToken(String deviceToken) {
        super.onNewToken(deviceToken);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Logging.i(TAG, "onNewToken: " + deviceToken);
        DeviceTokenPref.getInstance(this).saveDeviceToken(deviceToken);
        if (SharedPref.getBoolean(SharedPref.IS_LOGGED, false)) {
            GetSet.setLogged(true);
            addDeviceId();
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Logging.i(TAG, "onMessageReceived getData: " + remoteMessage.getData());
        Logging.i(TAG, "onMessageReceived getNotification: " + remoteMessage.getNotification());
        Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("onMessageReceived: received payload: %s", remoteMessage.getData());
        dbHelper = DBHelper.getInstance(this);
        mContext = this;
        appUtils = new AppUtils(this);
        Map<String, String> map = remoteMessage.getData();
        String scope = map.get(Constants.TAG_SCOPE);
        if (scope != null) {
            switch (scope) {
                case Constants.TAG_TEXT_CHAT:
                    setChatNotification(map);
                    break;
                case Constants.TAG_VIDEO_CALL:
                    onCalReceived(map, scope);
                    break;
                case Constants.TAG_ENDED:
                    onCalReceived(map, scope);
                    break;
                case Constants.TAG_FOLLOW:
                    if (GetSet.getFollowNotification().equals(Constants.TAG_TRUE))
                        setFollowNotification(map);
                    break;
                case Constants.TAG_ADMIN:
                    setAdminNotification(map);
                    break;
                case Constants.TAG_INTEREST:
                    if (GetSet.getInterestNotification())
                        setInterestNotification(map);
                    break;
                case Constants.TAG_MATCH:
                    if (GetSet.getInterestNotification())
                        setInterestNotification(map);
                    break;
                case Constants.TAG_FOLLOWER_ON_LIVE:
                    setNewPostNotification(map);
                    break;
                case Constants.TAG_STREAM_INVITATION:
                    setNewLiveNotification(map);
                    break;
                case Constants.TAG_LIKE:
                case Constants.TAG_GIFT:
                case Constants.TAG_COMMENT:
                    setFollowNotification(map);
                    break;
            }
        }
    }

    private void onCalReceived(Map<String, String> map, String scope) {
        Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("onCallReceived");
        String userId = map.get(Constants.TAG_USER_ID);
        String userName = map.get(Constants.TAG_USER_NAME);
        String userImage = map.get(Constants.TAG_USER_IMAGE);
        String receiverId = map.get(Constants.TAG_RECEIVER_ID);
        String chatType = map.get(Constants.TAG_CHAT_TYPE);
        String utcTime = map.get(Constants.TAG_CREATED_AT);
        String callType = map.get(Constants.TAG_CALL_TYPE);
        String roomId = map.get(Constants.TAG_ROOM_ID);
        String platform = map.get(Constants.TAG_PLATFORM);

        long diffInMs = System.currentTimeMillis() - AppUtils.getTimeFromUTC(mContext, utcTime);
        long diffSeconds = diffInMs / 1000;
        Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("Call elapsed time: %d (%d, %d)",
                diffSeconds, System.currentTimeMillis(), AppUtils.getTimeFromUTC(mContext, utcTime));

        if (diffSeconds < 30 && scope.equalsIgnoreCase(Constants.TAG_VIDEO_CALL)) {
      /*      if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){

                if(ActivityCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
                    Log.e(TAG, "onCalReceived: ::::::::if" );
                    AlertDialog dialog=new AlertDialog.Builder(getApplicationContext()).setMessage("hi")
                            .show();
                }
            }*/
            TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();
            Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("onCallReceived: Check call elapse: GOOD");
            if (!Constants.isInRandomCall && !Constants.isInStream && !VideoCallActivity.isInCall && isPhoneCallOn == 0) {
                VideoCallActivity.isInCall = true;
                Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("onCallReceived: Preparing intent");
                AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                Intent intent = appRTCUtils.connectToRoom(userId, Constants.TAG_RECEIVE, chatType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_ROOM_ID, roomId);
                intent.putExtra(Constants.TAG_PLATFORM, platform);
                intent.putExtra(Constants.TAG_USER_ID, userId);
                intent.putExtra(Constants.TAG_USER_NAME, userName);
                intent.putExtra(Constants.TAG_USER_IMAGE, userImage);
                intent.putExtra(Constants.TAG_RECEIVER_ID, receiverId);
                //   PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                startActivity(intent);
                Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("onCallReceived: Sending intent..");
            } else {
                IllegalStateException e = new IllegalStateException("Cannot start call activity");
                Timber.tag(VideoCallActivity.LOG_TAG_CALL).e(e, "Failed to process call");
            }
        } else if (scope.equalsIgnoreCase(Constants.TAG_ENDED)) {
            Timber.i("App.getCurrentActivity %s", App.getCurrentActivity());

            /*if (App.getCurrentActivity() instanceof VideoCallActivity) {
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();
                if (activity.isSender) {
                    if (activity.receiverId.equals(userId)) {
                        activity.finish();
                    }
                } else {
                    if (activity.userId.equals(userId)) {
                        activity.finish();
                    }
                }
            }*/


            if (App.getCurrentActivity() instanceof VideoCallActivity) {
                Timber.i("firebaseonCalReceived: %s", scope);
                Timber.i("firebaseonCalReceivedi: %s", userId);
                String iosUserId = userId.replaceAll("Optional", "");
                String ios = iosUserId.replace("(\")", "");
                Timber.i("firebaseonCalReceivedif: %s", iosUserId);
                Timber.i("firebaseonCalReceivediff: %s", ios);
                VideoCallActivity activity = (VideoCallActivity) App.getCurrentActivity();

                if (activity.isSender) {
                    if (activity.receiverId.equals(userId)) {
                        //activity.finishActivity();
                        activity.finish();
                    }
                } else {
                    if (activity.userId.equals(userId)) {
                        Timber.i("firebaseonCalReceivedifff: %s", userId);
                        //activity.finishActivity();
                        activity.finish();
                    }
                }
               /* if (userId.contains("Optional")){
                    Timber.i(TAG, "firebaseonCalReceivediffoptio: "+userId);
                    activity.finishActivity();
                    activity.finish();
                }*/

            } else if (App.getCurrentActivity() instanceof ChatActivity) {
                /*ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                activity.finish();*/
            } else if (App.getCurrentActivity() instanceof OthersProfileActivity) {
                /*OthersProfileActivity activity = (OthersProfileActivity) App.getCurrentActivity();
                activity.finish();*/
            } else if (App.getCurrentActivity() instanceof MainActivity) {

            } else {
                //App.getCurrentActivity().finish();
            }


        } else {
            Timber.tag(VideoCallActivity.LOG_TAG_CALL).d("onCallReceived: Call expired!");
        }
    }

    private void setChatNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (Constants.RANDOU_ENABLED) {
            if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
                /*intent = new Intent(getApplicationContext(), com.hitasoft.app.livza.randou.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_PROFILE);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
                intent.putExtra(Constants.TAG_PARTNER_NAME, data.get(Constants.TAG_USER_NAME));
                intent.putExtra(Constants.TAG_PARTNER_IMAGE, data.get(Constants.TAG_USER_IMAGE));*/
            }
        } else {
            if (!Constants.isInVideoCall) {
                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_PROFILE);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
                intent.putExtra(Constants.TAG_PARTNER_NAME, data.get(Constants.TAG_USER_NAME));
                intent.putExtra(Constants.TAG_PARTNER_IMAGE, data.get(Constants.TAG_USER_IMAGE));
                App.isAppOpened = true;

            }
        }
        String messageType = "";
        messageType = data.get(Constants.TAG_MSG_TYPE);
        if (GetSet.getChatNotification().equals(Constants.TAG_TRUE)) {
            if (App.getCurrentActivity() instanceof ChatActivity) {
                ChatActivity activity = (ChatActivity) App.getCurrentActivity();
                if (activity.getPartnerId() != null && !activity.getPartnerId().equals(data.get(Constants.TAG_USER_ID))) {
                    if (messageType.equals(Constants.TAG_TEXT)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), AppUtils.decryptMessage(data.get(Constants.TAG_MESSAGE)), intent);
                    }  else if (messageType.equals(Constants.TAG_AUDIO)) {
                        Log.i(TAG, "setChatNotificationau: " + messageType);
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.audio), intent);
                    } else if (!messageType.equals(Constants.TAG_MISSED)) {
                        sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.image), intent);
                    }
                }
            } else {
                if (messageType.equals(Constants.TAG_TEXT)) {
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), AppUtils.decryptMessage(data.get(Constants.TAG_MESSAGE)), intent);
                } else if (messageType.equals(Constants.TAG_AUDIO)) {
                    Log.i(TAG, "setChatNotificationau: " + messageType);
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.audio), intent);
                } else if (!messageType.equals(Constants.TAG_MISSED)) {
                    sendNotification(1, data.get(Constants.TAG_USER_NAME), getString(R.string.image), intent);
                }

            }
        }
    }

    private void setFollowNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (Constants.RANDOU_ENABLED) {
            if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
                /*intent = new Intent(getApplicationContext(), com.hitasoft.app.livza.randou.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_FOLLOW);
                intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));*/
            }
        } else if (data != null) {
            final String scope = Util.getEmptyIfNull(data.get("scope"));
            final Pattern mainLaunchPattern = Pattern.compile("like|gift|comment");

          /*  if(scope.equals("like")){
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.TAG_TYPE,Constants.HIS_LIKES);
                intent.putExtra(Constants.TAG_TITLE, "Like History");
            }else*/ if(scope.equals("gift")){
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.NOTIFICATION, scope);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.TAG_FROM, scope);
            }else{
               /* intent = new Intent(getApplicationContext(), NotificationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_NOTIFICATION_PAGE);
                intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));*/
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_NOTIFICATION_PAGE);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.TAG_FROM, scope);
            }
        } else {
            if (!Constants.isInVideoCall) {
                intent = new Intent(getApplicationContext(), OthersProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_FOLLOW);
                intent.putExtra(Constants.TAG_PARTNER_ID, data.get(Constants.TAG_USER_ID));
            }
        }
        String appName = getString(R.string.app_name);
        if (GetSet.getFollowNotification().equals(Constants.TAG_TRUE)) {
            sendNotification(2, appName, data.get(Constants.TAG_MESSAGE), intent);
        }
    }

    private void setAdminNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (Constants.RANDOU_ENABLED) {
            if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
                /*intent = new Intent(getApplicationContext(), com.hitasoft.app.livza.randou.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_ADMIN);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);*/
            }
        } else {
            if (!Constants.isInVideoCall) {
                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, Constants.TAG_ADMIN);
                intent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
                ChatResponse adminChat = dbHelper.getAdminChat(GetSet.getUserId());
                intent.putExtra(Constants.TAG_PARTNER_NAME, adminChat.getUserName().equals(null)?"Bingg Bongg Team":adminChat.getUserName());
//                Log.d(TAG, "setAdminNotification: " + adminChat.getUserName());
                App.isAppOpened = true;

            }
        }
        if (App.getCurrentActivity() instanceof ChatActivity) {
            ChatActivity activity = (ChatActivity) App.getCurrentActivity();
            if (!activity.isAdminChat) {
                String appName = getString(R.string.app_name);
                sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
            }
        } else {
            String appName = getString(R.string.app_name);
            sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
        }

    }

    private void setInterestNotification(Map<String, String> data) {
        Intent intent = new Intent();
        if (Constants.RANDOU_ENABLED) {
            if (!Constants.isInRandomCall && !Constants.isInVideoCall) {
                intent = new Intent(getApplicationContext(), com.app.binggbongg.fundoo.MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
                intent.putExtra(Constants.NOTIFICATION, data.get(Constants.TAG_SCOPE));
            }
        }
        String appName = getString(R.string.app_name);
        sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
    }

    private void setNewLiveNotification(Map<String, String> data) {
        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION, Constants.TAG_LIVE);
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_LIVE);
        intent.putExtra(StreamConstants.TAG_STREAM_NAME, data.get(StreamConstants.TAG_STREAM_NAME));

      /*  intent = new Intent(getApplicationContext(), LiveStreamActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
        intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
        intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
        intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
        intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());*/
        String appName = getString(R.string.app_name);
        sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
    }


    private void setNewPostNotification(Map<String, String> data) {
        Intent intent = new Intent();
        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.TAG_FROM_FOREGROUND, App.isOnAppForegrounded());
        String appName = getString(R.string.app_name);
        sendNotification(0, appName, data.get(Constants.TAG_MESSAGE), intent);
    }

    private void sendNotification(int notifyId, String title, String message, Intent intent) {
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();
        String CHANNEL_ID = getString(R.string.notification_channel_id);
        CharSequence channelName = getString(R.string.app_name);
       /* PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/

        PendingIntent resultPendingIntent;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            resultPendingIntent=PendingIntent.getActivity(getApplicationContext(), uniqueInt, intent, /*PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_ONE_SHOT |*/ PendingIntent.FLAG_IMMUTABLE);
        }else{
            resultPendingIntent=PendingIntent.getActivity(getApplicationContext(), uniqueInt, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);

            if (mNotifyManager != null) {
                mNotifyManager.createNotificationChannel(notificationChannel);
            }

            Log.d(TAG, "sendNotification: check1");
        } /*else {*/
            Log.d(TAG, "sendNotification: check2");
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

            mBuilder.setChannelId(CHANNEL_ID)
                    .setContentText(message)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.drawable.livza_sym)
                    .setFullScreenIntent(resultPendingIntent, true)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setAutoCancel(true);
            mNotifyManager.notify(getString(R.string.app_name), notifyId, mBuilder.build());

        }

    private void addDeviceId() {
        final String token = DeviceTokenPref.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        AddDeviceRequest request = new AddDeviceRequest();
        request.setUserId(GetSet.getUserId());
        request.setDeviceToken(token);
        request.setDeviceType(Constants.TAG_DEVICE_TYPE);
        request.setDeviceId(deviceId);
        request.setDeviceModel(AppUtils.getDeviceName());

        Call<Map<String, String>> call3 = apiInterface.pushSignIn(request);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();

            }
        });

    }
}
