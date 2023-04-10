package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavSoundResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("sounds")
    @Expose
    private List<Sound> sounds = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Sound> getSounds() {
        return sounds;
    }

    public void setSounds(List<Sound> sounds) {
        this.sounds = sounds;
    }

    public static class Sound {

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
