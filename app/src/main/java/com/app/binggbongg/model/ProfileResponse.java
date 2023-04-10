
package com.app.binggbongg.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileResponse implements Serializable {

    @SerializedName("total_vote_count")
    @Expose
    private String total_vote_count;

    @SerializedName("total_views_count")
    @Expose
    private String total_views_count;


    @SerializedName("total_referals_count")
    @Expose
    private String total_referals_count;


    @SerializedName("total_share_count")
    @Expose
    private String total_share_count;


    @SerializedName("giftbox_count")
    @Expose
    private String giftbox_count;


    @SerializedName("purchased_votes")
    @Expose
    private String purChasedVotes;


    @SerializedName("bluediamond_count")
    @Expose
    private String bluediamond_count;


    @SerializedName("goldenstar_count")
    @Expose
    private String goldenstar_count;

    @SerializedName("Votes_count")
    @Expose
    private String Votes_count;


    @SerializedName("country")
    @Expose
    private String country;


    @SerializedName("state")
    @Expose
    private String state;


    @SerializedName("city")
    @Expose
    private String city;


    @SerializedName("youtube_link")
    @Expose
    private String Youtube_link;


    @SerializedName("linkedin")
    @Expose
    private String linkedin;


    @SerializedName("instagram")
    @Expose
    private String instagram;


    @SerializedName("snapchat")
    @Expose
    private String snapchat;

    @SerializedName("facebook")
    @Expose
    private String fb;


    @SerializedName("whatsapp")
    @Expose
    private String whatsapp;


    @SerializedName("twitter")
    @Expose
    private String twitter;


    @SerializedName("tiktok")
    @Expose
    private String tiktok;

    @SerializedName("pinterest")
    @Expose
    private String pinterest;

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("blocked_by_me")
    @Expose
    private String blockedByMe;
    @SerializedName("blocked_me")
    @Expose
    private String blockedMe;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("age")
    @Expose
    private Integer age;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("user_image")
    @Expose
    private String userImage;
    @SerializedName("paypal_id")
    @Expose
    private String paypalId;

    @SerializedName("mailid")
    @Expose
    private String email;

     @SerializedName("phonenumber")
    @Expose
    private String phoneNumber;

     @SerializedName("weblink")
    @Expose
    private String webLink;

    @SerializedName("followers")
    @Expose
    private Integer followers;
    @SerializedName("followings")
    @Expose
    private Integer followings;
    @SerializedName("follow")
    @Expose
    private String follow;
    @SerializedName("available_gems")
    @Expose
    private Float availableGems;
    @SerializedName("videos_count")
    @Expose
    private Integer videosCount;
    @SerializedName("available_gifts")
    @Expose
    private Integer availableGifts;
    @SerializedName("gift_earnings")
    @Expose
    private Integer giftEarnings;
    @SerializedName("premium_member")
    @Expose
    private String premiumMember;
    @SerializedName("premium_expiry_date")
    @Expose
    private String premiumExpiryDate;
    @SerializedName("show_notification")
    @Expose
    private Boolean showNotification;
    @SerializedName("chat_notification")
    @Expose
    private Boolean chatNotification;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("follow_notification")
    @Expose
    private Boolean followNotification;
    @SerializedName("referal_link")
    @Expose
    private String referalLink;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("likes")
    @Expose
    private Integer likes;
    @SerializedName("comment_privacy")
    @Expose
    private String commentPrivacy;
    @SerializedName("message_privacy")
    @Expose
    private String messagePrivacy;
    @SerializedName("is_user_can_message")
    @Expose
    private Boolean isUserCanMessage;

    @SerializedName("money_price_value")
    @Expose
    private Float moneyPriceValue;

    @SerializedName("money_currency_symbol")
    @Expose
    private String moneyCurrencySymbol;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getMoneyCurrencySymbol() {
        return moneyCurrencySymbol;
    }

    public void setMoneyCurrencySymbol(String moneyCurrencySymbol) {
        this.moneyCurrencySymbol = moneyCurrencySymbol;
    }

    public Float getMoneyPriceValue() {
        return moneyPriceValue;
    }

    public void setMoneyPriceValue(Float moneyPriceValue) {
        this.moneyPriceValue = moneyPriceValue;
    }

    public String getTotal_vote_count() {
        return total_vote_count;
    }

    public void setTotal_vote_count(String total_vote_count) {
        this.total_vote_count = total_vote_count;
    }

    public String getTotal_views_count() {
        return total_views_count;
    }

    public void setTotal_views_count(String total_views_count) {
        this.total_views_count = total_views_count;
    }

    public String getTotal_referals_count() {
        return total_referals_count;
    }

    public void setTotal_referals_count(String total_referals_count) {
        this.total_referals_count = total_referals_count;
    }

    public String getTotal_share_count() {
        return total_share_count;
    }

    public void setTotal_share_count(String total_share_count) {
        this.total_share_count = total_share_count;
    }

    public String getGiftbox_count() {
        return giftbox_count;
    }

    public void setGiftbox_count(String giftbox_count) {
        this.giftbox_count = giftbox_count;
    }

    public String getPurChasedVotes() {
        return purChasedVotes;
    }

    public void setPurChasedVotes(String purChasedVotes) {
        this.purChasedVotes = purChasedVotes;
    }

    public String getBluediamond_count() {
        return bluediamond_count;
    }

    public void setBluediamond_count(String bluediamond_count) {
        this.bluediamond_count = bluediamond_count;
    }

    public String getGoldenstar_count() {
        return goldenstar_count;
    }

    public void setGoldenstar_count(String goldenstar_count) {
        this.goldenstar_count = goldenstar_count;
    }

    public String getVotes_count() {
        return Votes_count;
    }

    public void setVotes_count(String votes_count) {
        Votes_count = votes_count;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYoutube_link() {
        return Youtube_link;
    }

    public void setYoutube_link(String youtube_link) {
        Youtube_link = youtube_link;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getSnapchat() {
        return snapchat;
    }

    public void setSnapchat(String snapchat) {
        this.snapchat = snapchat;
    }

    public String getFb() {
        return fb;
    }

    public void setFb(String fb) {
        this.fb = fb;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getTiktok() {
        return tiktok;
    }

    public void setTiktok(String tiktok) {
        this.tiktok = tiktok;
    }

    public String getPinterest() {
        return pinterest;
    }

    public void setPinterest(String pinterest) {
        this.pinterest = pinterest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBlockedByMe() {
        return blockedByMe;
    }

    public void setBlockedByMe(String blockedByMe) {
        this.blockedByMe = blockedByMe;
    }

    public String getBlockedMe() {
        return blockedMe;
    }

    public void setBlockedMe(String blockedMe) {
        this.blockedMe = blockedMe;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getPaypalId() {
        return paypalId;
    }

    public void setPaypalId(String paypalId) {
        this.paypalId = paypalId;
    }

    public Integer getFollowers() {
        return followers;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public Integer getFollowings() {
        return followings;
    }

    public void setFollowings(Integer followings) {
        this.followings = followings;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

/*    public Integer getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(Integer availableGems) {
        this.availableGems = availableGems;
    }*/

    public Float getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(Float availableGems) {
        this.availableGems = availableGems;
    }

    public Integer getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(Integer videosCount) {
        this.videosCount = videosCount;
    }

    public Integer getAvailableGifts() {
        return availableGifts;
    }

    public void setAvailableGifts(Integer availableGifts) {
        this.availableGifts = availableGifts;
    }

    public Integer getGiftEarnings() {
        return giftEarnings;
    }

    public void setGiftEarnings(Integer giftEarnings) {
        this.giftEarnings = giftEarnings;
    }

    public String getPremiumMember() {
        return premiumMember;
    }

    public void setPremiumMember(String premiumMember) {
        this.premiumMember = premiumMember;
    }

    public String getPremiumExpiryDate() {
        return premiumExpiryDate;
    }

    public void setPremiumExpiryDate(String premiumExpiryDate) {
        this.premiumExpiryDate = premiumExpiryDate;
    }

    public Boolean getShowNotification() {
        return showNotification;
    }

    public void setShowNotification(Boolean showNotification) {
        this.showNotification = showNotification;
    }

    public Boolean getChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(Boolean chatNotification) {
        this.chatNotification = chatNotification;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(Boolean followNotification) {
        this.followNotification = followNotification;
    }

    public String getReferalLink() {
        return referalLink;
    }

    public void setReferalLink(String referalLink) {
        this.referalLink = referalLink;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getCommentPrivacy() {
        return commentPrivacy;
    }

    public void setCommentPrivacy(String commentPrivacy) {
        this.commentPrivacy = commentPrivacy;
    }

    public String getMessagePrivacy() {
        return messagePrivacy;
    }

    public void setMessagePrivacy(String messagePrivacy) {
        this.messagePrivacy = messagePrivacy;
    }

    public Boolean getUserCanMessage() {
        return isUserCanMessage;
    }

    public void setUserCanMessage(Boolean userCanMessage) {
        isUserCanMessage = userCanMessage;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        boolean result = false;
        if (object != null && object.getClass() == getClass()) {
            ProfileResponse method = (ProfileResponse) object;
            if (this.getUserId() != null &&
                    this.userId.equals(method.getUserId())) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.userId.hashCode();
        return hash;
    }
}
