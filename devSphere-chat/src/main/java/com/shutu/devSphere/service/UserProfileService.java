package com.shutu.devSphere.service;


import com.shutu.devSphere.model.entity.UserProfile;

public interface UserProfileService {
    UserProfile getByUserId(Long userId);
    void updateProfile(UserProfile profile);
}
