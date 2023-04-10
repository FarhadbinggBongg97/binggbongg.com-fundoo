package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetCountry {
    @SerializedName("result")
    private List<Results> countryList;

    public List<Results> getCountryList() {
        return countryList;
    }

    public void setCountryList(List<Results> countryList) {
        this.countryList = countryList;
    }

    public static class Results{
        @SerializedName("id")
        private String countryId;
        @SerializedName("countryname")
        private String countryName;

        public String getCountryId() {
            return countryId;
        }

        public void setCountryId(String countryId) {
            this.countryId = countryId;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }
    }
}
