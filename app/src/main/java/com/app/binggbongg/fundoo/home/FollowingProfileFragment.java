package com.app.binggbongg.fundoo.home;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.binggbongg.db.DBHelper;
import com.app.binggbongg.fundoo.HistoryActivity;
import com.app.binggbongg.fundoo.PrivacyActivity;
import com.app.binggbongg.fundoo.home.eventbus.UserBlocked;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.app.binggbongg.R;
import com.app.binggbongg.apprtc.util.AppRTCUtils;
import com.app.binggbongg.fundoo.App;
import com.app.binggbongg.fundoo.ChatActivity;
import com.app.binggbongg.fundoo.ConvertGiftActivity;
import com.app.binggbongg.fundoo.FollowersActivity;
import com.app.binggbongg.fundoo.GemsStoreActivity;
import com.app.binggbongg.fundoo.PrimeActivity;
import com.app.binggbongg.fundoo.ProfileVideos;
import com.app.binggbongg.fundoo.SingleImageActivity;
import com.app.binggbongg.fundoo.home.eventbus.FollowingFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.ForYouFollowFollowing;
import com.app.binggbongg.fundoo.home.eventbus.OtherProfileToFollowingProfile;
import com.app.binggbongg.fundoo.profile.FavouritesActivity;
import com.app.binggbongg.helper.LocaleManager;
import com.app.binggbongg.helper.NetworkReceiver;
import com.app.binggbongg.helper.callback.ProfileImageClickListener;
import com.app.binggbongg.model.FollowRequest;
import com.app.binggbongg.model.GetSet;
import com.app.binggbongg.model.ProfileRequest;
import com.app.binggbongg.model.ProfileResponse;
import com.app.binggbongg.utils.AdminData;
import com.app.binggbongg.utils.ApiClient;
import com.app.binggbongg.utils.ApiInterface;
import com.app.binggbongg.utils.AppUtils;
import com.app.binggbongg.utils.Constants;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


/*public class FollowingProfileFragment extends BaseFragment {*/
public class FollowingProfileFragment extends Fragment {

    ShapeableImageView YouTubeIV;
    ShapeableImageView instaIV;
    ShapeableImageView facebookIV;
    ShapeableImageView whatsappIV;
    ShapeableImageView linkedInIV;
    ShapeableImageView snapChatIV;
    ShapeableImageView twitterIV;

    AppUtils appUtils;

    ApiInterface apiInterface;
    ImageView btnBack, btnEdit, genderImage;
    ShapeableImageView profileImage;
    RoundedImageView iconPrime;
    TextView txtName, txtUserName, userBio, txtHeartCount, txtFansCount, txtFollowingsCount,
            txtVideoCount, txtCoinsCount, txtGiftCount, txtOtherUserVCount, totalVotes, totalDiamond,
            totalStar, totalGifts, receivedVotes, referralCount, shareCount, viewCount;
    LinearLayout ownUserLay, otherUserLay, fansLay, followingLay, displayCountLay;
    RelativeLayout favLay, videoLay, coinsLay, giftsLay, parentLay, sharelay, viewsLay
            , referralLay;
    TextView btnFollow, btnMessage, btnBlock;
    RelativeLayout vCard,btnVideoCall;
    MaterialCardView  videoCard, giftsCard, coinsCard, favCard, renewalLay, subscribeLay, shareCard
            , referralCard, viewsCard;
    String profile_id;
    ProgressBar progrssBar;

    ProfileResponse profile;
    ProfileImageClickListener profileImageClickListener;

    Call<ProfileResponse> call;
    boolean videoEnabled=false;
    private Context context;
    private DBHelper dbHelper;
    String chatId ="";


    public FollowingProfileFragment() {
        // Required empty public constructor
    }

    public static FollowingProfileFragment newInstance(ProfileImageClickListener mprofileImageClickListener) {
        FollowingProfileFragment fragment = new FollowingProfileFragment();
        fragment.profileImageClickListener = mprofileImageClickListener;
        return fragment;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingProfileUpdate event) {
        Timber.d("onMessageEvent: %s", event.id);
        profile_id = event.id;
        getUserProfile(event.id, event.profileImage);

        try {

            if (event.userType.equals("")) {
                otherUserLay.setVisibility(View.GONE);
                ownUserLay.setVisibility(View.VISIBLE);

            } else {
                otherUserLay.setVisibility(View.VISIBLE);
                ownUserLay.setVisibility(View.GONE);
            }

            if (call != null && call.isExecuted()) call.cancel();

            getUserProfile(event.id, event.profileImage);
            profile_id = event.id;
            if (btnFollow != null) btnFollow.setEnabled(false);

        } catch (Exception e) {
            Timber.i("Exception %s", e.getMessage());
        }
    }

