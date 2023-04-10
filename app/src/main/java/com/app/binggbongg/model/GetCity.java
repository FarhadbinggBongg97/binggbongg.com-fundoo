package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetCity {
    @SerializedName("result")
    private List<Results> cityList;

    public List<Results> getCityList() {
        return cityList;
    }

    public void setCityList(List<Results> cityList) {
        this.cityList = cityList;
    }

    public static class Results{
        @SerializedName("id")
        private String cityId;
        @SerializedName("cityname")
        private String cityName;

        public String getCityId() {
            return cityId;
        }

        public void setCityId(String cityId) {
            this.cityId = cityId;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }
    }
}
