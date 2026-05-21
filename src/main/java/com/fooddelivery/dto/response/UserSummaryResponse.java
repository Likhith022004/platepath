package com.fooddelivery.dto.response;

import com.fooddelivery.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private UserRole role;
    private LocalDateTime createdAt;
}
