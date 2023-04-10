package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SocialMediaLinks implements Serializable {

    @SerializedName("status")
    String status;

    @SerializedName("result")
    private Result result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Result {

        @SerializedName("youtube_link")
        private String youTubeLink;

        @SerializedName("linkedin")
        private String linkedInLink;

        @SerializedName("instagram")
        private String instaLink;

        @SerializedName("snapchat")
        private String snapChatLink;

        @SerializedName("fb")
        private String fbLink;

        @SerializedName("whatsapp")
        private String whatsAppLink;

        @SerializedName("twitter")
        private String twitterLink;

        @SerializedName("tiktok")
        private String tiktokLink;

        @SerializedName("pinterst")
        private String pinterestLink;

        public String getYouTubeLink() {
            return youTubeLink;
        }

        public void setYouTubeLink(String youTubeLink) {
            this.youTubeLink = youTubeLink;
        }

        public String getLinkedInLink() {
            return linkedInLink;
        }

        public void setLinkedInLink(String linkedInLink) {
            this.linkedInLink = linkedInLink;
        }

        public String getInstaLink() {
            return instaLink;
        }

        public void setInstaLink(String instaLink) {
            this.instaLink = instaLink;
        }

        public String getSnapChatLink() {
            return snapChatLink;
        }

        public void setSnapChatLink(String snapChatLink) {
            this.snapChatLink = snapChatLink;
        }

        public String getFbLink() {
            return fbLink;
        }

        public void setFbLink(String fbLink) {
            this.fbLink = fbLink;
        }

        public String getWhatsAppLink() {
            return whatsAppLink;
        }

        public void setWhatsAppLink(String whatsAppLink) {
            this.whatsAppLink = whatsAppLink;
        }

        public String getTwitterLink() {
            return twitterLink;
        }

        public void setTwitterLink(String twitterLink) {
            this.twitterLink = twitterLink;
        }

        public String getTiktokLink() {
            return tiktokLink;
        }

        public void setTiktokLink(String tiktokLink) {
            this.tiktokLink = tiktokLink;
        }

        public String getPinterestLink() {
            return pinterestLink;
        }

        public void setPinterestLink(String pinterestLink) {
            this.pinterestLink = pinterestLink;
        }
    }

}