    // Came Data From OthersProfileActivity
    @Subscribe(priority = 1)
    public void onMessageEvent(OtherProfileToFollowingProfile event) {
        if (profile_id.equals(event.id))
            getUserProfile(event.id, "");
    }

    private void getUserProfile(String profileUserId, String getProfileImage) {

        if (NetworkReceiver.isConnected()) {
            if (GetSet.getUserId() != null) {

                Glide.with(this)
                        .load(getProfileImage)
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                genderImage.setVisibility(View.INVISIBLE);
                displayCountLay.setVisibility(View.INVISIBLE);
                progrssBar.setVisibility(View.VISIBLE);

                parentLay.setAlpha(0.3f);
                userBio.setText("");
                txtHeartCount.setText("-");
                txtFansCount.setText("-");
                txtFollowingsCount.setText("-");
                txtOtherUserVCount.setText("-");
                txtVideoCount.setText("-");
                txtCoinsCount.setText("-");
               // txtGiftCount.setText("-");
                txtName.setText("");
                txtUserName.setText("");


                ProfileRequest request = new ProfileRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(profileUserId);
                call = apiInterface.getProfile(request);
                call.enqueue(new Callback<ProfileResponse>() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {

                        profile = response.body();

                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {


                            Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body()));

                            /*if (GetSet.getUserId() != null && !GetSet.getUserId().equals(profileUserId)) {
                                otherUserLay.setVisibility(View.VISIBLE);
                                ownUserLay.setVisibility(View.GONE);
                            } else if (GetSet.getUserId() != null && GetSet.getUserId().equals(profileUserId)) {
                                otherUserLay.setVisibility(View.GONE);
                                ownUserLay.setVisibility(View.VISIBLE);
                            }

                            *//*Glide.with(getContext())
                                    .load(response.body().getUserImage())

                                    .into(profileImage);*//*

                            txtHeartCount.setText(String.valueOf(profile.getLikes().equals(0) ? "-" : profile.getLikes()));
                            txtFansCount.setText(String.valueOf(profile.getFollowers().equals(0) ? "-" : profile.getFollowers()));
                            txtFollowingsCount.setText(String.valueOf(profile.getFollowings().equals(0) ? "-" : profile.getFollowings()));
                            txtOtherUserVCount.setText(String.valueOf(profile.getVideosCount().equals(0) ? "-" : profile.getVideosCount()));
                            txtVideoCount.setText(String.valueOf(profile.getVideosCount().equals(0) ? "-" : profile.getVideosCount()));
                            txtCoinsCount.setText(String.valueOf(profile.getAvailableGems().equals(0) ? "-" : profile.getAvailableGems()));
                            txtGiftCount.setText(String.valueOf(profile.getAvailableGifts().equals(0) ? "-" : profile.getAvailableGifts()));*/

                            setPrimeView(profile);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {

                    }
                });
            }
        } else {
//            App.makeToast(getString(R.string.no_internet_connection));
        }
    }

    private void initView(View rootView) {

        btnBack = rootView.findViewById(R.id.btnBack);
        btnEdit = rootView.findViewById(R.id.btnEdit);
        genderImage = rootView.findViewById(R.id.genderImage);


        profileImage = rootView.findViewById(R.id.profileImage);
        iconPrime = rootView.findViewById(R.id.iconPrime);

        txtName = rootView.findViewById(R.id.txtName);
        txtUserName = rootView.findViewById(R.id.txtUserName);
        userBio = rootView.findViewById(R.id.userBio);
        txtHeartCount = rootView.findViewById(R.id.txtHeartCount);
        txtFansCount = rootView.findViewById(R.id.txtFansCount);
        txtFollowingsCount = rootView.findViewById(R.id.txtFollowingsCount);
        txtVideoCount = rootView.findViewById(R.id.txtVideoCount);
        txtCoinsCount = rootView.findViewById(R.id.txtCoinsCount);
        txtGiftCount = rootView.findViewById(R.id.txtGiftCount);

        ownUserLay = rootView.findViewById(R.id.ownUserLay);
        otherUserLay = rootView.findViewById(R.id.otherUserLay);

        favLay = rootView.findViewById(R.id.favLay);
        videoLay = rootView.findViewById(R.id.videoLay);
        coinsLay = rootView.findViewById(R.id.coinsLay);
        giftsLay = rootView.findViewById(R.id.giftsLay);
        subscribeLay = rootView.findViewById(R.id.subscribeLay);
        renewalLay = rootView.findViewById(R.id.renewalLay);
        sharelay = rootView.findViewById(R.id.shareLay);
        referralLay = rootView.findViewById(R.id.referralLay);
        viewsLay = rootView.findViewById(R.id.viewsLay);
        vCard = rootView.findViewById(R.id.vCard);
        txtOtherUserVCount = rootView.findViewById(R.id.txtOtherUserVCount);

        btnFollow = rootView.findViewById(R.id.btnFollow);
        btnMessage = rootView.findViewById(R.id.btnMessage);

        btnVideoCall = rootView.findViewById(R.id.btnVideoCall);

        fansLay = rootView.findViewById(R.id.fansLay);
        followingLay = rootView.findViewById(R.id.followingLay);
        favCard = rootView.findViewById(R.id.favCard);

        coinsCard = rootView.findViewById(R.id.coinsCard);
        giftsCard = rootView.findViewById(R.id.giftsCard);
        videoCard = rootView.findViewById(R.id.videoCard);
        shareCard = rootView.findViewById(R.id.shareCard);
        referralCard = rootView.findViewById(R.id.referralCard);
        viewsCard = rootView.findViewById(R.id.viewsCard);

        displayCountLay = rootView.findViewById(R.id.displayCountLay);
        progrssBar = rootView.findViewById(R.id.progrssBar);
        parentLay = rootView.findViewById(R.id.parentLay);

        YouTubeIV=rootView.findViewById(R.id.youtubeIV);
        instaIV=rootView.findViewById(R.id.instaIV);
        facebookIV=rootView.findViewById(R.id.facebookIV);
        whatsappIV=rootView.findViewById(R.id.whatsappIV);
        linkedInIV=rootView.findViewById(R.id.linkedInIV);
        snapChatIV=rootView.findViewById(R.id.snapChatIV);
        twitterIV=rootView.findViewById(R.id.twitterIV);
        totalDiamond = rootView.findViewById(R.id.tv_totalDiamond);
        totalVotes = rootView.findViewById(R.id.tv_receivedVotes);
        totalGifts = rootView.findViewById(R.id.tv_totalGifts);
        totalStar = rootView.findViewById(R.id.tv_totalStar);
        receivedVotes = rootView.findViewById(R.id.txtVotesCount);
        viewCount = rootView.findViewById(R.id.txtViewssCount);
        referralCount = rootView.findViewById(R.id.txtReferralCount);
        shareCount = rootView.findViewById(R.id.txtShareCount);
        btnBlock = rootView.findViewById(R.id.btnBlock);
        dbHelper = DBHelper.getInstance(getActivity());
        chatId = GetSet.getUserId() + profile_id;

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setColorFilter(getResources().getColor(R.color.colorWhite));
        btnEdit.setVisibility(View.GONE);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setPrimeView(ProfileResponse profile) {

        progrssBar.setVisibility(View.GONE);
        genderImage.setVisibility(View.VISIBLE);


        genderImage.setImageDrawable(profile.getGender().equals(Constants.TAG_MALE) ?
                getResources().getDrawable(R.drawable.men) : getResources().getDrawable(R.drawable.women));

        if (profile.getBio().equals("")) userBio.setVisibility(View.GONE);
        else userBio.setVisibility(View.VISIBLE);


        /*txtName.setText(profile.getName());
        txtUserName.setText(profile.getUserName());*/

        if (profile.getBio().equals("")) userBio.setVisibility(View.GONE);
        else userBio.setVisibility(View.VISIBLE);


        if (profile.getFollow().contains(Constants.TAG_TRUE))
            btnFollow.setText(getResources().getString(R.string.unfollow));
        else btnFollow.setText(getResources().getString(R.string.follow));

        btnFollow.setEnabled(true);

        if (profile.getBlockedByMe() != null && !TextUtils.isEmpty(profile.getBlockedByMe())) {
            btnBlock.setVisibility(View.VISIBLE);
            if (profile.getBlockedByMe().equals(Constants.TAG_TRUE)) {
                btnBlock.setText(getString(R.string.unblock));
            } else {
                btnBlock.setText(getString(R.string.block));
            }
        } else {
            btnBlock.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(profile.getYoutube_link())){
            YouTubeIV.setVisibility(View.VISIBLE);
        }else{
            YouTubeIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getTwitter())){
            twitterIV.setVisibility(View.VISIBLE);
        }else{
            twitterIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getInstagram())){
            instaIV.setVisibility(View.VISIBLE);
        }else{
            instaIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getFb())){
            facebookIV.setVisibility(View.VISIBLE);
        }else{
            facebookIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getLinkedin())){
            linkedInIV.setVisibility(View.VISIBLE);
        }else{
            linkedInIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getSnapchat())){
            snapChatIV.setVisibility(View.VISIBLE);
        }else{
            snapChatIV.setVisibility(GONE);
        }

        if(!TextUtils.isEmpty(profile.getWhatsapp())){
            whatsappIV.setVisibility(View.VISIBLE);
        }else{
            whatsappIV.setVisibility(GONE);
        }

        userBio.setText(profile.getBio());

        txtHeartCount.setText(String.valueOf(profile.getLikes().equals(0) ? "-" : profile.getLikes()));
        txtFansCount.setText(String.valueOf(profile.getFollowers().equals(0) ? "-" : profile.getFollowers()));
        txtFollowingsCount.setText(String.valueOf(profile.getFollowings().equals(0) ? "-" : profile.getFollowings()));
        txtOtherUserVCount.setText(String.valueOf(profile.getVideosCount().equals(0) ? "-" : profile.getVideosCount()));
        txtVideoCount.setText(String.valueOf(profile.getVideosCount().equals(0) ? "-" : profile.getVideosCount()));
        txtCoinsCount.setText(String.valueOf(profile.getAvailableGems().equals(0) ? "-" : profile.getAvailableGems()));
     //   txtGiftCount.setText(String.valueOf(profile.getAvailableGifts().equals(0) ? "-" : profile.getAvailableGifts()));
        txtName.setText(profile.getName());
        txtUserName.setText(profile.getUserName());

        totalStar.setText(profile.getGoldenstar_count()!=null ?  profile.getGoldenstar_count(): "0");
        totalGifts.setText(profile.getGiftbox_count()!=null? profile.getGiftbox_count(): "0");
        totalVotes.setText(profile.getTotal_vote_count()!=null? profile.getTotal_vote_count(): "0");
        totalDiamond.setText(profile.getBluediamond_count()!=null? profile.getBluediamond_count(): "0");
        receivedVotes.setText(profile.getTotal_vote_count()!=null? profile.getTotal_vote_count(): "-");
        shareCount.setText(profile.getTotal_share_count()!=null? profile.getTotal_share_count():"0");
        referralCount.setText(profile.getTotal_referals_count()!=null? profile.getTotal_referals_count():"0");
        viewCount.setText(profile.getTotal_vote_count()!=null? profile.getTotal_views_count():"0");



        displayCountLay.setVisibility(View.VISIBLE);


     /*   Glide.with(getContext())
                .load(profile.getUserImage())

                .placeholder(R.drawable.default_profile_image)
                .into(profileImage);*/


        /*if (profile.getPremiumMember().equals(Constants.TAG_TRUE)) {
            iconPrime.setVisibility(View.VISIBLE);
            renewalLay.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
        } else if (GetSet.isOncePurchased()) {
            renewalLay.setVisibility(View.VISIBLE);
            subscribeLay.setVisibility(View.GONE);
            iconPrime.setVisibility(View.GONE);
        } else {
            iconPrime.setVisibility(View.GONE);
            subscribeLay.setVisibility(View.VISIBLE);
        }*/

         /*   Glide.with(this)
                    .load(profile.getUserImage())

                    .placeholder(R.drawable.default_profile_image)
                    .into(profileImage);*/

        parentLay.setAlpha(1f);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.fragment_following_profile, container, false);

        initView(view);

        return view;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        appUtils = new AppUtils(getActivity());

        fansLay.setOnClickListener(v -> {
            App.preventMultipleClick(fansLay);
            Intent followersIntent = new Intent(getContext(), FollowersActivity.class);
            followersIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWERS);
            followersIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
            followersIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(followersIntent);
        });


        followingLay.setOnClickListener(v -> {
            App.preventMultipleClick(followingLay);
            Intent followingsIntent = new Intent(getContext(), FollowersActivity.class);
            followingsIntent.putExtra(Constants.TAG_ID, Constants.TAG_FOLLOWINGS);
            followingsIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
            followingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(followingsIntent);
        });

        profileImage.setOnClickListener(v -> {
            App.preventMultipleClick(profileImage);

            Timber.d("onViewCreated: %s", profile.getUserImage());
            Intent intent = new Intent(getContext(), SingleImageActivity.class);
            intent.putExtra("image", String.valueOf(profile.getUserImage()));
            startActivity(intent);
            animateZoom(getActivity());
        });


        btnFollow.setOnClickListener(v -> {

            App.preventMultipleClick(btnFollow);
            followUnfollowUser(profile.getUserId());
        });

        btnBack.setOnClickListener(v -> {
            profileImageClickListener.onUserClicked(false);
        });

        btnBlock.setOnClickListener(v->{
            App.preventMultipleClick(btnBlock);
            blockUnBlockUser(profile_id);
        });

        favCard.setOnClickListener(v -> {
            App.preventMultipleClick(favCard);

            Intent intent = new Intent(getActivity(), FavouritesActivity.class);
            startActivity(intent);
        });


        // other profile
        vCard.setOnClickListener(v -> {
            App.preventMultipleClick(vCard);
            Intent intent = new Intent(getContext(), ProfileVideos.class);
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_FOLLOWING_PROFILE_UPDATE);
            intent.putExtra(Constants.TAG_PROFILE_ID, profile_id);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            animateZoom(getActivity());
        });

        // own profile
        videoCard.setOnClickListener(v -> {
            App.preventMultipleClick(videoCard);
            Intent intent = new Intent(getContext(), ProfileVideos.class);
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_OWN_PROFILE);
            intent.putExtra(Constants.TAG_PROFILE_ID, GetSet.getUserId());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

        });

        giftsCard.setOnClickListener(v -> {
            Intent giftIntent = new Intent(getActivity(), ConvertGiftActivity.class);
            giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(giftIntent);
        });

        coinsCard.setOnClickListener(v -> {
            App.preventMultipleClick(coinsCard);
            Intent gemsIntent = new Intent(getActivity(), GemsStoreActivity.class);
            gemsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(gemsIntent);
        });


        shareCard.setOnClickListener(v -> {
            App.preventMultipleClick(sharelay);
            Intent shareIntent = new Intent(getActivity(), HistoryActivity.class);
            shareIntent.putExtra(Constants.TAG_TYPE,"share");
            shareIntent.putExtra(Constants.TAG_TITLE, "Share");
            startActivity(shareIntent);
        });

        referralCard.setOnClickListener(v -> {
            App.preventMultipleClick(referralLay);
            Intent referral = new Intent(getActivity(), HistoryActivity.class);
            referral.putExtra(Constants.TAG_TYPE,"referral");
            referral.putExtra(Constants.TAG_TITLE,"Referral History");
            startActivity(referral);
        });

        viewsCard.setOnClickListener(v -> {
            App.preventMultipleClick(viewsLay);
            Intent viewIntent = new Intent(getActivity(), HistoryActivity.class);
            viewIntent.putExtra(Constants.TAG_TYPE, "views");
            viewIntent.putExtra(Constants.TAG_TITLE, "View History");
            startActivity(viewIntent);
        });

        btnMessage.setOnClickListener(v -> {
            App.preventMultipleClick(btnMessage);
            messageCheck();
          /*  Intent chatIntent = new Intent(getContext(), ChatActivity.class);
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());
            startActivity(chatIntent);*/
        });

        //add Audio call Addon code here

        btnVideoCall.setOnClickListener(v -> {
            App.preventMultipleClick(btnVideoCall);
            videoEnabled=true;
            if (profile.getBlockedByMe() != null && profile.getBlockedByMe().equals(Constants.TAG_TRUE)) {
                App.makeToast(getString(R.string.unblock_call_description));
            }else{
                mutualContact(profile.getUserId());
            }
            /*if (profile.getBlockedMe() != null && profile.getBlockedMe().equals(Constants.TAG_TRUE)) {
                App.makeToast(getString(R.string.unblock_call_description));
            } else {
                if (GetSet.getGems() >= AdminData.videoCallsGems) {
                    if (NetworkReceiver.isConnected()) {
                        AppRTCUtils appRTCUtils = new AppRTCUtils(getContext());
                        Intent callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_VIDEO);
                        callIntent.putExtra(Constants.TAG_USER_NAME, profile.getName());
                        callIntent.putExtra(Constants.TAG_USER_IMAGE, profile.getUserImage());
                        callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(callIntent);
                    } else {
                        App.makeToast(getString(R.string.no_internet_connection));
                    }
                } else {
                    App.makeToast(getString(R.string.not_enough_gems));
                }
            }*/
        });

        context=getContext();
        YouTubeIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getYoutube_link(),"com.google.android.youtube"));

        twitterIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getTwitter(),"com.twitter.android"));

        instaIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getInstagram(),"com.instagram.android"));

        facebookIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getFb(),"com.facebook.katana"));

        linkedInIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getLinkedin(),"com.inked"));

        snapChatIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getSnapchat(),"com.snapchat.android"));

        whatsappIV.setOnClickListener(v-> GotoSocialMedia(context,profile.getWhatsapp(),"com.whatsapp"));
    }

    private void blockUnBlockUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, partnerId);
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            String chatId = GetSet.getUserId() + profile.getUserId();
                            if (responseMap.get(Constants.TAG_BLOCK_STATUS).equals("1")) {
                                profile.setBlockedByMe(Constants.TAG_TRUE);
                                btnBlock.setText(getString(R.string.unblock));
                                App.makeToast(getString(R.string.user_blocked_successfully));
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
                                    dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                    dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, partnerId, profile.getUserName(), profile.getUserImage(), Constants.TAG_TRUE);
                                    if (dbHelper.isChatIdExists(chatId)) {
                                        dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                                        dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                                    }
                                }
                                EventBus.getDefault().post(new UserBlocked(true, profile_id));
                            } else {
                                if (dbHelper.isChatIdExists(chatId)) {
                                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
                                } else {
                                    dbHelper.insertBlockStatus(chatId, partnerId, profile.getUserName(), profile.getUserImage(), Constants.TAG_FALSE);
                                }
                                profile.setBlockedByMe(Constants.TAG_FALSE);
                                btnBlock.setText(getString(R.string.block));
                                App.makeToast(getString(R.string.unblocked_successfully));
                                EventBus.getDefault().post(new UserBlocked(false, profile_id));
                            }
                             getUserProfile(partnerId,"");
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    private void GotoSocialMedia(Context context, String link, String packageName) {
        if (link != null && !link.equals("")){
            if (packageName.equals("com.whatsapp")){
                link="https://wa.me/"+link;
            }
            if(URLUtil.isValidUrl(link)){
                Log.e("TAG", "GotoSocialMedia: ::::::::::::::::::::::::::::::::::::::::"+link );
                if (AppUtils.appInstalledOrNot(context,packageName)){
                  /*  Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(link.toString()));
                    startActivity(intent);*/
                    Intent intent = requireActivity().getPackageManager().getLaunchIntentForPackage(packageName);
                    intent.setData(Uri.parse(link.toString()));
                    startActivity(intent);
                }else{
                    Toast.makeText(context, "Cannot find any App to perform this link", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, PrivacyActivity.class);
                    intent.putExtra("from", "video");
                    intent.putExtra("link_url", link.toString());
                    startActivity(intent);
                }
            }else{
                Toast.makeText(context, "Update the valid url", Toast.LENGTH_SHORT).show();
            }

        }else if(TextUtils.isEmpty(link)){
            Toast.makeText(context, "No link has found", Toast.LENGTH_SHORT).show();
        }
    }


    private void mutualContact(String publisherId) {
        if (NetworkReceiver.isConnected()) {


            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_FOLLOWER_ID, publisherId);

            Timber.i("mutualcontact params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.mutualcontact(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    Timber.i("mutualcontact response=> %s", response);

                    if (response.body() != null) {
                        if (Objects.requireNonNull(response.body().get("mutual_follow")).equals("1")) {

                            if (profile.getBlockedMe() != null && profile.getBlockedMe().equals(Constants.TAG_TRUE)) {
                                App.makeToast(getString(R.string.unblock_call_description));
                            } else {
                                if (GetSet.getGems() >= AdminData.videoCallsGems) {
                                    if (NetworkReceiver.isConnected()) {
                                        AppRTCUtils appRTCUtils = new AppRTCUtils(getContext());
                                        Intent callIntent=new Intent();
                                        if(videoEnabled) {
                                            callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_VIDEO);
                                        }
                                        else
                                        {
                                            callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_AUDIO);

                                        }
                                        callIntent.putExtra(Constants.TAG_USER_NAME, profile.getName());
                                        callIntent.putExtra(Constants.TAG_USER_IMAGE, profile.getUserImage());
                                        callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(callIntent);
                                    } else {
                                        App.makeToast(getString(R.string.no_internet_connection));
                                    }
                                } else {
                                    App.makeToast(getString(R.string.not_enough_gems));
                                }
                           /*     if (GetSet.getGems() >= AdminData.videoCallsGems) {
                                    if (NetworkReceiver.isConnected()) {
                                        AppRTCUtils appRTCUtils = new AppRTCUtils(getContext());
                                        Intent callIntent = appRTCUtils.connectToRoom(profile.getUserId(), Constants.TAG_SEND, Constants.TAG_VIDEO);
                                        callIntent.putExtra(Constants.TAG_USER_NAME, profile.getName());
                                        callIntent.putExtra(Constants.TAG_USER_IMAGE, profile.getUserImage());
                                        callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(callIntent);
                                    } else {
                                        App.makeToast(getString(R.string.no_internet_connection));
                                    }
                                } else {
                                    App.makeToast(getString(R.string.not_enough_gems));
                                }*/
                            }

                        } else if (Objects.equals(response.body().get("mutual_follow"), "0")) {
                            App.dialog(getContext(), "", getResources().getString(R.string.mutual_followers_can_make_a_call), getResources().getColor(R.color.colorBlack));
                        }
                    }

                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void messageCheck() {

        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_PROFILE_ID, profile.getUserId());

            Timber.i("mutualcontact params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.messagecheck(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    if (response.body() != null) {
                        if (response.body().get("status").equals("true")) {
                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, profile.getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, profile.getName());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, profile.getUserImage());
                            chatIntent.putExtra(Constants.TAG_BLOCKED_BY_ME, profile.getBlockedByMe());

                            startActivity(chatIntent);
                        } else {
                            App.dialog(getContext(), "", getResources().getString(R.string.user_alert_message), getResources().getColor(R.color.colorBlack));
                        }
                    }

                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }

    public static void animateZoom(Context context) {
        ((Activity) context).
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }

    private void followUnfollowUser(String publisherId) {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());

            if (btnFollow.getText().equals(Constants.TAG_FOLLOW_USER))
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            else
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);

            Timber.d("followUnfollowUser: %s", App.getGsonPrettyInstance().toJson(followRequest));

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<java.util.Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Map<String, String> followResponse = response.body();

                    if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                        Timber.d("onResponse: %s=> ", followRequest.getType());

                        if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));
                            txtFansCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_in));
                            int getCount = Integer.parseInt(txtFansCount.getText().toString().replace("-", "0"));

                            if (followResponse.get(Constants.TAG_MESSAGE).equals("Followed successfully"))
                                txtFansCount.setText(String.valueOf(getCount + 1));
                            else if (followResponse.get(Constants.TAG_MESSAGE).equals("Unfollowed successfully"))
                                txtFansCount.setText(String.valueOf(getCount - 1));

                            if (btnFollow.getText().equals(Constants.TAG_FOLLOW_USER)) {
                                btnFollow.setText(Constants.TAG_UNFOLLOW_USER);
                                EventBus.getDefault().post(new ForYouFollowFollowing(false, publisherId));
                                EventBus.getDefault().post(new FollowingFollowFollowing(false, publisherId));

                            } else {
                                EventBus.getDefault().post(new ForYouFollowFollowing(true, publisherId));
                                EventBus.getDefault().post(new FollowingFollowFollowing(true, publisherId));
                                btnFollow.setText(Constants.TAG_FOLLOW_USER);
                            }

                            txtFansCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));
                        }

                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    private void goToPrime() {
        Intent prime = new Intent(getActivity(), PrimeActivity.class);
        prime.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(prime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if (call != null) call.cancel();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /*@Override
    public boolean onBackPressed() {
        //Timber.d("isVisible: %s", isVisibleToUser());
        profileImageClickListener.onUserClicked(false);
        return true;
    }*/
}