package com.app.binggbongg.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetState {
    @SerializedName("result")
    private List<Results> stateList;

    public List<Results> getStateList() {
        return stateList;
    }

    public void setStateList(List<Results> stateList) {
        this.stateList = stateList;
    }

    public static class Results{
        @SerializedName("state_id")
        private String stateId;
        @SerializedName("statename")
        private String stateName;

        public String getStateId() {
            return stateId;
        }

        public void setStateId(String stateId) {
            this.stateId = stateId;
        }

        public String getStateName() {
            return stateName;
        }

        public void setStateName(String stateName) {
            this.stateName = stateName;
        }
    }
}
