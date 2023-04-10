
package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

public class SignInResponse {

    @SerializedName("country")
    private String country;
    @SerializedName("state")
    private String state;
    @SerializedName("city")
    private String city;

    @SerializedName("age")
    private String age;
    @SerializedName("dob")
    private String dob;
    @SerializedName("gender")
    private String gender;
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("name")
    private String name;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("status")
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("accountexists")
    private boolean accountExists;
    @SerializedName("auth_token")
    private String authToken;
    @SerializedName("user_image")
    private String userImage;
    @SerializedName("message")
    private String message;
    @SerializedName("location")
    private String location;
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
    private boolean interestNotification;

    @SerializedName("premium_member")
    private String premiumMember;

    public boolean isInterestNotification() {
        return interestNotification;
    }

    public String getPremiumMember() {
        return premiumMember;
    }

    public void setPremiumMember(String premiumMember) {
        this.premiumMember = premiumMember;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
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

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAccountExists() {
        return accountExists;
    }

    public void setAccountExists(boolean accountExists) {
        this.accountExists = accountExists;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public boolean getInterestNotification() {
        return interestNotification;
    }

    public void setInterestNotification(boolean interestNotification) {
        this.interestNotification = interestNotification;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
