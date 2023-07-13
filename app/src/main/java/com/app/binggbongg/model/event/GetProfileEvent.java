package com.app.binggbongg.model.event;

import com.app.binggbongg.model.ProfileResponse;

public class GetProfileEvent {
    private ProfileResponse profileResponse;

    public ProfileResponse getProfileResponse() {
        return profileResponse;
    }

    public void setProfileResponse(ProfileResponse profileResponse) {
        this.profileResponse = profileResponse;
    }
}
