package com.fooddelivery.service;

import com.fooddelivery.dto.response.UserSummaryResponse;
import com.fooddelivery.enums.UserRole;

import java.util.List;

public interface UserService {

    List<UserSummaryResponse> getAllUsers();

    List<UserSummaryResponse> getUsersByRole(UserRole role);

    UserSummaryResponse getUserById(Long id);
}
