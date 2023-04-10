package hitasoft.serviceteam.livestreamingaddon.external.utils;

import android.content.Context;
import android.provider.ContactsContract;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by hitasoft on 12/3/18.
 */

public class Constants {

   /* public static final String SITE_URL = "https://folk.news"; // site main url
    public static final String API_URL = SITE_URL + ":3000/"; // base url for all api
    public static final String LIVE_SOCKET_URL = "https://folk.news:8082/"; // socket for live streaming*/

    /*public static final String SITE_URL = "https://binggbongg.com"; // site main url
    public static final String API_URL = SITE_URL + ":3000/"; // base url for all api
    public static final String LIVE_SOCKET_URL = "https://binggbongg.com:8082/";*/ // socket for live streaming
    /**
     * For publish and subscribe
     */

//    public static final String SITE_URL = "https://appservices.hitasoft.in"; // site main url
//    public static final String API_URL = SITE_URL + ":3001/"; // base url for all api
//    public static final String LIVE_SOCKET_URL = "https://appservices.hitasoft.in:8102/";

//    public final static String BASE_URL = "https://binggbongg.com:3000/";
    public static final String SITE_URL = "https://binggbongg.com"; // site main url
    public static final String API_URL = SITE_URL + ":3001/"; // base url for all api
    public static final String APPRTC_URL = "http://178.128.149.177:8080";
    public static final String LIVE_SOCKET_URL = "https://binggbongg.com:8102/";
    public static final String TAG_RECORDED = "recorded";


    public static final String IMAGE_DIRECTORY = "";
    public static final String GIFT_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/gifts/";
    public static final String IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/accounts/";
    public static final String PRIME_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/slider/";
    public static final String GEMS_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/gems/";
    public static final String CHAT_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/chats/";

    /*Used for Intent and other purpose*/
    public static final String TAG_LANGUAGE_CODE = "language_code";
    public static final String TAG_TRANSLATE_LANGUAGE_CODE = "translate_language_code";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String TAG_DEFAULT_LANGUAGE_CODE = LANGUAGE_ENGLISH;
    public static final String TAG_TITLE = "title";

    public static final String TAG_REPORT = "report";
    public static final String TAG_FROM = "from";
    public static final String TAG_VIDEO = "video";

    // Table column name:
    public static final String TAG_STATUS = "status";
    public static final String TYPE_STICKERS = "stickers";
    public static final String TYPE_GIFTS = "gifts";
    public static final String TAG_ANDROID = "android";
    public static final String TAG_GIFT_ID = "gift_id";
    public static final String TAG_GIFT_ICON = "gift_icon";
    public static final String TAG_GIFT_TITLE = "gift_title";
    public static final String TAG_GEMS_EARNINGS = "gems_earnings";
    public static final String TAG_GEMS_COUNT = "gems_count";
    public static final String TAG_GIFT_STATUS = "_giftStatus";
    public static final String TAG_GIFT = "gift";
    public static final String TAG_VIDEO_ID = "video_id";
    public static final String TAG_TOKEN = "token";
    public static final String TAG_STREAM_BASE_URL = "stream_base_url";
    public static final String NOTIFICATION = "notification";

    public static String TAG_USER_NAME = "user_name";
    public static String TAG_USER_IMAGE = "user_image";
    public static final String TAG_USER_ID = "user_id";
    public static String TAG_RECEIVER_ID = "receiver_id";
    public static String TAG_MESSAGE = "message";
    public static String TAG_TYPE = "type";
    public static String TAG_SUCCESS = "success";
    public static final String TAG_IMAGE = "image";
    public static final String TAG_HISTORY = "history";
    public static final String TAG_TEXT = "text";
    public static final String TAG_VISIBILITY = "visibility";
    public static final String TAG_TRUE = "true";
    public static final String TAG_FALSE = "false";
    public static final String TAG_NAME = "name";
    public static final String TAG_OTHER_PROFILE = "other_profile";
    public static final String TAG_RECENT = "recent";
    public static final int MAX_LIMIT = 20;
    public static final String TAG_USER = "user";
    public static final String TAG_LIVE = "live";
    public static final String TAG_LIVE_STREAMING = "livestreaming";
    public static final String TAG_SUBSCRIBE = "subscribe";


    /* minute * 1000 * seconds * milliseconds */
    public static final int CAMERA_REQUEST_CODE = 111;
    public static final int STREAM_REQUEST_CODE = 114;

    public static boolean isExternalPlay = false;
    public static boolean isInStream = false;

    public static String SHARED_PREFERENCE = "SavedPref";

    public static final String TAG_PARTNER_ID = "partner_id";
    public static final String TAG_PARTNER_NAME = "partner_name";
    public static final String TAG_PARTNER_IMAGE = "partner_image";
    public static final String IS_VIDEO_DELETED = "isVideoDeleted";
    public static final String TAG_VIDEO_TRIM = "videorecord";
    public static final String TAG_MIX_VIDEO = "audiomixvideo";
    public static final String TAG_MOV_AUDIO = "movaudio";
    public static final String TAG_VIDEO_THUMBNAIL = "coverpic";
    public static final String TAG_SINGLE_VIDEO = "singleVideo";


}
