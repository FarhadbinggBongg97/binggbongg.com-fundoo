package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HashTagResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private Result result;

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

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("total")
        @Expose
        private String total;
        @SerializedName("videos")
        @Expose
        private List<Video> videos = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<Video> getVideos() {
            return videos;
        }

        public void setVideos(List<Video> videos) {
            this.videos = videos;
        }

        public static class Video {

            @SerializedName("stream_id")
            @Expose
            private String streamId;
            @SerializedName("stream_thumbnail")
            @Expose
            private String streamThumbnail;
            @SerializedName("likes")
            @Expose
            private Integer likes;

            @SerializedName("isLiked")
            @Expose
            private Boolean isLiked;


            public String getStreamId() {
                return streamId;
            }

            public void setStreamId(String streamId) {
                this.streamId = streamId;
            }


            public String getStreamThumbnail() {
                return streamThumbnail;
            }

            public void setStreamThumbnail(String streamThumbnail) {
                this.streamThumbnail = streamThumbnail;
            }

            public Integer getLikes() {
                return likes;
            }

            public void setLikes(Integer likes) {
                this.likes = likes;
            }

            public Boolean getLiked() {
                return isLiked;
            }

            public void setLiked(Boolean liked) {
                isLiked = liked;
            }
        }

    }

}






