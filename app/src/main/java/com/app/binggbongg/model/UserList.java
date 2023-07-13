package com.app.binggbongg.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hendraanggrian.appcompat.widget.Mentionable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by hitasoft on 25/2/18.
 */

public class UserList {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result implements Mentionable {

        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("followers")
        @Expose
        private Integer followers;
        @SerializedName("followings")
        @Expose
        private Integer followings;

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

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
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

        @NonNull
        @NotNull
        @Override
        public CharSequence getUsername() {
            return userName;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public CharSequence getDisplayname() {
            return name;
        }

        @NonNull
        @Override
        public String toString() {
            return userName;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public Object getAvatar() {
            return userImage;
        }
    }
}
