package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FilterDetailsModel {

    @SerializedName("Status")
    @Expose
    private String status;

    @SerializedName("result")
    @Expose
    private List<Result> result;

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

    public static class Result{

        @SerializedName("fillter_name")
        @Expose
        private String fillterName;

        @SerializedName("id")
        @Expose
        private String id;

        @SerializedName("fillter")
        @Expose
        private String fillter;

        @SerializedName("image_url")
        @Expose
        private int imageUrl;

        public Result( String id,String fillterName, String fillter, int imageUrl) {
            this.fillterName = fillterName;
            this.id = id;
            this.fillter = fillter;
            this.imageUrl = imageUrl;
        }

        public String getFillterName() {
            return fillterName;
        }

        public void setFillterName(String fillterName) {
            this.fillterName = fillterName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFillter() {
            return fillter;
        }

        public void setFillter(String fillter) {
            this.fillter = fillter;
        }

        public int getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(int imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
