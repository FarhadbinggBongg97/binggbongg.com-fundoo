package com.app.binggbongg.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("nextloadmore")
    @Expose
    private String nextloadmore;
    @SerializedName("contest_text")
    @Expose
    private String Contest_text;
    @SerializedName("scroll_enable")
    @Expose
    private String scroll_enable;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getContest_text() {
        return Contest_text;
    }

    public void setContest_text(String contest_text) {
        Contest_text = contest_text;
    }

    public String getScroll_enable() {
        return scroll_enable;
    }

    public void setScroll_enable(String scroll_enable) {
        this.scroll_enable = scroll_enable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNextloadmore() {
        return nextloadmore;
    }

    public void setNextloadmore(String nextloadmore) {
        this.nextloadmore = nextloadmore;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    @SuppressLint("ParcelCreator")
    public class Result implements Parcelable {

        private boolean isShow;

        @SerializedName("link_url")
        @Expose
        private String link_url;


        @SerializedName("video_timing")
        @Expose
        private String video_timing;

        @SerializedName("votes_count")
        @Expose
        private String votes_count;

        @SerializedName("followers")
        @Expose
        private String followers;

        @SerializedName("video_views_count")
        @Expose
        private String video_views_count;

        @SerializedName("publisher_vote_count")
        @Expose
        private String publisher_vote_count;

        @SerializedName("lifetime_vote_count")
        @Expose
        private String lifetimeVoteCount;

        @SerializedName("contest_status")
        @Expose
        private String contest_status;

        @SerializedName("playtype")
        @Expose
        private String playtype;

        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("video_id")
        @Expose
        private String videoId;
        @SerializedName("publisher_id")
        @Expose
        private String publisherId;
        @SerializedName("soundtracks")
        @Expose
        private Soundtracks soundtracks;
        @SerializedName("publisher_image")
        @Expose
        private String publisherImage;
        @SerializedName("stream_thumbnail")
        @Expose
        private String streamThumbnail;
        @SerializedName("posted_by")
        @Expose
        private String postedBy;
        @SerializedName("likes")
        @Expose
        private String likes;
        @SerializedName("playback_url")
        @Expose
        private String playbackUrl;
        @SerializedName("video_gift_counts")
        @Expose
        private String videoGiftCounts;
        @SerializedName("video_comment_counts")
        @Expose
        private String videoCommentCounts;
        @SerializedName("isLiked")
        @Expose
        private Boolean isLiked;
        @SerializedName("followed_user")
        @Expose
        private Boolean followedUser;
        @SerializedName("video_isFavorite")
        @Expose
        private Boolean videoIsFavorite;
        @SerializedName("is_video_reported")
        @Expose
        private Boolean isVideoReported;
        @SerializedName("share_link")
        @Expose
        private String shareLink;
        @SerializedName("video_type")
        @Expose
        private String videoType;
        @SerializedName("ios_video")
        @Expose
        private String iosVideo;
        @SerializedName("android_video")
        @Expose
        private String androidVideo;
        @SerializedName("ios_url")
        @Expose
        private String iosUrl;
        @SerializedName("android_url")
        @Expose
        private String androidUrl;
        @SerializedName("duration")
        @Expose
        private String playbackDuration;
        @SerializedName("invite_link")
        @Expose
        private String inviteLink;
        @SerializedName("hashobj")
        @Expose
        private List<Object> hashobj = null;

        public String getLifetimeVoteCount() {
            return lifetimeVoteCount;
        }

        public void setLifetimeVoteCount(String lifetimeVoteCount) {
            this.lifetimeVoteCount = lifetimeVoteCount;
        }

        public boolean isShow() {
            return isShow;
        }

        public void setShow(boolean show) {
            isShow = show;
        }

        public String getPlaytype() {
            return playtype;
        }

        public void setPlaytype(String playtype) {
            this.playtype = playtype;
        }

        public String getVideoDescription() {
            return videoDescription;
        }

        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public String getPublisherId() {
            return publisherId;
        }

        public void setPublisherId(String publisherId) {
            this.publisherId = publisherId;
        }

        public Soundtracks getSoundtracks() {
            return soundtracks;
        }

        public void setSoundtracks(Soundtracks soundtracks) {
            this.soundtracks = soundtracks;
        }

        public String getPublisherImage() {
            return publisherImage;
        }

        public void setPublisherImage(String publisherImage) {
            this.publisherImage = publisherImage;
        }

        public String getStreamThumbnail() {
            return streamThumbnail;
        }

        public void setStreamThumbnail(String streamThumbnail) {
            this.streamThumbnail = streamThumbnail;
        }

        public String getPostedBy() {
            return postedBy;
        }

        public void setPostedBy(String postedBy) {
            this.postedBy = postedBy;
        }

        public String getLikes() {
            return likes;
        }

        public void setLikes(String likes) {
            this.likes = likes;
        }

        public String getPlaybackUrl() {
            return playbackUrl;
        }

        public void setPlaybackUrl(String playbackUrl) {
            this.playbackUrl = playbackUrl;
        }

        public String getVideoGiftCounts() {
            return videoGiftCounts;
        }

        public void setVideoGiftCounts(String videoGiftCounts) {
            this.videoGiftCounts = videoGiftCounts;
        }

        public String getVideoCommentCounts() {
            return videoCommentCounts;
        }

        public void setVideoCommentCounts(String videoCommentCounts) {
            this.videoCommentCounts = videoCommentCounts;
        }

        public Boolean getIsLiked() {
            return isLiked;
        }

        public void setIsLiked(Boolean isLiked) {
            this.isLiked = isLiked;
        }

        public Boolean getFollowedUser() {
            return followedUser;
        }

        public void setFollowedUser(Boolean followedUser) {
            this.followedUser = followedUser;
        }

        public Boolean getVideoIsFavorite() {
            return videoIsFavorite;
        }

        public void setVideoIsFavorite(Boolean videoIsFavorite) {
            this.videoIsFavorite = videoIsFavorite;
        }

        public Boolean getIsVideoReported() {
            return isVideoReported;
        }

        public void setIsVideoReported(Boolean isVideoReported) {
            this.isVideoReported = isVideoReported;
        }

        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
        }

        public String getVideoType() {
            return videoType;
        }

        public void setVideoType(String videoType) {
            this.videoType = videoType;
        }

        public String getIosVideo() {
            return iosVideo;
        }

        public void setIosVideo(String iosVideo) {
            this.iosVideo = iosVideo;
        }

        public String getAndroidVideo() {
            return androidVideo;
        }

        public void setAndroidVideo(String androidVideo) {
            this.androidVideo = androidVideo;
        }

        public String getIosUrl() {
            return iosUrl;
        }

        public void setIosUrl(String iosUrl) {
            this.iosUrl = iosUrl;
        }

        public String getAndroidUrl() {
            return androidUrl;
        }

        public void setAndroidUrl(String androidUrl) {
            this.androidUrl = androidUrl;
        }

        public String getPlaybackDuration() {
            return playbackDuration;
        }

        public void setPlaybackDuration(String playbackDuration) {
            this.playbackDuration = playbackDuration;
        }

        public String getInviteLink() {
            return inviteLink;
        }

        public void setInviteLink(String inviteLink) {
            this.inviteLink = inviteLink;
        }

        public List<Object> getHashobj() {
            return hashobj;
        }

        public void setHashobj(List<Object> hashobj) {
            this.hashobj = hashobj;
        }

        public String getLink_url() {
            return link_url;
        }

        public void setLink_url(String link_url) {
            this.link_url = link_url;
        }

        public String getVideo_timing() {
            return video_timing;
        }

        public void setVideo_timing(String video_timing) {
            this.video_timing = video_timing;
        }

        public String getVotes_count() {
            return votes_count;
        }

        public void setVotes_count(String votes_count) {
            this.votes_count = votes_count;
        }

        public String getFollowers() {
            return followers;
        }

        public void setFollowers(String followers) {
            this.followers = followers;
        }

        public String getVideo_views_count() {
            return video_views_count;
        }

        public void setVideo_views_count(String video_views_count) {
            this.video_views_count = video_views_count;
        }

        public String getPublisher_vote_count() {
            return publisher_vote_count;
        }

        public void setPublisher_vote_count(String publisher_vote_count) {
            this.publisher_vote_count = publisher_vote_count;
        }

        public String getContest_status() {
            return contest_status;
        }

        public void setContest_status(String contest_status) {
            this.contest_status = contest_status;
        }

        public Boolean getLiked() {
            return isLiked;
        }

        public void setLiked(Boolean liked) {
            isLiked = liked;
        }

        public Boolean getVideoReported() {
            return isVideoReported;
        }

        public void setVideoReported(Boolean videoReported) {
            isVideoReported = videoReported;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    public static class Soundtracks implements Serializable {

        @SerializedName("sound_id")
        @Expose
        private String soundId;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("sound_url")
        @Expose
        private String soundUrl;
        @SerializedName("cover_image")
        @Expose
        private String coverImage;
        @SerializedName("isFavorite")
        @Expose
        private Boolean isFavorite;
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

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }
    }
}
