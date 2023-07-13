package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SingleVideoResponse extends BaseResponse {


    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result {

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

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("mode")
        @Expose
        private String mode;
        @SerializedName("watch_count")
        @Expose
        private Integer watchCount;
        @SerializedName("streamed_on")
        @Expose
        private String streamedOn;
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
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("likes")
        @Expose
        private String likes;
        @SerializedName("playback_ready")
        @Expose
        private String playbackReady;
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
        @SerializedName("share_link")
        @Expose
        private String shareLink;
        @SerializedName("hashtags")
        @Expose
        private List<Hashtag> hashtags = null;
        @SerializedName("is_video_reported")
        @Expose
        private Boolean isVideoReported;
        @SerializedName("is_user_can_comment")
        @Expose
        private Boolean isUserCanComment;
        @SerializedName("video_type")
        @Expose
        private String videoType;
        @SerializedName("lifetime_vote_count")
        @Expose
        private String lifetimeVoteCount;

        public String getLifetimeVoteCount() {
            return lifetimeVoteCount;
        }

        public void setLifetimeVoteCount(String lifetimeVoteCount) {
            this.lifetimeVoteCount = lifetimeVoteCount;
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

        public Boolean getUserCanComment() {
            return isUserCanComment;
        }

        public void setUserCanComment(Boolean userCanComment) {
            isUserCanComment = userCanComment;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVideoDescription() {
            return videoDescription;
        }

        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Integer getWatchCount() {
            return watchCount;
        }

        public void setWatchCount(Integer watchCount) {
            this.watchCount = watchCount;
        }

        public String getStreamedOn() {
            return streamedOn;
        }

        public void setStreamedOn(String streamedOn) {
            this.streamedOn = streamedOn;
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLikes() {
            return likes;
        }

        public void setLikes(String likes) {
            this.likes = likes;
        }

        public String getPlaybackReady() {
            return playbackReady;
        }

        public void setPlaybackReady(String playbackReady) {
            this.playbackReady = playbackReady;
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

        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
        }

        public List<Hashtag> getHashtags() {
            return hashtags;
        }

        public void setHashtags(List<Hashtag> hashtags) {
            this.hashtags = hashtags;
        }

        public Boolean getIsVideoReported() {
            return isVideoReported;
        }

        public void setIsVideoReported(Boolean isVideoReported) {
            this.isVideoReported = isVideoReported;
        }

        public Boolean getIsUserCanComment() {
            return isUserCanComment;
        }

        public void setIsUserCanComment(Boolean isUserCanComment) {
            this.isUserCanComment = isUserCanComment;
        }

        public String getVideoType() {
            return videoType;
        }

        public void setVideoType(String videoType) {
            this.videoType = videoType;
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