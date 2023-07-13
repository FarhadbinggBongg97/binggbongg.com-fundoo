package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseVideo {

    @SerializedName("publisher_id")
    @Expose
    String publisherId;
    @SerializedName("publisher_image")
    @Expose
    String publisherImage;
    @SerializedName("playback_url")
    @Expose
    String playbackUrl;
    @SerializedName("play_type")
    @Expose
    String playType;
    @SerializedName("android_url")
    @Expose
    String androidUrl;
    @SerializedName("android_video")
    @Expose
    String androidVideo;
    @SerializedName("video_type")
    @Expose
    String videoType;

    public String getAndroidUrl() {
        return androidUrl;
    }

    public void setAndroidUrl(String androidUrl) {
        this.androidUrl = androidUrl;
    }

    public void setPlaybackUrl(String playbackUrl) {
        this.playbackUrl = playbackUrl;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherImage() {
        return publisherImage;
    }

    public void setPublisherImage(String publisherImage) {
        this.publisherImage = publisherImage;
    }

    public String getPlaybackUrl() {
        return playbackUrl;
    }


    public String getPlayType() {
        return playType;
    }

    public void setPlayType(String playType) {
        this.playType = playType;
    }

    public String getAndroidVideo() {
        return androidVideo;
    }

    public void setAndroidVideo(String androidVideo) {
        this.androidVideo = androidVideo;
    }

}
