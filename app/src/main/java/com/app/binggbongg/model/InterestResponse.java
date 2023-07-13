package com.app.binggbongg.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InterestResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("interests")
    @Expose
    private List<Interest> interests = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        this.interests = interests;
    }

    public static class Interest {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("int_icon")
        @Expose
        private String intIcon;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIntIcon() {
            return intIcon;
        }

        public void setIntIcon(String intIcon) {
            this.intIcon = intIcon;
        }

    }
}
