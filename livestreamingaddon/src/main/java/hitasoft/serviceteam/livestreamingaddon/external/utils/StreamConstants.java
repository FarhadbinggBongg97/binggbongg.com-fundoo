package hitasoft.serviceteam.livestreamingaddon.external.utils;


public class StreamConstants {

    /*    public static final String STREAM_SOCKET_IO_URL = Constants.SITE_URL + ":5000";*/
    public static final String STREAM_SOCKET_IO_URL = Constants.LIVE_SOCKET_URL;
//    public static final String RTMP_URL = "rtmp://mediaserver.hitasoft.in/LiveApp/";
   // public static final String RTMP_URL = "rtmp://137.184.76.48/LiveApp/";
    public static final String RTMP_URL = "rtmp://mediaserver.hitasoft.in/LiveApp/";

    public static final String TAG_STREAM_BASE_URL = "stream_base_url";

    public static final String TAG_STREAM_NAME = "stream_name";
    public static final String TAG_PUBLISHER_NAME = "posted_name";
    public static final String TAG_STREAM_IMAGE = "stream_image";
    public static final String TAG_PUBLISHER_ID = "publisher_id";
    public static final String TAG_RECORDING = "recording";
    public static final String TAG_LENS_FACING = "lens_facing";
    public static final String TAG_DURATION = "duration";
    public static String[] LIKE_COLOR = {"#05AC90", "#AC5b05", "#AC1905",
            "#AC0577", "#8305AC", "#0563aC", "#7BAC05"};
    public static final String TAG_TITLE = "title";
    public static final String TAG_NAME = "name";
    public static final String TAG_VIDEOS_COUNT = "videos_count";
    public static final String TAG_STREAM_DATA = "stream_data";
    public static final String TAG_RECENT = "recent";
    public static final String TAG_SUBSCRIBE = "subscribe";
    public static final String TAG_FROM = "from";
    public static final String TAG_PRIVATE = "private";
    public static final String TAG_STREAM_TOKEN = "stream_token";
    public static final String TAG_LIVE = "live";
    public static final String TAG_RECORDED = "recorded";


    public static final String TAG_TOKEN = "token";
    public static final String TAG_USER_ID = "user_id";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_USER_IMAGE = "user_image";


    public static final int MAX_LIMIT = 20;
    public static final long FADE_DURATION = 1000;
    public static final int DELETE_REQUEST_CODE = 100;
    public static final int STREAM_REQUEST_CODE = 101;

    /*Socket Key*/
    public static final String TAG_PUBLISH_STREAM = "_publishStream";
    public static final String TAG_STOP_STREAM = "stopstream";
    public static final String TAG_STREAM_END = "streamend";
    public static final String TAG_SUBSCRIBE_STREAM = "_subscribeStream";
    public static final String TAG_UNSUBSCRIBE_STREAM = "_unsubscribeStream";
    public static final String TAG_LIKED = "liked";
    public static final String TAG_TIME = "time";
    public static final String TAG_LIKE_COLOR = "like_color";
    public static final String TAG_STREAM_JOINED = "joined";
    public static final String TAG_SEND_MESSAGE = "_sendMsg";
    public static final String TAG_MESSAGE_RECEIVED = "_msgReceived";
    public static final String TAG_SUBSCRIBER_LEFT = "_subscriberLeft";
    public static final String TAG_STREAM_INFO = "_streamInfo";
    public static final String TAG_STREAM_BLOCKED = "_streamBlocked";
    public static final String TAG_GET_STREAM_INFO = "_getstreamInfo";

    public static final String TAG_GIFT_FROM = "gift_from";
    public static final String TAG_GIFT_TO = "gift_to";
    public static final String TAG_SEND_GIFT = "_sendGift";

    public static final String TAG_LINK_URL="link_url";
}
