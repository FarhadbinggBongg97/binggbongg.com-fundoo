package com.app.binggbongg.model;


import com.google.gson.annotations.Expose;
import com.app.binggbongg.utils.Constants;

public class BaseResponse {

    @Expose
    private String status;

    public boolean isSuccessFul() {
        return status != null && status.equals(Constants.TAG_TRUE);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
