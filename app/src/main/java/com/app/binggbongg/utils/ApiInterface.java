package com.app.binggbongg.utils;

import com.app.binggbongg.model.AddDeviceRequest;
import com.app.binggbongg.model.AdminMessageResponse;
import com.app.binggbongg.model.AppDefaultResponse;
import com.app.binggbongg.model.CommonResponse;
import com.app.binggbongg.model.ContestCategory;
import com.app.binggbongg.model.ConvertGiftRequest;
import com.app.binggbongg.model.ConvertGiftResponse;
import com.app.binggbongg.model.CountryResponse;
import com.app.binggbongg.model.DiscoverSoundResponse;
import com.app.binggbongg.model.ExploreStreamResponse;
import com.app.binggbongg.model.FavSoundResponse;
import com.app.binggbongg.model.FavVideoResponse;
import com.app.binggbongg.model.FilterDetailsModel;
import com.app.binggbongg.model.FollowRequest;
import com.app.binggbongg.model.FollowersResponse;
import com.app.binggbongg.model.FollowingHomeResponse;
import com.app.binggbongg.model.GemsPurchaseRequest;
import com.app.binggbongg.model.GemsPurchaseResponse;
import com.app.binggbongg.model.GemsStoreResponse;
import com.app.binggbongg.model.GetCity;
import com.app.binggbongg.model.GetCountry;
import com.app.binggbongg.model.GetState;
import com.app.binggbongg.model.GetVideosResponse;
import com.app.binggbongg.model.HashTagRequest;
import com.app.binggbongg.model.HashTagResponse;
import com.app.binggbongg.model.HelpResponse;
import com.app.binggbongg.model.HistoryModel;
import com.app.binggbongg.model.HistoryRequest;
import com.app.binggbongg.model.HomeResponse;
import com.app.binggbongg.model.InterestResponse;
import com.app.binggbongg.model.LiveStreamRequest;
import com.app.binggbongg.model.LiveStreamResponse;
import com.app.binggbongg.model.MutualListResponse;
import com.app.binggbongg.model.NotificationResponse;
import com.app.binggbongg.model.PostHashTagRes;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.model.RecentHistoryResponse;
import com.app.binggbongg.model.ReferFriendResponse;
import com.app.binggbongg.model.RenewalRequest;
import com.app.binggbongg.model.ReportRequest;
import com.app.binggbongg.model.ReportResponse;
import com.app.binggbongg.model.Request.DiscoverSoundRequest;
import com.app.binggbongg.model.Request.GetVideosRequest;
import com.app.binggbongg.model.Request.HomeRequest;
import com.app.binggbongg.model.Request.InterestRequest;
import com.app.binggbongg.model.ScrollVideoResponse;
import com.app.binggbongg.model.SearchHashTagRes;
import com.app.binggbongg.model.SearchMainPageResponse;
import com.app.binggbongg.model.SearchSoundRes;
import com.app.binggbongg.model.SearchUserRes;
import com.app.binggbongg.model.SearchUserResponse;
import com.app.binggbongg.model.SearchVideoResponse;
import com.app.binggbongg.model.SignInRequest;
import com.app.binggbongg.model.SignInResponse;
import com.app.binggbongg.model.SingleVideoResponse;
import com.app.binggbongg.model.SocialMediaLinks;
import com.app.binggbongg.model.StartStreamResponse;
import com.app.binggbongg.model.SubscriptionRequest;
import com.app.binggbongg.model.SubscriptionResponse;
import com.app.binggbongg.model.TermsResponse;
import com.app.binggbongg.model.TrendingStreamResponse;
import com.app.binggbongg.model.UserList;
import com.app.binggbongg.model.VideoCommentResponse;
import com.app.binggbongg.model.VoteDataModel;
import com.app.binggbongg.model.VoteMessage;

import java.util.HashMap;
import java.util.Map;

