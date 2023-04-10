package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryModel {

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("total_sent_votes")
    @Expose
    private String totalSentVotes;
    @SerializedName("total_purchased_votes")
    @Expose
    private String totalPurchasedVotes;

    @SerializedName("result")
    @Expose
    private List<Result> result;

    public String getTotalSentVotes() {
        return totalSentVotes;
    }

    public void setTotalSentVotes(String totalSentVotes) {
        this.totalSentVotes = totalSentVotes;
    }

    public String getTotalPurchasedVotes() {
        return totalPurchasedVotes;
    }

    public void setTotalPurchasedVotes(String totalPurchasedVotes) {
        this.totalPurchasedVotes = totalPurchasedVotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public class Result {

        @SerializedName("votes_purchased")
        @Expose
        private String votesPurchased;
        @SerializedName("votes_sent")
        @Expose
        private String votesSent;
        @SerializedName("votes_received")
        @Expose
        private String votesReceived;
        @SerializedName("golderstar_amount")
        @Expose
        private String goldenStarAmount;
        @SerializedName("date")
        @Expose
        private String date;
        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("time")
        @Expose
        private String time;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("full_name")
        @Expose
        private String fullName;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("video_id")
        @Expose
        private String videoId;
        @SerializedName("playback_url")
        @Expose
        private String playbackUrl;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("message")
        @Expose
        private String message;
        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("created_at")
        @Expose
        private String createdAt;

        public String getVideoDescription() {
            return videoDescription;
        }

        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getGoldenStarAmount() {
            return goldenStarAmount;
        }

        public void setGoldenStarAmount(String goldenStarAmount) {
            this.goldenStarAmount = goldenStarAmount;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getVotesPurchased() {
            return votesPurchased;
        }

        public void setVotesPurchased(String votesPurchased) {
            this.votesPurchased = votesPurchased;
        }

        public String getVotesSent() {
            return votesSent;
        }

        public void setVotesSent(String votesSent) {
            this.votesSent = votesSent;
        }

        public String getVotesReceived() {
            return votesReceived;
        }

        public void setVotesReceived(String votesReceived) {
            this.votesReceived = votesReceived;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public String getPlaybackUrl() {
            return playbackUrl;
        }

        public void setPlaybackUrl(String playbackUrl) {
            this.playbackUrl = playbackUrl;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

}


