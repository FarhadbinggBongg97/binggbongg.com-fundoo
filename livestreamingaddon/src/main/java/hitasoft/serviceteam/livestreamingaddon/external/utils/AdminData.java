package hitasoft.serviceteam.livestreamingaddon.external.utils;

import java.util.ArrayList;
import java.util.List;

import hitasoft.serviceteam.livestreamingaddon.model.AppDefaultResponse;
import hitasoft.serviceteam.livestreamingaddon.model.FilterGems;
import hitasoft.serviceteam.livestreamingaddon.model.FilterOptions;
import hitasoft.serviceteam.livestreamingaddon.model.Gift;
import hitasoft.serviceteam.livestreamingaddon.model.GiftsDetails;
import hitasoft.serviceteam.livestreamingaddon.model.MembershipPackages;
import hitasoft.serviceteam.livestreamingaddon.model.Report;

public class AdminData {
    public static final String TAG = AdminData.class.getSimpleName();
    public static Long freeGems = 0L;
    public static List<Report> reportList = new ArrayList<>();
    public static GiftsDetails giftsDetails = new GiftsDetails();
    public static List<Gift> giftList = new ArrayList<>();
    public static List<String> locationList = new ArrayList<>();
    public static List<MembershipPackages> membershipList = new ArrayList<>();
    public static String contactEmail = "";
    public static FilterGems filterGems = new FilterGems();
    public static FilterOptions filterOptions = new FilterOptions();
    public static String showAds = "1";
    public static String max_sound_duration = "60";
    public static Long inviteCredits;
    public static String googleAdsId = "";
    public static String welcomeMessage = "";
    public static String showVideoAd = "1";
    public static String showMoneyConversion = "0";
    public static String videoAdsClient = "";
    public static long videoAdsDuration = 5;
    public static long videoCallsGems = 0;
    public static AppDefaultResponse.StreamConnectionInfo streamDetails = null;




    public static void resetData() {
        freeGems = 0L;
        giftList = new ArrayList<>();
        giftsDetails = new GiftsDetails();
        reportList = new ArrayList<>();
        /*Add first item as Select all location filter*/
        locationList = new ArrayList<>();
        membershipList = new ArrayList<>();
        filterGems = new FilterGems();
        filterOptions = new FilterOptions();
        showAds = "1";
        showVideoAd = "1";
        inviteCredits = 0L;
        googleAdsId = null;
        showMoneyConversion = "0";
        videoAdsClient = "";
        videoAdsDuration = 5;
        videoCallsGems = 0;
        streamDetails = null;
    }

    public static boolean isAdEnabled() {
        boolean isAdEnabled = true;
        if (showAds.equals("1")) {
           /* if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                isAdEnabled = false;
            }*/
        } else {
            isAdEnabled = false;
        }
        return isAdEnabled;
    }
}