import hitasoft.serviceteam.livestreamingaddon.external.model.StreamDetails;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("activities/appdefaults/{platform}")
    Call<AppDefaultResponse> getAppDefaultData(@Path("platform") String platform);

    @POST("giftconversions/gifttomoneyconversion")
    Call<ConvertGiftResponse> convertToMoney(@Body ConvertGiftRequest request);

    @POST("accounts/signin")
    Call<SignInResponse> callSignIn(@Body SignInRequest request);

    @POST("accounts/signup")
    Call<SignInResponse> callSignUp(@Body SignInRequest request);

    @Multipart
    @POST("accounts/uploadprofile")
    Call<Map<String, String>> uploadProfileImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("accounts/upmychat")
    Call<Map<String, String>> uploadChatImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("accounts/upmychat")
    Call<Map<String, String>> uploadAudio(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @POST("devices/register")
    Call<Map<String, String>> pushSignIn(@Body AddDeviceRequest request);

    @DELETE("devices/unregister/{device_id}")
    Call<HashMap<String, String>> pushSignOut(@Path("device_id") String deviceId);

    @POST("accounts/profile")
    Call<ProfileResponse> getProfile(@Body ProfileRequest request);

    @GET("activities/followerslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFollowers(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @GET("activities/followingslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFollowings(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @POST("activities/follow")
    Call<Map<String, String>> followUser(@Body FollowRequest followRequest);

    @POST("activities/mutualfollowers")
    Call<MutualListResponse> getMutualList(@Body LiveStreamRequest request);

    @GET("activities/blockeduserslist/{user_id}/{offset}/{limit}")
    Call<SearchUserResponse> getBlockedList(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @FormUrlEncoded
    @POST("activities/blockuser")
    Call<HashMap<String, String>> blockUser(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("activities/unlockinterest")
    Call<HashMap<String, String>> unlockUser(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("accounts/searchuser")
    Call<SearchUserResponse> searchByUserName(@FieldMap HashMap<String, String> requestMap);

    @GET("helps/loginterms")
    Call<TermsResponse> getTerms();

    @GET("helps/allterms")
    Call<HelpResponse> getHelps();

    @GET("accounts/isActive/{user_id}")
    Call<HashMap<String, String>> isActive(@Path("user_id") String userId);

    @GET("chats/recentvideochats/{user_id}/{offset}/{limit}")
    Call<RecentHistoryResponse> getRecentHistory(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @POST("activities/reportuser")
    Call<ReportResponse> reportUser(@Body ReportRequest request);

    @Multipart
    @POST("activities/uploadreport")
    Call<CommonResponse> reportScreenUpload(@Part MultipartBody.Part image, @Part("report_id") RequestBody reportId);

    @Multipart
    @POST("accounts/upmyvideo")
    Call<CommonResponse> chatScreenUpload(@Part MultipartBody.Part image, @Part("user_id") RequestBody userId, @Part("partner_id") RequestBody partnerId);

    @GET("activities/gemstore/{user_id}/{platform}/{offset}/{limit}")
    Call<GemsStoreResponse> getGemsList(@Path("user_id") String userId, @Path("platform") String platform, @Path("offset") int offset, @Path("limit") int limit);

    @POST("payments/subscription")
    Call<SubscriptionResponse> paySubscription(@Body SubscriptionRequest request);

    @POST("payments/purchasegems")
    Call<GemsPurchaseResponse> buyGems(@Body GemsPurchaseRequest request);

        @POST("activities/gifttogems")
    Call<ConvertGiftResponse> convertGifts(@Body ConvertGiftRequest request);

    @GET("accounts/invitecredits/{referal_code}")
    Call<ReferFriendResponse> updateReferal(@Path("referal_code") String referalCode);

    @GET("accounts/rewardvideo/{user_id}")
    Call<Map<String, String>> updateVideoGems(@Path("user_id") String userId);

    @GET("activities/adminmessages/{user_id}/{platform}/{update_from}/{timestamp}")
    Call<AdminMessageResponse> getAdminMessages(@Path("user_id") String userId, @Path("platform") String platform, @Path("update_from") String updateFrom, @Path("timestamp") long currentTimeMillis);

    @GET("chats/clearvideochats/{user_id}")
    Call<HashMap<String, String>> clearRecentHistory(@Path("user_id") String userId);

    @GET("accounts/chargecalls/{user_id}")
    Call<HashMap<String, String>> chargeCalls(@Path("user_id") String userId);

    @POST("payments/renewalsubscription")
    Call<HashMap<String, String>> verifyPayment(@Body RenewalRequest renewalRequest);

    @POST("livestreams")
    Call<LiveStreamResponse> getCurrentStreams(@Body LiveStreamRequest request);

    @GET("activities/watchhistory/{user_id}/{offset}/{limit}")
    Call<LiveStreamResponse> getRecentWatched(@Path("user_id") String userId, @Path("limit") String limit, @Path("offset") String offset);

    @FormUrlEncoded
    @POST("accounts/showall")
    Call<UserList> getUsers(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/streamdetails")
    Call<StreamDetails> getStreamDetails(@Field(Constants.TAG_USER_ID) String userId, @Field(Constants.TAG_NAME) String streamName);

    @GET("livestreams/createstream/{publisher_id}")
    Call<HashMap<String, String>> getStreamName(@Path("publisher_id") String publisherId);

    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<StartStreamResponse> startStream(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<Map<String, String>> stopStream(@FieldMap Map<String, String> params);

    @Multipart
    @POST("livestreams/uploadstream")
    Call<Map<String, String>> uploadStreamImage(@Part MultipartBody.Part image,
                                                @Part("publisher_id") RequestBody publisherId,
                                                @Part("name") RequestBody streamName);


    @Multipart
    @POST("activities/albumimage")
    Call<Map<String, String>> uploadAlbumImage(@Part MultipartBody.Part image,
                                               @Part("user_id") RequestBody userId,
                                               @Part("sound_id") RequestBody soundId);


    @Multipart
    @POST("activities/uploadsound")
    Call<Map<String, String>> uploadAudio(@Part MultipartBody.Part file,
                                          @Part("user_id") RequestBody userId,
                                          @Part("token") RequestBody token,
                                          @Part("title") RequestBody title,
                                          @Part("duration") RequestBody duration);

    @Multipart
    @POST("livestreams/uploadstream")
    Call<Map<String, String>> uploadStream(@Part MultipartBody.Part file,
                                           @Part("video_id") RequestBody videoId);

    @Multipart
    @POST("livestreams/postvideo")
    Call<Map<String, String>> postVideo(@Part MultipartBody.Part file,
                                        @Part("description") RequestBody description,
                                        @Part("user_id") RequestBody userId,
                                        @Part("video_id") RequestBody video_id,
                                        @Part("devicetype") RequestBody devicetype,
                                        @Part("type") RequestBody type,
                                        @Part("mode") RequestBody mode,
                                        @Part("sound_id") RequestBody sound_id,
                                        @Part("hashtags") RequestBody hashtags,
                                        @Part("contest_category_id") RequestBody contestId,
                                        @Part("contest_category") RequestBody contestName,
                                        @Part("link_url") RequestBody videoLink,
                                        @Part("contest_status") RequestBody contestStatus);

    @Multipart
    @POST("livestreams/postvideo")
    Call<Map<String, String>> postVideoWithoutHashTag(@Part MultipartBody.Part file,
                                                      @Part("description") RequestBody description,
                                                      @Part("user_id") RequestBody userId,
                                                      @Part("video_id") RequestBody video_id,
                                                      @Part("devicetype") RequestBody devicetype,
                                                      @Part("type") RequestBody type,
                                                      @Part("mode") RequestBody mode,
                                                      @Part("sound_id") RequestBody sound_id,
                                                      @Part("contest_category_id") RequestBody contestId,
                                                      @Part("contest_category") RequestBody contestName,
                                                      @Part("link_url") RequestBody videoLink,
                                                      @Part("contest_status") RequestBody contestStatus);


    @FormUrlEncoded
    @POST("livestreams/reportstream")
    Call<Map<String, String>> reportStream(@FieldMap Map<String, String> params);

    @DELETE("livestreams/deletestream/{user_id}/{name}")
    Call<Map<String, String>> deleteVideo(@Path(Constants.TAG_USER_ID) String userId, @Path(Constants.TAG_NAME) String streamName);

    @FormUrlEncoded
    @POST("activities/updatewatchcount")
    Call<HashMap<String, String>> updateWatchCount(@FieldMap Map<String, String> params);

    @DELETE("activities/clearwatchhistory/{user_id}")
    Call<Map<String, String>> clearWatched(@Path(Constants.TAG_USER_ID) String userId);

    @POST("activities/hashtags")
    Call<HashTagResponse> getHashTags(@Body HashTagRequest request);

    @POST("activities/explorestreams")
    Call<ExploreStreamResponse> exploreStreams(@Body HashTagRequest request);

    @GET("activities/popularcountries/{popularType}")
    Call<CountryResponse> getPopularCountries(@Path("popularType") String popularType);

    @GET("activities/trendinghashtags")
    Call<TrendingStreamResponse> getTrendingHashTags();

    @FormUrlEncoded
    @POST("livestreams/sharestream")
    Call<Map<String, String>> shareStreams(@FieldMap Map<String, String> request);


    /* Fundoo*/
    @FormUrlEncoded
    @POST("activities/isMutualfollow")
    Call<Map<String, String>> mutualcontact(@FieldMap Map<String, String> request);


    @FormUrlEncoded
    @POST("accounts/messagecheck")
    Call<Map<String, String>> messagecheck(@FieldMap Map<String, String> request);


    @GET("accounts/getInterests")
    Call<InterestResponse> getInterestList();

    @POST("accounts/saveinterest")
    Call<Map<String, String>> saveInterest(@Body InterestRequest interestRequest);

    @POST("livestreams/home")
    Call<HomeResponse> getHomeData(@Body HomeRequest homeRequest);

    @POST("livestreams/home")
    Call<FollowingHomeResponse> getFollowingHomeData(@Body HomeRequest homeRequest);

    @FormUrlEncoded
    @POST("livestreams/singlevideo")
    Call<SingleVideoResponse> getSingleVideo(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchMainPageResponse> getPageMainSearch(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchUserRes> getSearchUser(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchVideoResponse> getSearchVideo(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchSoundRes> getSearchSound(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchHashTagRes> getSearchHash(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("livestreams/heart")
    Call<Map<String, String>> getHeartStatus(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/addfavorite")
    Call<Map<String, String>> setFav(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/sendgift")
    Call<Map<String, String>> sendGift(@FieldMap Map<String, String> params);

    @POST("activities/findsounds")
    Call<DiscoverSoundResponse> getSound(@Body DiscoverSoundRequest soundRequest);

    @POST("livestreams/getvideos")
    Call<GetVideosResponse> getvideos(@Body GetVideosRequest getVideoRequest);

    @FormUrlEncoded
    @POST("livestreams/getvideos")
    Call<GetVideosResponse> getRelatedSoundVideos(@FieldMap Map<String, String> params);

    @POST("activities/getfavorites")
    Call<FavVideoResponse> getfavvideos(@Body GetVideosRequest getVideoRequest);

    @POST("activities/getfavorites")
    Call<FavSoundResponse> getfavsounds(@Body GetVideosRequest getVideoRequest);

    @FormUrlEncoded
    @POST("chats/getcomment")
    Call<VideoCommentResponse> getVideoComments(@FieldMap Map<String, String> request);


    @FormUrlEncoded
    @POST("activities/notifications")
    Call<NotificationResponse> getNotifications(@FieldMap Map<String, String> request);

    /*@FormUrlEncoded
    @POST("chats/postcomment")
    Call<VideoCommentResponse> postComments(@FieldMap Map<String, String> request);*/

    @FormUrlEncoded
    @POST("chats/postcomment")
    Call<Map<String, String>> postComments(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("chats/deletecomment")
    Call<Map<String, String>> deleteComment(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/hashtags")
    Call<PostHashTagRes> getPostHashTags(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/setautoscroll")
    Call<Map<String,String>> setAutoScroll(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/vote")
    Call<Map<String,String>> setVote(@FieldMap Map<String, String> params);  //Not used

    @GET("livestreams/getcountry")
    Call<GetCountry> getCountry();

    @FormUrlEncoded
    @POST("livestreams/getstate")
    Call<GetState> getState(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/getcity")
    Call<GetCity> getCity(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("accounts/signup")
    Call<SignInResponse> signup(@FieldMap Map<String, String> params);

    @GET("activities/getcontestcategory")
    Call<ContestCategory> getContestData();

    @GET("livestreams/fillterdetails")
    Call<FilterDetailsModel> getFillters();

    @POST("activities/history")
    Call<HistoryModel> getHistory(@Body HistoryRequest request);

    @FormUrlEncoded
    @POST("livestreams/liveviews")
    Call<Map<String,String>> setViewCount(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("accounts/socialmedia")
    Call<SocialMediaLinks> getSocialMediaLink(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/videoscroll")
    Call<HomeResponse> getScrollVideos(@FieldMap HashMap<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/referralupdate")
    Call<HashMap<String,String>> updateReferral(@FieldMap HashMap<String, String> params);

    @FormUrlEncoded
    @POST("payments/goldstarconversion")
    Call<HashMap<String,String>> withdrawMoney(@FieldMap HashMap<String, String> params);

    @FormUrlEncoded
    @POST("/livestreams/share")
    Call<HashMap<String, String>> setShareCount(@FieldMap Map<String,String> params);

    @GET("votemessages/get-all-vote-messages")
    Call<VoteDataModel> getVotingData();
}
