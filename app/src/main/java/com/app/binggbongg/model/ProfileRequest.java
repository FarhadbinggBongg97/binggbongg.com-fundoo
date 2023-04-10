package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

public class ProfileRequest {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("name")
    private String name;
    @SerializedName("call_type")
    private String callType;
    @SerializedName("paypal_id")
    private String payPalId;
    @SerializedName("profile_id")
    private String profileId;
    @SerializedName("privacy_age")
    private String privacyAge;
    @SerializedName("privacy_contactme")
    private String privacyContactMe;
    @SerializedName("show_notification")
    private String showNotification;
    @SerializedName("chat_notification")
    private String chatNotification;
    @SerializedName("follow_notification")
    private String followNotification;
    @SerializedName("interest_notification")
    private String interestNotification;
    @SerializedName("bio")
    private String bio;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("message_privacy")
    private String message_privacy;

    @SerializedName("comment_privacy")
    private String comment_privacy;

    @SerializedName("weblink")
    private String website;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getComment_privacy() {
        return comment_privacy;
    }

    public void setComment_privacy(String comment_privacy) {
        this.comment_privacy = comment_privacy;
    }

    public String getMessage_privacy() {
        return message_privacy;
    }

    public void setMessage_privacy(String message_privacy) {
        this.message_privacy = message_privacy;
    }

    @SerializedName("message_privacy")


    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public String getPayPalId() {
        return payPalId;
    }

    public void setPayPalId(String payPalId) {
        this.payPalId = payPalId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getPrivacyAge() {
        return privacyAge;
    }

    public void setPrivacyAge(String privacyAge) {
        this.privacyAge = privacyAge;
    }

    public String getPrivacyContactMe() {
        return privacyContactMe;
    }

    public void setPrivacyContactMe(String privacyContactMe) {
        this.privacyContactMe = privacyContactMe;
    }

    public String getShowNotification() {
        return showNotification;
    }

    public void setShowNotification(String showNotification) {
        this.showNotification = showNotification;
    }

    public String getChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(String chatNotification) {
        this.chatNotification = chatNotification;
    }

    public String getFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(String followNotification) {
        this.followNotification = followNotification;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }


    public String getInterestNotification() {
        return interestNotification;
    }

    public void setInterestNotification(String interestNotification) {
        this.interestNotification = interestNotification;
    }
}
