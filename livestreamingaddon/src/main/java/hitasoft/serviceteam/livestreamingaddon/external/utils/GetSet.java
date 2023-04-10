package hitasoft.serviceteam.livestreamingaddon.external.utils;

public class GetSet {

    private static String userId = null;
    private static String token = null;
    private static String userName = null;
    private static String imageUrl = null;
    private static String applanguage = null;
    private static String location=null;
    private static String uniqueuserid=null;
    private static String userbadge=null;
    private static String phone_no_privacy=null;
    private static String stream_base_url= null;


    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        GetSet.userId = userId;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        GetSet.token = token;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        GetSet.userName = userName;
    }

    public static String getImageUrl() {
        return imageUrl;
    }

    public static void setImageUrl(String imageUrl) {
        GetSet.imageUrl = imageUrl;
    }

    public static String getStream_base_url() {
        return stream_base_url;
    }

    public static void setStream_base_url(String stream_base_url) {
        GetSet.stream_base_url = stream_base_url;
    }

    public static String getApplanguage() {
        return applanguage;
    }

    public static void setApplanguage(String applanguage) {
        GetSet.applanguage = applanguage;
    }

    public static String getLocation() {
        return location;
    }

    public static void setLocation(String location) {
        GetSet.location = location;
    }

    public static String getUniqueuserid() {
        return uniqueuserid;
    }

    public static void setUniqueuserid(String uniqueuserid) {
        GetSet.uniqueuserid = uniqueuserid;
    }

    public static String getUserbadge() {
        return userbadge;
    }

    public static void setUserbadge(String userbadge) {
        GetSet.userbadge = userbadge;
    }

    public static String getPhone_no_privacy() {
        return phone_no_privacy;
    }

    public static void setPhone_no_privacy(String phone_no_privacy) {
        GetSet.phone_no_privacy = phone_no_privacy;
    }
}
