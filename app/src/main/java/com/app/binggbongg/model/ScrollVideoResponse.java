package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ScrollVideoResponse {

    @SerializedName("status")
    @Expose
    private String status;
    /*@SerializedName("nextloadmore")
    @Expose
    private String loadMore;*/
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public static class Result extends BaseVideo {

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
       /* @SerializedName("hashobj")
        @Expose
        private List<Object> hashobj = null;*/


        public boolean isShow() {
            return isShow;
        }

        public void setShow(boolean show) {
            isShow = show;
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

        @Override
        public String getPublisherId() {
            return publisherId;
        }

        @Override
        public void setPublisherId(String publisherId) {
            this.publisherId = publisherId;
        }

        public Soundtracks getSoundtracks() {
            return soundtracks;
        }

        public void setSoundtracks(Soundtracks soundtracks) {
            this.soundtracks = soundtracks;
        }

        @Override
        public String getPublisherImage() {
            return publisherImage;
        }

        @Override
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

        @Override
        public String getPlaybackUrl() {
            return playbackUrl;
        }

        @Override
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

        public Boolean getLiked() {
            return isLiked;
        }

        public void setLiked(Boolean liked) {
            isLiked = liked;
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

        public Boolean getVideoReported() {
            return isVideoReported;
        }

        public void setVideoReported(Boolean videoReported) {
            isVideoReported = videoReported;
        }

        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
        }

        @Override
        public String getVideoType() {
            return videoType;
        }

        @Override
        public void setVideoType(String videoType) {
            this.videoType = videoType;
        }

        public String getIosVideo() {
            return iosVideo;
        }

        public void setIosVideo(String iosVideo) {
            this.iosVideo = iosVideo;
        }

        @Override
        public String getAndroidVideo() {
            return androidVideo;
        }

        @Override
        public void setAndroidVideo(String androidVideo) {
            this.androidVideo = androidVideo;
        }

        public String getIosUrl() {
            return iosUrl;
        }

        public void setIosUrl(String iosUrl) {
            this.iosUrl = iosUrl;
        }

        @Override
        public String getAndroidUrl() {
            return androidUrl;
        }

        @Override
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

        public static class Hashtag {

            @SerializedName("hashName")
            @Expose
            private String hashName;
            @SerializedName("is_hashfav")
            @Expose
            private Boolean isHashfav;

            public String getHashName() {
                return hashName;
            }

            public void setHashName(String hashName) {
                this.hashName = hashName;
            }

            public Boolean getIsHashfav() {
                return isHashfav;
            }

            public void setIsHashfav(Boolean isHashfav) {
                this.isHashfav = isHashfav;
            }
        }
    }
}
