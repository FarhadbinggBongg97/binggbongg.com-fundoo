package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SearchSoundRes {

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

    public static class Result implements Serializable {

        @SerializedName("sound_id")
        @Expose
        private String soundId;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("interest_id")
        @Expose
        private String interestId;
        @SerializedName("sound_url")
        @Expose
        private String soundUrl;
        @SerializedName("isFavorite")
        @Expose
        private Boolean isFavorite;
        @SerializedName("cover_image")
        @Expose
        private String coverImage;
        @SerializedName("duration")
        @Expose
        private String duration;

        public String getSoundId() {
            return soundId;
        }

        public void setSoundId(String soundId) {
            this.soundId = soundId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getInterestId() {
            return interestId;
        }

        public void setInterestId(String interestId) {
            this.interestId = interestId;
        }

        public String getSoundUrl() {
            return soundUrl;
        }

        public void setSoundUrl(String soundUrl) {
            this.soundUrl = soundUrl;
        }

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

    }


}
